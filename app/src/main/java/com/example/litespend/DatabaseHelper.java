package com.example.litespend;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "LiteSpend.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE transactions (" +
                "_id INTEGER PRIMARY KEY," +
                "type TEXT," +
                "amount REAL," +
                "note TEXT," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS transactions");
        onCreate(db);
    }

    public void addTransaction(String type, double amount, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO transactions (type, amount, note) VALUES (?, ?, ?)",
                new Object[]{type, amount, note});
        db.close();
    }

    public double getBalance() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(CASE WHEN type='ADD' THEN amount ELSE -amount END) FROM transactions", null);
        double balance = c.moveToFirst() ? c.getDouble(0) : 0;
        c.close();
        return balance;
    }

    public Cursor getDailyStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT strftime('%Y-%m-%d', timestamp) AS day, " +
                "SUM(CASE WHEN type='SPEND' THEN amount ELSE 0 END) AS spent, " +
                "SUM(CASE WHEN type='ADD' THEN amount ELSE 0 END) AS earned " +
                "FROM transactions GROUP BY day ORDER BY day DESC LIMIT 30", null);
    }
}