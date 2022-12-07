package com.example.nortifytoatomlitelight.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.function.BiConsumer;

import androidx.annotation.Nullable;

/**
 * 参考url:https://akira-watson.com/android/sqlite.html
 */
public abstract class NotifyToBluetoothDatabaseOpenHelper extends SQLiteOpenHelper {

    // データーベース名
    public static final String DATABASE_NAME = "NortyfyToBluetooth.db";

    // データーベースのバージョン
    //データベースの構成（Columnの変更など）を行った場合、値を増分する
    //テーブルごとのHelperクラスに配置するとversionがおかしくなりupgradeハンドラが意図せず動作してしまうので注意
    public static final int DATABASE_VERSION = 4;

    private static final String _ID = "_id";
    public static final String COLUMN_NAME_UPDATED = "updated";

    protected Map<String,String> columnNames;

    abstract public void SetColumnNames(Map<String,String> tmpData);

    abstract public Map<String,String> GetColumnNames();
    /**
     * Table名を取得します
     * @return
     */
    abstract protected String GetTableName();

    abstract protected String GetColumnNameUpdated();

    /**
     * コンストラクタ
     * @param context
     */
    public NotifyToBluetoothDatabaseOpenHelper
            (@Nullable Context context,String databaseName,int datavaseVersion) {
        super(context, databaseName, null, datavaseVersion);
        SetColumnNames(null);
    }

    /*
    abstract protected String GetSQLCreateEntries();
*/
    StringBuilder tmp=new StringBuilder(
            "CREATE TABLE " + GetTableName() + " (" +
                    _ID + " INTEGER PRIMARY KEY," /*+
                    COLUMN_NAME_UPDATED + " INTEGER,"*/);

    /**
     * Database作成SQL文を保存します
     * @return
     */
    public String GetSQLCreateEntries()
    {

        columnNames.forEach(new BiConsumer<String, String>() {
            @Override
            public void accept(String s, String s2) {
                tmp.append(s+" "+s2+",");
            }
        });
        tmp.delete(tmp.length()-1,tmp.length());
        tmp.append(")");

        return tmp.toString();
    }

    protected  String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + GetTableName();

    /**
     * updateするためのSQL文を作成
     * @return
     */
    public  String GetSQLUpdated(){
        return "UPDATE "+GetTableName()+" SET "+GetColumnNameUpdated()+" = "+"1" +" WHERE " +GetColumnNameUpdated()+" = 0";
    }

    /**
     * テーブル作成
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                GetSQLCreateEntries()
        );//TODO:ここが実行されると例外スロー
    }

    /**
     * Database削除SQL文を保存します
     * @return
     */
    abstract  protected String GetSQLDeleteEntries();
    /**
     * アップデートの判別、古いバージョンは削除して新規作成
     * @param sqLiteDatabase
     * @param i
     * @param i1
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(
                GetSQLDeleteEntries()
        );
        onCreate(sqLiteDatabase);
    }

    public void onDowngrade(SQLiteDatabase db,
                            int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Databaseの列名を文字列配列で取得
     * @return
     */
    public String[] GetArrayColumnNames(){
        int counter=0;
        String[] output=new String[this.columnNames.size()];
        for (String columnName:
                this.columnNames.keySet()) {
            output[counter]=columnName;
            counter++;
        }
        return output;
    }

    /**
     * データベースデータ取得用のCursorを作成
     * @param db
     * @return
     */
    protected Cursor makeDatabaseCursor(SQLiteDatabase db){
        Cursor cursor;
        try {
            Log.d("debug", "**********Cursor");

            String[] tmpString=this.GetArrayColumnNames();

            cursor = db.query(
                    GetTableName(),
                    tmpString,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }catch(Exception err){
            throw err;
        }
        return  cursor;
    }

    /**
     * データのアップデートを実行
     * @param cont
     * @param db
     */
    protected void updateData(Context cont,SQLiteDatabase db){
        try {
            if (db == null) {
                db = this.getReadableDatabase();
            }

            db.execSQL(this.GetSQLUpdated() );
        }catch(Exception err){
            throw err;
        }
    }

    /**
     * 現在日時をyyyy/MM/dd HH:mm:ss形式で取得する.<br>
     */
    public static String getNowDate(){
        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }
}
