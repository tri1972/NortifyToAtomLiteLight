package com.example.nortifytoatomlitelight;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.nortifytoatomlitelight.BleHelper.BleConnectionAtomLiteLight;
import com.example.nortifytoatomlitelight.BleHelper.IBleHelper;

import java.util.List;

import androidx.annotation.Nullable;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

public class NotifyToAtomLiteLightService  extends Service implements IBleHelper {
    public static final String EXTRA_DATA="com.example.nortifytoatomlitelight.NotifyToAtomLiteLightService.DATA";

    private Context mContext;
    private PhoneCallReceiver mReceiver;
    private Thread mThread;
    private Runnable mTarget;

    private Intent sourceIntent;
    /**
     * このServiceの状態を示す
     */
    public enum StatusNotifyToAtomLiteLightService{
        /**
         * 実行中
         */
        running,
        /**
         * 停止中
         */
        stopped
    }

    static private StatusNotifyToAtomLiteLightService currentServiceStatus= StatusNotifyToAtomLiteLightService.stopped;

    public static StatusNotifyToAtomLiteLightService GetCurrentServiceStatus(){
        return  currentServiceStatus;
    }

    /**
     * コンストラクタ
     */
    public NotifyToAtomLiteLightService(){
    }

    /**
     * サービス生成時に呼び出される
     */
    @Override
    public void onCreate()
    {
        super.onCreate();
        this.mContext=getApplicationContext();
        //Service側でPhoneCallReceiverクラスを登録し、IntentFilterもここで登録する
        //最初はmanifestにて行っていた
        this.mReceiver=new PhoneCallReceiver();
        IntentFilter mInitentFilter=new IntentFilter();
        mInitentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        mInitentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        mInitentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(mReceiver,mInitentFilter);

        //Service状態を返すIntentをインスタンス化
        this.sourceIntent=new Intent(mContext, NotifyService.ReceiveNotifyService.class);
    }

    /**
     * Context.startService時に呼び出される。複数回サービスを起動した場合、このメソッドは複数回呼び出される
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(currentServiceStatus== StatusNotifyToAtomLiteLightService.stopped) {//二重実行を防ぐ
            currentServiceStatus= StatusNotifyToAtomLiteLightService.running;
            //BLE接続開始
            if(BleConnectionAtomLiteLight.ConnectStart(this.mContext, this)) {
                currentServiceStatus= StatusNotifyToAtomLiteLightService.running;
                this.mThread = new Thread(this.mTarget) {
                    @Override
                    public void run() {
                        Integer count = 0;
                        while ((true)) {
                            Log.d("NotifyToAtomLiteLightService", "run:NotifyToAtomLiteLightService " + count.toString());
                            count++;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                this.mThread.start();
            }else{
                currentServiceStatus= StatusNotifyToAtomLiteLightService.stopped;

            }
        }
        //現状のServiceの状態をBroadCast
        this.sourceIntent.setAction("com.example.broadcast.MY_NOTIFICATION");
        sourceIntent.putExtra(EXTRA_DATA,currentServiceStatus);
        mContext.sendBroadcast(sourceIntent);

        //Toast.makeText(this.getBaseContext(),"tetewet",Toast.LENGTH_SHORT).show();
        return START_NOT_STICKY;
        //return START_STICKY;
        //return START_REDELIVER_INTENT;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BleConnectionAtomLiteLight.ConnectStop(mContext);
        unregisterReceiver(this.mReceiver);//ここでReceiverを終了させないとandroid.app.intentreceiverleakedが発生する
        try {
            synchronized (this.mThread){
                this.mThread.notify();
                this.mThread.wait(6);
                this.mThread.interrupt();
                this.mThread.wait(6);

            }
            currentServiceStatus= StatusNotifyToAtomLiteLightService.stopped;
            //現状のServiceの状態をBroadCast
            this.sourceIntent.setAction("com.example.broadcast.MY_NOTIFICATION");
            sourceIntent.putExtra(EXTRA_DATA,currentServiceStatus);
            mContext.sendBroadcast(sourceIntent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
