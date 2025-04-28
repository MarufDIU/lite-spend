package com.example.litespend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "MoneyTracker.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COL_ID = "_id";
    private static final String COL_TYPE = "type";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_NOTE = "note";
    private static final String COL_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TYPE + " TEXT, " +
                COL_AMOUNT + " REAL, " +
                COL_NOTE + " TEXT, " +
                COL_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(db);
    }

    public void addTransaction(String type, double amount, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TYPE, type);
        values.put(COL_AMOUNT, amount);
        values.put(COL_NOTE, note);
        db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
    }

    public double getBalance() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(CASE WHEN type='ADD' THEN amount ELSE -amount END) FROM " + TABLE_TRANSACTIONS, null);
        double balance = c.moveToFirst() ? c.getDouble(0) : 0;
        c.close();
        return balance;
    }

    public Cursor getAllTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS + " ORDER BY " + COL_TIMESTAMP + " DESC", null);
    }

    public Cursor getDailyStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT strftime('%Y-%m-%d', timestamp) AS day, " +
                "SUM(CASE WHEN type='SPEND' THEN amount ELSE 0 END) AS spent, " +
                "SUM(CASE WHEN type='ADD' THEN amount ELSE 0 END) AS earned " +
                "FROM " + TABLE_TRANSACTIONS + " GROUP BY day ORDER BY day DESC LIMIT 30", null);
    }
}