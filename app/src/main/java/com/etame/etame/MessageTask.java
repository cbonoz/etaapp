package com.etame.etame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;

/**
 * Created by cbono on 1/6/16.
 */
// DownloadImage AsyncTask
public class MessageTask extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "MessageTask";

    //private static String mapURL;
    private static ImageView image;
    private static Context context;
    private static String message;
    private static String phoneNumber;

    public MessageTask(String p, String msg, Context c) {
        phoneNumber = p;
        message = msg;
        context = c;
    }

    @Override
    protected Bitmap doInBackground(String...URL) {
        Log.i(TAG,"started task");

        String mapURL = URL[0];
        Log.i(TAG, mapURL);
        Bitmap bitmap = null;
        try {
            // Download Image from URL
            InputStream input = new java.net.URL(mapURL).openStream();

            // Decode Bitmap
            bitmap = BitmapFactory.decodeStream(input);
            Log.i(TAG, bitmap.getByteCount()+"");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        // Set the bitmap into ImageView
        //image.setImageBitmap(result);
        // Close progressdialog
        Log.i(TAG,"onPostExecute");

        Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address",phoneNumber);
        smsIntent.putExtra("sms_body", message);
        //smsIntent.putExtra("Img", result);
        context.startActivity(smsIntent);




        messageNotify();

    }


    private void messageNotify() {
        Log.i(TAG,"messageNotify");

        //when done with sending message - create toast on current activity context
        if (context!=null) {
            Toast toast = Toast.makeText(context, "Message Sent!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}