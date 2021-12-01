package com.ayush.manageartworks;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import com.pdftron.pdf.config.ViewerConfig;
import com.pdftron.pdf.controls.DocumentActivity;

public class MainActivity extends AppCompatActivity {

    private static final int LOAD_REQUEST_CODE = 1;
    private static final String TAG = "Main Activity Log";
    ViewerConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        config = new ViewerConfig.Builder().openUrlCachePath(this.getCacheDir().getAbsolutePath()).build();
    }

    public void loadPdfFromStorage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, LOAD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==LOAD_REQUEST_CODE){
            Uri uri = data.getData();
            Log.d(TAG,"Pdf selected"+uri);
            DocumentActivity.openDocument(this, uri, config);
        }
    }

    public void loadDemoPdf(View view) {
        final Uri fileLink = Uri.parse("https://pdftron.s3.amazonaws.com/downloads/pl/PDFTRON_mobile_about.pdf");
        DocumentActivity.openDocument(this, fileLink, config);
    }
}