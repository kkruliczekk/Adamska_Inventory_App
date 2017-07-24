package com.example.android.adamska_inventory_app;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.adamska_inventory_app.data.ContractClass;

import static android.R.attr.data;

/**
 * Created by kasia on 22.07.17.
 */

public class CursorAdapter extends android.widget.CursorAdapter {

     /**
     * Constructs a new {@link CursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public CursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

     /**Make a new blank list item view.
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

     /**Binds the  data in the current row pointed to by cursor to the given list item layout.
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int nameColumnIndex = cursor.getColumnIndex(ContractClass.InventoryEntry.COLUMN_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ContractClass.InventoryEntry.COLUMN_PRICE);
        int availabilityColumnIndex = cursor.getColumnIndex(ContractClass.InventoryEntry.COLUMN_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(ContractClass.InventoryEntry.COLUMN_IMAGE);

        String productName = cursor.getString(nameColumnIndex);
        double productPrice = cursor.getInt(priceColumnIndex);
        int productQuantity = cursor.getInt(availabilityColumnIndex);
        byte[] productImage = cursor.getBlob(availabilityColumnIndex);
        //TODO wez obrazek

        // If the quantity is empty string or null, then set it as 0.
        //if (TextUtils.isEmpty(productQuantity)) {
           // productQuantity = context.getInt(0);
       // }

        viewHolder.nameView.setText(productName);
        viewHolder.priceView.setText(productName);
        viewHolder.availabilityView.setText(productQuantity);
        //TODO viewHolder.imageView.setImageBitmap(productImage);

    }

    //Cache of the children views for the list item
    public static class ViewHolder {
        public final TextView nameView;
        public final TextView priceView;
        public final TextView availabilityView;
        public final ImageView imageView;

        public ViewHolder (View view) {
            nameView = (TextView) view.findViewById(R.id.name_on_list);
            priceView = (TextView) view.findViewById(R.id.price_on_list);
            availabilityView = (TextView) view.findViewById(R.id.quantity_on_list);
            imageView = (ImageView) view.findViewById(R.id.image_on_list);
        }
    }
}
