package com.example.nortifytoatomlitelight;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //権限の取得

        if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        )
        {
            Toast.makeText(this, "Permissions are already granted.", Toast.LENGTH_LONG).show();
        } else {
            final int REQUEST_CODE = 1;
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.READ_CALL_LOG
            }, REQUEST_CODE);
        }

        //FloatingButtonのリスナーを登録
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("debug", "push!!: ");
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Intent intent = new Intent(getApplication(), NotifyService.class);
                intent.putExtra("REQUEST_CODE", 1);

                // Serviceの開始
                //startService(intent);
                startForegroundService(intent);
            }
        });
        //ForeGroundServiceを実行する為のコード

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.ECLAIR_0_1){
            NotificationChannel channel=new NotificationChannel(
                    NotifyService.CHANNELL_ID,
                    "お知らせ",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("お知らせを通知します。");
            NotificationManager manager= (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }

        Intent intentAtomLite= new Intent(this, NotifyToAtomLiteLightService.class);
        this.startService(intentAtomLite);

        Intent intentNotify= new Intent(this, NotifyService.class);
        this.startService(intentNotify);
    }

    private  boolean checkAndRequestPermissions()
    {
        int readPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int read_call_log = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG);

        List listPermissionsNeeded = new ArrayList<>();

        if (readPhoneState != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (read_call_log != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CALL_LOG);
        }

        if (read_call_log != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        }

        if (read_call_log != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            /*
            ActivityCompat.requestPermissions(this,
                    (String[]) listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
**/
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // パーミッションが必要な処理
        /*
        BroadcastReceiver receiver = new PhoneCallReceiver();
        IntentFilter filter = new IntentFilter("android.permission.READ_PHONE_STATE");
        this.registerReceiver(receiver, filter);
        */

        /*
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // パーミッションが必要な処理
                    BroadcastReceiver receiver = new PhoneCallReceiver();
                    IntentFilter filter = new IntentFilter("android.permission.READ_PHONE_STATE");
                    this.registerReceiver(receiver, filter);

                } else {
                    // パーミッションが得られなかった時
                    // 処理を中断する・エラーメッセージを出す・アプリケーションを終了する等
                }
            }
        }*/
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this,"セッティングだよ",Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}