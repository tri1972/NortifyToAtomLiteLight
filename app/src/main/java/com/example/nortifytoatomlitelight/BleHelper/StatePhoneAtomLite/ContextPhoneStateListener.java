package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

public class ContextPhoneStateListener {

    private IStatePhoneInAtomLite state;

    private IStatePhoneInAtomLite beforeState;

    private StatePhoneAtomLiteLightHelper helper;
    public StatePhoneAtomLiteLightHelper GetHelper(){
        return this.helper;
    }

    public ContextPhoneStateListener(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic){
        helper=new StatePhoneAtomLiteLightHelper(gatt,characteristic);
        beforeState=PhoneStateIdle.getInstance();
    }

    public void setState(IStatePhoneInAtomLite state) {
        this.state = state;
    }

    public void SendAtomLite() {
        state.SetStatePhoneAtomLiteLightHelper(this.helper);
        state.SendAtomLite();
    }

    public void ChangeState(IStatePhoneInAtomLite state) {
        try {
            this.state = state;
            if (!state.getClass().getName().equals(beforeState.getClass().getName())) {
                this.state = state.ChangeState(beforeState);
                this.beforeState=this.state;
            } else {
                ;
            }
        }catch (Exception e){
            throw e;
        }
    }
}
