package com.ayush.manageartworks;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.ayush.manageartworks.ml.SsdMobilenetV11Metadata1;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;

public class Detect {
    String TAG = "Detect Class";
    public Detect(Bitmap bitmap){
        try {
            SsdMobilenetV11Metadata1 model = SsdMobilenetV11Metadata1.newInstance(MainActivity.context);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(bitmap);

            // Runs model inference and gets result.
            SsdMobilenetV11Metadata1.Outputs outputs = model.process(image);
            TensorBuffer locations = outputs.getLocationsAsTensorBuffer();
            TensorBuffer classes = outputs.getClassesAsTensorBuffer();
            TensorBuffer scores = outputs.getScoresAsTensorBuffer();
            TensorBuffer numberOfDetections = outputs.getNumberOfDetectionsAsTensorBuffer();

            Toast.makeText(MainActivity.context,"Number of Objects : "+numberOfDetections.getIntArray()[0],Toast.LENGTH_SHORT).show();
            // Releases model resources if no longer used.
            model.close();
            Log.i(TAG,"Model loaded and Closed Successfully");
        } catch (IOException e) {
            Toast.makeText(MainActivity.context,"ERROR in DETECT::"+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
}
