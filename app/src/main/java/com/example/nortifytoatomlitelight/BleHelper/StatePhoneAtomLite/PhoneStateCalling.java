package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

public class PhoneStateCalling implements IStatePhoneInAtomLite {

    private static PhoneStateCalling instance=new PhoneStateCalling();

    private PhoneStateCalling(){

    }

    public static IStatePhoneInAtomLite getInstance() {
        return instance;
    }

    @Override
    public void SendAtomLite(ContextPhoneStateListener context) {
        context.GetHelper().writeCharacteristicTimeout(new byte[]{'a'});
    }

    //TODO:このクラスのように電話ステートからのBLE送信について、他のステートでも実行していく
    @Override
    public void ChangeState(ContextPhoneStateListener context, int condition) {

    }
}
