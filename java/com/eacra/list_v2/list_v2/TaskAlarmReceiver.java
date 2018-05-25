package com.eacra.list_v2.list_v2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

// Created by Ewen on 2018-03-14.

// A broadcast receiver must be used instead of a normal class because the onReceive method ensures that everything is executed before the device cpu goes back to sleep
// CPU GOES BACK TO SLEEP AFTER 10 SECONDS
public class TaskAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("values", 0);
        int arrayPosition = intent.getIntExtra("arrayPosition", 0);
        int id = intent.getIntExtra("id", 22);
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");

        // Create intent to launch MainActivity when notification is clicked
        Intent tapAction = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pendingTapAction = PendingIntent.getActivity(context.getApplicationContext(), id, tapAction, 0);
        // Create intent to respond to done button
        Intent doneIntent = new Intent(context.getApplicationContext(), DoneNotificationReceiver.class);
        doneIntent.putExtra("arrayPosition", arrayPosition);
        doneIntent.putExtra("id", id);
        doneIntent.putExtra("text", intent.getStringExtra("text"));
        PendingIntent donePendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, doneIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        // Create intent to respond to delete button
        Intent deleteIntent = new Intent(context.getApplicationContext(), DeleteNotificationReceiver.class);
        deleteIntent.putExtra("arrayPosition", arrayPosition);
        deleteIntent.putExtra("id", id);
        deleteIntent.putExtra("text", text);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        // Create the notification and set appearance
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_1")
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setColor(Color.rgb(33, 150, 243))
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingTapAction)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_notification_icon, context.getString(R.string.done), donePendingIntent)
                .addAction(R.drawable.ic_delete_icon, context.getString(R.string.delete), deletePendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_1_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            // Each channel allows for user control over notifications(importance, sound, etc) through the system settings
            NotificationChannel channel = new NotificationChannel("channel_1", name, importance);
            channel.setDescription(context.getString(R.string.channel_1_description));
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        // Show the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}