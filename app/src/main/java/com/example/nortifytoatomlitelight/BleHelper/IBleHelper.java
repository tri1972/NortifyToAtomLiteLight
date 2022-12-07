package com.example.nortifytoatomlitelight.BleHelper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;

import java.util.List;

public interface IBleHelper {
    /**
     * Sevice取得Callback関数
     * @param services
     */
    void OnGetService(List<BluetoothGattService> services);

    /**
     * Characteristic取得Callback関数
     * @param characteristics
     */
    void OnGetCharacteristic(List<BluetoothGattCharacteristic> characteristics );

    void AccessBleScanCallback(ScanResult data);

    /**
     * gattServerへのコネクション状態が変更されたら実行されるCallback関数
     * @param status
     */
    void OnGetConnectionChangeNewStateAction(Integer status);

    BluetoothGattCharacteristic GetCharacteristic();
}
