package com.leagueofshadows.encrypto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

class Db extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Encrypto.db";
    private static final String TABLE_NAME = "Files";
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_ADDRESS = "Address";
    private static final String COLUMN_SENT = "Sent";
    private static final String COLUMN_DATABASE_ID = "DatabaseID";
    private static final String COLUMN_DOWNLOAD = "Download";
    private static final String COLUMN_FROM = "FromUser";
    private static final String COLUMN_Decrypt = "Decrypt";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_KEY = "Key";
    private Context context;

    Db(Context context) {
        super(context, DATABASE_NAME , null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ TABLE_NAME +
                "(id integer primary key autoincrement , "+COLUMN_NAME+" varchar not null, "+COLUMN_ADDRESS+" varchar not null, "+COLUMN_DOWNLOAD+" integer not null, "+COLUMN_SENT+" integer not null, "+COLUMN_DATABASE_ID+" varchar not null, "+COLUMN_FROM+" varchar not null , "+COLUMN_Decrypt+" integer not null, "+COLUMN_KEY+" varchar not null)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    void addFile(FilesObject file)
    {
        String name = file.getName();
        String address = file.getAddress();
        int sent = file.getSent();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues con = new ContentValues();
        con.put(COLUMN_NAME,name);
        con.put(COLUMN_ADDRESS,address);
        con.put(COLUMN_SENT,sent);
        con.put(COLUMN_DATABASE_ID,file.getDatabaseId());
        con.put(COLUMN_DOWNLOAD,file.getDownload());
        con.put(COLUMN_Decrypt,file.getDecrypt());
        con.put(COLUMN_FROM,file.getFrom());
        con.put(COLUMN_KEY,file.getKey());
        db.insert(TABLE_NAME,null,con);
    }

    ArrayList<FilesObject> getFiles()
    {
         SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(" SELECT * FROM "+TABLE_NAME,null);
        ArrayList<FilesObject> files = new ArrayList<>();
        if(cursor.getCount()!=0)
        {
            cursor.moveToFirst();
            int l = cursor.getCount();
            for (int i = 0; i < l; i++)
            {
                FilesObject File  = new FilesObject(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)),cursor.getInt(cursor.getColumnIndex(COLUMN_DOWNLOAD)),cursor.getInt(cursor.getColumnIndex(COLUMN_SENT)),Integer.toString(cursor.getInt(0)),cursor.getString(cursor.getColumnIndex(COLUMN_DATABASE_ID)),cursor.getString(cursor.getColumnIndex(COLUMN_FROM)),cursor.getInt(cursor.getColumnIndex(COLUMN_Decrypt)),cursor.getString(cursor.getColumnIndex(COLUMN_KEY)));
                files.add(File);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return files;
    }

    ArrayList<FilesObject> getDFiles() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(" SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_DOWNLOAD+" = ?",new String[]{"0"});
        ArrayList<FilesObject> files = new ArrayList<>();
        if(cursor.getCount()!=0)
        {
            cursor.moveToFirst();
            int l = cursor.getCount();
            for (int i = 0; i < l; i++)
            {
                FilesObject File  = new FilesObject(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)),cursor.getInt(cursor.getColumnIndex(COLUMN_DOWNLOAD)),cursor.getInt(cursor.getColumnIndex(COLUMN_SENT)),cursor.getString(0),cursor.getString(cursor.getColumnIndex(COLUMN_DATABASE_ID)),cursor.getString(cursor.getColumnIndex(COLUMN_FROM)),cursor.getInt(cursor.getColumnIndex(COLUMN_Decrypt)),cursor.getString(cursor.getColumnIndex(COLUMN_KEY)));
                files.add(File);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return files;
    }

    void updateDownload(String id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues update = new ContentValues();
        update.put(COLUMN_DOWNLOAD,1);
        db.update(TABLE_NAME,update,COLUMN_ID+" = ?",new String[]{id});
    }

    void updateDecrpt(String id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues update = new ContentValues();
        update.put(COLUMN_Decrypt,1);
        db.update(TABLE_NAME,update,COLUMN_ID+" = ?",new String[]{id});
    }

    void drop() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }
}