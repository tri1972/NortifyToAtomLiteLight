package com.example.nortifytoatomlitelight;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.nortifytoatomlitelight.Database.CommunicationDatabaseOpenHelper;
import com.example.nortifytoatomlitelight.Database.NotifyToBluetoothDatabaseOpenHelper;
import com.example.nortifytoatomlitelight.Database.SettingDatabaseOpenHelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    private SettingDatabaseOpenHelper helper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(helper == null){
            helper = new SettingDatabaseOpenHelper(this);
        }
        SharedPreferences prefs =PreferenceManager.getDefaultSharedPreferences(this);
        helper.insertData(prefs.getString("signature","null"),
                prefs.getString("BleDeviceAddress","null"),
                prefs.getString("BleDeviceName","null"));
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

}