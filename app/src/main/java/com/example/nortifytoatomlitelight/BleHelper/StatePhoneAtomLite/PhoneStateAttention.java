package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

public class PhoneStateAttention implements IStatePhoneInAtomLite  {

    private static PhoneStateAttention instance=new PhoneStateAttention();

    private StatePhoneAtomLiteLightHelper mhelper;

    private PhoneStateAttention(){

    }

    public static IStatePhoneInAtomLite getInstance() {
        return instance;
    }

    @Override
    public void SendAtomLite() {
        this.mhelper.writeCharacteristicTimeout(new byte[]{'c'});
    }

    //TODO:このクラスのように電話ステートからのBLE送信について、他のステートでも実行していく
    @Override
    public IStatePhoneInAtomLite ChangeState( IStatePhoneInAtomLite beforeState) {
        return getInstance();
    }

    @Override
    public void SetStatePhoneAtomLiteLightHelper(StatePhoneAtomLiteLightHelper helper) {
        this.mhelper=helper;
    }
}
