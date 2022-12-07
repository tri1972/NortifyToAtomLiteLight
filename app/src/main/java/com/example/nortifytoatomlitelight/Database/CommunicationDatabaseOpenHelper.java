package com.example.nortifytoatomlitelight.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

/**
 * 参考url:https://akira-watson.com/android/sqlite.html
 */
public class CommunicationDatabaseOpenHelper extends NotifyToBluetoothDatabaseOpenHelper {

    /**
     * DB内のテーブル名を設定
     */
    public static final String TABLE_NAME = "logdb";

    /**
     * この関数にて使用するデータベースのColumn名、とデータ種を設定した連想配列を作成する
     * @param tmpData
     */
    public void SetColumnNames(Map<String,String> tmpData){
        if(tmpData==null) {
            if(columnNames==null){
                columnNames = new HashMap<String, String>();
            }
            //ここでデータベース作成のためのColumn名とデータ種を追加する
            columnNames.put("updated", "INTEGER");
            columnNames.put("note", "TEXT");
            columnNames.put("datetime", "TEXT");
            columnNames.put("phone_number", "TEXT");
            columnNames.put("state", "TEXT");
            columnNames.put("type", "TEXT");
        }
    }

    public Map<String,String> GetColumnNames(){
        return this.columnNames;
    }

    /**
     * コンストラクタ
     * @param context
     */
    public CommunicationDatabaseOpenHelper
            (@Nullable Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);

        //SetColumnNames(null);
    }

    @Override
    protected String GetTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String GetColumnNameUpdated() {
        return COLUMN_NAME_UPDATED;
    }
/*
    @Override
    protected String GetSQLCreateEntries() {
        return GetSQLCreateEntries();
    }
*/
    @Override
    protected String GetSQLDeleteEntries() {
        return SQL_DELETE_ENTRIES;
    }


    /**
     * dbへデータを保存する
     * @param state
     * @param Note
     * @param updated
     * @param phoneNumber
     */
    public void insertData(String state, String Note,boolean updated,String phoneNumber,String deviceType){

        try {
            SQLiteDatabase db= this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put((String) columnNames.keySet().toArray()[0], deviceType);
            values.put((String) columnNames.keySet().toArray()[1], state);
            values.put((String) columnNames.keySet().toArray()[2], Note);
            values.put((String) columnNames.keySet().toArray()[3], updated);
            values.put((String) columnNames.keySet().toArray()[4], phoneNumber);
            values.put((String) columnNames.keySet().toArray()[5], NotifyToBluetoothDatabaseOpenHelper.getNowDate());

            db.insert(TABLE_NAME, null, values);
        }catch (Exception err){
            throw err;
        }
    }


    /**
     * DBよりデータを取得します
     * @param cont Context
     * @param isAddUpdated 更新されたデータのみを取得するか否か
     * @return
     */
    public StringBuilder ReadData(Context cont, boolean isAddUpdated){

        StringBuilder sbuilder = new StringBuilder();

        try {
            SQLiteDatabase db= this.getReadableDatabase();

            Cursor cursor= this.makeDatabaseCursor(db);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {

                if (cursor.getInt(3) == 0 || !isAddUpdated) {
                    sbuilder.append(cursor.getString(1));
                    sbuilder.append(": ");
                    sbuilder.append(cursor.getString(2));
                    sbuilder.append(": ");
                    sbuilder.append(cursor.getString(3));
                    sbuilder.append(": ");
                    sbuilder.append(cursor.getString(4));
                    sbuilder.append(": ");
                    sbuilder.append(cursor.getString(5));
                    sbuilder.append("\n");
                }
                cursor.moveToNext();
            }
            // 忘れずに！
            cursor.close();

            if (isAddUpdated) {
                updateData(cont,db);
            }
        }catch(Exception err){
            throw err;
        }
        return sbuilder;
    }

    public Map<String ,List<String>> GetAllColumnStringData(boolean isAddUpdated){
        SQLiteDatabase db;
        StringBuilder sbuilder = new StringBuilder();
        Map<String ,List<String>> output=new HashMap<String ,List<String>>();
        List<Integer> indexString=new ArrayList<>();

        try {
            db = this.getReadableDatabase();

            Log.d("debug", "**********Cursor");
            String[] tmpColumnNameStrings=new String[this.columnNames.size()];
            int counter=0;
            for (String columnName:
                 this.columnNames.values()) {
                tmpColumnNameStrings[counter]=columnName;
                counter++;
            }
            Cursor cursor = db.query(
                    TABLE_NAME,
                    tmpColumnNameStrings,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();


            for (int i = 0; i < cursor.getCount(); i++) {

                if (cursor.getInt(3) == 0 || !isAddUpdated) {
                    for (String columnName:
                            this.columnNames.keySet()) {
                        for(int j=0;j<cursor.getColumnCount();j++){
                            if(cursor.getColumnName(j)==columnName){
                                if(this.columnNames.get(columnName).equals("TEXT")){
                                    indexString.add(j);
                                    output.put(columnName,null);
                                }
                            }
                        }
                    }
                    for(int k=0;k<indexString.size();k++){
                        cursor.getString(indexString.get (k));
                        List<String> outputArray= output.get(cursor.getColumnName(indexString.get (k)));
                        if(outputArray==null){
                            outputArray=new  ArrayList<String>();
                        }
                        outputArray.add(cursor.getString(indexString.get (k)));
                    }
                }
                cursor.moveToNext();

            }

            // 忘れずに！
            cursor.close();

        }catch(Exception err){
            throw err;
        }
        return output;
    }
}
