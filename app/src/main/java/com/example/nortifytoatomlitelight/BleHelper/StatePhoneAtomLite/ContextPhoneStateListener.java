package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

public class ContextPhoneStateListener {

    private final String TAG=this.getClass().getName();
    private IStatePhoneInAtomLite state;

    private IStatePhoneInAtomLite beforeState;

    private StatePhoneAtomLiteLightHelper helper;
    public StatePhoneAtomLiteLightHelper GetHelper(){
        return this.helper;
    }

    /**
     * コンストラクタ
     * @param gatt
     * @param characteristic
     */
    public ContextPhoneStateListener(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic){
        helper=new StatePhoneAtomLiteLightHelper(gatt,characteristic);
        beforeState=PhoneStateIdle.getInstance();
    }

    /**
     * stateを設定する
     * @param state
     */
    public void setState(IStatePhoneInAtomLite state) {
        this.state = state;
    }

    public void SendAtomLite() {
        state.SetStatePhoneAtomLiteLightHelper(this.helper);
        state.SendAtomLite();
    }

    public void ChangeState(IStatePhoneInAtomLite state) {
        this.state = state;
        if (!state.getClass().getName().equals(beforeState.getClass().getName())) {
            if(state.ChangeState(beforeState)!=null) {
                this.state = state.ChangeState(beforeState);
                this.beforeState = this.state;
            }else{
                throw new NullPointerException(TAG+":state is null");
            }
        } else {
            ;
        }
    }
}
