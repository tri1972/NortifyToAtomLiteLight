package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

import android.telephony.TelephonyManager;

import com.example.nortifytoatomlitelight.BleHelper.BleConnectionAtomLiteLight;

public class PhoneStateIdle implements IStatePhoneInAtomLite  {

    private static PhoneStateIdle instance=new PhoneStateIdle();

    private StatePhoneAtomLiteLightHelper mhelper;

    private PhoneStateIdle(){

    }

    public static IStatePhoneInAtomLite getInstance() {
        return instance;
    }

    @Override
    public void SendAtomLite() {
        //Idleの場合は送るものはなし;
    }

    //TODO:このクラスのように電話ステートからのBLE送信について、他のステートでも実行していく
    @Override
    public IStatePhoneInAtomLite ChangeState(IStatePhoneInAtomLite beforeState) {
        if(beforeState instanceof PhoneStateCalling){
            return PhoneStateCalled.getInstance();
        }else if(beforeState instanceof PhoneStateIdle) {
            return BleGattServerStateConnect.getInstance();
        }else if(beforeState instanceof PhoneStateAttention){
            return PhoneStateAttention.getInstance();
        }else if(beforeState instanceof PhoneStateCalled){
            return PhoneStateCalled.getInstance();
        }else{
            return null;
        }
    }

    @Override
    public void SetStatePhoneAtomLiteLightHelper(StatePhoneAtomLiteLightHelper helper) {
        this.mhelper=helper;
    }
}
