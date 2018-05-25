package com.eacra.list_v2.list_v2;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

// Created by Ewen on 2018-03-21

public class DeleteNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int arrayPosition, id, numberOfTasks, numberOfCompletedTasks;

        // Obtain saved values
        SharedPreferences sharedPrefs = context.getSharedPreferences("values", 0);
        id = intent.getIntExtra("id", 22);
        numberOfTasks = sharedPrefs.getInt("numberOfTasks", 0);
        numberOfCompletedTasks = sharedPrefs.getInt("numberOfCompletedTasks", 0);
        // Find arrayPosition
        arrayPosition = 0;
        for (int i = 0; i != numberOfTasks; i++) {
            if (id == sharedPrefs.getInt("taskStartTimeArray[" + i + "]", 2500)) {
                arrayPosition = i;
            }
        }
        // Update values
        numberOfTasks -= 1;
        if (sharedPrefs.getBoolean("taskCompletionArray[" + arrayPosition + "]", false) == true) {
            numberOfCompletedTasks -= 1;
        }
        // Calculate percentage
        double numberOfTasks_double = numberOfTasks;
        double numberOfCompletedTasks_double = numberOfCompletedTasks;
        double checkmarkPercent_double;
        checkmarkPercent_double = numberOfCompletedTasks_double/numberOfTasks_double * 100;
        int percentageComplete = (int)checkmarkPercent_double;
        // Save/delete values
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt("numberOfTasks", numberOfTasks).commit();
        editor.putInt("numberOfCompletedTasks", numberOfCompletedTasks).commit();
        editor.putInt("percentageComplete", percentageComplete).commit();
        // Delete task values
        editor.remove("taskStartTimeArray[" + arrayPosition + "]").commit();
        editor.remove("taskStartHourArray[" + arrayPosition + "]").commit();
        editor.remove("taskStartMinuteArray[" + arrayPosition + "]").commit();
        editor.remove("taskStringArray[" + arrayPosition + "]").commit();
        editor.remove("taskCompletionArray[" + arrayPosition + "]").commit();
        editor.remove("dailyTaskArray[" + arrayPosition + "]").commit();
        // Bump everything up to take deleted data's spots
        for (int i = arrayPosition; i != numberOfTasks; i++) {
            editor.putInt("taskStartTimeArray[" + i + "]", sharedPrefs.getInt("taskStartTimeArray[" + (i + 1) + "]", 2500)).commit();
            editor.putInt("taskStartHourArray[" + i + "]", sharedPrefs.getInt("taskStartHourArray[" + (i + 1) + "]", 0)).commit();
            editor.putInt("taskStartMinuteArray[" + i + "]", sharedPrefs.getInt("taskStartMinuteArray[" + (i + 1) + "]", 0)).commit();
            editor.putString("taskStringArray[" + i + "]", sharedPrefs.getString("taskStringArray[" + (i + 1) + "]", null)).commit();
            editor.putBoolean("taskCompletionArray[" + i + "]", sharedPrefs.getBoolean("taskCompletionArray[" + (i + 1) + "]", false)).commit();
            editor.putInt("dailyTaskArray[" + i + "]", sharedPrefs.getInt("dailyTaskArray[" + (i + 1) + "]", 0)).commit();
        }
        // Delete notification when done
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }
}
