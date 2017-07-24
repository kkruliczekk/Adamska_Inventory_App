package com.example.android.adamska_inventory_app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import java.io.OutputStream;

import static android.R.attr.data;
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
    private void saveProduct() {
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
            return;
        }

        //Check if the User inserts required field - name and price
        //if not - display message
        if (nameString.isEmpty()) {
            Toast.makeText(this, getString(R.string.warning_name), Toast.LENGTH_SHORT).show();

            //TODO zapytaj czy tak może być
        } else if (priceString.isEmpty() || Integer.parseInt(priceString) < 0) {
            Toast.makeText(this, getString(R.string.warning_price), Toast.LENGTH_SHORT).show();

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
                saveProduct();
                //Leave the menu
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

