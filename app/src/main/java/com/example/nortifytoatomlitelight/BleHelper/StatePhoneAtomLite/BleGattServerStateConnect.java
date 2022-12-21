package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;
import com.example.nortifytoatomlitelight.BleHelper.BleConnectionAtomLiteLight;

public class BleGattServerStateConnect  implements IStatePhoneInAtomLite{

    private static BleGattServerStateConnect instance=new BleGattServerStateConnect();

    private BleGattServerStateConnect (){

    }

    public static IStatePhoneInAtomLite getInstance() {
        return instance;
    }
    @Override
    public void SendAtomLite(ContextPhoneStateListener context) {
        context.GetHelper().writeCharacteristicTimeout(new byte[]{'d'});
    }

    @Override
    public void ChangeState(ContextPhoneStateListener context, int condition) {

    }
}
