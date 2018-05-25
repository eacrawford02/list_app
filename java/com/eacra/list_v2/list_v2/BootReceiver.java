package com.eacra.list_v2.list_v2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences sharedPrefs = context.getSharedPreferences("values", 0);
            int numberOfTasks = sharedPrefs.getInt("numberOfTasks", 0);
            Calendar calendar = Calendar.getInstance();
            int mCurrentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int mCurrentMinute = calendar.get(Calendar.MINUTE);
            int mCurrentTime = (mCurrentHour * 100) + mCurrentMinute;

            // Reschedule alarms
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            // Set alarm to delete all tasks at midnight
            Intent resetIntent = new Intent(context.getApplicationContext(), ResetAlarmReceiver.class);
            PendingIntent resetPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, resetIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            long timeTomorrow = calendar.getTimeInMillis();
            timeTomorrow += 24 * 60 * 60 * 1000;
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeTomorrow, resetPendingIntent);
            // Schedule alarms for each task
            for (int i = 0; i != numberOfTasks; i++) {
                int taskStartTime = sharedPrefs.getInt("taskStartTimeArray[" + i + "]", 2500);
                if (taskStartTime != 2500 && taskStartTime > mCurrentTime) {
                    Intent alarmIntent = new Intent(context.getApplicationContext(), TaskAlarmReceiver.class);
                    alarmIntent.putExtra("arrayPosition", i);
                    alarmIntent.putExtra("id", taskStartTime);
                    alarmIntent.putExtra("title", context.getString(R.string.to_do));
                    alarmIntent.putExtra("text", sharedPrefs.getString("taskStringArray[" + i + "]", null));
                    PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), taskStartTime, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    calendar.set(Calendar.HOUR_OF_DAY, sharedPrefs.getInt("taskStartHourArray[" + i + "]", 0));
                    calendar.set(Calendar.MINUTE, sharedPrefs.getInt("taskStartMinuteArray[" + i + "]", 0));

                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
                }
            }
        }
    }
}
