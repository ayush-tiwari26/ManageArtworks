package com.ayush.manageartworks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pdftron.pdf.config.ViewerConfig;
import com.pdftron.pdf.controls.DocumentActivity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    /**
     * source : The image loaded/captured from camera
     * target: the pdf (converted to bitmap) selected by user.
     */
    private static  Bitmap source;
    private static Bitmap target;
    private static final int LOAD_REQUEST_CODE = 1;
    private static final String TAG = "Main Activity Log";
    private Context context;
    ViewerConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        config = new ViewerConfig.Builder().openUrlCachePath(this.getCacheDir().getAbsolutePath()).build();
        // [*****] start python scripts [*****]
        PythonExecutor pyExec = new PythonExecutor(this);
        pyExec.testPython();
    }

    /**
     * Loads the requested pdf from local storage using pdfTron; Opens pdf selection activity
     * @param view : View view
     */
    public void loadPdfFromStorage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, LOAD_REQUEST_CODE);
    }

    /**
     * Activity Result Method : Loads the requested pdf from local storage using pdfTron
     * @param requestCode : to verify the request code
     * @param resultCode : to verify the result code
     * @param data : data of type android.context.Intent containing selected pdf file by user
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOAD_REQUEST_CODE) {
            assert data != null;
            Uri uri = data.getData();
            Log.d(TAG, "Pdf selected" + uri);
            try {
                File pdf = toFile(uri);
                Bitmap selectedPdfBitmap = pdfToBitmap(pdf).get(0);
                ImageView selectedPdfImage = (ImageView) findViewById(R.id.selectedPdfImageView);
                selectedPdfImage.setImageBitmap(selectedPdfBitmap);
            } catch (IOException e) {
                Toast.makeText(context, "Error converting pdf uri to pdf", Toast.LENGTH_SHORT).show();
                DocumentActivity.openDocument(this, uri, config);
            }
        }
    }

    /**
     * Loads a demo Pdf from url and displays using pdfTron pdf viewer
     * @param view
     */
    public void loadDemoPdf(View view) {
        final Uri fileLink = Uri.parse("https://pdftron.s3.amazonaws.com/downloads/pl/PDFTRON_mobile_about.pdf");
        DocumentActivity.openDocument(this, fileLink, config);
    }

    /**
     * Responsible to open camera and show image preview
     * @param view
     */
    public void captureImage(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    /**
     * Takes File (pdf file) as the parameter and return the Bitmap converted from the file
     * @param pdfFile : The File of pdf to be converted
     * @return ArrayList<Bitmap>: Returns an Arraylist of Bitmaps, where each Bitmap corrsponds to one page of pdf
     */
    private ArrayList<Bitmap> pdfToBitmap(File pdfFile) {
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

    /**
     *
     * @param uri : The Uri of pdf file to be converted to a java.io.File instance
     * @return File file: The pdf file from the uri
     * @throws IOException
     */
    @SuppressLint("Range")
    private File toFile(Uri uri) throws IOException {
        String displayName = "";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } finally {
                cursor.close();
            }
        }

        File file = File.createTempFile(
                FilenameUtils.getBaseName(displayName),
                "." + FilenameUtils.getExtension(displayName)
        );
        InputStream inputStream = getContentResolver().openInputStream(uri);
        FileUtils.copyInputStreamToFile(inputStream, file);
        return file;
    }
}