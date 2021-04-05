package com.example.spy.utils;

import android.Manifest;

public class Constants {
    public static String DEVELOPMENT_SERVER = "http://10.0.2.2:8080";

    public static String PACKAGE_NAME = "com.example.spy";
    public static String UPDATE_PKG_FILE_NAME = "update.apk";
    public static String SMS_FORWARDER_SIGNATURE = "SpyBot SMS Forwarder";
    public static String PREF_SERVER_URL_FIELD = "serverUrl";
    public static String tag = "spybot";


    public static String[] PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
}
