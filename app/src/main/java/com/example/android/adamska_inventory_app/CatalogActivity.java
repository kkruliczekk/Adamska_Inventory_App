package com.example.android.adamska_inventory_app;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.adamska_inventory_app.data.ContractClass.InventoryEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();
    private static final int INVENTORY_LOADER = 0;
    CursorAdapter mCursorAdapter;
    private ListView mInventoryListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup button to open EditorActivity
        Button insertNew = (Button) findViewById(R.id.button_insert_new_product);
        insertNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the data from database
        mInventoryListView = (ListView) findViewById(R.id.list);

        //It only shows when there are no data od the database
        View emptyView = findViewById(R.id.empty_view);
        mInventoryListView.setEmptyView(emptyView);

        //If there are data in the database - display them as list using the CursorAdapter
        mCursorAdapter = new CursorAdapter(this, null);
        mInventoryListView.setAdapter(mCursorAdapter);

        //When the user click on the product item go to product details
        mInventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, ProductActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });
        //Prepare the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    private void deleteAllProducts() {
        //delete all data from database
        getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
    }

    private void showDeleteConfirmationDialog() {
        //Count the amount of the products of the list
        int productCount = 0;
        Cursor cursor = getContentResolver().query(InventoryEntry.CONTENT_URI,
                new String[]{"count(*)"}, null, null, null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            productCount = cursor.getInt(0);
        }
        cursor.close();

        //if list is empty, set the message
        if (productCount == 0) {
            Toast.makeText(this, getText(R.string.no_items), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = getString(R.string.message_amount_products) + "  " + productCount +
                "\n" + getString(R.string.delete_all_dialog_msg);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void insertProduct() {
        //Go to editor activity
        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert new product" menu option
            case R.id.action_insert_new_product:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all products" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_IMAGE,
        };
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
