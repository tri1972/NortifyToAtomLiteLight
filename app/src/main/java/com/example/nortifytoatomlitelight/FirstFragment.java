package com.example.nortifytoatomlitelight;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nortifytoatomlitelight.Database.CommunicationDatabaseOpenHelper;
import com.example.nortifytoatomlitelight.Database.NotifyToBluetoothDatabaseOpenHelper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class FirstFragment extends Fragment {

    private CommunicationDatabaseOpenHelper helper;
    private SQLiteDatabase db;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (helper == null) {
            helper = new CommunicationDatabaseOpenHelper(view.getContext());
        }
        final TextView textview= view.findViewById(R.id.textView_log);
        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
        view.findViewById(R.id.button_view_all_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textview.setText(helper.ReadData(view.getContext(),false));
                //textview.setText(readData(view.getContext(),false));
            }
        });
        view.findViewById(R.id.button_delete_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteData(view.getContext());
            }
        });
        view.findViewById(R.id.button_update_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textview.setText(helper.ReadData(view.getContext(),true));
                //textview.setText(readData(view.getContext(),true));
            }
        });
        view.findViewById(R.id.button_start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent= new Intent(getContext(),NotificationService.class);
                Intent intent= new Intent(getContext(), NotifyService.class);
                getContext().startForegroundService(intent);
            }
        });
        view.findViewById(R.id.button_stop_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getContext(), NotifyService.class);
                getContext().stopService(intent);
            }
        });
    }

    /**
     * Dbのテーブルを消去します
     * @param cont Context
     */
    private void deleteData(Context cont){
        try {
            if (helper == null) {
                helper = new CommunicationDatabaseOpenHelper(cont);
            }

            if (db == null) {
                db = helper.getReadableDatabase();
            }
            db.execSQL("delete from "+ helper.TABLE_NAME);
            //db.delete(helper.TABLE_NAME, null, null);
        }catch (Exception err){
            throw err;
        }
    }

}