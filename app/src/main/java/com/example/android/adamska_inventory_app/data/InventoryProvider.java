package com.example.android.adamska_inventory_app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static com.example.android.adamska_inventory_app.data.ContractClass.CONTENT_AUTHORITY;
import static com.example.android.adamska_inventory_app.data.ContractClass.PATH_INVENTORY;

/**
 * Created by kasia on 22.07.17.
 */

public class InventoryProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int PRODUCTS = 0;

    private static final int PRODUCT_ID = 1;

    //Build up a tree of UriMatcher objects
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_INVENTORY, PRODUCTS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_INVENTORY + "/#", PRODUCT_ID);
    }

    //Database helper object
    private DatabaseHelper mDbHelper;

    //Initialize the provider and the database helper object.
    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    //Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //Cursor which holds the result of the query
        Cursor cursor;

        //Check if the UriMatcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            //Query directly on the all products data
            //Cursor contains all rows from the table
            case PRODUCTS:
                cursor = database.query(ContractClass.InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            //For the one product you need to first extract the id of the row from Uri and use it to create a
            //query of the data from that row.
            //Cursor contains that row
            case PRODUCT_ID:
                selection = ContractClass.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ContractClass.InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query - unknow URI" + uri);
        }
        //Register to watch a content URI for changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ContractClass.InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ContractClass.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    //Insert new data into the provider with the given ContentValues.
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            //Check if the Uri was about products
            case PRODUCTS:
                //if the Uri was about products, call method insertProduct
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    //Insert a new product into the database. Create a new Uri for the new row

    private Uri insertProduct(Uri uri, ContentValues values) {
        Log.v("Kasia", "vlues: " + values);
        //Name can not be null
        String name = values.getAsString(ContractClass.InventoryEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Name of product is required.");
        }

        //Producer can not be null
        Integer producer = values.getAsInteger(ContractClass.InventoryEntry.COLUMN_PRODUCER);
        if (producer == null || !ContractClass.InventoryEntry.isValidProducer(producer)) {
            throw new IllegalArgumentException("Valid manufacturer is required.");
        }

        //Price can not be null and can not be smaller than 0
        Integer price = values.getAsInteger(ContractClass.InventoryEntry.COLUMN_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Valid price is required.");
        }
        //If quantity is provided - it can not be smaller than 0
        Integer quantity = values.getAsInteger(ContractClass.InventoryEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity can not be a negative number.");
        }
        //Description can be null so it is no point to write something here
        //Image can be null

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Insert the new product with the given data
        long id = database.insert(ContractClass.InventoryEntry.TABLE_NAME, null, values);
        //If the ID -1 it means that the insertion failed - log and error
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for: " + uri);
            return null;
        }
        //Notify registered observers that a row was added
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    //Delete the date with the given selection and selectionArg
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                //Delete all rows from the table that match the given selection and selectionArg
                rowsDeleted = database.delete(ContractClass.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                //From a given ID create a selection and selectionArg and delete a single row from the table
                selection = ContractClass.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ContractClass.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion failed for: " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    //Update the date from the given row - according to the given selection and selection arguments
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                //extract the ID from the Uri and use it to create a proper selection and selectionArg
                selection = ContractClass.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update failed for: " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //If the {@link inventoryEntry.COLUMN_NAME} key is present:
        //Name can not be null
        if (values.containsKey(ContractClass.InventoryEntry.COLUMN_NAME)) {
            String name = values.getAsString(ContractClass.InventoryEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Name of product is required.");
            }
        }

        //If the {@link inventoryEntry.COLUMN_PRODUCER} key is present:
        //Producer can not be null
        if (values.containsKey(ContractClass.InventoryEntry.COLUMN_PRODUCER)) {
            Integer producer = values.getAsInteger(ContractClass.InventoryEntry.COLUMN_PRODUCER);
            if (producer == null || !ContractClass.InventoryEntry.isValidProducer(producer)) {
                throw new IllegalArgumentException("Valid manufacturer is required.");
            }
        }

        //If the {@link inventoryEntry.COLUMN_PRICE} key is present:
        //Price can not be null and can not be smaller than 0
        if (values.containsKey(ContractClass.InventoryEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(ContractClass.InventoryEntry.COLUMN_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Valid price is required.");
            }
        }

        //If the {@link inventoryEntry.COLUMN_QUANTITY} key is present:
        //If quantity is provided - it can not be smaller than 0
        if (values.containsKey(ContractClass.InventoryEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(ContractClass.InventoryEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Quantity can not be a negative number.");
            }
        }
        //Description can be null so it is no point to write something here

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Update on the database and get the number of rows affected
        int rowsUpdated = database.update(ContractClass.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        //Return the amount of rows updated
        return rowsUpdated;
    }
}