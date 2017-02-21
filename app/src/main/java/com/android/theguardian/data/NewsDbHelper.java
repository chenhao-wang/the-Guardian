package com.android.theguardian.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.theguardian.data.NewsContract.NewsEntry;

public class NewsDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "news.db";

    public NewsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_NEWS_TABLE = "CREATE TABLE " + NewsEntry.TABLE_NAME + " (" +
                NewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                NewsEntry.COLUMN_SECTION + " TEXT NOT NULL," +
                NewsEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                NewsEntry.COLUMN_THUMBNAIL_URL + " TEXT," +
                NewsEntry.COLUMN_WEB_URL + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_NEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NewsEntry.TABLE_NAME);
        onCreate(db);
    }
}
