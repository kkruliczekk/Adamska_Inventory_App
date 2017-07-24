package com.example.android.adamska_inventory_app;

import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.adamska_inventory_app.data.ContractClass;

import static android.R.attr.data;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    CursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Find the ListView which will be populated with the data from database
        ListView inventoryListView = (ListView) findViewById(R.id.list);

        //Find and set empty view on the ListView
        //It only shows when there are no data od the database
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        //If there are data in the database - display them as list using the CursorAdapter
        mCursorAdapter = new CursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        //When the user click on the product item go to product details
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, ProductActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ContractClass.InventoryEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });
        //Prepare the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }


    private void deleteAllProducts() {
        //delete all data from database
        getContentResolver().delete(ContractClass.InventoryEntry.CONTENT_URI, null, null);
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
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                ContractClass.InventoryEntry._ID,
                ContractClass.InventoryEntry.COLUMN_NAME,
                ContractClass.InventoryEntry.COLUMN_PRICE,
                ContractClass.InventoryEntry.COLUMN_QUANTITY,
                ContractClass.InventoryEntry.COLUMN_IMAGE,
        };
        return new CursorLoader(this,
                ContractClass.InventoryEntry.CONTENT_URI,
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
