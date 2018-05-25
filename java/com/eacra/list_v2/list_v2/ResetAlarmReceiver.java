package com.eacra.list_v2.list_v2;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.Calendar;

// Created by Ewen on 2018-03-22

public class ResetAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int numberOfTasks, numberOfDailyTasks;
        Boolean done;

        SharedPreferences sharedPrefs = context.getSharedPreferences("values", 0);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        SharedPreferences sharedPrefsPersistent = context.getSharedPreferences("dailyValues", 0);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        numberOfTasks = sharedPrefs.getInt("numberOfTasks", 0);
        numberOfDailyTasks = sharedPrefsPersistent.getInt("numberOfDailyTasks", 0);
        done = sharedPrefs.getBoolean("done", false);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        if (!done) {
            editor.clear().apply();
            numberOfTasks = numberOfDailyTasks;
            int n = 0;
            for (int i = 0; i != numberOfDailyTasks; i++) {
                int taskDay = sharedPrefsPersistent.getInt("dailyTaskArray[" + i + "]", 0);
                // Checks which type of daily task each task is (daily, weekday, weekend)
                if ( ((day == 1 || day == 7) && taskDay == 3) || ((day > 1 && day < 7) && taskDay == 2) || (day <= 7 && taskDay == 1) ) {
                    editor.putInt("taskStartTimeArray[" + n + "]", sharedPrefsPersistent.getInt("taskStartTimeArray[" + i + "]", 2500));
                    editor.putInt("taskStartHourArray[" + n + "]", sharedPrefsPersistent.getInt("taskStartHourArray[" + i + "]", 0));
                    editor.putInt("taskStartMinuteArray[" + n + "]", sharedPrefsPersistent.getInt("taskStartMinuteArray[" + i + "]", 0));
                    editor.putString("taskStringArray[" + n + "]", sharedPrefsPersistent.getString("taskStringArray[" + i + "]", null));
                    editor.putBoolean("taskCompletionArray[" + n + "]", sharedPrefsPersistent.getBoolean("taskCompletionArray[" + i + "]", false));
                    editor.putInt("dailyTaskArray[" + n + "]", sharedPrefsPersistent.getInt("dailyTaskArray[" + i + "]", 0));
                    n += 1;
                }
                else {
                    numberOfTasks -= 1;
                }
            }

            editor.putInt("numberOfTasks", numberOfTasks);
            editor.putInt("numberOfCompletedTasks", 0);
            editor.putInt("percentageComplete", 0);
        }

        editor.putBoolean("done", false);
        editor.apply();

        // Schedule alarms for each task
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for (int i = 0; i != numberOfTasks; i++) {
            if (sharedPrefs.getInt("taskStartTimeArray[" + i + "]", 2500) != 2500) {
                Intent alarmIntentData = new Intent(context.getApplicationContext(), TaskAlarmReceiver.class);
                alarmIntentData.putExtra("arrayPosition", i);
                alarmIntentData.putExtra("id", sharedPrefs.getInt("taskStartTimeArray[" + i + "]", 2500));
                alarmIntentData.putExtra("title", context.getString(R.string.to_do));
                alarmIntentData.putExtra("text", sharedPrefs.getString("taskStringArray[" + i + "]", null));
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, sharedPrefs.getInt("taskStartTimeArray[" + i + "]", 2500), alarmIntentData, PendingIntent.FLAG_CANCEL_CURRENT);
                // Set the alarm start time
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, sharedPrefs.getInt("taskStartHourArray[" + i + "]", 0));
                calendar.set(Calendar.MINUTE, sharedPrefs.getInt("taskStartMinuteArray[" + i + "]", 0));

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
            }
        }

    }
}
