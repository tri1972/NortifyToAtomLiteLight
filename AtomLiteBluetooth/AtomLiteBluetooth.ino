#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include "M5Atom.h"


#define BLE_PASSKEY 123456 //ペアリング時のパスコード値設定
#define DEVICE_NAME "ESP32TRITRI" //Bluetoothデバイス名設定

#define RED_LED   19  //赤色LEDのGPIO番号を指定
#define GREEN_LED 23  //緑色LEDのGPIO番号を指定
#define BLUE_LED  33  //青色LEDのGPIO番号を指定
#define SPEAKER 22    //圧電スピーカーのGPIO番号を指定
#define SPEAKER_CHANNEL 1 // ledcWriteToneのChannel番号を指定
#define IS_GPIO_POS false //GPIO出力を正論理でやるか否か(signalTowerはtrue、基板上LEDの場合はfalseとする)

#define RECEIVE_CALLING "a" //"受信中"のステータス受信のための文字列
#define RECEIVE_CONNECT "d" //"接続中"のステータス受信のための文字列
#define RECEIVE_CALLED "b" //"受信あり"のステータス受信のための文字列
#define RECEIVE_ATTENTION "c" //"注意"のステータス受信のための文字列
#define RECEIVE_DISCONNECT "e" //"切断中"のステータス受信のための文字列
#define SEND_RESET "f" //"切断中"のステータス受信のための文字列

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic = NULL;

bool ledOnOff=true;
bool deviceConnected = false;
bool oldDeviceConnected = false;
uint32_t value = 0;
hw_timer_t * tim0 = NULL;
uint16_t gpioLEDStatus=0;
uint8_t resetCounter=0;

//参考Url_Bluetooth：https://hawksnowlog.blogspot.com/2021/09/esp32-implementes-notify-services.html
//参考Url_Timer割り込み：https://lang-ship.com/blog/work/esp32-timer/
// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

#define SERVICE_UUID        "3a8da5fe-812e-4db2-a2e6-74ed48f80805"
#define CHARACTERISTIC_UUID "bde69ed0-d0c2-4073-89fc-d1d4e3067dc2"

// FastLEDライブラリの設定（CRGB構造体）
CRGB dispColor(uint8_t r, uint8_t g, uint8_t b) {
  return (CRGB)((r << 16) | (g << 8) | b);
}

enum statusBlinkMode{
  on,
  off
};

enum statusBLEState{
  connect,
  disconnect,
  calling,
  called,
  attension,
  reset,
  none,
};

volatile statusBlinkMode statusBlink;
volatile statusBLEState bleState=disconnect;
volatile statusBLEState bleStateOld=disconnect;
 
class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      bleStateOld=bleState;
      bleState=connect;
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      bleStateOld=bleState;
      bleState=disconnect;
    }
};

class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();
      String strValue=value.c_str();

      if (strValue.length() > 0) {
        if(strValue.equals(RECEIVE_CALLING)){
            Serial.println("Calling");
            bleStateOld=bleState;
            bleState=calling;
        }else if(strValue.equals(RECEIVE_CALLED)){
            Serial.println("called");
            bleStateOld=bleState;
            bleState=called;
        }else if(strValue.equals(RECEIVE_ATTENTION)){
            Serial.println("Attension");
            bleStateOld=bleState;
            bleState=attension;
        }else if(strValue.equals(RECEIVE_CONNECT)){
            Serial.println("Connect");
            bleStateOld=bleState;
            bleState=connect;
        }else if(strValue.equals(RECEIVE_DISCONNECT)){
            Serial.println("Disconnect");
            bleStateOld=bleState;
            bleState=disconnect;
        }else{
            bleStateOld=bleState;
            bleState=none;
        }
        Serial.println("*********");
        Serial.print("New value: ");
        for (int i = 0; i < value.length(); i++)
          Serial.print(value[i]);

        Serial.println();
        Serial.println("*********");
      }
    }
};

// ペアリング処理用
//参考Url：https://qiita.com/poruruba/items/eff3fedb1d4a63cbe08d
class MySecurity : public BLESecurityCallbacks {
  bool onConfirmPIN(uint32_t pin){
    return false;
  }

  uint32_t onPassKeyRequest(){
    Serial.println("ONPassKeyRequest");
    return BLE_PASSKEY;
  }

  void onPassKeyNotify(uint32_t pass_key){
    // ペアリング時のPINの表示
    Serial.println("onPassKeyNotify number");
    Serial.println(pass_key);
  }

  bool onSecurityRequest(){
    Serial.println("onSecurityRequest");
    return true;
  }

  void onAuthenticationComplete(esp_ble_auth_cmpl_t cmpl){
    Serial.println("onAuthenticationComplete");
    if(cmpl.success){
      // ペアリング完了
      Serial.println("auth success");
    }else{
      // ペアリング失敗
      Serial.println("auth failed");
    }
  }
};

//Timer割り込みイベントハンドラ
void IRAM_ATTR onTimer() {
  if(statusBlink==on){
    statusBlink=off;
  }else{
    statusBlink=on;
  }
}

void setLED(uint8_t colorPin,uint8_t turn){
  if(IS_GPIO_POS){
    digitalWrite(RED_LED,LOW);
    digitalWrite(GREEN_LED,LOW);
    digitalWrite(BLUE_LED,LOW);
    if(ledOnOff){
      if(turn==HIGH){
        digitalWrite(colorPin, HIGH);
      }else{
        digitalWrite(colorPin, LOW);
      }
    }
  }else{
    digitalWrite(RED_LED,HIGH);
    digitalWrite(GREEN_LED,HIGH);
    digitalWrite(BLUE_LED,HIGH);
    if(ledOnOff){
      if(turn==HIGH){
        digitalWrite(colorPin, LOW);
      }else{
        digitalWrite(colorPin, HIGH);
      }
    }
  }
}

void setup() {
  Serial.begin(115200);
  int time;                           // the variable used to set the Timer
  
  //BLEデバイスの作成
  BLEDevice::init(DEVICE_NAME);
  //  BLE Serverの作成
  pServer = BLEDevice::createServer();
  // BLE Serviceの作成
  BLEService *pService = pServer->createService(SERVICE_UUID);
  // BLE Characteristicの作成
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );
  //Serverコールバック関数の登録
  pServer->setCallbacks(new MyServerCallbacks());
  //Characteristicコールバック関数の登録
  pCharacteristic->setCallbacks(new MyCallbacks());
  
  //Pearing用コールバック関数の登録
  BLEDevice::setSecurityCallbacks(new MySecurity());   
  //Pearing設定
  BLESecurity *pSecurity = new BLESecurity();
  pSecurity->setKeySize(16);

  pSecurity->setAuthenticationMode(ESP_LE_AUTH_BOND);
  pSecurity->setCapability(ESP_IO_CAP_OUT);
  pSecurity->setInitEncryptionKey(ESP_BLE_ENC_KEY_MASK | ESP_BLE_ID_KEY_MASK);

  uint32_t passkey = BLE_PASSKEY;
  esp_ble_gap_set_security_param(ESP_BLE_SM_SET_STATIC_PASSKEY, &passkey, sizeof(uint32_t));
  
  // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
  // Create a BLE Descriptor
  pCharacteristic->addDescriptor(new BLE2902());

  // serviceの開始
  pService->start();
  // advertisingの取得
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  //advertisingの開始
  BLEDevice::startAdvertising();
  Serial.println("Waiting a client connection to notify...");
  // 本体初期化（UART有効, I2C無効, LED有効）
  M5.begin(false, false, true);
  // LED全消灯（赤, 緑, 青）
  M5.dis.drawpix(0, dispColor(0, 0, 0));

  //Timer割り込み設定
  tim0 = timerBegin(1, 80, true);
  timerAttachInterrupt(tim0, &onTimer, true);
  timerAlarmWrite(tim0, 1000000, true);
  timerAlarmEnable(tim0);

  //GPIO（LED)設定
  pinMode(RED_LED, OUTPUT);
  pinMode(GREEN_LED, OUTPUT);
  pinMode(BLUE_LED, OUTPUT);
  
  //全消灯
  setLED(RED_LED,LOW);
  setLED(GREEN_LED,LOW);
  setLED(BLUE_LED,LOW);
  
  //スピーカー設定
  ledcSetup(SPEAKER_CHANNEL, 12000, 8);
  ledcAttachPin(SPEAKER, SPEAKER_CHANNEL);
}

void loop() {
  //LEDランプによるステータス表示
  switch(bleState){
    case connect://青色常時点灯
      ledcWriteTone(SPEAKER_CHANNEL, 0);//スピーカー消音
      M5.dis.drawpix(0, dispColor(0, 0, 255));
      setLED(BLUE_LED,HIGH);
      break;
    case disconnect://青色点滅
      switch(statusBlink){
        case on:
          //ledcWriteTone(SPEAKER_CHANNEL, 262);//スピーカー低音”ド”
          M5.dis.drawpix(0, dispColor(0, 0, 255));
          setLED(BLUE_LED,HIGH);
          break;
        case off:
          //ledcWriteTone(SPEAKER_CHANNEL, 523);//スピーカー高音”ド”
          M5.dis.drawpix(0, dispColor(0, 0, 0));
          setLED(BLUE_LED,LOW);
          break;
      }
      break;
    case calling://赤色点滅
      ledOnOff=true;//コールされたら、LEDをONにする
      ledcWriteTone(SPEAKER_CHANNEL, 0);//スピーカー消音
      switch(statusBlink){
        case on:
          ledcWriteTone(SPEAKER_CHANNEL, 262);//スピーカー低音”ド”
          M5.dis.drawpix(0, dispColor(0, 255, 0));
          setLED(RED_LED,HIGH);
          break;
        case off:
          ledcWriteTone(SPEAKER_CHANNEL, 523);//スピーカー高音”ド”
          M5.dis.drawpix(0, dispColor(0, 0, 0));
          setLED(RED_LED,LOW );
          break;
      }
      break;
    case called://赤色常時点灯
      ledcWriteTone(SPEAKER_CHANNEL, 0);//スピーカー消音
      M5.dis.drawpix(0, dispColor(0, 255, 0));
      setLED(RED_LED,HIGH );
      break;
    case attension://黄常時点灯
      ledcWriteTone(SPEAKER_CHANNEL, 0);//スピーカー消音
      M5.dis.drawpix(0, dispColor(255, 255, 0));
      setLED(GREEN_LED,HIGH );
      break;
    case reset://ボタン押下の場合のリセット信号送信期間
        if(resetCounter<3){
          char sendData[]=SEND_RESET;
          uint8_t* valueBleKey=(uint8_t*)(sendData);
          Serial.println("reset");
          pCharacteristic->setValue(valueBleKey, 4);
          pCharacteristic->notify();
        }else{
          if(bleStateOld==disconnect){
            bleState=disconnect;
          }else{
            bleState=connect;
          }
          bleStateOld=reset;
          resetCounter=0;
        }
      switch(statusBlink){
        case on:
          resetCounter++;
          break;
        case off:
          break;
      }
      break;
  }
    // notify changed value
    /*
    if (deviceConnected) {
        pCharacteristic->setValue((uint8_t*)&value, 4);
        pCharacteristic->notify();
        value++;
        delay(1000); // bluetooth stack will go into congestion, if too many packets are sent, in 6 hours test i was able to go as low as 3ms
    }*/
    // disconnecting
    if (!deviceConnected && oldDeviceConnected) {
        delay(500); // give the bluetooth stack the chance to get things ready
        pServer->startAdvertising(); // restart advertising
        Serial.println("close connection and restart advertising");
        oldDeviceConnected = deviceConnected;
    }
    // connecting
    if (deviceConnected && !oldDeviceConnected) {
        // do stuff here on connecting
        oldDeviceConnected = deviceConnected;
    }
    M5.update();
    if (M5.Btn.wasPressed())
    {
      ledOnOff=!ledOnOff;
      bleStateOld=bleState;
      bleState=reset;
      /*
      if(IS_GPIO_POS)
      {
        setLED(RED_LED, ((gpioLEDStatus % 3) > 0) ? HIGH : LOW );
        setLED(GREEN_LED, (((gpioLEDStatus+1) % 3) > 0) ? HIGH : LOW );
        setLED(BLUE_LED, (((gpioLEDStatus+2) % 3) > 0) ? HIGH : LOW );
      }else{
        setLED(RED_LED, ((gpioLEDStatus % 3) > 0) ? LOW : HIGH  );
        setLED(GREEN_LED, (((gpioLEDStatus+1) % 3) > 0) ? LOW : HIGH  );
        setLED(BLUE_LED, (((gpioLEDStatus+2) % 3) > 0) ? LOW : HIGH  );
      }
      gpioLEDStatus++;
      */
    }
    
  delay(20);
}
