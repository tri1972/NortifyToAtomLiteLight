package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

public class PhoneStateCalled implements IStatePhoneInAtomLite{

    private static PhoneStateCalled instance=new PhoneStateCalled();

    private PhoneStateCalled(){

    }

    public static IStatePhoneInAtomLite getInstance() {
        return instance;
    }

    @Override
    public void SendAtomLite(ContextPhoneStateListener context) {
        context.GetHelper().writeCharacteristicTimeout(new byte[]{'b'});
    }

    //TODO:このクラスのように電話ステートからのBLE送信について、他のステートでも実行していく
    @Override
    public void ChangeState(ContextPhoneStateListener context, int condition) {

    }

}
