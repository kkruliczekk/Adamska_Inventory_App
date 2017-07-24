package com.example.android.adamska_inventory_app.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by kasia on 22.07.17.
 */

public class ContractClass {

    //Name of the content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    //base URI which apps will use to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //Possible path
    public static final String PATH_INVENTORY = "inventory";

    private ContractClass() {
    }

    public static abstract class InventoryEntry implements BaseColumns {

        //The content Uri to access the data from the inventory app in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        //The MIME type of the content Uri for the inventory list
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //The MIME type of the content Uri for the single product from the inventory list
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //Name of the table and headlines of the columns using to create a inventory table
        public static final String TABLE_NAME = "Inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRODUCER = "manufacturer";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_IMAGE = "image";

        //possible values for manufacturer
        public static final int PRODUCER_NOTHING = 0;
        public static final int PRODUCER_PLANSZOWECZKA = 1;
        public static final int PRODUCER_MARAJO = 2;
        public static final int PRODUCER_REBEL = 3;
        public static final int PRODUCER_GRANNA = 4;
        public static final int PRODUCER_GALAKTA = 5;

        /**
         * Returns whether or not the given producer is chosen
         */
        public static boolean isValidProducer(int producer) {
            if (producer == PRODUCER_NOTHING || producer == PRODUCER_PLANSZOWECZKA || producer == PRODUCER_MARAJO
                    || producer == PRODUCER_REBEL || producer == PRODUCER_GRANNA || producer == PRODUCER_GALAKTA) {
                return true;
            }
            return false;
        }
    }
}
