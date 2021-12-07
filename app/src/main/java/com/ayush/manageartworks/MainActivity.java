package com.ayush.manageartworks;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;

import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.pdftron.pdf.config.ViewerConfig;
import com.pdftron.pdf.controls.DocumentActivity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class MainActivity extends AppCompatActivity {

    PyObject pyObject;
    private static final int LOAD_REQUEST_CODE = 1;
    private static final String TAG = "Main Activity Log";
    public static Context context;
    ViewerConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        config = new ViewerConfig.Builder().openUrlCachePath(this.getCacheDir().getAbsolutePath()).build();

        //start python scripts

        runPython(this);
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
            try {
                File pdf = toFile(uri);
                Bitmap selectedPdfBitmap = pdfToBitmap(pdf).get(0);
                //TODO: show the selected pdf as image
                ImageView selectedPdfImage = (ImageView) findViewById(R.id.selectedPdfImageView);
                selectedPdfImage.setImageBitmap(selectedPdfBitmap);
            } catch (IOException e) {
                Toast.makeText(context,"Error converting pdf uri to pdf",Toast.LENGTH_SHORT).show();
                DocumentActivity.openDocument(this, uri, config);
            }
        }
    }

    public void loadDemoPdf(View view) {
        final Uri fileLink = Uri.parse("https://pdftron.s3.amazonaws.com/downloads/pl/PDFTRON_mobile_about.pdf");
        DocumentActivity.openDocument(this, fileLink, config);
    }

    //Resonsible to open camera and show image preview
    public void captureImage(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    //converts pdf to bitmap arraylist
    private  ArrayList<Bitmap> pdfToBitmap(File pdfFile) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();

        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));

            Bitmap bitmap;
            final int pageCount = renderer.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);

                int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();

                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                bitmaps.add(bitmap);

                // close the page
                page.close();

            }
            // close the renderer
            renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmaps;

    }

    //returns file at given uri
    private File toFile(Uri uri) throws IOException {
        String displayName = "";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if(cursor != null && cursor.moveToFirst()){
            try {
                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }finally {
                cursor.close();
            }
        }

        File file =  File.createTempFile(
                FilenameUtils.getBaseName(displayName),
                "."+FilenameUtils.getExtension(displayName)
        );
        InputStream inputStream = getContentResolver().openInputStream(uri);
        FileUtils.copyInputStreamToFile(inputStream, file);
        return file;
    }

    //*****Python*****//
    public void runPython(Context context) {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
        }
        Python py = Python.getInstance();
        pyObject = py.getModule("hello_world");
        String msg = pyObject.callAttr("hello").toString();
        Toast.makeText(context,"This came from pytgon :: "+msg,Toast.LENGTH_SHORT).show();
    }
}