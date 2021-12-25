package com.example.receipttracker;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ImageActivity extends AppCompatActivity {

    private Button backButton;
    private Button retakeButton;
    private ImageView imageReceiptView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Bundle extras = getIntent().getExtras();

        backButton = findViewById(R.id.backButton);
        retakeButton = findViewById(R.id.retakeButton);
        imageReceiptView = findViewById(R.id.imageReceiptView);

        if (extras != null){
            String image = extras.getString("image");
            imageReceiptView.setImageURI(Uri.parse(image));
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        retakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}