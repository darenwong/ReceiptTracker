package com.example.receipttracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.navigation.ui.AppBarConfiguration;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.receipttracker.ui.dashboard.DashboardFragment;
import com.example.receipttracker.ui.home.HomeFragment;
import com.example.receipttracker.ui.notifications.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements ReceiptInfoDialog.FragmentReceiptListener, FilterDateDialog.FragmentFilterDateListener {

    private FloatingActionButton cameraBtn;
    private ImageView exportToCSV;
    private ImageView deleteBtn;

    private TextView filterDate;
    private Date filterStartDate;
    private Date filterEndDate;

    private CheckBox selectAllCheckbox;

    private ArrayList<Receipt> receiptArrayList;
    private ArrayList<Receipt> receiptAdapterArrayList = new ArrayList<Receipt>();

    private ReceiptListAdapter adapter;
    private ListView recordListView;

    private ReceiptInfoDialog receiptInfoDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.receipttracker", MODE_PRIVATE);

        selectAllCheckbox = findViewById(R.id.selectAllCheckbox);
        selectAllCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    for (Receipt receipt: receiptArrayList){
                        receipt.setChecked(true);
                    }
                } else {
                    for (Receipt receipt: receiptArrayList){
                        receipt.setChecked(false);
                    }
                }
                adapter.notifyDataSetChanged();

            }
        });

        filterEndDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);
        filterStartDate = cal.getTime();


        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        //NavigationUI.setupWithNavController(navView, navController);

        navView.setBackground(null);

        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            switch(id){
                case R.id.navigation_dashboard:
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new DashboardFragment()).commit();
                    break;
                case R.id.navigation_home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();
                    break;
                case R.id.navigation_notifications:
                    break;
            }
            return true;
        });

        //SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.clear().apply();


        filterDate = findViewById(R.id.filterDate);

        SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MMM-yyyy");
        filterDate.setText(fmtOut.format(filterStartDate) + " to " + fmtOut.format(filterEndDate));
        filterDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FilterDateDialog filterDateDialog = new FilterDateDialog(filterStartDate, filterEndDate);

                //filterDateDialog.show(getSupportFragmentManager(), "filterDateDialog");

                Pair<Long, Long> selectionDates = new Pair<Long, Long>(filterStartDate.getTime(),
                        filterEndDate.getTime());

                MaterialDatePicker dateRangePicker =
                    MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select dates")
                        .setSelection(selectionDates)
                        .build();

                dateRangePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair>() {
                    @Override
                    public void onPositiveButtonClick(Pair selection) {
                        Log.i("selected dates", selection.toString());
                        onInputFilterDateSent(new Date((Long) selection.first), new Date((Long) selection.second));
                    }
                });

                dateRangePicker.show(getSupportFragmentManager(), "filterDateDialog");
            }
        });


        cameraBtn = findViewById(R.id.cameraBtn);


        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("ask camera permission", "asked");
                //askCameraPermissions();


                Receipt newReceipt = new Receipt(0.0f, getPreferences(Context.MODE_PRIVATE).getString("saved_currency", "MYR"), new Date(), "", "Other", "", "", false, false );

                receiptInfoDialog = new ReceiptInfoDialog(newReceipt, adapter, true);
                receiptInfoDialog.show(getSupportFragmentManager(), "receiptInfoDialog");
            }
        });


        loadData();

        recordListView = findViewById(R.id.recordListView);
        recordListView.setOnItemClickListener(new  android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {

                Log.i("selectedReceipt", "receipt clicked");
                Receipt selectedReceipt = (Receipt) parent.getItemAtPosition(position);

                if (selectedReceipt.getSummary() == true){
                    return ;
                }

                receiptInfoDialog = new ReceiptInfoDialog(selectedReceipt, adapter, false);


                receiptInfoDialog.show(getSupportFragmentManager(), "receiptInfoDialog");
            }
        });

        adapter = new ReceiptListAdapter(this, R.layout.custom_list_view, receiptAdapterArrayList);

        recordListView.setAdapter(adapter);
        uncheckAllReceipts();


    }

    public void onExportButtonClick(View v) {

        if (checkIsEmpty() == true){
            Toast.makeText(getApplicationContext(),"No items selected to export",Toast.LENGTH_SHORT).show();
            return ;
        }

        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(getApplicationContext(), v.findViewById(R.id.exportToCSV));
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(getApplicationContext(),"You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                exportToCSV();
                return true;
            }
        });

        popup.show();//showing popup menu
    }

    public void onDeleteButtonClick(View v){
        if (checkIsEmpty() == true){
            Toast.makeText(getApplicationContext(),"No item selected to delete",Toast.LENGTH_SHORT).show();
            return ;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);


        //Setting message manually and performing action on button click
        builder.setMessage("Are you sure you want to delete these receipts?")
            .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ArrayList<Receipt> newReceiptArrayList = new ArrayList<Receipt>();
                        for (Receipt receipt: receiptArrayList){
                            if (receipt.getChecked() == false){
                                newReceiptArrayList.add(receipt);
                            }
                        }

                        Toast.makeText(getApplicationContext(),"Deleted " + (newReceiptArrayList.size() - receiptArrayList.size()) + " receipts", Toast.LENGTH_SHORT).show();

                        receiptArrayList = newReceiptArrayList;
                        refreshReceiptAdapterArrayList();
                        adapter.notifyDataSetChanged();

                        saveData();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Delete Receipts");
        alert.show();

    }

    public Boolean checkIsEmpty(){
        Boolean checkedItemIsEmpty = true;
        for (Receipt receipt: receiptArrayList){
            if (receipt.getChecked() == true){
                checkedItemIsEmpty = false;
                break;
            }
        }
        return checkedItemIsEmpty;
    }

    public void exportToCSV(){
        String columnString =   "\"No\",\"Date\",\"Title\",\"Category\",\"Currency\",\"Amount\",\"Note\"";
        String combinedString = columnString;

        for (Integer i = 0; i < receiptArrayList.size(); i++){
            Receipt receipt = receiptArrayList.get(i);
            if (receipt.getChecked() == true){
                String dataString   =   "\"" + Integer.toString(i+1) +"\",\"" + receipt.getDate().toString() +"\",\"" + receipt.getTitle() + "\",\"" + receipt.getCategory() + "\",\"" + receipt.getCurrency() + "\",\"" + receipt.getAmount().toString() + "\", \"" + receipt.getNote() + "\"";

                combinedString += "\n" + dataString;
            }
        }


        File file   = null;
        File root   = Environment.getExternalStorageDirectory();
        if (root.canWrite()){
            File dir    =   new File (root.getAbsolutePath() + "/PersonData");
            dir.mkdirs();
            file   =   new File(dir, "Data.csv");
            FileOutputStream out   =   null;
            try {
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out.write(combinedString.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Uri u1  =   FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
        //u1  =   Uri.fromFile(file);

        Intent sendIntent = new Intent(Intent.ACTION_SEND);

        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Person Details");
        sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
        sendIntent.setType("text/csv");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share File");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(shareIntent);
    }

    @Override
    public void onInputReceiptSent(Receipt receipt, Boolean isCreateNew) {
        Log.i("received data", isCreateNew.toString());
        if (isCreateNew == true){
            receiptArrayList.add(receipt);
        }
        refreshReceiptAdapterArrayList();
        adapter.notifyDataSetChanged();
        saveData();
    }

    @Override
    public void onInputFilterDateSent(Date startDate, Date endDate) {
        Log.i("received date", startDate.toString());

        filterStartDate = startDate;
        filterEndDate = endDate;

        SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MMM-yyyy");
        filterDate.setText(fmtOut.format(filterStartDate) + " to " + fmtOut.format(filterEndDate));

        refreshReceiptAdapterArrayList();
        adapter.notifyDataSetChanged();
    }

    private void uncheckAllReceipts(){
        for (Receipt receipt: receiptArrayList){
            receipt.setChecked(false);
        }
        adapter.notifyDataSetChanged();
    }

    private void refreshReceiptAdapterArrayList(){
        receiptAdapterArrayList.clear();

        if (receiptArrayList.size() > 0){
            Collections.sort(receiptArrayList, Collections.reverseOrder());

            for (Integer i = 0; i<receiptArrayList.size(); i++){
                Date receiptDate = receiptArrayList.get(i).getDate();
                if (receiptDate.before(filterStartDate) == true || receiptDate.after(filterEndDate) == true){
                    continue;
                }

                String before = "";
                String after = new SimpleDateFormat("dd-MM-yyyy").format(receiptArrayList.get(i).getDate());

                if (i > 0){
                    before = new SimpleDateFormat("dd-MM-yyyy").format(receiptArrayList.get(i-1).getDate());
                }
                Log.i(String.valueOf(i), before + " vs " + after);
                if (i == 0 || !before.equals(after)){
                    receiptAdapterArrayList.add(new Receipt(null, null, receiptArrayList.get(i).getDate(), null, null, null, null, true, false));
                }
                receiptAdapterArrayList.add(receiptArrayList.get(i));
            }
        }
    }

    private void loadData() {

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.receipttracker", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.clear().apply();

        Gson gson = new Gson();

        String json = sharedPreferences.getString("receipts", null);

        Type type = new TypeToken<ArrayList<Receipt>>() {}.getType();

        receiptArrayList = gson.fromJson(json, type);


        if (receiptArrayList == null){
            receiptArrayList = new ArrayList<Receipt>();
        }
        refreshReceiptAdapterArrayList();
        //receiptArrayList.add(new Receipt(null, null, null, null, null, null, null, true));
    }

    private void saveData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.receipttracker", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(receiptArrayList);
        editor.putString("receipts", json);
        editor.apply();

        Toast.makeText(this, "Saved array", Toast.LENGTH_SHORT).show();
    }




    public void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 101);
        } else {
            Log.i("existing Permission", String.valueOf(Manifest.permission.CAMERA));
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.i("grantResult", String.valueOf(grantResults));ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 101);
                openCamera();
            } else {
                Toast.makeText(this, "Camera Permission is Required to use camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        //Toast.makeText(this, "Camera Open Request", Toast.LENGTH_SHORT).show();
        dispatchTakePictureIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 102){
            if (resultCode == Activity.RESULT_OK){
                File f = new File(currentPhotoPath);

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                Receipt newReceipt = new Receipt(0.0f, getPreferences(Context.MODE_PRIVATE).getString("saved_currency", "MYR"), new Date(), "", "Other", "", contentUri.toString(), false, false );

                receiptInfoDialog.updateReceiptPhoto(contentUri.toString());
                //receiptInfoDialog = new ReceiptInfoDialog(newReceipt, adapter, true);
                //receiptInfoDialog.show(getSupportFragmentManager(), "receiptInfoDialog");
            }
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,   //prefix
                ".jpg",          //suffix
                storageDir       //directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("error", "error");
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.i("camera status", "opening");
                startActivityForResult(takePictureIntent, 102);
            } else {
                Log.i("error", "photoFile is null");
            }
        } else {
            Log.i("error", "no camera activity");
        }
    }


}