package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

public interface IStatePhoneInAtomLite {

    public abstract void SendAtomLite(ContextPhoneStateListener context);
    public abstract void ChangeState(ContextPhoneStateListener context, int condition);

}
