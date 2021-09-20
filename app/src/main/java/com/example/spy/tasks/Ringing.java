package com.example.spy.tasks;

import android.app.NotificationManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import com.example.spy.R;
import com.example.spy.utils.AppController;

public class Ringing {
    public Ringing(int notif, String pesan) {


        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            if(alert == null){
                // alert is null, using backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                // I can't see this ever being null (as always have a default notification)
                // but just incase
                if(alert == null) {
                    // alert backup is null, using 2nd backup
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
            }

            //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            if( notif == 1){
                //Define Notification Manager
                NotificationManager notificationManager = (NotificationManager) new AppController().getContext().getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(new AppController().getContext())
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Notif")
                        .setContentText( pesan )
                        .setSound(alert); //This sets the sound to play

                //Display notification
                notificationManager.notify(0, mBuilder.build());
            }else{
                Ringtone r = RingtoneManager.getRingtone(new AppController().getContext(), alert);
                r.play();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
