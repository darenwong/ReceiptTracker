package com.example.receipttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "receiptdata.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS receiptdetails(receipt_id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR, amount FLOAT, currency VARCHAR, date DATE, category VARCHAR, note VARCHAR, image VARCHAR, isSummary BOOLEAN, isChecked BOOLEAN)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS receiptdetails");
    }

    public void insertReceipt(Receipt receipt){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO receiptdetails (title, amount, currency, date, category, note, image, isSummary, isChecked) VALUES (?,?,?,?,?,?,?,?,?)",new String[]{receipt.getTitle(), receipt.getAmount().toString(), receipt.getCurrency(), receipt.getDate().toString(), receipt.getCategory(), receipt.getNote(), receipt.getImage(), receipt.getChecked().toString(), receipt.getSummary().toString()});
    }

    public void updateReceipt(Receipt receipt){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title",receipt.getTitle()); //These Fields should be your String values of actual column names
        cv.put("amount",receipt.getAmount());
        cv.put("currency",receipt.getCurrency());
        cv.put("date", receipt.getDate().toString());
        cv.put("category", receipt.getCategory());
        cv.put("note", receipt.getNote());
        cv.put("image", receipt.getImage());
        cv.put("isSummary", receipt.getSummary());
        cv.put("isChecked", receipt.getChecked());

        db.update("receiptdetails", cv, "receipt_id = ?", new String[]{receipt.getId().toString()});
    }

    public void deleteReceipt(Receipt receipt){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM receiptdetails WHERE receipt_id = ?", new String[]{receipt.getId().toString()});
    }

    public Cursor getReceipt(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM receiptdetails", null);
        return cursor;
    }

    public void dropTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS receiptdetails");
        db.execSQL("CREATE TABLE IF NOT EXISTS receiptdetails(receipt_id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR, amount FLOAT, currency VARCHAR, date DATE, category VARCHAR, note VARCHAR, image VARCHAR, isSummary BOOLEAN, isChecked BOOLEAN)");
    }
}
