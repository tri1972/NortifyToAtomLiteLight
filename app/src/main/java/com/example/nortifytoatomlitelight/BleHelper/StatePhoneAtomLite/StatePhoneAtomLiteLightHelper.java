package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.widget.Toast;

import com.example.nortifytoatomlitelight.BleHelper.BleConnectionAtomLiteLight;
import com.example.nortifytoatomlitelight.R;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

//TODO:このクラスに対して、BleConnectionAtomLiteLightクラスの機能をステートパターンに合わせて実装していく
public class StatePhoneAtomLiteLightHelper {

    private static final long GATTSERVER_TIMEOUT=1000;
    private static BluetoothGattCharacteristic characteristic;
    private static BluetoothGatt gatt;
    private Context context;

    /**
     * コンストラクタ
     */
    public StatePhoneAtomLiteLightHelper(Context context){
        this.context=context;
    }

    /**
     * GattサーバーのCharacteristicにデータを書き込みます（Timeout付き）
     * @param bytes
     */
    public void writeCharacteristicTimeout(final byte [] bytes){

        Supplier<Integer> exec=new Supplier<Integer>(){
            private Context mCon=context;
            private byte [] mBytes=bytes;

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
        };

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(exec);

        try {
            future.get(GATTSERVER_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Toast.makeText(context,context.getString(R.string.message_Gatt_Passive_Timeout),Toast.LENGTH_LONG).show();
        }

    }

    /**
     * GattサーバーのCharacteristicにデータを書き込みます
     * @param bytes
     * @param mCon
     */
    private void writeBytesCharacteristic(byte[] bytes,Context mCon){
        if(characteristic!=null) {
            characteristic.setValue(bytes);
            gatt.writeCharacteristic(characteristic);
        }else{
            Toast.makeText(mCon,mCon.getString(R.string.message_Gatt_disconnect), Toast.LENGTH_LONG).show();
        }
    }
}
