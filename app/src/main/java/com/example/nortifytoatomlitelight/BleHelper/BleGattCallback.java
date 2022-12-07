package com.example.nortifytoatomlitelight.BleHelper;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.core.util.Consumer;

public class BleGattCallback  extends BluetoothGattCallback {
    private BluetoothGatt mBluetoothGatt = null;    // Gattサービスの検索、キャラスタリスティックの読み書き
    private static final UUID UUID_NOTIFY                  = UUID.fromString( "00002902-0000-1000-8000-00805f9b34fb" );//定義済みUUID：Client Characteristic Configuration
    Consumer<List<BluetoothGattCharacteristic>> mGetChracteristicsAction;
    Consumer<List<BluetoothGattService>> mGetServicesAction;
    Consumer<Integer> mGetConnectionChangeStateAction;
    /**
     * コンストラクタ
     * @param getConnectionChangeStateAction
     * @param getChracteristicsAction characteristics取得用関数型インターフェース
     * @param getServicesAction sercvices取得用関数型インターフェース
     */
    public BleGattCallback(
            Consumer<Integer> getConnectionChangeStateAction,
            Consumer<List<BluetoothGattCharacteristic>> getChracteristicsAction,
                           Consumer<List<BluetoothGattService>> getServicesAction){
        this.mGetConnectionChangeStateAction=getConnectionChangeStateAction;
        this.mGetChracteristicsAction =getChracteristicsAction;
        this.mGetServicesAction=getServicesAction;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        mGetConnectionChangeStateAction.accept(newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        try {
            List<BluetoothGattService> services = gatt.getServices();
            this.mGetServicesAction.accept(services);
            //this.mParent.OnGetService(services);
            //List<String> strUUID = new ArrayList<>();
            List<BluetoothGattCharacteristic> characteristic = new ArrayList<>();
            for (BluetoothGattService service : services
            ) {
                //strUUID.add(service.getUuid().toString());
                for (BluetoothGattCharacteristic Characteristic : service.getCharacteristics()) {
                    UUID tmp = new UUID(0x0000ffffffffffffL, 0xffffffffffffffffL);
                    if (Long.compareUnsigned(tmp.getMostSignificantBits(), Characteristic.getUuid().getMostSignificantBits()) == -1) {
                        characteristic.add(Characteristic);
                        //Notify取得用の設定
                        gatt.setCharacteristicNotification(Characteristic,true);
                        //Descriptor(Client Characteristic Configuration)を取得します
                        BluetoothGattDescriptor mDescriptor = Characteristic.getDescriptor(UUID_NOTIFY);
                        //取得したデスクリプターに、ノーティフィケーションを有効にする値をセットします。
                        mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE );
                        //BluetoothGattオブジェクトを用いて、デスクリプターを書き込みます。
                        gatt.writeDescriptor(mDescriptor);
                    }
                }
            }
            //this.mParent.OnGetCharacteristic(characteristic);
            this.mGetChracteristicsAction.accept(characteristic);
        }catch(Exception err){
            throw err;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic,
                                      int status) {
    }

    /**
     * Chracteristicに変更があれば（今回の設定ではNotifyがあれば呼ばれる
     * {@inheritDoc}
     */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        byte[]       byteChara = characteristic.getValue();
        int num = ByteBuffer.wrap(byteChara).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

}