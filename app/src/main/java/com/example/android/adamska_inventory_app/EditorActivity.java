package com.example.android.adamska_inventory_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.adamska_inventory_app.data.ContractClass;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.android.adamska_inventory_app.data.ContractClass.InventoryEntry.PRODUCER_NOTHING;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_INVENTORY_LOADER = 0;

    private static final int REQUEST_CODE = 1;
    private Bitmap bitmap;
    private ImageView mImageView;

    private Uri mCurrentProductUri;

    private EditText mEditName;
    private EditText mEditQuantity;
    private EditText mEditPrice;
    private EditText mEditDescription;

    private Spinner mManufacturerSpinner;

    private int mManufacturer = PRODUCER_NOTHING;

    private boolean mProductHasChanged = false;

    //Check if changes were introduced wto the edit fields
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            // This is a insert call, so change the app bar to say "Enter a Product"
            setTitle(getString(R.string.new_product_activity_name));
        } else {
            setTitle(getString(R.string.editor_activity_name));
            // Initialize a loader to read the data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        //Find all relevant views that will need to read user input from
        mEditName = (EditText) findViewById(R.id.edit_name);
        mImageView = (ImageView) findViewById(R.id.edit_image);
        mManufacturerSpinner = (Spinner) findViewById(R.id.spinner_manufacturer);
        mEditQuantity = (EditText) findViewById(R.id.edit_quantity);
        mEditPrice = (EditText) findViewById(R.id.edit_price);
        mEditDescription = (EditText) findViewById(R.id.edit_description);

        setupSpinner();

        // Setup OnTouchListeners on all the input fields to determine if the user
        // has touched or modified them
        mEditName.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
        mManufacturerSpinner.setOnTouchListener(mTouchListener);
        mEditQuantity.setOnTouchListener(mTouchListener);
        mEditPrice.setOnTouchListener(mTouchListener);
        mEditDescription.setOnTouchListener(mTouchListener);

        Button download = (Button) findViewById(R.id.download_image);
        download.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Pick image from user gallery
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    //Read image from input stream
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
            try {
                // We need to recyle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }
                InputStream stream = getContentResolver().openInputStream(data.getData());
                //Create bitmap from stream
                bitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                mImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner.
        // the spinner will use the default layout
        ArrayAdapter manufacturerSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_producer_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        manufacturerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mManufacturerSpinner.setAdapter(manufacturerSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mManufacturerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.producer_planszoweczka))) {
                        mManufacturer = ContractClass.InventoryEntry.PRODUCER_PLANSZOWECZKA;
                    } else if (selection.equals(getString(R.string.producer_marajo))) {
                        mManufacturer = ContractClass.InventoryEntry.PRODUCER_MARAJO;
                    } else if (selection.equals(getString(R.string.producer_rebel))) {
                        mManufacturer = ContractClass.InventoryEntry.PRODUCER_REBEL;
                    } else if (selection.equals(getString(R.string.producer_granna))) {
                        mManufacturer = ContractClass.InventoryEntry.PRODUCER_GRANNA;
                    } else if (selection.equals(getString(R.string.producer_galakta))) {
                        mManufacturer = ContractClass.InventoryEntry.PRODUCER_GALAKTA;
                    } else {
                        mManufacturer = PRODUCER_NOTHING;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mManufacturer = PRODUCER_NOTHING;
            }
        });
    }

    //Get user input and save new data into database
    private boolean saveProduct() {
        //read from input fields
        String nameString = mEditName.getText().toString().trim();
        String quantityString = mEditQuantity.getText().toString().trim();
        String priceString = mEditPrice.getText().toString().trim();
        String descriptionString = mEditDescription.getText().toString().trim();

        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && bitmap == null && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) && mManufacturer == ContractClass.InventoryEntry.PRODUCER_NOTHING &&
                TextUtils.isEmpty(descriptionString)) {
            return true;
        }

        //Check if the User inserts required field - name and price
        //if not - display message
        if (nameString.isEmpty()) {
            Toast.makeText(this, getString(R.string.warning_name), Toast.LENGTH_SHORT).show();
            return false;
        } else if (priceString.isEmpty() || Integer.parseInt(priceString) < 0) {
            Toast.makeText(this, getString(R.string.warning_price), Toast.LENGTH_SHORT).show();
            return false;

            //if required fields are not empty - add the data to database
        } else {
            // Create a ContentValues object
            ContentValues values = new ContentValues();
            //attributes from the editor are the values
            values.put(ContractClass.InventoryEntry.COLUMN_NAME, nameString);
            values.put(ContractClass.InventoryEntry.COLUMN_PRODUCER, mManufacturer);

            // If the quantity is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default.
            int quantity = 0;
            if (!TextUtils.isEmpty(quantityString)) {
                quantity = Integer.parseInt(quantityString);
            }
            values.put(ContractClass.InventoryEntry.COLUMN_QUANTITY, quantity);

            //Convert price to int
            int price = Integer.parseInt(priceString);
            values.put(ContractClass.InventoryEntry.COLUMN_PRICE, price);

            //If no image was chosen, set the default one
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.deposit);
            }
            //Convert image to the ByteArray, so that it could be put into database
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 0, outputStream);
            byte[] imageToDatabase = outputStream.toByteArray();
            values.put(ContractClass.InventoryEntry.COLUMN_IMAGE, imageToDatabase);

            //If no description was added, set the default text
            String description = getString(R.string.coming_soon);
            if (descriptionString != null) {
                description = descriptionString;
            }
            values.put(ContractClass.InventoryEntry.COLUMN_DESCRIPTION, description);


            // Determine if this is a new or existing product by checking if mCurrentProduct Uri is null or not
            if (mCurrentProductUri == null) {
                // This is a NEW product, so insert a new data into the provider,
                //, returning the new content URI
                Uri newUri = getContentResolver().insert(ContractClass.InventoryEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_successful),
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                //Otherwise this is an existing product, so udate the data
                int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        return true;
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
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
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert new product" menu option
            case R.id.action_save:
                if (saveProduct()) {
                    //Leave the menu
                    finish();
                }
                return true;
            case android.R.id.home:
                // If the product has not changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the table
        String[] projection = {
                ContractClass.InventoryEntry._ID,
                ContractClass.InventoryEntry.COLUMN_NAME,
                ContractClass.InventoryEntry.COLUMN_PRICE,
                ContractClass.InventoryEntry.COLUMN_QUANTITY,
                ContractClass.InventoryEntry.COLUMN_IMAGE,
                ContractClass.InventoryEntry.COLUMN_DESCRIPTION,
                ContractClass.InventoryEntry.COLUMN_PRODUCER
        };

        //Execute the ContentProvider's query method on a background tread
        return new CursorLoader(this,
                ContractClass.InventoryEntry.CONTENT_URI,
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
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ContractClass.InventoryEntry.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ContractClass.InventoryEntry.COLUMN_PRICE);
            int availabilityColumnIndex = cursor.getColumnIndex(ContractClass.InventoryEntry.COLUMN_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ContractClass.InventoryEntry.COLUMN_IMAGE);
            int descriptionColumnIndex = cursor.getColumnIndex(ContractClass.InventoryEntry.COLUMN_DESCRIPTION);
            int producerColumnIndex = cursor.getColumnIndex(ContractClass.InventoryEntry.COLUMN_PRODUCER);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(nameColumnIndex);
            int productPrice = cursor.getInt(priceColumnIndex);
            int productQuantity = cursor.getInt(availabilityColumnIndex);
            byte[] productImage = cursor.getBlob(imageColumnIndex);
            String productDescription = cursor.getString(descriptionColumnIndex);
            int productManufacturer = cursor.getInt(producerColumnIndex);

            // Update the views on the screen with the values from the database
            mEditName.setText(productName);
            mEditPrice.setText(Integer.toString(productPrice));
            mEditQuantity.setText(Integer.toString(productQuantity));
            mEditDescription.setText(productDescription);

            //TODO sprawdź czy to dobrze
            //Convert the image from database to bitmap
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImage, 0, productImage.length);
            mImageView.setImageBitmap(imageBitmap);

            //map the constant value from the database
            // into one of the dropdown options (0 is Nothing, 1 is Planszoweczka, 2 is Marajo,
            // 3 is Rebel, 4 is Granna, 5 is Galakta).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (productManufacturer) {
                case ContractClass.InventoryEntry.PRODUCER_PLANSZOWECZKA:
                    mManufacturerSpinner.setSelection(1);
                    break;
                case ContractClass.InventoryEntry.PRODUCER_MARAJO:
                    mManufacturerSpinner.setSelection(2);
                    break;
                case ContractClass.InventoryEntry.PRODUCER_REBEL:
                    mManufacturerSpinner.setSelection(3);
                    break;
                case ContractClass.InventoryEntry.PRODUCER_GRANNA:
                    mManufacturerSpinner.setSelection(4);
                    break;
                case ContractClass.InventoryEntry.PRODUCER_GALAKTA:
                    mManufacturerSpinner.setSelection(5);
                    break;
                default:
                    mManufacturerSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mEditName.setText("");
        mEditPrice.setText("");
        mEditQuantity.setText("");
        mEditDescription.setText("");
        mManufacturerSpinner.setSelection(0);
        mImageView.setImageBitmap(null);
    }
}

