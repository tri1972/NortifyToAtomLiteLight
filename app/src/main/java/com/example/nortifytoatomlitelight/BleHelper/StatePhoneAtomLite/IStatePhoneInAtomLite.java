package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

public interface IStatePhoneInAtomLite {

    public abstract void SendAtomLite();
    public abstract void ChangeState(int condition);
    public  abstract void SetStatePhoneAtomLiteLightHelper(StatePhoneAtomLiteLightHelper helper);

}
