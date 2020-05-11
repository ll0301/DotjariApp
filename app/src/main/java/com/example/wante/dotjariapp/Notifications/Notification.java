package com.example.wante.dotjariapp.Notifications;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

public class Notification extends ContextWrapper {

    // fcm 을 보내봅시다.

    private static final String CHANNEL_ID = "com.example.wante.dotjariapp";
    private static final String CHANNEL_NAME = "dotjariapp";

    private NotificationManager notificationManager;

    public Notification(Context base) {
        super(base);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);

    }

    NotificationManager getManager() {

        if(notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return  notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getNotification(String title, String body, PendingIntent pendingIntent
    , Uri soundUri, String icon) {
        return new android.app.Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(Integer.parseInt(icon))
                .setSound(soundUri)
                .setAutoCancel(true);
    }

}
