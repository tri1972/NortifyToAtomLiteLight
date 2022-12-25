package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.widget.Toast;

import com.example.nortifytoatomlitelight.R;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

//TODO:このクラスに対して、BleConnectionAtomLiteLightクラスの機能をステートパターンに合わせて実装していく
public class StatePhoneAtomLiteLightHelper {

    private static final long GATTSERVER_TIMEOUT=1000;
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGatt mGatt;

    /**
     *  コンストラクタ
     * @param gatt
     * @param mCharacteristic
     */
    public StatePhoneAtomLiteLightHelper(BluetoothGatt gatt,BluetoothGattCharacteristic mCharacteristic){
        this.mGatt=gatt;
        this.mCharacteristic = mCharacteristic;
    }

    /**
     * GattサーバーのCharacteristicにデータを書き込みます（Timeout付き）
     * @param bytes
     */
    public void writeCharacteristicTimeout(final byte [] bytes){

        Supplier<Integer> exec=new Supplier<Integer>(){
            private byte [] mBytes=bytes;

            /**
             * Supplier型関数（値を返すのみ、この関数の返送値はダミーです）
             * @return
             */
            @Override
            public Integer get() {
                try {
                    writeBytesCharacteristic(this.mBytes);
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
            //TODO:タイムアウトエラー処理を記述する
        }

    }

    /**
     * GattサーバーのCharacteristicにデータを書き込みます
     * @param bytes
     */
    private void writeBytesCharacteristic(byte[] bytes){
        if(this.mGatt!=null) {
            if (mCharacteristic != null) {
                mCharacteristic.setValue(bytes);
                this.mGatt.writeCharacteristic(mCharacteristic);
            } else {
                //TODO:Characteristic取得エラー処理を記述する
            }
        }else{
            //TODO:GattServer取得エラー処理を記述する
        }
    }
}
