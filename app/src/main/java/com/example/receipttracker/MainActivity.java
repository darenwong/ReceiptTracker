

package com.example.receipttracker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.receipttracker.ui.dashboard.DashboardFragment;
import com.example.receipttracker.ui.home.HomeFragment;
import com.example.receipttracker.ui.notifications.NotificationsFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.nl.entityextraction.DateTimeEntity;
import com.google.mlkit.nl.entityextraction.Entity;
import com.google.mlkit.nl.entityextraction.EntityAnnotation;
import com.google.mlkit.nl.entityextraction.EntityExtraction;
import com.google.mlkit.nl.entityextraction.EntityExtractionParams;
import com.google.mlkit.nl.entityextraction.EntityExtractor;
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions;
import com.google.mlkit.nl.entityextraction.FlightNumberEntity;
import com.google.mlkit.nl.entityextraction.MoneyEntity;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;



public class MainActivity extends AppCompatActivity implements ReceiptInfoDialog.FragmentReceiptListener, FilterDateDialog.FragmentFilterDateListener {

    private FloatingActionButton cameraBtn;
    private ImageView exportToCSV;
    private ImageView deleteBtn;

    private TextView filterDate;
    private Date filterStartDate;
    private Date filterEndDate;

    private CheckBox selectAllCheckbox;

    private ArrayList<Receipt> receiptArrayList = new ArrayList<Receipt>();
    private ArrayList<Receipt> receiptAdapterArrayList = new ArrayList<Receipt>();

    private ReceiptListAdapter adapter;
    private ListView recordListView;

    private ReceiptInfoDialog receiptInfoDialog;

    private TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    private InputImage receiptImage;

    private String receiptTitle = "";
    private Float receiptTotal = 0.0f;
    private Date receiptDateOCR = new Date();

    private ActivityResultLauncher<Intent> takePictureResultLauncher;

    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
*/

        db = new DBHelper(this);
/*
        Receipt newReceipt = new Receipt(0.0f, getPreferences(Context.MODE_PRIVATE).getString("saved_currency", "MYR"), new Date(), "", "Other", "", "", false, false );

        db.insertReceipt(newReceipt);
        Log.i("receipt inserted", "ok");
        Cursor cursor = db.getReceipt();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Log.i("cursor val", cursor.getString(0));
                cursor.moveToNext();
            }
        }
*/
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.receipttracker", MODE_PRIVATE);

        selectAllCheckbox = findViewById(R.id.selectAllCheckbox);
        selectAllCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    for (Receipt receipt: receiptAdapterArrayList){
                        if (receipt.getSummary() == true) {
                            continue;
                        }
                        receipt.setChecked(true);
                    }
                } else {
                    for (Receipt receipt: receiptAdapterArrayList){
                        if (receipt.getSummary() == true) {
                            continue;
                        }
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
                        //Log.i("selected dates", selection.toString());
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
                //Log.i("ask camera permission", "asked");
                //askCameraPermissions();


                Receipt newReceipt = new Receipt(-1,0.0f, getPreferences(Context.MODE_PRIVATE).getString("saved_currency", "MYR"), new Date(), "", "Other", "", "", false, false );

                receiptInfoDialog = new ReceiptInfoDialog(newReceipt, adapter, true);
                receiptInfoDialog.show(getSupportFragmentManager(), "receiptInfoDialog");
            }
        });

        refreshReceiptAdapterArrayList();

        recordListView = findViewById(R.id.recordListView);
        recordListView.setOnItemClickListener(new  android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {

                //Log.i("selectedReceipt", "receipt clicked");
                Receipt selectedReceipt = (Receipt) parent.getItemAtPosition(position);

                if (selectedReceipt.getSummary()){
                    return ;
                }

                receiptInfoDialog = new ReceiptInfoDialog(selectedReceipt, adapter, false);


                receiptInfoDialog.show(getSupportFragmentManager(), "receiptInfoDialog");
            }
        });

        adapter = new ReceiptListAdapter(this, R.layout.custom_list_view, receiptAdapterArrayList);

        recordListView.setAdapter(adapter);
        uncheckAllReceipts();

        takePictureResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            File f = new File(currentPhotoPath);

                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(f);
                            mediaScanIntent.setData(contentUri);
                            sendBroadcast(mediaScanIntent);

                            try {
                                receiptImage = InputImage.fromFilePath(getApplicationContext(), contentUri);
                                //Uri testUri = Uri.parse("android.resource://com.example.receipttracker/" + R.drawable._096_receipt);

                                //receiptImage = InputImage.fromFilePath(getApplicationContext(), testUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            Task<Text> mlResult =
                                    recognizer.process(receiptImage)
                                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                                @Override
                                                public void onSuccess(Text visionText) {
                                                    //Log.i("process image status: ", "success");
                                                    String resultText = visionText.getText();
                                                    //Log.i("visionText: ", resultText);
                                                    Pattern pattern = Pattern.compile("([0-9]+\\.[0-9]+)");
                                                    ArrayList<Float> listOfHeadings = new ArrayList<>();

                                                    Matcher match = pattern.matcher(resultText);
                                                    while (match.find()) {
                                                        listOfHeadings.add(Float.parseFloat(match.group()));
                                                    }

                                                    if (!visionText.getTextBlocks().isEmpty() && !visionText.getTextBlocks().get(0).getLines().isEmpty()){
                                                        receiptTitle = visionText.getTextBlocks().get(0).getLines().get(0).getText();
                                                    }
                                                    if (!listOfHeadings.isEmpty()){
                                                        receiptTotal = Collections.max(listOfHeadings);
                                                    }

                                                    EntityExtractor entityExtractor =
                                                            EntityExtraction.getClient(
                                                                    new EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
                                                                            .build());
                                                    entityExtractor
                                                            .downloadModelIfNeeded()
                                                            .addOnSuccessListener(
                                                                    aVoid -> {
                                                                        // Model downloading succeeded, you can call the extraction API here.

                                                                        EntityExtractionParams params = new EntityExtractionParams
                                                                                .Builder(visionText.getText())
                                                                                .build();
                                                                        entityExtractor
                                                                                .annotate(params)
                                                                                .addOnSuccessListener(new OnSuccessListener<List<EntityAnnotation>>() {
                                                                                    @Override
                                                                                    public void onSuccess(List<EntityAnnotation> entityAnnotations) {
                                                                                        // Annotation process was successful, you can parse the EntityAnnotations list here.
                                                                                        String TAG = "Entity found";
                                                                                        ArrayList<Float> listOfAmount = new ArrayList<>();

                                                                                        for (EntityAnnotation entityAnnotation : entityAnnotations) {
                                                                                            List<Entity> entities = entityAnnotation.getEntities();
                                                                                            String annotatedText = entityAnnotation.getAnnotatedText();

                                                                                            //Log.d(TAG, String.format("Range: [%d, %d)", entityAnnotation.getStart(), entityAnnotation.getEnd()));
                                                                                            for (Entity entity : entities) {
                                                                                                switch (entity.getType()) {
                                                                                                    case Entity.TYPE_DATE_TIME:
                                                                                                        DateTimeEntity dateTimeEntity = entity.asDateTimeEntity();
                                                                                                        Log.i(TAG, "Granularity: " + dateTimeEntity.getDateTimeGranularity());
                                                                                                        Log.i(TAG, "Timestamp: " + dateTimeEntity.getTimestampMillis());
                                                                                                        receiptDateOCR = new Date(dateTimeEntity.getTimestampMillis());
                                                                                                        Log.i(TAG, "DateTime: " + receiptDateOCR.toString());
                                                                                                        break;
                                                                                                    case Entity.TYPE_ADDRESS:
                                                                                                        Log.i(TAG, "Address: " + annotatedText);
                                                                                                        //receiptTitle = annotatedText;
                                                                                                        break;
                                                                                                    case Entity.TYPE_MONEY:
                                                                                                        MoneyEntity moneyEntity = entity.asMoneyEntity();
                                                                                                        Log.i(TAG, "Currency: " + moneyEntity.getUnnormalizedCurrency());
                                                                                                        Log.i(TAG, "Integer Part: " + moneyEntity.getIntegerPart());
                                                                                                        Log.i(TAG, "Fractional Part: " + moneyEntity.getFractionalPart());

                                                                                                        //listOfAmount.add((float) moneyEntity.getIntegerPart() + ((float) moneyEntity.getFractionalPart())/100);

                                                                                                        break;
                                                                                                    default:
                                                                                                        Log.i(TAG, "Entity: " + entity.getType());
                                                                                                        break;
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        //if (!listOfAmount.isEmpty()){
                                                                                        //    receiptTotal = Collections.max(listOfAmount);
                                                                                        //}
                                                                                        receiptInfoDialog.updateReceiptTitle(receiptTitle);
                                                                                        receiptInfoDialog.updateReceiptTotal(receiptTotal);
                                                                                        receiptInfoDialog.updateReceiptDate(receiptDateOCR);
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        // Check failure message here.
                                                                                    }
                                                                                });

                                                                    })
                                                            .addOnFailureListener(
                                                                    exception -> {
                                                                        // Model downloading failed.
                                                                    });

                                                    //Log.i("regex result", listOfHeadings.toString());
                                                    //Log.i("title", receiptTitle);
                                                    //Log.i("total: ", receiptTotal.toString());

                                                    receiptInfoDialog.updateReceiptTitle(receiptTitle);
                                                    receiptInfoDialog.updateReceiptTotal(receiptTotal);
                                                    receiptInfoDialog.updateReceiptDate(receiptDateOCR);
/*
                                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                                            String blockText = block.getText();
                                            Point[] blockCornerPoints = block.getCornerPoints();
                                            Rect blockFrame = block.getBoundingBox();
                                            for (Text.Line line : block.getLines()) {
                                                String lineText = line.getText();
                                                Point[] lineCornerPoints = line.getCornerPoints();
                                                Rect lineFrame = line.getBoundingBox();
                                                for (Text.Element element : line.getElements()) {
                                                    String elementText = element.getText();
                                                    Point[] elementCornerPoints = element.getCornerPoints();
                                                    Rect elementFrame = element.getBoundingBox();
                                                }
                                            }
                                        }
*/                                                    ;                                    }
                                            })
                                            .addOnFailureListener(
                                                    new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    });



                            //Receipt newReceipt = new Receipt(receiptTotal, getPreferences(Context.MODE_PRIVATE).getString("saved_currency", "MYR"), new Date(), receiptTitle, "Other", "", contentUri.toString(), false, false );

                            receiptInfoDialog.updateReceiptPhoto(contentUri.toString());
                            //receiptInfoDialog = new ReceiptInfoDialog(newReceipt, adapter, true);
                            //receiptInfoDialog.show(getSupportFragmentManager(), "receiptInfoDialog");
                        }
                    }
                });

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
                //Toast.makeText(getApplicationContext(),"You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();

                switch (item.getTitle().toString()){
                    case "CSV":
                        exportToCSV();
                        break;
                    case "PDF":
                        exportToPDF();
                        break;
                    case "ZIP":
                        try {
                            exportToZIP();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }

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
                        Integer countDeleted = 0;

                        for (Receipt receipt: receiptArrayList){
                            if (receipt.getChecked() == true){
                                db.deleteReceipt(receipt);
                                countDeleted += 1;
                            }
                        }

                        String message = "Deleted " + countDeleted;
                        if (countDeleted > 1){
                             message += " receipts";
                        } else {
                            message += " receipt";
                        }
                        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();

                        refreshReceiptAdapterArrayList();
                        adapter.notifyDataSetChanged();
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

    @Override
    public void onInputReceiptSent(Receipt receipt, Boolean isCreateNew) {
        //Log.i("received data", isCreateNew.toString());
        if (isCreateNew == true){
            //receiptArrayList.add(receipt);
            db.insertReceipt(receipt);
        } else {
            db.updateReceipt(receipt);
        }
        refreshReceiptAdapterArrayList();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onInputFilterDateSent(Date startDate, Date endDate) {
        //Log.i("received date", startDate.toString());

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
        receiptArrayList.clear();
        loadData();

        if (receiptArrayList.size() > 0){
            Collections.sort(receiptArrayList, Collections.reverseOrder());

            Calendar cal = Calendar.getInstance();
            cal.setTime(filterStartDate);
            cal.add(Calendar.DATE, -1);
            Date oneDayBeforeStart = cal.getTime();

            cal.setTime(filterEndDate);
            cal.add(Calendar.DATE, 1);
            Date oneDayAfterEnd = cal.getTime();

            for (Integer i = 0; i<receiptArrayList.size(); i++){
                Date receiptDate = receiptArrayList.get(i).getDate();
                //Log.i("receipt details", receiptDate.toString());
                if ((receiptDate.before(oneDayAfterEnd) == true && receiptDate.after(oneDayBeforeStart) == true) == false){
                    continue;
                }

                String before = "";
                String after = new SimpleDateFormat("dd-MM-yyyy").format(receiptArrayList.get(i).getDate());

                if (i > 0){
                    before = new SimpleDateFormat("dd-MM-yyyy").format(receiptArrayList.get(i-1).getDate());
                }
                //Log.i(String.valueOf(i), before + " vs " + after);
                if (i == 0 || !before.equals(after)){
                    receiptAdapterArrayList.add(new Receipt(-1,null, null, receiptArrayList.get(i).getDate(), null, null, null, null, true, false));
                }
                receiptAdapterArrayList.add(receiptArrayList.get(i));
            }
        }
    }
/*
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
    }*/


    private void loadData() {
        //db.dropTable();
        Cursor cursor = db.getReceipt();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Log.i("all column", Arrays.toString(cursor.getColumnNames()));

                Integer id_col = cursor.getColumnIndex("receipt_id");
                Integer id = Integer.valueOf(cursor.getString(id_col));
                Integer title_col = cursor.getColumnIndex("title");
                String title = cursor.getString(title_col);

                Integer amount_col = cursor.getColumnIndex("amount");
                Float amount = Float.parseFloat(cursor.getString(amount_col));
                Integer cur_col = cursor.getColumnIndex("currency");
                String currency = cursor.getString(cur_col);
                Integer date_col = cursor.getColumnIndex("date");
                Date date = new Date(cursor.getString(date_col));
                Integer cat_col = cursor.getColumnIndex("category");
                String category = cursor.getString(cat_col);
                Integer note_col = cursor.getColumnIndex("note");
                String note = cursor.getString(note_col);
                Integer img_col = cursor.getColumnIndex("image");
                String image = cursor.getString(img_col);
                Integer isSum_col = cursor.getColumnIndex("isSummary");
                Boolean isSum = Boolean.parseBoolean(cursor.getString(isSum_col));
                Integer isChecked_col = cursor.getColumnIndex("isChecked");
                Boolean isChecked = Boolean.parseBoolean(cursor.getString(isChecked_col));


                Log.i("cursor val", id+title + currency + date + category + note + image + isSum + isChecked);
                Receipt newReceipt = new Receipt(id,amount, currency, date, title, category, note, image, isSum, isChecked );

                receiptArrayList.add(newReceipt);
                cursor.moveToNext();
            }
        }
    }
/*
    private void saveData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.receipttracker", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(receiptArrayList);
        editor.putString("receipts", json);
        editor.apply();

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }
*/

    public void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 101);
        } else {
            //Log.i("existing Permission", String.valueOf(Manifest.permission.CAMERA));
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Log.i("grantResult", String.valueOf(grantResults));ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 101);
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
/*
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

                try {
                    receiptImage = InputImage.fromFilePath(this, contentUri);
                    //Uri testUri = Uri.parse("android.resource://com.example.receipttracker/" + R.drawable._096_receipt);

                    //receiptImage = InputImage.fromFilePath(this, testUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Task<Text> result =
                        recognizer.process(receiptImage)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(Text visionText) {
                                        //Log.i("process image status: ", "success");
                                        String resultText = visionText.getText();
                                        //Log.i("visionText: ", resultText);
                                        Pattern pattern = Pattern.compile("([0-9]+\\.[0-9]+)");
                                        ArrayList<Float> listOfHeadings = new ArrayList<>();

                                        Matcher match = pattern.matcher(resultText);
                                        while (match.find()) {
                                            listOfHeadings.add(Float.parseFloat(match.group()));
                                        }

                                        if (!visionText.getTextBlocks().isEmpty() && !visionText.getTextBlocks().get(0).getLines().isEmpty()){
                                            receiptTitle = visionText.getTextBlocks().get(0).getLines().get(0).getText();
                                        }
                                        if (!listOfHeadings.isEmpty()){
                                            receiptTotal = Collections.max(listOfHeadings);
                                        }

                                        //Log.i("regex result", listOfHeadings.toString());
                                        //Log.i("title", receiptTitle);
                                        //Log.i("total: ", receiptTotal.toString());

                                        receiptInfoDialog.updateReceiptTitle(receiptTitle);
                                        receiptInfoDialog.updateReceiptTotal(receiptTotal);

                                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                                            String blockText = block.getText();
                                            Point[] blockCornerPoints = block.getCornerPoints();
                                            Rect blockFrame = block.getBoundingBox();
                                            for (Text.Line line : block.getLines()) {
                                                String lineText = line.getText();
                                                Point[] lineCornerPoints = line.getCornerPoints();
                                                Rect lineFrame = line.getBoundingBox();
                                                for (Text.Element element : line.getElements()) {
                                                    String elementText = element.getText();
                                                    Point[] elementCornerPoints = element.getCornerPoints();
                                                    Rect elementFrame = element.getBoundingBox();
                                                }
                                            }
                                        }
                                        ;                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                e.printStackTrace();
                                            }
                                        });



                Receipt newReceipt = new Receipt(receiptTotal, getPreferences(Context.MODE_PRIVATE).getString("saved_currency", "MYR"), new Date(), receiptTitle, "Other", "", contentUri.toString(), false, false );

                receiptInfoDialog.updateReceiptPhoto(contentUri.toString());
                //receiptInfoDialog = new ReceiptInfoDialog(newReceipt, adapter, true);
                //receiptInfoDialog.show(getSupportFragmentManager(), "receiptInfoDialog");
            }
        }
    }*/

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
                //Log.i("error", "error");
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //Log.i("camera status", "opening");
                //startActivityForResult(takePictureIntent, 102);
                takePictureResultLauncher.launch(takePictureIntent);
            } else {
                //Log.i("error", "photoFile is null");
            }
        } else {
            //Log.i("error", "no camera activity");
        }
    }

    public ArrayList<Receipt> getCheckedReceipts(){
        ArrayList<Receipt> checkedReceipts = new ArrayList<>();

        for (Integer i = 0; i < receiptArrayList.size(); i++){
            Receipt receipt = receiptArrayList.get(i);
            if (receipt.getChecked() == true){
                checkedReceipts.add(receipt);
            }
        }

        return checkedReceipts;
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
        //File root   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File root = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (root.canWrite()){
            File dir    =   new File (root.getAbsolutePath() + "/ReceiptTrackerData");
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

    public void exportToPDF(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.pdf_layout, null);

        ArrayList<Receipt> checkedReceipts = getCheckedReceipts();

        PDFRecyclerAdapter pdfRecyclerAdapter = new PDFRecyclerAdapter(this, checkedReceipts);

        RecyclerView recyclerView = view.findViewById(R.id.pdf_marks);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        recyclerView.setAdapter(pdfRecyclerAdapter);


        DisplayMetrics displayMetrics = new DisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.getDisplay().getRealMetrics(displayMetrics);
        } else {
            this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }

        view.measure(
                View.MeasureSpec.makeMeasureSpec(
                        displayMetrics.widthPixels, View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(
                        displayMetrics.heightPixels, View.MeasureSpec.EXACTLY
                )
        );

        view.layout(0,0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        //Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 297, 421, true);
        Bitmap newBitmap = getResizedBitmap(bitmap, 595, 842);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();

            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            page.getCanvas().drawBitmap(newBitmap, 0f, 0f, null);
            pdfDocument.finishPage(page);

            for (Integer i = 1; i < checkedReceipts.size()+1; i++){
                Receipt receipt = checkedReceipts.get(i-1);

                PdfDocument.Page photoPage = pdfDocument.startPage(pageInfo);
                Bitmap receiptBitmap = null;
                try {
                    photoPage.getCanvas().drawText(Integer.toString(i) + ". " + receipt.getTitle() + " - " + receipt.getCurrency() + " " + receipt.getAmount(), 10, 25, new Paint());

                    receiptBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(receipt.getImage()));
                    Bitmap newBitmap2 = Bitmap.createScaledBitmap(receiptBitmap, 500, 710, true);
                    photoPage.getCanvas().drawBitmap(newBitmap2, 50f, 50f, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pdfDocument.finishPage(photoPage);
            }



            File file   = null;
            //File root   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File root = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            if (root.canWrite()){
                File dir    =   new File (root.getAbsolutePath() + "/ReceiptTrackerData");
                dir.mkdirs();
                file   =   new File(dir, "Data.pdf");
                FileOutputStream out   =   null;
                try {
                    out = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    pdfDocument.writeTo(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pdfDocument.close();
            }


            Uri u1  =   FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
            //u1  =   Uri.fromFile(file);

            Intent sendIntent = new Intent(Intent.ACTION_SEND);

            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Person Details");
            sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
            sendIntent.setType("text/pdf");

            Intent shareIntent = Intent.createChooser(sendIntent, "Share File");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(shareIntent);
        }

    }

    public void exportToZIP() throws IOException {
        File file   = null;
        //File root   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File root = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (root.canWrite()){
            File dir    =   new File (root.getAbsolutePath() + "/ReceiptTrackerData");
            dir.mkdirs();
            file   =   new File(dir, "Data.zip");
            ZipOutputStream out = null;

            try {
                out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList<Receipt> checkedReceipts = getCheckedReceipts();

            Integer BUFFER_SIZE = 1024;
            byte data[] = new byte[BUFFER_SIZE];

            for (Integer i = 1; i < checkedReceipts.size()+1; i++) {
                Receipt receipt = checkedReceipts.get(i - 1);

                FileInputStream fi = null;
                BufferedInputStream origin = null;
                try {
                    fi = openFileInput(receipt.getImage());
                    //fi = new FileInputStream(getContentResolver().openFileDescriptor(Uri.parse(receipt.getImage()), "r").getFileDescriptor());
                    origin = new BufferedInputStream(fi, BUFFER_SIZE);
                    ZipEntry entry = new ZipEntry(receipt.getImage().substring(receipt.getImage().lastIndexOf("/")+1));

                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    if (origin != null) {
                        origin.close();
                    }
                }
            }
            out.close();

        }

        Uri u1  =   FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
        //u1  =   Uri.fromFile(file);

        Intent sendIntent = new Intent(Intent.ACTION_SEND);

        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Person Details");
        sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
        sendIntent.setType("application/zip");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share File");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(shareIntent);
    }

    public Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }


}