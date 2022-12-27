package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;
import com.example.nortifytoatomlitelight.BleHelper.BleConnectionAtomLiteLight;

public class BleGattServerStateConnect  implements IStatePhoneInAtomLite{

    private static BleGattServerStateConnect instance=new BleGattServerStateConnect();
    private StatePhoneAtomLiteLightHelper mhelper;

    private BleGattServerStateConnect (){

    }

    public static IStatePhoneInAtomLite getInstance() {
        return instance;
    }
    @Override
    public void SendAtomLite() {
        mhelper.writeCharacteristicTimeout(new byte[]{'d'});
    }

    @Override
    public IStatePhoneInAtomLite ChangeState(IStatePhoneInAtomLite beforeState) {
        return null;
    }

    @Override
    public void SetStatePhoneAtomLiteLightHelper(StatePhoneAtomLiteLightHelper helper) {
        this.mhelper=helper;
    }
}
