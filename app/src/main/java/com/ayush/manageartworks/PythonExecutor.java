package com.ayush.manageartworks;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class PythonExecutor {

    private Context context;
    private PyObject pyObject;
    public Bitmap source;
    public Bitmap target;

    public PythonExecutor(Context context){
        this.context=context;
    }

    public PythonExecutor(Context context, Bitmap source, Bitmap target) {
        this.context = context;
        this.source = source;
        this.target = target;
    }

    /**
     * Test Method : To test weather python script runs as expected using chaquopy
     */
    public void testPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
        }
        Python py = Python.getInstance();
        pyObject = py.getModule("hello_world");
        String msg = pyObject.callAttr("hello").toString();
        Toast.makeText(context, "This came from ppython script :: " + msg, Toast.LENGTH_SHORT).show();
    }

}
