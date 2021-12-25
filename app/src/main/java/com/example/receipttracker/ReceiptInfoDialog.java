package com.example.receipttracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class ReceiptInfoDialog extends DialogFragment {

    private FragmentReceiptListener listener;

    private TextInputEditText editTextAmount;
    private TextInputEditText editTextTitle;
    private AutoCompleteTextView editTextCategory;
    private TextInputEditText editTextNote;
    private AutoCompleteTextView textViewDate;
    private ImageView imageViewReceipt;

    private String currency;
    private ReceiptListAdapter receiptAdapter;
    private Set<Currency> currencies = Currency.getAvailableCurrencies();

    private Receipt receipt;
    private Boolean isCreateNew;

    private Date selectedDate;

    public ReceiptInfoDialog(Receipt receipt, ReceiptListAdapter adapter, Boolean isCreateNew) {
        this.receipt = receipt;
        receiptAdapter = adapter;
        this.isCreateNew = isCreateNew;
    }

    public interface FragmentReceiptListener {
        void onInputReceiptSent(Receipt receipt, Boolean isCreateNew);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentReceiptListener) {
            listener = (FragmentReceiptListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentReceiptListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.receipt_dialog, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        receipt.setTitle(editTextTitle.getText().toString());
                        receipt.setAmount(Float.parseFloat(editTextAmount.getText().toString()));
                        receipt.setCategory(editTextCategory.getText().toString());
                        receipt.setCurrency(currency);
                        receipt.setDate(selectedDate);
                        receipt.setNote(editTextNote.getText().toString());

                        getActivity().getPreferences(MODE_PRIVATE).edit().putString("saved_currency", currency).apply();

                        //receiptAdapter.notifyDataSetChanged();
                        listener.onInputReceiptSent(receipt, isCreateNew);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ReceiptInfoDialog.this.getDialog().cancel();
                    }
                });

        editTextAmount = view.findViewById(R.id.amount);
        editTextTitle = view.findViewById(R.id.title);
        editTextCategory = view.findViewById(R.id.category);
        textViewDate = view.findViewById(R.id.receipt_date);
        editTextNote = view.findViewById(R.id.note);
        imageViewReceipt = view.findViewById(R.id.receipt_photo);

        editTextAmount.setText(String.valueOf(receipt.getAmount()));
        editTextTitle.setText(receipt.getTitle());
        editTextNote.setText(receipt.getNote());
        imageViewReceipt.setImageURI(Uri.parse(receipt.getImage()));
        currency = receipt.getCurrency();

        selectedDate = receipt.getDate();

        imageViewReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent receiptImageIntent = new Intent(getActivity(), ImageActivity.class);
                receiptImageIntent.putExtra("image", receipt.getImage());
                startActivity(receiptImageIntent);

            }
        });

        SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MMM-yyyy");
        textViewDate.setText(fmtOut.format(receipt.getDate()), false);

        textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Calendar now = Calendar.getInstance();
                final Calendar c = Calendar.getInstance();
                c.setTime(selectedDate);

                /*
                DatePickerDialog dpd = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            //textViewDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            Calendar c = Calendar.getInstance();
                            c.set(year, monthOfYear, dayOfMonth,0,0);
                            textViewDate.setText(fmtOut.format(c.getTime()));
                            selectedDate = c.getTime();
                        }
                    }, c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DATE));
                dpd.show();*/

                MaterialDatePicker datePicker =
                    MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select dates")
                        .setSelection(selectedDate.getTime())
                        .build();

                datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        Log.i("selected dates", selection.toString());

                        textViewDate.setText(fmtOut.format(new Date((Long) selection)));
                        selectedDate = new Date((Long) selection);
                    }
                });

                datePicker.show(getActivity().getSupportFragmentManager(), "selectDateDialog");

            }
        }

        );

        //get the spinner from the xml.
        AutoCompleteTextView dropdown = view.findViewById(R.id.spinner1);
        //create a list of items for the spinner.
        List items = new ArrayList();

        for (Currency currency: currencies) {
            //System.out.printf("%s\t%s\t%s\n",currency.getDisplayName(), currency.getSymbol(), currency.toString());
            items.add(currency.toString());
        }

        Collections.sort(items);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.list_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);


        //dropdown.setSelection(adapter.getPosition(currency));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            dropdown.setText(currency, false);
        }
/*
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.i("dropdown id", String.valueOf(position));
                currency = items.get(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
*/
        dropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                currency = items.get(position).toString();
            }
        });

        //get the spinner from the xml.
        editTextCategory = view.findViewById(R.id.category);
        //create a list of items for the spinner.
        List categoryItems = new ArrayList();

        categoryItems.add("Housing");
        categoryItems.add("Transport");
        categoryItems.add("Food");
        categoryItems.add("Utilities");
        categoryItems.add("Healthcare");
        categoryItems.add("Entertainment");
        categoryItems.add("Lifestyle");
        categoryItems.add("Other");

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(getContext(), R.layout.list_item, categoryItems);
        //set the spinners adapter to the previously created one.
        editTextCategory.setAdapter(categoryAdapter);


        //dropdown.setSelection(adapter.getPosition(currency));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            editTextCategory.setText(receipt.getCategory(), false);
        }

        editTextCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editTextCategory.setText(categoryItems.get(position).toString());
            }
        });


        return builder.create();
    }


}
