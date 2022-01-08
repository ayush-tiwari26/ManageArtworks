package com.ayush.manageartworks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pdftron.pdf.config.ViewerConfig;
import com.pdftron.pdf.controls.DocumentActivity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    /**
     * source : The image loaded/captured from camera
     * target: the pdf (converted to bitmap) selected by user.
     */
    protected static PythonExecutor pyExec;
    protected static  Bitmap source;
    protected static Bitmap target;
    private static final int LOAD_REQUEST_CODE = 1;
    private static final String TAG = "Main Activity Log";
    public static Context context;
    public static TextView ssimTextView;
    ViewerConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        ssimTextView = (TextView)findViewById(R.id.ssimTextView);
        ssimTextView.setVisibility(View.INVISIBLE);
        config = new ViewerConfig.Builder().openUrlCachePath(this.getCacheDir().getAbsolutePath()).build();
        pyExec = new PythonExecutor(this);
//        // [*****] start python scripts [*****]
//        PythonExecutor pyExec = new PythonExecutor(this);
//        pyExec.testPython();

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
                ImageView selectedPdfImage = (ImageView) findViewById(R.id.targetImageView);
                selectedPdfImage.setImageBitmap(selectedPdfBitmap);
                target = selectedPdfBitmap;
                //Setting Target Image On Pdf Selection
                ((ImageView)(findViewById(R.id.targetImageView))).setImageBitmap(target);
            } catch (IOException e) {
                Toast.makeText(context, "Error converting pdf uri to pdf", Toast.LENGTH_SHORT).show();
                DocumentActivity.openDocument(this, uri, config);
            }
        }
    }

    /**
     * Responsible to open camera and show image preview
     * @param view : View view
     */
    public void captureImage(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Toast.makeText(this, "Image Got", Toast.LENGTH_SHORT ).show();
        //Setting Source Image View On Main Activity Restart
        ((ImageView)(findViewById(R.id.sourceImageView))).setImageBitmap(source);
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
    @SuppressWarnings("JavaDoc")
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

    /**
     * Loads a demo Pdf from url and displays using pdfTron pdf viewer
     * @param view : View view
     */
    public void loadDemoPdf(View view) {
        final Uri fileLink = Uri.parse("https://pdftron.s3.amazonaws.com/downloads/pl/PDFTRON_mobile_about.pdf");
        DocumentActivity.openDocument(this, fileLink, config);
    }

    /**
     * Function to start comparison between both bitmaps(images)
     * @param view
     */
    public void startComparison(View view) {
        if(source==null){
            Toast.makeText(context, "Select A Source To Compare", Toast.LENGTH_SHORT).show();
            return;
        }
        if(target==null){
            Toast.makeText(context, "Select A Target To Compare", Toast.LENGTH_SHORT).show();
            return;
        }
        String sourceStr = bitmapToString(source);
        String targetStr = bitmapToString(target);

        //Starting python executor method to pass data to python script
        pyExec.startComparison(sourceStr, targetStr);
    }

    /**
     * Converts Bitmap to Byte Array
     * @param map : Bitmap of image
     * @return returns byte array of bitmap
     */
    public String bitmapToString(Bitmap map){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        map.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

    /**
     * Called as soon as user clicks on source or target image
     * runs python code for object detection in given bitmap
     * @param view
     */
    public void startObjectDetection(View view) {
        try{
            Bitmap image = ((BitmapDrawable)((ImageView)view).getDrawable()).getBitmap();
            ((ImageView)view).setAlpha(0.5f);
            String byteStr = bitmapToString(image);
            //Starting python executor method to pass data to python script
            //pyExec.detectObject(byteStr);
            Detect detect = new Detect(image);
        }catch(Exception e){
            Toast.makeText(context, "Error loading Image :: "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
}