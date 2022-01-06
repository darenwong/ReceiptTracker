package com.example.receipttracker;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceiptListAdapter extends ArrayAdapter<Receipt> {
    private static final String TAG = "ReceiptListAdapter";
    private Context mContext;
    int mResource;
    private List<Receipt> objects;

    public ReceiptListAdapter(@NonNull Context context, int resource, @NonNull List<Receipt> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Log.i("getView", getItem(position).toString());
        if (getItem(position) != null && getItem(position).getSummary() == false) {
            Float amount = getItem(position).getAmount();
            String currency = getItem(position).getCurrency();
            Date date = getItem(position).getDate();
            String title = getItem(position).getTitle();
            String category = getItem(position).getCategory();
            String note = getItem(position).getNote();
            String image = getItem(position).getImage();

            //Receipt receipt = new Receipt(amount, currency, date, title, category, note, image);

            LayoutInflater inflater = LayoutInflater.from(mContext);

            convertView = inflater.inflate(R.layout.custom_list_view, parent, false);

            TextView rTitle = (TextView) convertView.findViewById(R.id.textView1);
            TextView rAmount = (TextView) convertView.findViewById(R.id.textView2);
            ImageView rImage = (ImageView) convertView.findViewById(R.id.imageView);
            CheckBox rCheck = (CheckBox) convertView.findViewById(R.id.checkBox);

            rCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if (isChecked) {
                        getItem(position).setChecked(true);
                    } else {
                        getItem(position).setChecked(false);
                    }
                }
            });
            rCheck.setChecked(getItem(position).getChecked());

            rTitle.setText(title);
            rAmount.setText(currency + " " + String.format("%.02f", amount));
            rImage.setImageURI(Uri.parse(image));

        } else {
            Date date = getItem(position).getDate();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.custom_date_list_view, parent, false);

            TextView rText = (TextView) convertView.findViewById(R.id.dateTextView);
            rText.setText(new SimpleDateFormat("dd-MMM-yyyy").format(date));
        }


        return convertView;
    }
}
