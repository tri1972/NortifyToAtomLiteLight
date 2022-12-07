package com.example.nortifytoatomlitelight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NortyfyToAtomLiteLightServiceReceiver extends BroadcastReceiver {
    public static final String EXTRA_DATA="com.example.nortifytoatomlitelight.NortyfyToAtomLiteLightServiceReceiver.DATA";
    enum typeStateService{
        start,
        end,
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        int senddata= intent.getIntExtra(NotifyService.EXTRA_DATA,0);
        Intent targetIntent= new Intent(context, NotifyToAtomLiteLightService.class);
        if(senddata==0){
            //context.startForegroundService(targetIntent);
            context.startService(targetIntent);
        }else if(senddata==1){
            context.stopService(targetIntent);
        }else{

        }
    }
}
