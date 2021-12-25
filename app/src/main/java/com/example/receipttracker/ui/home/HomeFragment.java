package com.example.receipttracker.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.receipttracker.BuildConfig;
import com.example.receipttracker.FilterDateDialog;
import com.example.receipttracker.MainActivity;
import com.example.receipttracker.R;
import com.example.receipttracker.Receipt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private ImageView exportToCSV;
    private ImageView deleteBtn;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        exportToCSV = root.findViewById(R.id.exportToCSV);
        exportToCSV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) requireActivity()).onExportButtonClick(v);
                }
            }
        );


        deleteBtn = root.findViewById(R.id.deleteButton);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
                 public void onClick(View v) {
                     ((MainActivity) requireActivity()).onDeleteButtonClick(v);
                 }
            }
        );

        return root;
    }

}