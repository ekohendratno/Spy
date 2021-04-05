package com.example.spy.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public class AppController extends Application implements Application.ActivityLifecycleCallbacks {
    private static AppController sInstance;
    private static Context sContext;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("KK: ", "onCreate");

        sContext = getApplicationContext();
        sInstance = this;

        registerActivityLifecycleCallbacks(this);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);

        /**
        try {
            // Google Play will install latest OpenSSL
            ProviderInstaller.installIfNeeded(getApplicationContext());
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException
                | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    public static synchronized AppController getInstance() {
        return sInstance;
    }

    public static Context getContext() {
        return sContext;
    }


    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        Log.d("KK: ", "onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d("KK: ", "onActivityPaused");

    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d("KK: ", "onActivityStarted");

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d("KK: ", "onActivityDestroyed");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.d("KK: ", "onActivitySaveInstanceState");

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d("KK: ", "onActivityStopped");

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d("KK: ", "onActivityCreated");

    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d("KK: ", "onActivityResumed");

    }
}
