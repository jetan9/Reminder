package com.jetan.www.reminders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by anningyu on 2017-01-12.
 */

public class RemindersDbAdapter {
    public static final String COL_ID = "_id";
    public static final String COL_CONTENT = "content";
    public static final String COL_IMPORTANT = "important";

    public static final int INDEX_ID = 0;
    public static final int INDEX_CONTENT = INDEX_ID + 1;
    public static final int INDEX_IMPORTANT = INDEX_ID + 2;

    private static final String TAG = "RemindersDbAdapter";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private static final String DATABASE_NAME = "db_reminders";
    private static final String TABLE_NAME = "tb_reminders";
    private static final int DATABASE_VERSION = 1;

    private final Context ctx;

    public RemindersDbAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void open() throws SQLException {
        dbHelper = new DatabaseHelper(ctx);
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    public long createReminder(String name, boolean important) {
        ContentValues values = new ContentValues();
        values.put(COL_CONTENT, name);
        values.put(COL_IMPORTANT, important ? 1 : 0);
        return db.insert(TABLE_NAME, null, values);
    }

    public long createReminder(Reminder reminder) {
        return createReminder(reminder.getContent(), reminder.getImportant() == 1);
    }

    public Reminder fetchReminderById(int id) {
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_ID,
                COL_CONTENT, COL_IMPORTANT}, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return new Reminder(
                cursor.getInt(INDEX_ID),
                cursor.getString(INDEX_CONTENT),
                cursor.getInt(INDEX_IMPORTANT)
        );
    }

    public Cursor fetchAllReminders() {
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_ID, COL_CONTENT, COL_IMPORTANT},
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void updateReminder(Reminder reminder) {
        ContentValues values = new ContentValues();
        values.put(COL_CONTENT, reminder.getContent());
        values.put(COL_IMPORTANT, reminder.getImportant());
        db.update(TABLE_NAME, values,
                COL_ID + "=?", new String[]{String.valueOf(reminder.getId())});
    }

    public void deleteReminderById(int id) {
        db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteAllReminders() {
        db.delete(TABLE_NAME, null, null);
    }

    private static final String DATABASE_CREATE =
            "create table if not exists " + TABLE_NAME + " ( " +
                    COL_ID + " integer primary key autoincrement, " +
                    COL_CONTENT + " text, " +
                    COL_IMPORTANT + " integer );";


    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("drop table if exists " + TABLE_NAME);
            onCreate(db);
        }
    }
}
