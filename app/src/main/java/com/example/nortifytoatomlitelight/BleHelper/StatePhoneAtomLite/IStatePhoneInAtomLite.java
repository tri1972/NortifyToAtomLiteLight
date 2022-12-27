package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

public interface IStatePhoneInAtomLite {

    public abstract void SendAtomLite();
    public abstract IStatePhoneInAtomLite ChangeState(IStatePhoneInAtomLite beforeState);
    public  abstract void SetStatePhoneAtomLiteLightHelper(StatePhoneAtomLiteLightHelper helper);

}
