package com.example.nortifytoatomlitelight;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.nortifytoatomlitelight.BleHelper.BleConnectionAtomLiteLight;
import com.example.nortifytoatomlitelight.BleHelper.IBleHelper;

import java.util.List;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

/**
 * Serviceによる別スレッドでの実行
 * 参考url：https://akira-watson.com/android/service.html
 */
public class NotifyService extends Service implements IBleHelper{
    static final String CHANNELL_ID="777";
    private static final int ID_NOTIFY=1;
    public Context mContext;
    public static final String EXTRA_DATA="com.example.nortifytoatomlitelight.NortyfyToAtomLiteLightService.DATA";
    static Notification.Builder mBuilder;
    static Notification mNotification;
    static NotificationManager mNotificationManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        this.mContext=getApplicationContext();
    }

    //@RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    try {
        // to do something
        Log.d("debug", "onStartCommand: ");
        int requestCode = intent.getIntExtra("REQUEST_CODE", 0);
        String channelId = "default";
        String title = this.mContext.getString(R.string.Service_Title);

        mNotificationManager=
                (NotificationManager) this.mContext
                        .getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification　Channel 設定
        NotificationChannel channel = new NotificationChannel(
                CHANNELL_ID, title, NotificationManager.IMPORTANCE_DEFAULT);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(channel);

            //Service開始用のintent
            Intent intentNotifyStartService = new Intent(this,NortyfyToAtomLiteLightServiceReceiver.class);
            intentNotifyStartService.putExtra(NotifyService.EXTRA_DATA,0);


            //Service終了用のintent
            Intent intentNotifyStopService = new Intent(this,NortyfyToAtomLiteLightServiceReceiver.class);
            intentNotifyStopService.putExtra(NotifyService.EXTRA_DATA,1);

            //PendingIntent の主な目的は、あなたのアプリのプロセスから実行されたかのように、
            // 包含している Intent を使用する許可を別のアプリに付与することです。
            //　PendingIntentの状態をNotifyで見ているので、このオブジェクトの下記の設定によりNotifyのボタン動作も変わってくる
            //参考url　https://qiita.com/ryo_mm2d/items/77cf4e6da7add219c75c
            //Service開始用のintent
            PendingIntent contentIntentStart =
                    PendingIntent.getBroadcast(//Serviceに限定してIntentを渡す為の関数（ほかにもgetActivityなどがある）
                            mContext,
                            0,//このリクエストコードはPendingIntentオブジェクトごとに変える必要がある（同じだと同一のPendingIntentであるとみなされるらしい・・・）
                            intentNotifyStartService,
                            PendingIntent.FLAG_UPDATE_CURRENT
                                    //| PendingIntent.FLAG_ONE_SHOT//このフラグがあると一度Notifyのボタンが押されたあとはそのボタンは使えなくなる
                                    | PendingIntent.FLAG_IMMUTABLE);//このフラグがないとエラーになる
            //Service終了用のintent
            PendingIntent contentIntentStop =
                    PendingIntent.getBroadcast(//Serviceに限定してIntentを渡す為の関数（ほかにもgetActivityなどがある）
                            mContext,
                            1,//このリクエストコードはPendingIntentオブジェクトごとに変える必要がある
                            intentNotifyStopService,
                            PendingIntent.FLAG_UPDATE_CURRENT
                                    //| PendingIntent.FLAG_ONE_SHOT//このフラグがあると一度Notifyのボタンが押されたあとはそのボタンは使えなくなる
                                    |PendingIntent.FLAG_IMMUTABLE);//このフラグがないとエラーになる
            //Service開始用アクション
            Notification.Action actionStartService =
                    new Notification.Action(
                            android.R.drawable.star_on,
                            this.mContext.getString(R.string.Notify_Content_Button_1),
                            contentIntentStart
                    );

            //Service終了用アクション
            Notification.Action actionStopService =
                    new Notification.Action(
                            android.R.drawable.star_on,
                            this.mContext.getString(R.string.Notify_Content_Button_2),
                            contentIntentStop
                    );

            mBuilder= new Notification.Builder(this.mContext, CHANNELL_ID)
                    .setContentTitle(title)
                    // android標準アイコンから
                    .setSmallIcon(android.R.drawable.ic_menu_call)
                    .setContentText(this.mContext.getString(R.string.Service_Text))
                    .setAutoCancel(true)
                    .setContentIntent(contentIntentStop)//notifyタップ時に実行される動作を指定する
                    .setWhen(System.currentTimeMillis())
                    //.addAction(actionStartService)
                    .setActions(actionStartService,actionStopService);
            mNotification = mBuilder.build();
            mNotification.flags=Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;

            // startForeground
            //この関数を実行しないと例外がスローされる
            startForeground(ID_NOTIFY, mNotification);
            updateNotifyContentText(this,NotifyToAtomLiteLightService.GetCurrentServiceStatus());
        }
    }catch (Exception err){
        throw err;
    }
        //Toast.makeText(this.getBaseContext(),"tetewet",Toast.LENGTH_SHORT).show();
        return START_NOT_STICKY;
        //return START_STICKY;
        //return START_REDELIVER_INTENT;
    }

    /**
     * NotifyのContentTextをServiceの状態により書き換える
     * @param context
     * @param sendData　StatusNotifyToAtomLiteLightServiceのステータス
     */
    private static void updateNotifyContentText(Context context,NotifyToAtomLiteLightService.StatusNotifyToAtomLiteLightService sendData){
        // Update the existing notification builder content:
        mBuilder.setContentTitle(context.getString(R.string.Notify_Title));
        if(sendData ==NotifyToAtomLiteLightService.StatusNotifyToAtomLiteLightService.running)
        {
            mBuilder.setContentText(context.getString((R.string.Notify_Content_Text_1)));
        }else if(sendData ==NotifyToAtomLiteLightService.StatusNotifyToAtomLiteLightService.stopped)
        {
            mBuilder.setContentText(context.getString((R.string.Notify_Content_Text_2)));
        }else{

        }

        // Build a notification object with updated content:
        mNotification = mBuilder.build();

        // Publish the new notification with the existing ID:
        mNotificationManager.notify (ID_NOTIFY, mNotification);
    }

    public static class ReceiveNotifyService extends BroadcastReceiver{
        /**
         * 受け取ったIntentにより、Notifyの内容を書き換える
         * 参考Url：https://docs.microsoft.com/ja-jp/xamarin/android/app-fundamentals/notifications/local-notifications
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            NotifyToAtomLiteLightService.StatusNotifyToAtomLiteLightService sendData=
                    (NotifyToAtomLiteLightService.StatusNotifyToAtomLiteLightService)
                            intent.getSerializableExtra(NotifyToAtomLiteLightService.EXTRA_DATA);
            updateNotifyContentText(context,sendData);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

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