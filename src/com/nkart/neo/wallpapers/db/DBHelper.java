package com.nkart.neo.wallpapers.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.nkart.neo.MyApplication;

import java.util.ArrayList;
import java.util.Objects;

import app.lawnchair.LawnchairApp;


public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper dbHelper = null;

    private static final String DATABASE_NAME = "WallpaperDB.db";
    private static final String TABLE_NAME = "downloads";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_LOCATION = "loc";

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }

    public static DBHelper getInstance() {
        if (dbHelper == null) {
            dbHelper = new DBHelper(Objects.requireNonNull(LawnchairApp.getInstance()).getApplicationContext());
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table downloads " + "(url text, loc text)");
        db.execSQL("create table favorites " + "(url text, original text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS downloads");
        db.execSQL("DROP TABLE IF EXISTS favorites");
        onCreate(db);
    }

    public boolean insertData(String urls, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_URL, urls);
        contentValues.put(COLUMN_LOCATION, location);
        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public ArrayList<String> getAllLocation() {
        ArrayList<String> array_list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select loc from downloads", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            array_list.add(res.getString(res.getColumnIndex(COLUMN_LOCATION)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }


    public Integer deleteSingleRow(String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_LOCATION + " = ? ", new String[]{location});
    }


    public String getImageURI(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        String imageURI = null;
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_LOCATION}, COLUMN_URL + " = ? ", new String[]{url}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            imageURI = cursor.getString(cursor.getColumnIndex(COLUMN_LOCATION));
            cursor.close();
        }
        return imageURI;
    }


    public boolean getURL(String url) {
        boolean ok;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_URL}, COLUMN_URL + " = ? ", new String[]{url}, null, null, null);
        ok = cursor.getCount() != 0;
        cursor.close();
        return ok;
    }

    public boolean getFavorite(String url) {
        boolean ok;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("favorites", new String[]{COLUMN_URL}, COLUMN_URL + " = ? ", new String[]{url}, null, null, null);
        ok = cursor.getCount() != 0;
        cursor.close();
        return ok;
    }

    public boolean addToFavorites(String url, String ori) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_URL, url);
        contentValues.put("original", ori);
        db.insert("favorites", null, contentValues);
        return true;
    }

    public Integer removeFromFavorites(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("favorites", COLUMN_URL + " = ? ", new String[]{url});
    }

    public ArrayList<String> getAllFavoriteItems() {
        ArrayList<String> array_list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select url from favorites", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            array_list.add(res.getString(res.getColumnIndex(COLUMN_URL)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public ArrayList<String> getAllFavoriteOriginal() {
        ArrayList<String> array_list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select original from favorites", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            array_list.add(res.getString(res.getColumnIndex("original")));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
}