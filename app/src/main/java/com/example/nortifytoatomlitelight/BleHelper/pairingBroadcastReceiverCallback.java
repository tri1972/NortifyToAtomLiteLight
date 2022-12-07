package com.example.nortifytoatomlitelight.BleHelper;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class pairingBroadcastReceiverCallback extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null){
            return;
        }
        if(intent.getAction()== BluetoothDevice.ACTION_PAIRING_REQUEST){
            //ハードコーディングされたpinを設定したい場合はこちらを実装する
            /*
            BluetoothDevice device= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(device!=null){
                device.setPin(new byte[]{0xf,0xf});
            }
            this.abortBroadcast();*/
        }
    }
}
