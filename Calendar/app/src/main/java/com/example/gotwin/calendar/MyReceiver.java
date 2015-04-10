package com.example.gotwin.calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class MyReceiver extends BroadcastReceiver {

    NotificationManager notificationManager;

    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentTitle(intent.getStringExtra("Title"));
        builder.setContentText(intent.getStringExtra("Message"));
        builder.setSmallIcon(R.drawable.ic_stat_name);
        builder.setAutoCancel(true);
        builder.setWhen(intent.getLongExtra("Date", 0));
        builder.setDefaults(Notification.DEFAULT_SOUND);

        Notification notifi = builder.build();

        notifi.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notifi);
    }
}
