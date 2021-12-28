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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class DashboardFragment extends Fragment {

    private AutoCompleteTextView spinnerCurrency;
    private AutoCompleteTextView spinnerYear;
    private ArrayList<String> currencyList;
    private ArrayList<Integer> yearList;
    private ArrayAdapter<String> currencyAdapter;
    private ArrayAdapter<Integer> yearAdapter;

    private BarChart chart;
    private PieChart pieChart;

    private ArrayList<Receipt> receiptArrayList;

    private ArrayList<String> frequencyChoice = new ArrayList<>(Arrays.asList("Daily", "Monthly", "Yearly"));
    private Integer frequencySelected = 0;

    private Integer selectedYear = Calendar.getInstance().get(Calendar.YEAR);
    private String selectedCurrency;

    private HashMap<String, HashMap<Integer, List<Float>>> receiptMap = new HashMap<String, HashMap<Integer, List<Float>>>();
    private HashMap<String, HashSet<Integer>> receiptCurrencyToYearMap = new HashMap<String, HashSet<Integer>>();
    private HashMap<Integer, HashSet<String>> receiptYearToCurrencyMap = new HashMap<Integer, HashSet<String>>();
    private HashMap<String, HashMap<Integer, HashMap<String, Float>>> receiptCategoryMap = new HashMap<String, HashMap<Integer, HashMap<String, Float>>>();

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

        pieChart = root.findViewById(R.id.pieChart_view);
        showPieChart();
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
                showPieChart();
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
                showPieChart();
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

            if (receiptCategoryMap.containsKey(currency) == false){
                receiptCategoryMap.put(currency, new HashMap<Integer, HashMap<String, Float>>());
            }

            if (receiptCategoryMap.get(currency).containsKey(receiptYear) == false){
                receiptCategoryMap.get(currency).put(receiptYear, new HashMap<String, Float>());

                receiptCategoryMap.get(currency).get(receiptYear).put("Housing", 0f);
                receiptCategoryMap.get(currency).get(receiptYear).put("Transport", 0f);
                receiptCategoryMap.get(currency).get(receiptYear).put("Food", 0f);
                receiptCategoryMap.get(currency).get(receiptYear).put("Utilities", 0f);
                receiptCategoryMap.get(currency).get(receiptYear).put("Healthcare", 0f);
                receiptCategoryMap.get(currency).get(receiptYear).put("Entertainment", 0f);
                receiptCategoryMap.get(currency).get(receiptYear).put("Lifestyle", 0f);
                receiptCategoryMap.get(currency).get(receiptYear).put("Other", 0f);
            }
            Float categoryTotal = receiptCategoryMap.get(currency).get(receiptYear).get(receipt.getCategory());
            receiptCategoryMap.get(currency).get(receiptYear).put(receipt.getCategory(), categoryTotal + receipt.getAmount());
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
        totalReceiptsTextView.setText(String.valueOf(Math.round(totalReceipts)));

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

    private void showPieChart(){

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = "";

        //initializing data
        Map<String, Float> typeAmountMap = receiptCategoryMap.get(selectedCurrency).get(selectedYear);

        //initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#304567"));
        colors.add(Color.parseColor("#309967"));
        colors.add(Color.parseColor("#476567"));
        colors.add(Color.parseColor("#890567"));
        colors.add(Color.parseColor("#a35567"));
        colors.add(Color.parseColor("#ff5f67"));
        colors.add(Color.parseColor("#bb86fc"));
        colors.add(Color.parseColor("#6200ee"));

        //input data and fit data into pie chart entry
        for(String type: typeAmountMap.keySet()){
            pieEntries.add(new PieEntry(typeAmountMap.get(type).floatValue(), type));
        }

        //collecting the entries with label name
        PieDataSet pieDataSet = new PieDataSet(pieEntries,label);
        //setting text size of the value
        pieDataSet.setValueTextSize(12f);
        //providing color list for coloring different entries
        pieDataSet.setColors(colors);
        pieDataSet.setDrawValues(false);
        //pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        //pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        //pieChart.setEntryLabelColor(Color.BLACK);


        //grouping the data set from entry to chart
        PieData pieData = new PieData(pieDataSet);
        //showing the value of the entries, default true if not set


        pieChart.setData(pieData);
        pieChart.animateXY(1000,1000);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);

        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.invalidate();
    }

}