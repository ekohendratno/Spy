package com.example.spy.services;

import android.annotation.SuppressLint;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;


@SuppressLint("NewApi")
public class NotificationReadService extends NotificationListenerService {
    String tag = "spybot";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log.i(tag, "**********  onNotificationPosted");
        //Log.i(tag, "ID :" + sbn.id + "\t" + sbn.notification?.tickerText + "\t" + sbn.packageName);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.i(tag, "********** onNotificationRemoved");
        //Log.i(tag, "ID :" + sbn?.id + "\t" + sbn?.notification + "\t" + sbn?.packageName)
    }
}
