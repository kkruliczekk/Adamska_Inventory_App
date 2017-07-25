package com.example.android.adamska_inventory_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.adamska_inventory_app.data.ContractClass.InventoryEntry;

import org.w3c.dom.Text;

import static android.R.attr.id;
import static com.example.android.adamska_inventory_app.R.style.quantity;
import static com.example.android.adamska_inventory_app.data.InventoryProvider.LOG_TAG;

public class ProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURRENT_INVENTORY_LOADER = 0;
    private TextView mName;
    private TextView mPrice;
    private ImageView mImage;
    private TextView mQuantity;
    private TextView mDescription;
    private TextView mManufacturer;
    private TextView mEMail;
    private Uri mCurrentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

//        TODO // Setup button to order
//        Button order = (Button) findViewById(R.id.order_button);
//        order.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String address = ;
//                String name = ;
//                String quantity =;
//                String massage = getString(R.string.ordering_message) + name + getString(R.string.ordering_quantity) + quantity;
//                Intent orderIntent = new Intent(Intent.ACTION_SENDTO);
//                orderIntent.setData(Uri.parse("mailto:"+ address)); // only email apps should handle this
//                orderIntent.putExtra(Intent.EXTRA_SUBJECT, "want to order: " + name );
//                orderIntent.putExtra(Intent.EXTRA_TEXT, massage );
//                try {
//                    startActivity(orderIntent);
//                    finish();
//                    Log.i(LOG_TAG, "Finished sending email...");
//                } catch (android.content.ActivityNotFoundException ex) {
//                    Toast.makeText(ProductActivity.this, getString(R.string.warning_sending_email), Toast.LENGTH_SHORT).show();
//
//                }
//            }
//        });

        // Find the view which will be populated with the data from database
        mName = (TextView) findViewById(R.id.overview_name);
        mPrice = (TextView) findViewById(R.id.overview_price);
        mImage = (ImageView) findViewById(R.id.overview_image);
        mQuantity = (TextView) findViewById(R.id.overview_quantity);
        mDescription = (TextView) findViewById(R.id.overview_description);
        mManufacturer = (TextView) findViewById(R.id.overview_manufacturer);
        mEMail = (TextView) findViewById(R.id.overview_email);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //Prepare the loader
        getLoaderManager().initLoader(CURRENT_INVENTORY_LOADER, null, this);

    }

    private void editProduct() {
        //Go to editor activity
        Intent intent = new Intent(ProductActivity.this, EditorActivity.class);
        Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
        intent.setData(currentProductUri);
        startActivity(intent);
    }

    /**
     * Perform the deletion of the current product in the database.
     */
    private void deleteProduct() {

        // Call the ContentResolver to delete the pet at the given content URI.
        // Pass in null for the selection and selection args because the mCurrentProductUri
        // content URI already identifies the pet that we want.
        int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_successful),
                    Toast.LENGTH_SHORT).show();
        }

        // Close the activity
        finish();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_one_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_edit_product:
                editProduct();
                //Leave the menu
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete_one:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the activity shows all product attributes, define a projection that contains
        // all columns from the table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_IMAGE,
                InventoryEntry.COLUMN_DESCRIPTION,
                InventoryEntry.COLUMN_PRODUCER
        };

        //Execute the ContentProvider's query method on a background tread
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int availabilityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_DESCRIPTION);
            int producerColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCER);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(nameColumnIndex);
            int productPrice = cursor.getInt(priceColumnIndex);
            int productQuantity = cursor.getInt(availabilityColumnIndex);
            byte[] productImage = cursor.getBlob(imageColumnIndex);
            String productDescription = cursor.getString(descriptionColumnIndex);
            int productManufacturer = cursor.getInt(producerColumnIndex);

            // Update the views on the screen with the values from the database
            mName.setText(productName);
            mPrice.setText(Integer.toString(productPrice));
            mQuantity.setText(Integer.toString(productQuantity));
            mDescription.setText(productDescription);

            //Convert the image from database to bitmap
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImage, 0, productImage.length);
            mImage.setImageBitmap(imageBitmap);

            //map the constant value from the database
            // into one of the dropdown options (0 is Nothing, 1 is Planszoweczka, 2 is Marajo,
            // 3 is Rebel, 4 is Granna, 5 is Galakta).
//TODO użyć danych z bazy - włożyć maile do bazy?
            String manufacturer;
            String eMail;
            if (productManufacturer == InventoryEntry.PRODUCER_PLANSZOWECZKA) {
                manufacturer = getString(R.string.producer_planszoweczka);
                eMail = getString(R.string.address_planszoweczka);
            } else if (productManufacturer == InventoryEntry.PRODUCER_MARAJO) {
                manufacturer = getString(R.string.producer_marajo);
                eMail = getString(R.string.address_marajo);
            } else if (productManufacturer == InventoryEntry.PRODUCER_REBEL) {
                manufacturer = getString(R.string.producer_rebel);
                eMail = getString(R.string.address_rebel);
            } else if (productManufacturer == InventoryEntry.PRODUCER_GRANNA) {
                manufacturer = getString(R.string.producer_granna);
                eMail = getString(R.string.address_granna);
            } else if (productManufacturer == InventoryEntry.PRODUCER_GALAKTA) {
                manufacturer = getString(R.string.producer_galakta);
                eMail = getString(R.string.address_galakta);
            } else {
                manufacturer = getString(R.string.producer_nothing);
                eMail = getString(R.string.address_nothing);
            }

            mManufacturer.setText(manufacturer);
            mEMail.setText(eMail);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
// If the loader is invalidated, clear out all the data from the input fields.
        mName.setText("");
        mPrice.setText("");
        mQuantity.setText("");
        mDescription.setText("");
        mManufacturer.setText("");
        mEMail.setText("");
        mImage.setImageBitmap(null);
    }
}
