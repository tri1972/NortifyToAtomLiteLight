package com.example.nortifytoatomlitelight.BleHelper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.nortifytoatomlitelight.Database.SettingDatabaseOpenHelper;
import com.example.nortifytoatomlitelight.LeDeviceListAdapter;
import com.example.nortifytoatomlitelight.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import androidx.core.util.Consumer;

public class BleConnectionAtomLiteLight{

    public enum IdentificationBLEStatus{
        /**
         * 正常
         */
        OK,
        /**
         * Bluetoothが有効になっていない
         */
        BluetoothAdapterDisable,
        /**
         * Bluetoothをサポートしていない
         */
        BluetoothNotSupported
    }
    /**
     * 接続先のAtomLiteの状態
     */
    public enum  TypeStatusAtomLiteBLE{
        Connected,
        DisConnected,
        Calling,
        Called,
        Attention,
        Reset,
    }

    /**
     * 現状のAtomliteが検知する電話の状態
     */
    public enum TypeStatusAtomLiteTelephone{
        /**
         * 電話待ち受け中の状態
         */
        Idle,
        /**
         * 電話がかかっている状態
         */
        Calling,
        /**
         * 電話があった状態
         */
        Called,
        Attention,
    }

    /**
     * GattServerへの接続ステータスについてBluetoothProfileのIdを文字列に変換します
     * @param con
     * @param statusId
     * @return
     */
    public String GetGattConnectionStatusToString(Context con,Integer statusId){
        String output=null;
        switch (statusId){
            case BluetoothProfile.STATE_CONNECTED:
                output=con.getString(R.string.status_GattConnection_STATE_CONNECTED);
                break;
            case BluetoothProfile.STATE_CONNECTING:
                output=con.getString(R.string.status_GattConnection_STATE_CONNECTING);
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                output=con.getString(R.string.status_GattConnection_STATE_DISCONNECTED);
                break;
            case BluetoothProfile.STATE_DISCONNECTING:
                output=con.getString(R.string.status_GattConnection_STATE_DISCONNECTING);
                break;
            default:
        }
        return output;
    }


    private static final String TAG = "BleConnectionAtomLiteLight";

    /**
     * AtomLiteの状態を取得します
     * @return
     */
    public  TypeStatusAtomLiteBLE GetStatusAtomLiteBLE(){
        return  this.mStatusAtomLiteBLE;
    }
    private static TypeStatusAtomLiteBLE mStatusAtomLiteBLE;
    //TODO:このステータス状態保持用の変数が勝手にnullになる・・・
    public  TypeStatusAtomLiteTelephone GetStatusAtomLiteTelephone(){
        return mStatusAtomLiteTelephone;
    }
    private TypeStatusAtomLiteTelephone mStatusAtomLiteTelephone;
    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothLeScanner bleScanner;
    private static ScanCallback bscan;

    /**
     * Characteristicの取得
     * @return
     */
    public  static BluetoothGattCharacteristic GetBluetoothGattCharacteristic(){
        return mCharacteristic;
    }
    private static BluetoothGattCharacteristic mCharacteristic;

    /**
     * GattServerオブジェクトの取得
     * @return
     */
    public static BluetoothGatt GetGattServer(){
        return mGatt;
    }
    private static BluetoothGatt mGatt;

    private static final long GATTSERVER_TIMEOUT=1000;

    //deperecatedのAPIを使った実装で使用（現状未使用）
    private static Handler handler = new Handler();
    private static LeDeviceListAdapter leDeviceListAdapter;
    private static boolean mScanning;
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            leDeviceListAdapter.addDevice(device);
                            leDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };



    //BLEデバイスが使えるかどうかチェック
    public IdentificationBLEStatus IdentificationBLE(Context cont) {
        IdentificationBLEStatus output= IdentificationBLEStatus.OK;
        try {
            // Use this check to determine whether BLE is supported on the device. Then
            // you can selectively disable BLE-related features.
            if (cont.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                final BluetoothManager bluetoothManager =
                        (BluetoothManager) cont.getSystemService(Context.BLUETOOTH_SERVICE);
                bluetoothAdapter = bluetoothManager.getAdapter();
                // Ensures Bluetooth is available on the device and it is enabled. If not,
                // displays a dialog requesting user permission to enable Bluetooth.
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    output= IdentificationBLEStatus.BluetoothAdapterDisable;
                }
            } else {
                output= IdentificationBLEStatus.BluetoothNotSupported;
            }
        } catch (Exception err) {
            throw err;
        }
        return output;
    }

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public void ScanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }


    /**
     * Bluetoothにて周辺のデバイスをSCANします
     * こちらの関数が現状のAPIに対応している
     * @param parent
     */
    public void ScanLeDevice2(IBleHelper parent) {
        if (bleScanner == null) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
            bscan = new BleScanCallback( parent);
        }
        ((BleScanCallback) bscan).getmResults();
        bleScanner.startScan(buildScanFilters(), buildScanSettings(), bscan);
    }

    /**
     * Return a List of {@link android.bluetooth.le.ScanFilter} objects to filter by Service UUID.
     * @return
     */
    private final List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        scanFilters.add(builder.build());

        return scanFilters;
    }

    /**
     * Return a {@link android.bluetooth.le.ScanSettings} object set to use low power (to preserve battery life).
     * @return
     */
    private final ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        return builder.build();
    }

    /**
     * BLEのScanを中止します
     *
     * @param cont
     */
    public final void ScanStop(Context cont) {
        if (bleScanner != null) {
            if (bscan != null) {
                bleScanner.stopScan(bscan);
            }
        } else {
            Toast.makeText(cont, "Not ScanBlueTooth!!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * BLE接続を行います
     * @param context
     * @param parent
     * @return 真：正常、偽：異常発生
     */
    public final boolean ConnectStart(final Context context, final IBleHelper parent) {
        boolean output=true;
        try {
            SettingDatabaseOpenHelper helper=new SettingDatabaseOpenHelper(context);

            String address = helper.GetBleAddress();
            String storeDeviceName= helper.GetBleDeviceName();
            if(storeDeviceName!=null) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if (bluetoothAdapter != null) {
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            //String deviceHardwareAddress = device.getAddress(); // MAC address
                            if (deviceName.equals(storeDeviceName)) {
                                try {
                                    //TODO:ペアリングした時のUUIDが実際にはなかった場合（別のデバイスに変更してUUIDが異なったりした場合、接続できなくなるのを防ぎたい。
                                    mGatt = device.connectGatt
                                            (context,
                                                    true,
                                                    new BleGattCallback(
                                                            new Consumer<Integer>() {
                                                                @Override
                                                                public void accept(Integer data) {
                                                                    parent.OnGetConnectionChangeNewStateAction(data);
                                                                    try {
                                                                        if (data == BluetoothProfile.STATE_CONNECTED) {//この条件分岐はAndroidのBLE接続ステートで分岐する
                                                                            //TODO:再接続時に現状の電話ステート状態を送信するようにする
                                                                            switch (mStatusAtomLiteTelephone) {//mStatusAtomLiteTelephoneがnullになることに注意！！！
                                                                                case Called:
                                                                                    SendCalled(context);
                                                                                    break;
                                                                                case Calling:
                                                                                    SendCalling(context);
                                                                                    break;
                                                                                case Attention:
                                                                                    SendAttention(context);
                                                                                case Idle:
                                                                                    break;
                                                                            }

                                                                        } else if (data == BluetoothProfile.STATE_DISCONNECTED) {

                                                                        }
                                                                    } catch (NullPointerException err) {
                                                                        Log.e(TAG, "nullエラー発生");
                                                                    }
                                                                }
                                                            },
                                                            new Consumer<List<BluetoothGattCharacteristic>>() {
                                                                @Override
                                                                public void accept(List<BluetoothGattCharacteristic> data) {
                                                                    mCharacteristic = data.get(0);
                                                                    parent.OnGetCharacteristic(data);
                                                                }
                                                            },
                                                            new Consumer<List<BluetoothGattService>>() {
                                                                @Override
                                                                public void accept(List<BluetoothGattService> data) {
                                                                    parent.OnGetService(data);
                                                                }
                                                            }
                                                    ),
                                                    BluetoothDevice.TRANSPORT_LE
                                            );
                                }catch(Exception err){
                                    Log.e(TAG, "nullエラー発生");
                                }
                            }
                        }
                    }
                    mCharacteristic = parent.GetCharacteristic();
                } else {
                    output=false;
                    Toast.makeText(context, context.getString(R.string.status_Bluetooth_Not_Use_Bluetooth_Device), Toast.LENGTH_LONG).show();
                }
            }else{
                output=false;
                Toast.makeText(context, context.getString(R.string.status_Bluetooth_Not_Setting_Bluetooth_Device_Name), Toast.LENGTH_LONG).show();
            }
        } catch (Exception err) {
            throw err;
        }
        return output;
    }

    public final void RequestPearing(Context context, final IBleHelper parent){
        try {
            SettingDatabaseOpenHelper helper=new SettingDatabaseOpenHelper(context);
            String deviceName = helper.GetBleAddress();

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for(BluetoothDevice device :pairedDevices){
                if(device.getName().equals(deviceName)){
                    device.createBond();
                    mGatt = device.connectGatt
                            (context,
                                    true,
                                    new BleGattCallback(
                                            new Consumer<Integer>() {
                                                @Override
                                                public void accept(Integer data) {
                                                    parent.OnGetConnectionChangeNewStateAction(data);
                                                }
                                            },
                                            new Consumer<List<BluetoothGattCharacteristic>>() {
                                                @Override
                                                public void accept(List<BluetoothGattCharacteristic> data) {
                                                    mCharacteristic =data.get(0);
                                                    parent.OnGetCharacteristic(data);
                                                }
                                            },
                                            new Consumer<List<BluetoothGattService>>() {
                                                @Override
                                                public void accept(List<BluetoothGattService> data) {
                                                    parent.OnGetService(data);
                                                }
                                            }
                                    ),
                                    BluetoothDevice.TRANSPORT_LE);
                    break;
                }
            }
            mCharacteristic =parent.GetCharacteristic();
        }catch (Exception err){
            throw err;
        }
    }

    /**
     * BLE接続を終了します
     * @param context
     */
    public final void ConnectStop(Context context) {
        //mStatusAtomLiteTelephone= TypeStatusAtomLiteTelephone.Idle;
        if(mGatt !=null) {
            mStatusAtomLiteBLE = TypeStatusAtomLiteBLE.DisConnected;
            mGatt.close();
            Toast.makeText(context,context.getString(R.string.message_Gatt_Close), Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(context,context.getString(R.string.message_Gatt_disconnect), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 着信中をBluetoothのgattサーバーに送信します
     * @param context
     */
    public final void SendCalling(Context context){
        mStatusAtomLiteTelephone= TypeStatusAtomLiteTelephone.Calling;
        mStatusAtomLiteBLE = TypeStatusAtomLiteBLE.Calling;
        writeCharacteristicTimeout(context,new byte[]{'a'});
    }


    /**
     * 着信ありをBluetoothのgattサーバーに送信します
     * @param context
     */
    public final void SendCalled(Context context) {
        mStatusAtomLiteTelephone= TypeStatusAtomLiteTelephone.Called;
        mStatusAtomLiteBLE = TypeStatusAtomLiteBLE.Called;
        this.writeCharacteristicTimeout(context,new byte[]{'b'});
    }

    /**
     * 通知ありをBluetoothのgattサーバーに送信します
     * @param context
     */
    public final void SendAttention(Context context) {
        mStatusAtomLiteBLE = TypeStatusAtomLiteBLE.Attention;
        this.writeCharacteristicTimeout(context,new byte[]{'c'});
    }

    /**
     * 接続中状態をBluetoothのgattサーバーに送信します
     * @param context
     */
    public final void SendConnect(Context context) {
        //mStatusAtomLiteTelephone= TypeStatusAtomLiteTelephone.Idle;
        mStatusAtomLiteBLE = TypeStatusAtomLiteBLE.Connected;
        this.writeCharacteristicTimeout(context,new byte[]{'d'});
    }

    /**
     * GattサーバーのCharacteristicにデータを書き込みます（Timeout付き）
     * @param context
     * @param bytes
     */
    private void writeCharacteristicTimeout(final Context context,byte [] bytes){
        writeCharacteristic exec=new writeCharacteristic(context);
        exec.setSendBytes(bytes);
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(exec);
        try {
            future.get(GATTSERVER_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Toast.makeText(context,context.getString(R.string.message_Gatt_Passive_Timeout),Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Characteristic書き込み用関数型インターフェース
     */
    private class writeCharacteristic implements Supplier<Integer>{
        private Context mCon;
        private byte [] mBytes;

        /**
         * 送信用データ
         * @param bytes
         */
        public void setSendBytes(byte [] bytes){
            this.mBytes=bytes;
        }

        /**
         * コンストラクタ
         * @param con
         */
        writeCharacteristic(Context con){
            this.mCon=con;
        }

        /**
         * Supplier型関数（値を返すのみ、この関数の返送値はダミーです）
         * @return
         */
        @Override
        public Integer get() {
            try {
                writeBytesCharacteristic(this.mBytes,mCon);
                //Thread.sleep(200000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return 0;
        }
    }

    /**
     * GattサーバーのCharacteristicにデータを書き込みます
     * @param bytes
     * @param mCon
     */
    private void writeBytesCharacteristic(byte[] bytes,Context mCon){

        if(mCharacteristic !=null) {
            mCharacteristic.setValue(bytes);
            mGatt.writeCharacteristic(mCharacteristic);
        }else{
            Toast.makeText(mCon,mCon.getString(R.string.message_Gatt_disconnect), Toast.LENGTH_LONG).show();
        }
    }
}
