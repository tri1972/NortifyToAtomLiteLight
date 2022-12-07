package com.example.nortifytoatomlitelight.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.IccOpenLogicalChannelResponse;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

public class SettingDatabaseOpenHelper extends NotifyToBluetoothDatabaseOpenHelper  {

    /**
     * DB内のテーブル名を設定
     */
    public static final String TABLE_NAME = "setting_db";

    /**
     * コンストラクタ
     *
     * @param context
     */
    public SettingDatabaseOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
        //SetColumnNames(null);
    }

    @Override
    public void SetColumnNames(Map<String, String> tmpData) {
        if(tmpData==null) {
            if(columnNames==null){
                columnNames = new HashMap<String, String>();
            }
            //ここでデータベース作成のためのColumn名とデータ種を追加する
            columnNames.put("name", "TEXT");
            columnNames.put("ble_address", "TEXT");
            columnNames.put("ble_name", "TEXT");
        }
    }

    @Override
    public Map<String, String> GetColumnNames() {
        return this.columnNames;
    }

    @Override
    protected String GetTableName() {
        return TABLE_NAME;
    }

    /**
     * dbへデータを保存する
     * @param name
     * @param ble_address
     */
    public void insertData(String name, String ble_address, String ble_name){

        try {
            SQLiteDatabase db= this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put((String) columnNames.keySet().toArray()[0], name);
            values.put((String) columnNames.keySet().toArray()[1], ble_address);
            values.put((String) columnNames.keySet().toArray()[2], ble_name);

            db.insert(TABLE_NAME, null, values);
        }catch (Exception err){
            throw err;
        }
    }

    /**
     * DBより最新BleAddressを取得
     * @return
     */
    public String GetBleAddress() {
        return GetColumnLastData("ble_address");
    }

    public String GetBleDeviceName() {
        return GetColumnLastData("ble_name");
    }
    /**
     * DBより指定したColumnの最後のデータを取得します
     * @param columnName
     * @return
     */
    public String GetColumnLastData(String columnName) {
        String output;
        try {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = this.makeDatabaseCursor(db);
            cursor.moveToFirst();

            if(cursor.getCount()>0) {
                cursor.moveToLast();
                int columnIndex = cursor.getColumnIndex("ble_address");
                if (columnIndex > -1) {
                    output = cursor.getString(columnIndex);
                } else {
                    throw new ArrayIndexOutOfBoundsException();
                }
            }else{
                output=null;
            }
        } catch (Exception err) {
            throw err;
        }
        return output;
    }


    //TODO:下記の二つのメソッドは親クラスにぶち込んだほうがよいような・・・
    @Override
    protected String GetColumnNameUpdated() {
        return COLUMN_NAME_UPDATED;
    }

    @Override
    protected String GetSQLDeleteEntries() {
        return SQL_DELETE_ENTRIES;
    }
}
