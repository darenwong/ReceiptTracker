package com.example.receipttracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class FilterDateDialog extends DialogFragment {

    private FragmentFilterDateListener listener;

    private TextView textViewStartDate;
    private TextView textViewEndDate;

    private Date filterStartDate;
    private Date filterEndDate;

    public FilterDateDialog(Date filterStartDate, Date filterEndDate) {
        this.filterStartDate = filterStartDate;
        this.filterEndDate = filterEndDate;
    }

    public interface FragmentFilterDateListener {
        void onInputFilterDateSent(Date startDate, Date endDate);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentFilterDateListener) {
            listener = (FragmentFilterDateListener) context;
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

        View view = inflater.inflate(R.layout.filter_date_dialog, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        listener.onInputFilterDateSent(filterStartDate, filterEndDate);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FilterDateDialog.this.getDialog().cancel();
                    }
                });

        textViewStartDate = view.findViewById(R.id.textViewStartDate);
        textViewEndDate = view.findViewById(R.id.textViewEndDate);



        SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MMM-yyyy");
        textViewStartDate.setText("From: "+fmtOut.format(filterStartDate));
        textViewStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Calendar now = Calendar.getInstance();
                final Calendar c = Calendar.getInstance();
                c.setTime(filterStartDate);
                DatePickerDialog dpd = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                //textViewStartDate.setText("Start Date: " + dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                Calendar c = Calendar.getInstance();
                                c.set(year, monthOfYear, dayOfMonth,0,0);
                                filterStartDate = c.getTime();
                                textViewEndDate.setText("From: "+fmtOut.format(filterStartDate));
                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
                dpd.getDatePicker().setMaxDate(filterEndDate.getTime());
                dpd.show();
            }
        });

        textViewEndDate.setText("To: "+fmtOut.format(filterEndDate));
        textViewEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Calendar now = Calendar.getInstance();
                final Calendar c = Calendar.getInstance();
                c.setTime(filterEndDate);

                DatePickerDialog dpd = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                //textViewEndDate.setText("End Date: " + dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                Calendar c = Calendar.getInstance();
                                c.set(year, monthOfYear, dayOfMonth,0,0);
                                filterEndDate = c.getTime();
                                textViewEndDate.setText("To: "+fmtOut.format(filterEndDate));
                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
                dpd.getDatePicker().setMinDate(filterStartDate.getTime());
                dpd.show();
            }
        });

        return builder.create();
    }


}
