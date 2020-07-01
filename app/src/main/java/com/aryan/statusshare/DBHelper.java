package com.aryan.statusshare;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ss_main.db";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE status (status TEXT, user_id_fk INTEGER, timestamp TEXT, user_name TEXT);");
        db.execSQL("CREATE TABLE friends (friend_id INTEGER , friend_name TEXT, friend_status INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS status");
        db.execSQL("DROP TABLE IF EXISTS friends");
        onCreate(db);
    }

    public void refresh(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS status;");
        db.execSQL("DROP TABLE IF EXISTS friends;");
        db.execSQL("CREATE TABLE status (status TEXT, user_id_fk INTEGER, timestamp TEXT, user_name TEXT);");
        db.execSQL("CREATE TABLE friends (friend_id INTEGER , friend_name TEXT, friend_status INTEGER);");
    }

    public boolean addPost(String status2, int uid, String timestamp, String uname) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.query("status", new String[]{"status", "timestamp"}, "status = ? AND user_id_fk = ?", new String[]{status2,String.valueOf(uid)}, null, null, "timestamp");
        int n = res.getCount();
        res.close();
        if (n >= 1)
            return true;
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put("status_id",1);
        contentValues.put("status", status2);
        contentValues.put("user_id_fk", uid);
        contentValues.put("timestamp",timestamp);
        contentValues.put("user_name",uname);
        db.insert("status", null, contentValues);
        return true;
    }

    public String[] getOwnPosts(int uid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.query("status", new String[]{"status", "timestamp"}, "user_id_fk = ?", new String[]{String.valueOf(uid)}, null, null, "timestamp desc");
        res.moveToFirst();
        int n = res.getCount();
        String[] status = new String[n];
        int i = 0;
        while (!res.isAfterLast()) {
            status[i] = res.getString(res.getColumnIndex("status")) + "   " + res.getString(res.getColumnIndex("timestamp"));
            i++;
            res.moveToNext();
        }
        res.close();
        return status;
    }

    public String[] showFeed() {
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor res = db.rawQuery("SELECT * from status;",null);
        Cursor res = db.query("status", new String[]{"status", "timestamp","user_name"}, null, null, null, null, "timestamp desc");
        res.moveToFirst();
        int n = res.getCount();
        String[] status = new String[n];
        int i = 0;
        while (!res.isAfterLast()) {
            status[i] = res.getString(res.getColumnIndex("user_name")) + ":   "+ res.getString(res.getColumnIndex("status")) + "   " + res.getString(res.getColumnIndex("timestamp"));
            i++;
            res.moveToNext();
        }
        res.close();
        return status;
    }

    public boolean addFriendPending(String friend_name, int friend_id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.query("friends",new String[]{"friend_id"},"friend_id = ?", new String[]{String.valueOf(friend_id)},null,null,null);
        int n = res.getCount();
        res.close();
        if (n >= 1)
            return true;
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("friend_id", friend_id);
        contentValues.put("friend_name", friend_name);
        contentValues.put("friend_status",0);
        db.insert("friends", null, contentValues);
        return true;
    }

    public boolean addFriend(String friend_name, int friend_id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.query("friends",new String[]{"friend_id"},"friend_id = ?", new String[]{String.valueOf(friend_id)},null,null,null);
        int n = res.getCount();
        res.close();
        if (n == 1) {
            db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("friend_status",1);
            db.update("friends",contentValues,"friend_id = ?",new String[]{String.valueOf(friend_id)});
            return true;
        }
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("friend_id", friend_id);
        contentValues.put("friend_name", friend_name);
        contentValues.put("friend_status",1);
        db.insert("friends", null, contentValues);
        return true;
    }

    public String[] getFriends() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.query("friends", new String[]{"friend_name"}, "friend_status = 1", null, null, null, null);
        res.moveToFirst();
        int n = res.getCount();
        String[] friends = new String[n];
        int i = 0;
        while (!res.isAfterLast()) {
            friends[i] = res.getString(res.getColumnIndex("friend_name"));
            i++;
            res.moveToNext();
        }
        res.close();
        return friends;
    }

    public String[] getPendingFriends() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.query("friends", new String[]{"friend_name"}, "friend_status = 0", null, null, null, null);
        res.moveToFirst();
        int n = res.getCount();
        String[] friends = new String[n];
        int i = 0;
        while (!res.isAfterLast()) {
            friends[i] = res.getString(res.getColumnIndex("friend_name"));
            i++;
            res.moveToNext();
        }
        res.close();
        return friends;
    }
}
