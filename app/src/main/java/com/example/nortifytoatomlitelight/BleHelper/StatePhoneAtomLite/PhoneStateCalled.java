package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

public class PhoneStateCalled implements IStatePhoneInAtomLite{

    private static PhoneStateCalled instance=new PhoneStateCalled();
    private StatePhoneAtomLiteLightHelper mhelper;
    private PhoneStateCalled(){

    }

    public static IStatePhoneInAtomLite getInstance() {
        return instance;
    }

    @Override
    public void SendAtomLite() {
        this.mhelper.writeCharacteristicTimeout(new byte[]{'b'});
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
