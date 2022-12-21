package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

public class PhoneStateAttention implements IStatePhoneInAtomLite  {

    private static PhoneStateAttention instance=new PhoneStateAttention();

    private PhoneStateAttention(){

    }

    public static IStatePhoneInAtomLite getInstance() {
        return instance;
    }

    @Override
    public void SendAtomLite(ContextPhoneStateListener context) {
        context.GetHelper().writeCharacteristicTimeout(new byte[]{'c'});
    }

    //TODO:このクラスのように電話ステートからのBLE送信について、他のステートでも実行していく
    @Override
    public void ChangeState(ContextPhoneStateListener context, int condition) {

    }
}
