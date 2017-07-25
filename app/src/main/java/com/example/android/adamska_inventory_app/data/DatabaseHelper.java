package com.example.android.adamska_inventory_app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by kasia on 22.07.17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    //Name of the database file
    private static final String DATABASE_NAME = "inventory.db";

    //Database version
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Create the database for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        //String that contans the SQL statement to create the inventory table
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE "
                + ContractClass.InventoryEntry.TABLE_NAME + " ("
                + ContractClass.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ContractClass.InventoryEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ContractClass.InventoryEntry.COLUMN_IMAGE + " BLOB, "
                + ContractClass.InventoryEntry.COLUMN_PRODUCER + " INTEGER NOT NULL DEFAULT 0, "
                + ContractClass.InventoryEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + ContractClass.InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ContractClass.InventoryEntry.COLUMN_DESCRIPTION + " TEXT);";

        //Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    //If the database needs to be upgraded this method is called
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
