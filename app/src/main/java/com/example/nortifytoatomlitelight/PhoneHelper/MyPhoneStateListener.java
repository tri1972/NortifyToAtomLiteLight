package com.example.nortifytoatomlitelight.PhoneHelper;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.nortifytoatomlitelight.BleHelper.BleConnectionAtomLiteLight;
import com.example.nortifytoatomlitelight.BleHelper.IBleHelper;
import com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite.BleGattServerStateConnect;
import com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite.ContextPhoneStateListener;
import com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite.PhoneStateAttention;
import com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite.PhoneStateCalled;
import com.example.nortifytoatomlitelight.BleHelper.StatePhoneAtomLite.PhoneStateCalling;
import com.example.nortifytoatomlitelight.Database.CommunicationDatabaseOpenHelper;

import java.util.List;

public class MyPhoneStateListener extends PhoneStateListener implements IBleHelper {

    private static final String DEVICE_TYPE="Phone";
    private Context mContext;
    private CommunicationDatabaseOpenHelper helper;

    /**
     * onCallStateChangedが前に実行されたときの電話状態
     */
    public static int beforeState;

    public MyPhoneStateListener(Context context){
        this.mContext=context;
        if(helper == null){
            helper = new CommunicationDatabaseOpenHelper(mContext);
        }
    }
    private ContextPhoneStateListener mContextPhoneStateListener;
    @SuppressWarnings("unused")
    private final String TAG = getClass().getSimpleName();
    //TODO:電話番号の取得が実機ではできない
    public void onCallStateChanged(int state, String callNumber) {
        this.mContextPhoneStateListener=new ContextPhoneStateListener(mContext);
        Log.d(TAG, ":" + state+"-PhoneNumber:"+callNumber);
        switch(state){
            case TelephonyManager.CALL_STATE_IDLE:      //待ち受け（終了時）
                Toast.makeText(mContext, "CALL_STATE_IDLE", Toast.LENGTH_LONG).show();
                helper.insertData("CALL_STATE_IDLE","",false,callNumber,DEVICE_TYPE);
                if(this.beforeState==TelephonyManager.CALL_STATE_RINGING){
                    //this.mContextPhoneStateListener.setState(PhoneStateCalled.getInstance());
                    //this.mContextPhoneStateListener.SendAtomLite();
                    new BleConnectionAtomLiteLight().SendCalled(mContext);
                }else if(this.beforeState==TelephonyManager.CALL_STATE_OFFHOOK) {
                    //this.mContextPhoneStateListener.setState(BleGattServerStateConnect.getInstance());
                    //this.mContextPhoneStateListener.SendAtomLite();
                    new BleConnectionAtomLiteLight().SendConnect(mContext);
                }
                beforeState=TelephonyManager.CALL_STATE_IDLE;
                break;
            case TelephonyManager.CALL_STATE_RINGING:   //着信
                //this.mContextPhoneStateListener.setState(PhoneStateCalling.getInstance());
                //this.mContextPhoneStateListener.SendAtomLite();
                new BleConnectionAtomLiteLight().SendCalling(mContext);
                Toast.makeText(mContext, "CALL_STATE_RINGING: " + callNumber, Toast.LENGTH_LONG).show();
                helper.insertData("ALL_STATE_RINGING",callNumber,false,callNumber,DEVICE_TYPE);
                beforeState=TelephonyManager.CALL_STATE_RINGING;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:   //通話
                //this.mContextPhoneStateListener.setState(PhoneStateAttention.getInstance());
                //this.mContextPhoneStateListener.SendAtomLite();
                new BleConnectionAtomLiteLight().SendAttention(mContext);
                Toast.makeText(mContext, "CALL_STATE_OFFHOOK", Toast.LENGTH_LONG).show();
                helper.insertData("CALL_STATE_OFFHOOK","",false,callNumber,DEVICE_TYPE);
                beforeState=TelephonyManager.CALL_STATE_OFFHOOK;
                break;
        }
    }
    @Override
    public void OnGetService(List<BluetoothGattService> services) {

    }

    @Override
    public void OnGetCharacteristic(List<BluetoothGattCharacteristic> characteristics) {

    }

    @Override
    public void AccessBleScanCallback(ScanResult data) {

    }

    @Override
    public void OnGetConnectionChangeNewStateAction(Integer status) {

    }

    @Override
    public BluetoothGattCharacteristic GetCharacteristic() {
        return null;
    }
}



