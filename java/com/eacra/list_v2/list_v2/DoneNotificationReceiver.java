package com.eacra.list_v2.list_v2;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

// Created by Ewen on 2018-03-15.

public class DoneNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int arrayPosition, id, numberOfTasks, numberOfCompletedTasks;
        String text;

        // Obtain saved values
        SharedPreferences sharedPrefs = context.getSharedPreferences("values", 0);
        id = intent.getIntExtra("id", 22);
        numberOfTasks = sharedPrefs.getInt("numberOfTasks", 0);
        // Find arrayPosition
        arrayPosition = 0;
        for (int i = 0; i != numberOfTasks; i++) {
            if (id == sharedPrefs.getInt("taskStartTimeArray[" + i + "]", 2500)) {
                arrayPosition = i;
            }
        }

        numberOfCompletedTasks = sharedPrefs.getInt("numberOfCompletedTasks", 0);
        // Update values
        numberOfCompletedTasks += 1;
        // Calculate percentage
        double numberOfTasks_double = numberOfTasks;
        double numberOfCompletedTasks_double = numberOfCompletedTasks;
        double checkmarkPercent_double;
        checkmarkPercent_double = numberOfCompletedTasks_double/numberOfTasks_double * 100;
        int percentageComplete = (int)checkmarkPercent_double;
        // Save values
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.remove("taskCompletionArray[" + arrayPosition + "]");
        editor.putBoolean("taskCompletionArray[" + arrayPosition + "]", true);
        editor.putInt("numberOfCompletedTasks", numberOfCompletedTasks);
        editor.putInt("percentageComplete", percentageComplete);
        editor.apply();
        // Delete notification when done
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }
}
