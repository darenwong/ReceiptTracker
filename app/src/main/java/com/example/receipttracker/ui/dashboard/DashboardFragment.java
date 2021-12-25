package com.example.receipttracker.ui.dashboard;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.receipttracker.MainActivity;
import com.example.receipttracker.R;
import com.example.receipttracker.Receipt;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class DashboardFragment extends Fragment {

    private AutoCompleteTextView spinnerCurrency;
    private AutoCompleteTextView spinnerYear;
    private ArrayList<String> currencyList;
    private ArrayList<Integer> yearList;
    private ArrayAdapter<String> currencyAdapter;
    private ArrayAdapter<Integer> yearAdapter;

    private BarChart chart;

    private ArrayList<Receipt> receiptArrayList;

    private ArrayList<String> frequencyChoice = new ArrayList<>(Arrays.asList("Daily", "Monthly", "Yearly"));
    private Integer frequencySelected = 0;

    private Integer selectedYear = Calendar.getInstance().get(Calendar.YEAR);
    private String selectedCurrency;

    private HashMap<String, HashMap<Integer, List<Float>>> receiptMap = new HashMap<String, HashMap<Integer, List<Float>>>();
    private HashMap<String, HashSet<Integer>> receiptCurrencyToYearMap = new HashMap<String, HashSet<Integer>>();
    private HashMap<Integer, HashSet<String>> receiptYearToCurrencyMap = new HashMap<Integer, HashSet<String>>();

    private TextView totalAmountTextView;
    private TextView totalReceiptsTextView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        loadData();
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        totalAmountTextView = root.findViewById(R.id.totalAmount);
        totalReceiptsTextView = root.findViewById(R.id.totalReceipts);

        Log.i("selectedCurrency", selectedCurrency.toString());
        setUpSpinnerCurrency(root);
        setUpSpinnerYear(root);



        float groupSpace = 0.08f;
        float barSpace = 0.06f; // x4 DataSet
        float barWidth = 0.4f; // x4 DataSet
        // (0.2 + 0.03) * 4 + 0.08 = 1.00 -> interval per "group"

        int groupCount = 6;
        int startYear = 0;

        chart = (BarChart) root.findViewById(R.id.chart);


        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getXAxisValues()));

        updateBarChart();



        return root;
    }

    private void updateBarChart() {
        BarData data = new BarData(getDataSet());
        chart.setData(data);
        //chart.setFitBars(true);
        chart.getAxisLeft().setDrawLabels(true);
        chart.getAxisRight().setDrawLabels(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);

        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        //chart.setDescription((Description) "My Chart");

        chart.getXAxis().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisRight().setDrawAxisLine(false);


        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setAxisMinimum(0f);
        chart.getXAxis().setLabelCount(12);

        chart.setDrawBorders(false);

        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.animateXY(1000, 1000);

        // specify the width each bar should have
        //chart.getBarData().setBarWidth(barWidth);

        // restrict the x-axis range
        //chart.getXAxis().setAxisMinimum(startYear-1);

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        //chart.getXAxis().setAxisMaximum(startYear + chart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);
        //chart.groupBars(startYear-0.5f, groupSpace, barSpace);
        chart.invalidate();
    }

    private void setUpSpinnerCurrency(View root){
        //get the spinner from the xml.
        spinnerCurrency = root.findViewById(R.id.spinner_currency);

        //create a list of items for the spinner.
        currencyList = new ArrayList<String>(receiptMap.keySet());


        currencyAdapter = new ArrayAdapter<String>(getContext(), R.layout.list_item, currencyList);
        //set the spinners adapter to the previously created one.
        spinnerCurrency.setAdapter(currencyAdapter);

        //spinnerCurrency.setSelection(currencyAdapter.getPosition(selectedCurrency));
        //spinnerCurrency.setListSelection(currencyAdapter.getPosition(selectedCurrency));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            spinnerCurrency.setText(selectedCurrency, false);
        }

        spinnerCurrency.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectedCurrency = currencyList.get(position).toString();

                yearList.clear();
                yearList.addAll(new ArrayList<Integer>(receiptCurrencyToYearMap.get(selectedCurrency)));
                currencyAdapter.notifyDataSetChanged();

                Log.i("new year", selectedCurrency +" "+yearList.toString());
                yearAdapter.notifyDataSetChanged();
                updateBarChart();
            }
        });
    }

    private void setUpSpinnerYear(View root) {
        //get the spinner from the xml.
        spinnerYear = root.findViewById(R.id.spinner_year);

        //create a list of items for the spinner.
        yearList = new ArrayList<Integer>(receiptMap.get(selectedCurrency).keySet());


        yearAdapter = new ArrayAdapter<Integer>(getContext(), R.layout.list_item, yearList);
        //set the spinners adapter to the previously created one.
        spinnerYear.setAdapter(yearAdapter);


        //spinnerYear.setSelection(yearAdapter.getPosition(selectedYear));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            spinnerYear.setText(selectedYear.toString(), false);
        }

        spinnerYear.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectedYear = yearList.get(position);

                currencyList.clear();
                currencyList.addAll(new ArrayList<String>(receiptYearToCurrencyMap.get(selectedYear)));
                currencyAdapter.notifyDataSetChanged();
                Log.i("new currency", selectedYear +" "+currencyList.toString());
                updateBarChart();
            }

        });
    }

    private void loadData() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("com.example.receipttracker", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.clear().apply();

        Gson gson = new Gson();

        String json = sharedPreferences.getString("receipts", null);

        Type type = new TypeToken<ArrayList<Receipt>>() {}.getType();

        receiptArrayList = gson.fromJson(json, type);


        if (receiptArrayList == null){
            receiptArrayList = new ArrayList<Receipt>();
        }


        Calendar cal = Calendar.getInstance();

        for (Receipt receipt: receiptArrayList) {
            cal.setTime(receipt.getDate());
            Integer receiptYear = cal.get(Calendar.YEAR);
            Integer receiptMonth = cal.get(Calendar.MONTH);
            String currency = receipt.getCurrency();

            if (receiptMap.containsKey(currency) == false){
                receiptMap.put(currency, new HashMap<Integer, List<Float>>());
            }

            if (receiptMap.get(currency).containsKey(receiptYear) == false){
                receiptMap.get(currency).put(receiptYear, Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            }

            if (receiptCurrencyToYearMap.containsKey(currency) == false){
                receiptCurrencyToYearMap.put(currency, new HashSet<Integer>());
            }
            receiptCurrencyToYearMap.get(currency).add(receiptYear);

            if (receiptYearToCurrencyMap.containsKey(receiptYear) == false) {
                receiptYearToCurrencyMap.put(receiptYear, new HashSet<String>());
            }
            receiptYearToCurrencyMap.get(receiptYear).add(currency);

            Float currentTotal = receiptMap.get(currency).get(receiptYear).get(receiptMonth);
            receiptMap.get(currency).get(receiptYear).set(receiptMonth, currentTotal + receipt.getAmount());

            Float currentTotalReceipt = receiptMap.get(currency).get(receiptYear).get(receiptMonth+12);
            receiptMap.get(currency).get(receiptYear).set(receiptMonth + 12, currentTotalReceipt + 1);
        }


        if (selectedCurrency == null) {
            ArrayList<String> currencyList = new ArrayList<String>(receiptMap.keySet());
            if (currencyList.size() > 0) {
                selectedCurrency = currencyList.get(0);
            }
        }
    }

    private ArrayList getDataSet() {
        ArrayList dataSets = new ArrayList();
        ArrayList valueSet = new ArrayList();

        Float totalAmount = 0f;
        Float totalReceipts = 0f;
        for (Integer i = 0; i < 12; i ++){
            if (selectedCurrency == null){
                valueSet.add(new BarEntry(i, 0f));
            } else {
                valueSet.add(new BarEntry(i, receiptMap.get(selectedCurrency).get(selectedYear).get(i)));
                totalAmount += receiptMap.get(selectedCurrency).get(selectedYear).get(i);
                totalReceipts += receiptMap.get(selectedCurrency).get(selectedYear).get(i+12);
            }
        }

        totalAmountTextView.setText(selectedCurrency + " " + totalAmount);
        totalReceiptsTextView.setText(totalReceipts.toString());

        BarDataSet barDataSet = new BarDataSet(valueSet, selectedCurrency);
        barDataSet.setColor(Color.rgb(98, 0, 238));
        dataSets.add(barDataSet);
        return dataSets;
    }

    private ArrayList getXAxisValues() {
        ArrayList xAxis = new ArrayList();
        xAxis.add("Jan");
        xAxis.add("Feb");
        xAxis.add("Mar");
        xAxis.add("Apr");
        xAxis.add("May");
        xAxis.add("Jun");
        xAxis.add("Jul");
        xAxis.add("Aug");
        xAxis.add("Sep");
        xAxis.add("Oct");
        xAxis.add("Nov");
        xAxis.add("Dec");
        return xAxis;
    }
    /*
    private ArrayList getDataSet() {
        ArrayList dataSets = null;
        List<Float> data = receiptMap.get()
        ArrayList valueSet1 = new ArrayList();
        BarEntry v1e1 = new BarEntry(0, 110.000f); // Jan

        valueSet1.add(v1e1);
        BarEntry v1e2 = new BarEntry(1, 40.000f); // Feb
        valueSet1.add(v1e2);
        BarEntry v1e3 = new BarEntry(2, 60.000f); // Mar
        valueSet1.add(v1e3);
        BarEntry v1e4 = new BarEntry(3, 30.000f); // Apr
        valueSet1.add(v1e4);
        BarEntry v1e5 = new BarEntry(4, 90.000f); // May
        valueSet1.add(v1e5);
        BarEntry v1e6 = new BarEntry(5,100.000f); // Jun
        valueSet1.add(v1e6);

        ArrayList valueSet2 = new ArrayList();
        BarEntry v2e1 = new BarEntry(0,150.000f); // Jan
        valueSet2.add(v2e1);
        BarEntry v2e2 = new BarEntry(1, 90.000f); // Feb
        valueSet2.add(v2e2);
        BarEntry v2e3 = new BarEntry(2, 120.000f); // Mar
        valueSet2.add(v2e3);
        BarEntry v2e4 = new BarEntry(3,60.000f); // Apr
        valueSet2.add(v2e4);
        BarEntry v2e5 = new BarEntry(4,20.000f); // May
        valueSet2.add(v2e5);
        BarEntry v2e6 = new BarEntry(5,80.000f); // Jun
        valueSet2.add(v2e6);

        BarDataSet barDataSet1 = new BarDataSet(valueSet1, "Brand 1");
        barDataSet1.setColor(Color.rgb(0, 155, 0));
        BarDataSet barDataSet2 = new BarDataSet(valueSet2, "Brand 2");
        barDataSet2.setColor(Color.rgb(0, 0, 155));

        dataSets = new ArrayList();
        dataSets.add(barDataSet1);
        dataSets.add(barDataSet2);
        return dataSets;
    }

    private ArrayList getXAxisValues() {
        ArrayList xAxis = new ArrayList();
        xAxis.add("JAN");
        xAxis.add("FEB");
        xAxis.add("MAR");
        xAxis.add("APR");
        xAxis.add("MAY");
        xAxis.add("JUN");
        return xAxis;
    }*/
}