package com.example.nortifytoatomlitelight.PhoneHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.nortifytoatomlitelight.PhoneHelper.MyPhoneStateListener;

/**
 * 参考url
 * https://qiita.com/kabayan/items/190936f4b71cf048c1e4
 * https://stackoverflow.com/questions/52369874/broadcast-receiver-for-phone-state-changed-not-working
 */
public class PhoneCallReceiver extends BroadcastReceiver {

    @SuppressWarnings("unused")
    private final String TAG = getClass().getSimpleName();

    private Context ctx;

    private MyPhoneStateListener mPhoneListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        ctx = context;
        try {
            //TelephonyManagerの生成
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //リスナーの登録
            if(this.mPhoneListener==null) {
                this.mPhoneListener = new MyPhoneStateListener(context);
            }
            tm.listen(this.mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        } catch (Exception e) {
            Log.e(TAG, ":" + e);
        }
    }
}/**
 * カスタムリスナーの登録
 * 着信〜終了 CALL_STATE_RINGING > CALL_STATE_OFFHOOK > CALL_STATE_IDLE
 * 不在着信 CALL_STATE_RINGING > CALL_STATE_IDLE
 */

