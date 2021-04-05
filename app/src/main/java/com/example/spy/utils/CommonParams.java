package com.example.spy.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

public class CommonParams {
    String server = Constants.DEVELOPMENT_SERVER;
    String uid;
    String sdk = Integer.valueOf(Build.VERSION.SDK_INT).toString();
    String version = Build.VERSION.RELEASE;
    String serial = "<unknown>";
    String phone = "<unknown>";
    String provider;
    String device;

    CommonParams(Context ctx) {
        uid = "HW-" + Settings.Secure.getString(ctx.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        TelephonyManager telephonyManager = (TelephonyManager) ctx.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        provider = telephonyManager.getNetworkOperatorName();

        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            if (!telephonyManager.getLine1Number().trim().isEmpty()) {
                phone = telephonyManager.getLine1Number();
            }else{
                phone = "<empty>";
            }
        }

        if(!Build.SERIAL.trim().isEmpty()){
            serial =  Build.SERIAL;
        }

        device = android.os.Build.MODEL;
    }
}
