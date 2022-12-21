package com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite;

import android.content.Context;

public class ContextPhoneStateListener {

    private  IStatePhoneInAtomLite state;


    private StatePhoneAtomLiteLightHelper helper;
    public StatePhoneAtomLiteLightHelper GetHelper(){
        return this.helper;
    }

    public ContextPhoneStateListener(Context context){
        helper=new  StatePhoneAtomLiteLightHelper(context);
    }

    public void setState(IStatePhoneInAtomLite state) {
        this.state = state;
    }

    public void SendAtomLite() {
        state.SendAtomLite(this);
    }

    public void ChangeState(ContextPhoneStateListener context, int condition) {
        state.ChangeState(context,condition);
    }
}
