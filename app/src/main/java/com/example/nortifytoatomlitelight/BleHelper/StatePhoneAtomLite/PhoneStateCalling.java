package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

public class PhoneStateCalling implements IStatePhoneInAtomLite {

    private static PhoneStateCalling instance=new PhoneStateCalling();
    private StatePhoneAtomLiteLightHelper mhelper;
    private PhoneStateCalling(){

    }

    public static IStatePhoneInAtomLite getInstance() {
        return instance;
    }

    @Override
    public void SendAtomLite() {
        this.mhelper.writeCharacteristicTimeout(new byte[]{'a'});
    }

    //TODO:このクラスのように電話ステートからのBLE送信について、他のステートでも実行していく
    @Override
    public IStatePhoneInAtomLite ChangeState(IStatePhoneInAtomLite beforeState) {
        return getInstance();
    }

    @Override
    public void SetStatePhoneAtomLiteLightHelper(StatePhoneAtomLiteLightHelper helper) {
        this.mhelper=helper;
    }
}
