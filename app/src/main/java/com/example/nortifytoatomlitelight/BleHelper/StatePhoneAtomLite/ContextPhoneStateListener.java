package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

public class ContextPhoneStateListener {

    private  IStatePhoneInAtomLite state;


    private StatePhoneAtomLiteLightHelper helper;
    public StatePhoneAtomLiteLightHelper GetHelper(){
        return this.helper;
    }

    public ContextPhoneStateListener(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic){
        helper=new StatePhoneAtomLiteLightHelper(gatt,characteristic);
    }

    public void setState(IStatePhoneInAtomLite state) {
        this.state = state;
    }

    public void SendAtomLite() {
        state.SetStatePhoneAtomLiteLightHelper(this.helper);
        state.SendAtomLite();
    }

    public void ChangeState(int condition) {
        state.ChangeState(condition);
    }
}
