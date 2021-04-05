package com.example.spy.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Broadcast Receiver", Toast.LENGTH_SHORT).show();
        Log.d("spybot", "Broadcast Receiver");

        Intent activityIntent = new Intent(context, CommandService.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) context.startService(activityIntent);
    }
}
