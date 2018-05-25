package com.eacra.list_v2.list_v2;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Arrays;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //arrays that keep track of task data and order
    public static String[] taskStringArray, temporaryStringArray;
    //arrays that keep track of whether or not a task has been completed (check marked)
    public static Boolean[] taskCompletionArray, temporaryCompletionArray;
    //arrays that keep track of task start time
    public static int[] taskStartTimeArray, temporaryStartTimeArray, taskStartHourArray, temporaryStartHourArray, taskStartMinuteArray, temporaryStartMinuteArray, dailyTaskArray, temporaryDailyTaskArray;
    public static int percentageComplete;
    public static Boolean done;

    //relevant activity_main objects
    public Toolbar Toolbar;
    public ScrollView ScrollView;
    public FloatingActionButton doneButton;
    public TextView percentageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize layout objects
        Toolbar = findViewById(R.id.my_toolbar);
        ScrollView = findViewById(R.id.scrollView);
        doneButton = findViewById(R.id.done_button);
        percentageTextView = findViewById(R.id.percentage_Complete);
        // Set up main activity
        setSupportActionBar(Toolbar);
        getSupportActionBar().setTitle("My List");
        // Set alarm to delete all tasks at midnight
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), ResetAlarmReceiver.class);
        PendingIntent resetIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        long timeTomorrow = calendar.getTimeInMillis();
        timeTomorrow += 24 * 60 * 60 * 1000;
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeTomorrow, resetIntent);
        // Load saved variables
        SharedPreferences sharedPrefs = getSharedPreferences("values", 0);
        SharedPreferences sharedPrefsPersistent = getSharedPreferences("dailyValues", 0);
        int numberOfTasks_s = sharedPrefs.getInt("numberOfTasks", 0);
        int numberOfCompletedTasks_s = sharedPrefs.getInt("numberOfCompletedTasks", 0);
        int numberOfDailyTasks_s = sharedPrefsPersistent.getInt("numberOfDailyTasks", 0);
        int percentageComplete_s = sharedPrefs.getInt("percentageComplete", 0);
        Boolean done_s = sharedPrefs.getBoolean("done", false);
        int[] taskStartTimeArray_s = new int[numberOfTasks_s];
        int[] taskStartHourArray_s = new int[numberOfTasks_s];
        int[] taskStartMinuteArray_s = new int[numberOfTasks_s];
        String[] taskStringArray_s = new String[numberOfTasks_s];
        Boolean[] taskCompletionArray_s = new Boolean[numberOfTasks_s];
        int[] dailyTaskArray_s = new int[numberOfTasks_s];
        for (int i = 0; i != numberOfTasks_s; i++) {
            taskStartTimeArray_s[i] = sharedPrefs.getInt("taskStartTimeArray[" + i + "]", 2500);
            taskStartHourArray_s[i] = sharedPrefs.getInt("taskStartHourArray[" + i + "]", 0);
            taskStartMinuteArray_s[i] = sharedPrefs.getInt("taskStartMinuteArray[" + i + "]", 0);
            taskStringArray_s[i] = sharedPrefs.getString("taskStringArray[" + i + "]", null);
            taskCompletionArray_s[i] = sharedPrefs.getBoolean("taskCompletionArray[" + i + "]", false);
            dailyTaskArray_s[i] = sharedPrefs.getInt("dailyTaskArray[" + i + "]", 0);
        }

        assembler(numberOfTasks_s, numberOfCompletedTasks_s, numberOfDailyTasks_s, percentageComplete_s, done_s, taskStartTimeArray_s, taskStartHourArray_s, taskStartMinuteArray_s, taskStringArray_s, taskCompletionArray_s, dailyTaskArray_s);
    }


    public static class TaskFragment extends Fragment {
        public static int numberOfTasks, numberOfCompletedTasks, numberOfDailyTasks;
        protected int arrayPosition, startTime, startHour, startMinute, dailyTask;
        protected boolean contentChecker, isChecked;
        protected String taskTextString;
        // Needed to cancel alarms
        protected PendingIntent notificationAlarmIntent;
        //Provides a reference to the MainActivity & objects
        public TextView percentageTextView;
        public FloatingActionButton doneButton;

        //relevant task_fragment objects
        public CheckBox checkBox;
        public TextView taskTextView;
        public ImageButton editButton;
        public ImageButton deleteButton;
        public TextView taskTimeTextView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {
            final View fragment1 = inflater.inflate(R.layout.task_fragment, container, false);
            //initialize variables
            isChecked = false;
            dailyTask = 0;
            contentChecker = getArguments().getBoolean("contentChecker");
            checkBox = fragment1.findViewById(R.id.taskFragment_checkbox);
            taskTextView = fragment1.findViewById(R.id.taskFragment_taskText);
            editButton = fragment1.findViewById(R.id.taskFragment_editButton);
            deleteButton = fragment1.findViewById(R.id.taskFragment_deleteButton);
            taskTimeTextView = fragment1.findViewById(R.id.taskFragment_taskTime);
            taskTimeTextView.setVisibility(View.GONE);
            percentageTextView = getActivity().findViewById(R.id.percentage_Complete);
            doneButton = getActivity().findViewById(R.id.done_button);

            if (contentChecker == true) {
                // Initialize saved variables
                arrayPosition = getArguments().getInt("arrayPosition");
                startTime = getArguments().getInt("startTime");
                startHour = getArguments().getInt("startHour");
                startMinute = getArguments().getInt("startMinute");
                isChecked = getArguments().getBoolean("isChecked");
                taskTextString = getArguments().getString("taskTextString");
                dailyTask = getArguments().getInt("dailyTask");
                taskTextView.setText(taskTextString);
                editButton.setImageResource(R.drawable.ic_more_icon);
                if (isChecked) {
                    checkBox.setChecked(true);
                }
                // Display time
                displayTime();
            }
            else {
                checkBox.setEnabled(false);
                taskTextView.setEnabled(false);
            }

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // A thread is used to keep the UI  S N A P P Y
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Calendar calendar = Calendar.getInstance();
                            int mCurrentHour = calendar.get(Calendar.HOUR_OF_DAY);
                            int mCurrentMinute = calendar.get(Calendar.MINUTE);
                            int mCurrentTime = (mCurrentHour * 100) + mCurrentMinute;
                            arrayPosition = getArrayPosition(taskTextString);
                            if (checkBox.isChecked()) {
                                numberOfCompletedTasks += 1;
                                taskCompletionArray[arrayPosition] = true;
                                // Cancel notification
                                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                                if (startTime != 2500 && startTime > mCurrentTime && !done) {
                                    alarmManager.cancel(getNotificationPendingIntent(arrayPosition, startTime, getString(R.string.to_do), taskTextString));
                                }
                            }
                            else {
                                numberOfCompletedTasks -= 1;
                                taskCompletionArray[arrayPosition] = false;
                                // Schedule notification
                                if (startTime != 2500 && startTime > mCurrentTime && !done) {
                                    ((MainActivity) getActivity()).scheduleNotificationAlarm(arrayPosition, startTime, startHour, startMinute, getString(R.string.to_do), taskTextString);
                                }
                            }
                            percentageComplete = ((MainActivity)getActivity()).calculatePercentage();
                            // Talk back to UI thread
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    percentageTextView.setText(percentageComplete + "%");
                                    if (percentageComplete == 100) {
                                        percentageTextView.setTextColor(getResources().getColor(R.color.colorAccent));
                                    }
                                    else {
                                        percentageTextView.setTextColor(getResources().getColor(R.color.textColorPrimary));
                                    }
                                }
                            });
                            // Save values
                            Context context = getActivity();
                            ((MainActivity)getActivity()).saveTaskValues(context);
                        }
                    }).start();
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If the task has already been initialized, then a popup menu with options will show
                    if (contentChecker == true) {
                        Context popupTheme = new ContextThemeWrapper(getActivity(), R.style.PopupTheme);
                        PopupMenu TaskMenu = new PopupMenu(popupTheme, v);
                        TaskMenu.inflate(R.menu.task_menu);
                        TaskMenu.show();
                        // Handle click events
                        TaskMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.edit_item:
                                        arrayPosition = getArrayPosition(taskTextString);
                                        // Passes previously set data to task dialog
                                        TaskDialog TaskDialog = new TaskDialog();
                                        Bundle args = new Bundle();
                                        args.putInt("startTime", startTime);
                                        args.putInt("startHour", startHour);
                                        args.putInt("startMinute", startMinute);
                                        args.putString("taskTextString", taskTextString);
                                        args.putBoolean("contentChecker", contentChecker);
                                        args.putInt("dailyTask", dailyTask);
                                        TaskDialog.setArguments(args);
                                        TaskDialog.setTargetFragment(TaskFragment.this, 22);
                                        TaskDialog.show(getFragmentManager(), "TaskDialog");
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                    }
                    else {
                        TaskDialog TaskDialog = new TaskDialog();
                        Bundle args = new Bundle();
                        args.putInt("startTime", 2500);
                        args.putBoolean("contentChecker", contentChecker);
                        args.putInt("dailyTask", dailyTask);
                        TaskDialog.setArguments(args);
                        TaskDialog.setTargetFragment(TaskFragment.this, 22);
                        TaskDialog.show(getFragmentManager(), "TaskDialog");
                    }
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contentChecker == true) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Calendar calendar = Calendar.getInstance();
                                int mCurrentHour = calendar.get(Calendar.HOUR_OF_DAY);
                                int mCurrentMinute = calendar.get(Calendar.MINUTE);
                                int mCurrentTime = (mCurrentHour * 100) + mCurrentMinute;

                                // Fragment state changes
                                arrayPosition = getArrayPosition(taskTextString);
                                ((MainActivity)getActivity()).deleteTaskValues(arrayPosition);
                                numberOfTasks -= 1;
                                percentageComplete = ((MainActivity)getActivity()).calculatePercentage();
                                // Cancel future alarm/notification
                                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                                NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                                if (startTime != 2500 && !done) {
                                    if (startTime > mCurrentTime) {
                                        alarmManager.cancel(getNotificationPendingIntent(arrayPosition, startTime, getString(R.string.to_do), taskTextString));
                                    }
                                    else {
                                        notificationManager.cancel(startTime);
                                    }
                                }
                                // UI changes and updates
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getFragmentManager().beginTransaction()
                                                .remove(TaskFragment.this).commit();
                                        if (numberOfTasks == 0 || done) {
                                            doneButton.setEnabled(false);
                                        }
                                        percentageTextView.setText(percentageComplete + "%");
                                        if (percentageComplete == 100) {
                                            percentageTextView.setTextColor(getResources().getColor(R.color.colorAccent));
                                        }
                                        else {
                                            percentageTextView.setTextColor(getResources().getColor(R.color.textColorPrimary));
                                        }
                                    }
                                });
                                // Save values
                                Context context = getActivity();
                                ((MainActivity)getActivity()).saveTaskValues(context);
                            }
                        }).start();
                    }
                    else {
                        // This has to be put in an else statement because android shits itself if it isn't
                        // yo if you're reading this pls tell me why aaa
                        getFragmentManager().beginTransaction()
                                .remove(TaskFragment.this).commit();
                    }
                }
            });

            return fragment1;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            SharedPreferences sharedPrefsPersistent = getActivity().getSharedPreferences("dailyValues", 0);
            SharedPreferences.Editor persistentEditor = sharedPrefsPersistent.edit();
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            int mCurrentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int mCurrentMinute = calendar.get(Calendar.MINUTE);
            int mCurrentTime = (mCurrentHour * 100) + mCurrentMinute;
            // Advance day by 1 if editing tasks for tomorrow (to ensure that a weekday task doesn't show up on a weekend and vice versa)
            if (done) {
                if (day != 7) {
                    day += 1;
                }
                else {
                    day = 1;
                }
            }
            if (requestCode == 22 && resultCode == -1) {
                // This NEEDS to be done before the values are updated
                // Cancel existing alarm
                if (startTime != 2500) {
                    alarmManager.cancel(getNotificationPendingIntent(arrayPosition, startTime, getString(R.string.to_do), taskTextString));
                }

                // Update values
                startTime = data.getIntExtra("startTime", 2500);
                startHour = data.getIntExtra("startHour", 0);
                startMinute = data.getIntExtra("startMinute", 0);
                dailyTask = data.getIntExtra("dailyTask", 0);
                taskTextString = data.getStringExtra("editTaskMessage");
                // Checks which type of daily task each task is (daily/not daily, weekday, weekend) to ensure that this task is displayed only on the correct day
                if ( ((day == 1 || day == 7) && dailyTask == 3) || ((day > 1 && day < 7) && dailyTask == 2) || (day <= 7 && dailyTask == 1) || (dailyTask == 0) ) {
                    taskTextView.setText(taskTextString);

                    // Update arrays
                    if (contentChecker == false) {
                        if (!done) {
                            doneButton.setEnabled(true);
                        }
                        checkBox.setEnabled(true);
                        taskTextView.setEnabled(true);
                        editButton.setImageResource(R.drawable.ic_more_icon);
                        numberOfTasks += 1;
                        contentChecker = true;
                        arrayPosition = ((MainActivity) getActivity()).orderTasks(startTime);
                        ((MainActivity) getActivity()).addTaskValues(arrayPosition, startHour, startMinute, taskTextString, isChecked, dailyTask);
                    } else {
                        // Compensate for decrement in deleteTaskValues method
                        if (taskCompletionArray[arrayPosition] == true) {
                            numberOfCompletedTasks += 1;
                        }
                        ((MainActivity) getActivity()).deleteTaskValues(arrayPosition);
                        arrayPosition = ((MainActivity) getActivity()).orderTasks(startTime);
                        ((MainActivity) getActivity()).addTaskValues(arrayPosition, startHour, startMinute, taskTextString, isChecked, dailyTask);
                    }
                    // Set alarm/update notification only if task has set time and if the "day" isn't done depending on start time
                    if (startTime != 2500 && !done) {
                        if (startTime < mCurrentTime) {
                            ((MainActivity) getActivity()).showNotification(startTime, arrayPosition, getString(R.string.to_do), taskTextString);
                        }
                        else {
                            notificationAlarmIntent = ((MainActivity) getActivity()).scheduleNotificationAlarm(arrayPosition, startTime, startHour, startMinute, getString(R.string.to_do), taskTextString);
                        }
                    }
                    // Display time
                    displayTime();
                    // update percentage
                    percentageComplete = ((MainActivity) getActivity()).calculatePercentage();
                    percentageTextView.setText(percentageComplete + "%");
                    percentageTextView.setText(percentageComplete + "%");
                    if (percentageComplete == 100) {
                        percentageTextView.setTextColor(getResources().getColor(R.color.colorAccent));
                    } else {
                        percentageTextView.setTextColor(getResources().getColor(R.color.textColorPrimary));
                    }
                }
                // Save values
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Save normal values
                        Context context = getActivity();
                        ((MainActivity)getActivity()).saveTaskValues(context);
                        // Save daily task values
                        SharedPreferences sharedPrefsPersistent = getActivity().getSharedPreferences("dailyValues", 0);
                        SharedPreferences.Editor persistentEditor = sharedPrefsPersistent.edit();
                        if (dailyTask > 0) {
                            if (numberOfDailyTasks > 1) {
                                // Order daily task values
                                // Load start times to order
                                // Array size is already set to include new value
                                int[] taskOrder = new int[numberOfDailyTasks];
                                int position = 0;
                                // Account for numberOfDailyTasks increment in in dialog
                                for (int i = 0; i != numberOfDailyTasks - 1; i++) {
                                    taskOrder[i] = sharedPrefsPersistent.getInt("taskStartTimeArray[" + i + "]", 2500);
                                }
                                // Insert new value and sort
                                taskOrder[numberOfDailyTasks - 1] = startTime;
                                Arrays.sort(taskOrder);
                                // Find new value
                                for (int i = 0; i != numberOfDailyTasks; i++) {
                                    if (taskOrder[i] == startTime) {
                                        position = i;
                                    }
                                }
                                // Shift values after new value position down one to make room
                                for (int i = numberOfDailyTasks; i != position; i--) {
                                    persistentEditor.putInt("taskStartTimeArray[" + i + "]", sharedPrefsPersistent.getInt("taskStartTimeArray[" + (i - 1) + "]", 2500));
                                    persistentEditor.putInt("taskStartHourArray[" + i + "]", sharedPrefsPersistent.getInt("taskStartHourArray[" + (i - 1) + "]", 0));
                                    persistentEditor.putInt("taskStartMinuteArray[" + i + "]", sharedPrefsPersistent.getInt("taskStartMinuteArray[" + (i - 1) + "]", 0));
                                    persistentEditor.putString("taskStringArray[" + i + "]", sharedPrefsPersistent.getString("taskStringArray[" + (i - 1) + "]", null));
                                    persistentEditor.putBoolean("taskCompletionArray[" + i + "]", sharedPrefsPersistent.getBoolean("taskCompletionArray[" + (i - 1) + "]", false));
                                    persistentEditor.putInt("dailyTaskArray[" + i + "]", sharedPrefsPersistent.getInt("dailyTaskArray[" + (i - 1) + "]", 0));
                                }
                                // Remove old values occupying space
                                persistentEditor.remove("taskStartTimeArray[" + position + "]");
                                persistentEditor.remove("taskStartHourArray[" + position + "]");
                                persistentEditor.remove("taskStartMinuteArray[" + position + "]");
                                persistentEditor.remove("taskStringArray[" + position + "]");
                                persistentEditor.remove("taskCompletionArray[" + position + "]");
                                persistentEditor.remove("dailyTaskArray[" + position + "]");
                                // Add new values
                                persistentEditor.putInt("taskStartTimeArray[" + position + "]", startTime);
                                persistentEditor.putInt("taskStartHourArray[" + position + "]", startHour);
                                persistentEditor.putInt("taskStartMinuteArray[" + position + "]", startMinute);
                                persistentEditor.putString("taskStringArray[" + position + "]", taskTextString);
                                persistentEditor.putBoolean("taskCompletionArray[" + position + "]", isChecked);
                                persistentEditor.putInt("dailyTaskArray[" + position + "]", dailyTask);
                            }
                            else {
                                persistentEditor.putInt("taskStartTimeArray[" + 0 + "]", startTime);
                                persistentEditor.putInt("taskStartHourArray[" + 0 + "]", startHour);
                                persistentEditor.putInt("taskStartMinuteArray[" + 0 + "]", startMinute);
                                persistentEditor.putString("taskStringArray[" + 0 + "]", taskTextString);
                                persistentEditor.putBoolean("taskCompletionArray[" + 0 + "]", isChecked);
                                persistentEditor.putInt("dailyTaskArray[" + 0 + "]", dailyTask);
                            }
                            persistentEditor.apply();
                        }
                    }
                }).start();
                ((MainActivity)getActivity()).refreshActivityDisplay(taskStartTimeArray, taskStartHourArray, taskStartMinuteArray, taskStringArray, taskCompletionArray, dailyTaskArray);
            }
        }

        public int getArrayPosition(String taskTextString) {
            int arrayPosition = 0;

            for (int i = 0; i != TaskFragment.numberOfTasks; i++) {
                if (taskStringArray[i] == taskTextString) {
                    arrayPosition = i;
                }
            }
            return arrayPosition;
        }

        public void displayTime() {
            // Makes sure task has a set start time
            if (startTime != 2500) {
                taskTimeTextView.setVisibility(View.VISIBLE);
                // after 12 pm
                if (startHour > 12 && startHour != 0) {
                    int standardHour = startHour - 12;
                    if (startMinute == 0) {
                        taskTimeTextView.setText(standardHour + ":" + startMinute + "0 PM");
                    }
                    else if (startMinute < 10) {
                        taskTimeTextView.setText(standardHour + ":0" + startMinute + " PM");
                    }
                    else {
                        taskTimeTextView.setText(standardHour + ":" + startMinute + " PM");
                    }
                }
                // at 12 pm
                else if (startHour == 12) {
                    if (startMinute == 0) {
                        taskTimeTextView.setText(12 + ":" + startMinute + "0 PM");
                    }
                    else if(startMinute < 10) {
                        taskTimeTextView.setText(12 + ":0" + startMinute + " PM");
                    }
                    else {
                        taskTimeTextView.setText(12 + ":" + startMinute + " PM");
                    }
                }
                // at 12 am
                else if (startHour == 0) {
                    if (startMinute == 0) {
                        taskTimeTextView.setText(12 + ":" + startMinute + "0 AM");
                    }
                    else if(startMinute < 10) {
                        taskTimeTextView.setText(12 + ":0" + startMinute + " PM");
                    }
                    else {
                        taskTimeTextView.setText(12 + ":" + startMinute + " AM");
                    }
                }
                // before 12 pm
                else {
                    if (startMinute == 0) {
                        taskTimeTextView.setText(startHour + ":" + startMinute + "0 AM");
                    }
                    else if(startMinute < 10) {
                        taskTimeTextView.setText(12 + ":0" + startMinute + " PM");
                    }
                    else {
                        taskTimeTextView.setText(startHour + ":" + startMinute + " AM");
                    }
                }
            }
        }

        public PendingIntent getNotificationPendingIntent(int arrayPosition, int startTime, String title, String text) {
            Intent intent = new Intent(getActivity().getApplicationContext(), TaskAlarmReceiver.class);
            intent.putExtra("arrayPosition", arrayPosition);
            intent.putExtra("id", startTime);
            intent.putExtra("title", title);
            intent.putExtra("text", text);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), startTime, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            return alarmIntent;
        }

    }

    public static class TaskDialog extends DialogFragment {
        EditText editText;
        ImageButton clearButton;
        Button advancedButton;
        CheckBox startTimeCheckbox, dailyTaskCheckbox, weekdayCheckbox, weekendCheckbox;
        TextView startTimeTextView, dailyTaskTextView, weekdayTextView, weekendTextView;
        int startTime;
        int startHour;
        int startMinute;
        int dailyTask;
        String taskTextString;
        Boolean contentChecker, advanced;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Pass null as the parent view because its going in the dialog layout
            View view = inflater.inflate(R.layout.task_dialog, null);
            editText = view.findViewById(R.id.taskDialog_editText);
            clearButton = view.findViewById(R.id.taskDialog_clearButton);
            advancedButton = view.findViewById(R.id.taskDialog_advanced_button);
            startTimeCheckbox = view.findViewById(R.id.taskDialog_startTime_checkbox);
            startTimeTextView = view.findViewById(R.id.taskDialog_startTime_textView);
            dailyTaskCheckbox = view.findViewById(R.id.taskDialog_dailyTask_checkbox);
            dailyTaskTextView = view.findViewById(R.id.taskDialog_dailyTask_textView);
            weekdayCheckbox = view.findViewById(R.id.taskDialog_weekday_checkbox);
            weekdayTextView = view.findViewById(R.id.taskDialog_weekday_textView);
            weekendCheckbox = view.findViewById(R.id.taskDialog_weekend_checkbox);
            weekendTextView = view.findViewById(R.id.taskDialog_weekend_textView);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
            builder.setView(view);
            advanced = false;
            // Weekday and weekend options are hidden by default
            weekdayCheckbox.setVisibility(View.GONE);
            weekdayTextView.setVisibility(View.GONE);
            weekendCheckbox.setVisibility(View.GONE);
            weekendTextView.setVisibility(View.GONE);
            // Daily task default is 0
            dailyTask = getArguments().getInt("dailyTask");
            // Check checkbox if task already had set time (fragments with no set time have a startTime value of 2500)
            startTime = getArguments().getInt("startTime");
            contentChecker = getArguments().getBoolean("contentChecker");
            if (startTime != 2500) {
                startTimeCheckbox.setChecked(true);
                startTimeTextView.setText(R.string.keep_start_time);
                startHour = getArguments().getInt("startHour");
                startMinute = getArguments().getInt("startMinute");
            }
            // Sets editText to previously chosen text
            if (contentChecker == true) {
                taskTextString = getArguments().getString("taskTextString");
                editText.setText(taskTextString);
                editText.setSelection(editText.getText().length());
                // Check daily task checkbox if task was a daily, weekday or weekend task
                if (dailyTask > 0) {
                    TaskFragment.numberOfDailyTasks -= 1;
                    dailyTaskCheckbox.setChecked(true);
                    dailyTaskTextView.setText(R.string.keep_daily_task);
                }
            }
            // Clear editText when clearButton is clicked
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = editText.getText().toString();
                    if (!text.equals("")) {
                        editText.setText("");
                    }
                }
            });
            // Show weekday and weekend options when advancedButton is pressed
            advancedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!advanced) {
                        weekdayCheckbox.setVisibility(View.VISIBLE);
                        weekdayTextView.setVisibility(View.VISIBLE);
                        weekendCheckbox.setVisibility(View.VISIBLE);
                        weekendTextView.setVisibility(View.VISIBLE);
                        advanced = true;
                        advancedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_icon, 0);
                    }
                    else {
                        weekdayCheckbox.setVisibility(View.GONE);
                        weekdayTextView.setVisibility(View.GONE);
                        weekendCheckbox.setVisibility(View.GONE);
                        weekendTextView.setVisibility(View.GONE);
                        advanced = false;
                        advancedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_icon, 0);
                    }
                }
            });
            dailyTaskCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (dailyTaskCheckbox.isChecked()) {
                        dailyTask = 1;
                        weekdayCheckbox.setChecked(true);
                        weekendCheckbox.setChecked(true);
                    }
                    else {
                        if (dailyTask == 1) {
                            weekdayCheckbox.setChecked(false);
                            weekendCheckbox.setChecked(false);
                            dailyTask = 0;
                        }
                    }
                }
            });
            weekdayCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (weekdayCheckbox.isChecked()) {
                        if (weekendCheckbox.isChecked()) {
                            dailyTask = 1;
                            dailyTaskCheckbox.setChecked(true);
                        }
                        else {
                            dailyTask = 2;
                        }
                    }
                    else {
                        if (weekendCheckbox.isChecked()) {
                            dailyTask = 3;
                            dailyTaskCheckbox.setChecked(false);
                        }
                        else {
                            dailyTask = 0;
                        }
                    }
                }
            });
            weekendCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (weekendCheckbox.isChecked()) {
                        if (weekdayCheckbox.isChecked()) {
                            dailyTask = 1;
                            dailyTaskCheckbox.setChecked(true);
                        }
                        else {
                            dailyTask = 3;
                        }
                    }
                    else {
                        if (weekdayCheckbox.isChecked()) {
                            dailyTask = 2;
                            dailyTaskCheckbox.setChecked(false);
                        }
                        else {
                            dailyTask = 0;
                        }
                    }
                }
            });
            //Add action buttons (save and cancel)
                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPrefsPersistent = getActivity().getSharedPreferences("dailyValues", 0);
                        SharedPreferences.Editor persistentEditor = sharedPrefsPersistent.edit();

                        String editTaskMessage = editText.getText().toString();
                        if (editTaskMessage.equals("")) {
                            TaskDialog.this.getDialog().cancel();
                        }
                        else if (startTimeCheckbox.isChecked()) {
                            if (dailyTask > 0) {
                                TaskFragment.numberOfDailyTasks += 1;
                            }
                            DialogFragment StartTimePicker = new StartTimePicker();
                            Bundle args = new Bundle();
                            args.putString("editTaskMessage", editTaskMessage);
                            args.putInt("startTime", startTime);
                            args.putInt("dailyTask", dailyTask);
                            // If task already had a set time, pass previous time to be TimePicker default times
                            if (startTime != 2500) {
                                args.putInt("startHour", startHour);
                                args.putInt("startMinute", startMinute);
                            }
                            StartTimePicker.setArguments(args);
                            StartTimePicker.setTargetFragment(getTargetFragment(), 22);
                            StartTimePicker.show(getFragmentManager(), "StartTimePicker");
                        }
                        else {
                            if (dailyTask > 0) {
                                TaskFragment.numberOfDailyTasks += 1;
                            }
                            Intent data = new Intent();
                            data.putExtra("editTaskMessage", editTaskMessage);
                            data.putExtra("startTime", 2500);
                            data.putExtra("dailyTask", dailyTask);
                            getTargetFragment().onActivityResult(getTargetRequestCode(), -1, data);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TaskDialog.this.getDialog().cancel();
                    }
                });

            return builder.create();
        }
    }

    public static class StartTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        Calendar calendar;
        int hour;
        int minute;
        int startTime;
        String editTaskMessage;
        int dailyTask;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            editTaskMessage = getArguments().getString("editTaskMessage");
            startTime = getArguments().getInt("startTime");
            dailyTask = getArguments().getInt("dailyTask");
            // If the task already had a set time, then the TimePicker default time will be that previously set time
            if (startTime != 2500) {
                hour = getArguments().getInt("startHour");
                minute = getArguments().getInt("startMinute");
            }
            else {
                calendar = Calendar.getInstance();
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
            }

            return new TimePickerDialog(getActivity(), this, hour, minute, false);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar = Calendar.getInstance();
            int mCurrentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int mCurrentMinute = calendar.get(Calendar.MINUTE);
            int mCurrentTime = (mCurrentHour * 100) + mCurrentMinute;

            int chosenStartTime = (hourOfDay * 100) + minute;
            Boolean errorChecker = false;
            // Makes sure that this time slot is free
            for (int i = 0; i != TaskFragment.numberOfTasks; i++) {
                if (taskStartTimeArray[i] == chosenStartTime) {
                    errorChecker = true;
                }
            }
            SharedPreferences sharedPrefsPersistent = getActivity().getSharedPreferences("dailyValues", 0);
            for (int i = 0; i != TaskFragment.numberOfDailyTasks; i++) {
                if (sharedPrefsPersistent.getInt("taskStartTimeArray[" + i + "]", 2500) == chosenStartTime) {
                    errorChecker = true;
                }
            }
            // Makes sure time is in the future (valid)
            if ((chosenStartTime < mCurrentTime) && !done) {
                errorChecker = true;
            }
            // Bypasses error message if task already had set time
            if (startTime != 2500) {
                errorChecker = false;
            }
            // Collect and send data back to TaskFragment only if time slot is free and time is valid
            if (!errorChecker) {
                Intent data = new Intent();
                data.putExtra("editTaskMessage", editTaskMessage);
                data.putExtra("startTime", chosenStartTime);
                data.putExtra("startHour", hourOfDay);
                data.putExtra("startMinute", minute);
                data.putExtra("dailyTask", dailyTask);
                getTargetFragment().onActivityResult(getTargetRequestCode(), -1, data);
            }
            else {
                // Throws error message allowing to reset time
                DialogFragment TimeErrorDialog = new TimeErrorDialog();
                Bundle args = new Bundle();
                args.putInt("startTime", chosenStartTime);
                args.putInt("currentTime", mCurrentTime);
                args.putString("editTaskMessage", editTaskMessage);
                args.putInt("dailyTask", dailyTask);
                TimeErrorDialog.setArguments(args);
                TimeErrorDialog.setTargetFragment(getTargetFragment(), 22);
                TimeErrorDialog.show(getFragmentManager(), "TimeErrorDialog");
            }
        }
    }

    public static class TimeErrorDialog extends DialogFragment {
        public int startTime, currentTime;
        public String editTaskMessage;
        public int dailyTask;
        public TextView errorTextView;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            startTime = getArguments().getInt("startTime");
            editTaskMessage = getArguments().getString("editTaskMessage");
            dailyTask = getArguments().getInt("dailyTask");
            currentTime = getArguments().getInt("currentTime");

            // Pass null as the parent view because it's going in the dialog layout
            View view = inflater.inflate(R.layout.error_dialog, null);
            builder.setView(view);
            errorTextView = view.findViewById(R.id.error_dialog_text);

            if (startTime <= currentTime) {
                errorTextView.setText(getString(R.string.this_time_is_invalid));
            }
            // set startTime to 2500 so that the time picker displays the current time
            startTime = 2500;

            builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DialogFragment StartTimePicker = new StartTimePicker();
                    Bundle args = new Bundle();
                    args.putString("editTaskMessage", editTaskMessage);
                    args.putInt("startTime", startTime);
                    args.putInt("dailyTask", dailyTask);
                    StartTimePicker.setArguments(args);
                    StartTimePicker.setTargetFragment(getTargetFragment(), 22);
                    StartTimePicker.show(getFragmentManager(), "StartTimePicker");
                }
            });

            return builder.create();
        }
    }

    public static class ConfirmationDialogFragment extends DialogFragment {
        public FloatingActionButton doneButton;
        public TextView percentageTextView;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            doneButton = getActivity().findViewById(R.id.done_button);
            percentageTextView = getActivity().findViewById(R.id.percentage_Complete);

            // Pass null as the parent view because it's going in the dialog layout
            View view = inflater.inflate(R.layout.confirmation_dialog, null);
            builder.setView(view);
            // Add action buttons
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_WEEK);
                    int mCurrentHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int mCurrentMinute = calendar.get(Calendar.MINUTE);
                    int mCurrentTime = (mCurrentHour * 100) + mCurrentMinute;
                    // increment to get value of tomorrow
                    if (day != 7) {
                        day += 1;
                    }
                    else {
                        day = 1;
                    }
                    LinearLayout FragmentContainer = getActivity().findViewById(R.id.fragment_container);
                    FragmentContainer.removeAllViews();
                    // Cancel alarms and notifications
                    NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                    notificationManager.cancelAll();
                    for (int i = 0; i != TaskFragment.numberOfTasks; i++) {
                        if (taskStartTimeArray[i] != 2500 && taskStartTimeArray[i] > mCurrentTime) {
                            Intent intent = new Intent(getActivity().getApplicationContext(), TaskAlarmReceiver.class);
                            intent.putExtra("arrayPosition", i);
                            intent.putExtra("id", taskStartTimeArray[i]);
                            intent.putExtra("title", getString(R.string.to_do));
                            intent.putExtra("text", taskStringArray[i]);
                            PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), taskStartTimeArray[i], intent, PendingIntent.FLAG_CANCEL_CURRENT);
                            alarmManager.cancel(alarmIntent);
                        }
                    }
                    // Reset all values
                    TaskFragment.numberOfTasks = 0;
                    TaskFragment.numberOfCompletedTasks = 0;
                    taskStartTimeArray = null;
                    taskStartHourArray = null;
                    taskStartMinuteArray = null;
                    taskStringArray = null;
                    taskCompletionArray = null;
                    dailyTaskArray = null;
                    temporaryStartTimeArray = null;
                    temporaryStartHourArray = null;
                    temporaryStartMinuteArray = null;
                    temporaryStringArray = null;
                    temporaryCompletionArray = null;
                    temporaryDailyTaskArray = null;
                    doneButton.setEnabled(false);
                    done = true;
                    // Load daily tasks and arrays
                    if (TaskFragment.numberOfDailyTasks > 0) {
                        TaskFragment.numberOfTasks = TaskFragment.numberOfDailyTasks;
                        // Create new arrays
                        taskStartTimeArray = new int[TaskFragment.numberOfDailyTasks];
                        taskStartHourArray = new int[TaskFragment.numberOfDailyTasks];
                        taskStartMinuteArray = new int[TaskFragment.numberOfDailyTasks];
                        taskStringArray = new String[TaskFragment.numberOfDailyTasks];
                        taskCompletionArray = new Boolean[TaskFragment.numberOfDailyTasks];
                        dailyTaskArray = new int[TaskFragment.numberOfDailyTasks];
                        // Daily tasks are already ordered
                        SharedPreferences sharedPrefsPersistent = getActivity().getSharedPreferences("dailyValues", 0);
                        int n = 0;
                        for (int i = 0; i != TaskFragment.numberOfDailyTasks; i++) {
                            int taskDay = sharedPrefsPersistent.getInt("dailyTaskArray[" + i + "]", 0);
                            // Checks which type of daily task each task is (weekend, weekday, daily)
                            if ( ((day == 1 || day == 7) && taskDay == 3) || ((day > 1 && day < 7) && taskDay == 2) || (day <= 7 && taskDay == 1) ) {
                                // Initialize array values
                                taskStartTimeArray[n] = sharedPrefsPersistent.getInt("taskStartTimeArray[" + i + "]", 2500);
                                taskStartHourArray[n] = sharedPrefsPersistent.getInt("taskStartHourArray[" + i + "]", 0);
                                taskStartMinuteArray[n] = sharedPrefsPersistent.getInt("taskStartMinuteArray[" + i + "]", 0);
                                taskStringArray[n] = sharedPrefsPersistent.getString("taskStringArray[" + i + "]", null);
                                taskCompletionArray[n] = false;
                                dailyTaskArray[n] = sharedPrefsPersistent.getInt("dailyTaskArray[" + i + "]", 0);
                                // Load fragments
                                Bundle args = new Bundle();
                                args.putBoolean("contentChecker", true);
                                args.putInt("arrayPosition", n);
                                args.putInt("startTime", taskStartTimeArray[n]);
                                args.putInt("startHour", taskStartHourArray[n]);
                                args.putInt("startMinute", taskStartMinuteArray[n]);
                                args.putString("taskTextString", taskStringArray[n]);
                                args.putBoolean("isChecked", false);
                                args.putInt("dailyTask", dailyTaskArray[n]);
                                TaskFragment TaskFragment = new TaskFragment();
                                TaskFragment.setArguments(args);
                                getFragmentManager().beginTransaction()
                                        .add(R.id.fragment_container, TaskFragment).commit();
                                n += 1;
                            }
                            else {
                                if (TaskFragment.numberOfTasks > 0) {
                                    TaskFragment.numberOfTasks -= 1;
                                    temporaryStartHourArray = new int[TaskFragment.numberOfTasks];
                                    temporaryStartMinuteArray = new int[TaskFragment.numberOfTasks];
                                    temporaryStringArray = new String[TaskFragment.numberOfTasks];
                                    temporaryCompletionArray = new Boolean[TaskFragment.numberOfTasks];
                                    temporaryDailyTaskArray = new int[TaskFragment.numberOfTasks];
                                    // Copy data
                                    System.arraycopy(taskStartHourArray, 0, temporaryStartHourArray, 0, TaskFragment.numberOfTasks);
                                    System.arraycopy(taskStartMinuteArray, 0, temporaryStartMinuteArray, 0, TaskFragment.numberOfTasks);
                                    System.arraycopy(taskStringArray, 0, temporaryStringArray, 0, TaskFragment.numberOfTasks);
                                    System.arraycopy(taskCompletionArray, 0, temporaryCompletionArray, 0, TaskFragment.numberOfTasks);
                                    System.arraycopy(dailyTaskArray, 0, temporaryDailyTaskArray, 0, TaskFragment.numberOfTasks);
                                    // Create new arrays
                                    taskStartHourArray = new int[TaskFragment.numberOfTasks];
                                    taskStartMinuteArray = new int[TaskFragment.numberOfTasks];
                                    taskStringArray = new String[TaskFragment.numberOfTasks];
                                    taskCompletionArray = new Boolean[TaskFragment.numberOfTasks];
                                    dailyTaskArray = new int[TaskFragment.numberOfTasks];
                                    // Copy data
                                    System.arraycopy(temporaryStartHourArray, 0, taskStartHourArray, 0, TaskFragment.numberOfTasks);
                                    System.arraycopy(temporaryStartMinuteArray, 0, taskStartMinuteArray, 0, TaskFragment.numberOfTasks);
                                    System.arraycopy(temporaryStringArray, 0, taskStringArray, 0, TaskFragment.numberOfTasks);
                                    System.arraycopy(temporaryCompletionArray, 0, taskCompletionArray, 0, TaskFragment.numberOfTasks);
                                    System.arraycopy(temporaryDailyTaskArray, 0, dailyTaskArray, 0, TaskFragment.numberOfTasks);
                                }
                            }
                        }

                    }
                    percentageComplete = ((MainActivity)getActivity()).calculatePercentage();
                    percentageTextView.setText(percentageComplete + "%");
                    if (percentageComplete == 100) {
                        percentageTextView.setTextColor(getResources().getColor(R.color.colorAccent));
                    }
                    else {
                        percentageTextView.setTextColor(getResources().getColor(R.color.textColorPrimary));
                    }
                    ((MainActivity) getActivity()).getSupportActionBar().setTitle("My List - Tomorrow");
                    // Save values
                    Context context = getActivity();
                    ((MainActivity)getActivity()).saveTaskValues(context);
                }
            })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ConfirmationDialogFragment.this.getDialog().cancel();
                        }
                    });

            return builder.create();
        }
    }

    public void addTask(View view) {
        TaskFragment TaskFragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putBoolean("contentChecker", false);
        TaskFragment.setArguments(args);
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, TaskFragment).commit();
        ScrollView.fullScroll(View.FOCUS_DOWN);
    }

    public void done(View view) {
        if (TaskFragment.numberOfTasks > 0) {
            ConfirmationDialogFragment ConfirmationDialogFragment = new ConfirmationDialogFragment();
            ConfirmationDialogFragment.show(getFragmentManager(), "ConfirmDialogFragment");
        }
    }

    public void assembler(int numberOfTasks, int numberOfCompletedTasks, int numberOfDailyTasks, int percentage, Boolean done_s, int[] taskStartTimeArray_s, int[] taskStartHourArray_s,
                          int[] taskStartMinuteArray_s, String[] taskStringArray_s, Boolean[] taskCompletionArray_s, int[] dailyTaskArray_s) {
        for (int i = 0; i != numberOfTasks; i++) {
            Bundle args = new Bundle();
            args.putBoolean("contentChecker", true);
            args.putInt("arrayPosition", i);
            args.putInt("startTime", taskStartTimeArray_s[i]);
            args.putInt("startHour", taskStartHourArray_s[i]);
            args.putInt("startMinute", taskStartMinuteArray_s[i]);
            args.putBoolean("isChecked", taskCompletionArray_s[i]);
            args.putString("taskTextString", taskStringArray_s[i]);
            args.putInt("dailyTask", dailyTaskArray_s[i]);
            TaskFragment TaskFragment = new TaskFragment();
            TaskFragment.setArguments(args);
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, TaskFragment).commit();
        }

        // Initialize values
        TaskFragment.numberOfTasks = numberOfTasks;
        TaskFragment.numberOfCompletedTasks = numberOfCompletedTasks;
        TaskFragment.numberOfDailyTasks = numberOfDailyTasks;
        percentageComplete = percentage;
        done = done_s;
        taskStartTimeArray = taskStartTimeArray_s;
        taskStartHourArray = taskStartHourArray_s;
        taskStartMinuteArray = taskStartMinuteArray_s;
        taskStringArray = taskStringArray_s;
        taskCompletionArray = taskCompletionArray_s;
        dailyTaskArray = dailyTaskArray_s;
        // Set up MainActivity
        if (TaskFragment.numberOfTasks == 0 || done == true) {
            doneButton.setEnabled(false);
        }
        percentageTextView.setText(percentageComplete + "%");
        if (percentageComplete == 100) {
            percentageTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        }
        else {
            percentageTextView.setTextColor(getResources().getColor(R.color.textColorPrimary));
        }
        if (done) {
            getSupportActionBar().setTitle("My List - Tomorrow");
        }
    }

    // Call this method ONLY after numberOfTasks has been incremented
    public int orderTasks(int startTime) {
        int arrayPosition = 0;

        if (TaskFragment.numberOfTasks > 1) {
            // add startTime value to array and sort
            temporaryStartTimeArray = new int[TaskFragment.numberOfTasks - 1];
            System.arraycopy(taskStartTimeArray, 0, temporaryStartTimeArray, 0, TaskFragment.numberOfTasks - 1);
            taskStartTimeArray = new int[TaskFragment.numberOfTasks];
            System.arraycopy(temporaryStartTimeArray, 0, taskStartTimeArray, 0, TaskFragment.numberOfTasks - 1);
            taskStartTimeArray[TaskFragment.numberOfTasks - 1] = startTime;
            Arrays.sort(taskStartTimeArray);
            //find startTime location
            for (int i = 0; i != TaskFragment.numberOfTasks; i++) {
                if (taskStartTimeArray[i] == startTime) {
                    arrayPosition = i;
                }
            }
        }
        else {
            taskStartTimeArray = new int[TaskFragment.numberOfTasks];
            taskStartTimeArray[0] = startTime;
            arrayPosition = 0;
        }
        return arrayPosition;
    }

    // Call this method after numberOfTasks has been incremented
    public void addTaskValues(int arrayPosition, int startHour, int startMinute, String taskTextString, Boolean isChecked, int dailyTask) {
        if (TaskFragment.numberOfTasks > 1) {
            temporaryStartHourArray = new int[TaskFragment.numberOfTasks - 1];
            temporaryStartMinuteArray = new int[TaskFragment.numberOfTasks - 1];
            temporaryStringArray = new String[TaskFragment.numberOfTasks - 1];
            temporaryCompletionArray = new Boolean[TaskFragment.numberOfTasks - 1];
            temporaryDailyTaskArray = new int[TaskFragment.numberOfTasks - 1];
            // Copy data
            System.arraycopy(taskStartHourArray, 0, temporaryStartHourArray, 0, TaskFragment.numberOfTasks - 1);
            System.arraycopy(taskStartMinuteArray, 0, temporaryStartMinuteArray, 0, TaskFragment.numberOfTasks - 1);
            System.arraycopy(taskStringArray, 0, temporaryStringArray, 0, TaskFragment.numberOfTasks - 1);
            System.arraycopy(taskCompletionArray, 0, temporaryCompletionArray, 0, TaskFragment.numberOfTasks - 1);
            System.arraycopy(dailyTaskArray, 0, temporaryDailyTaskArray, 0, TaskFragment.numberOfTasks - 1);
            // Create new, larger arrays
            taskStartHourArray = new int[TaskFragment.numberOfTasks];
            taskStartMinuteArray = new int[TaskFragment.numberOfTasks];
            taskStringArray = new String[TaskFragment.numberOfTasks];
            taskCompletionArray = new Boolean[TaskFragment.numberOfTasks];
            dailyTaskArray = new int[TaskFragment.numberOfTasks];
            // Set new values
            taskStartHourArray[arrayPosition] = startHour;
            taskStartMinuteArray[arrayPosition] = startMinute;
            taskStringArray[arrayPosition] = taskTextString;
            taskCompletionArray[arrayPosition] = isChecked;
            dailyTaskArray[arrayPosition] = dailyTask;
            // Copy values from temporary arrays to actual arrays
            int n = 0;
            for (int i = 0; i != TaskFragment.numberOfTasks; i++) {
                if (i != arrayPosition) {
                    taskStartHourArray[i] = temporaryStartHourArray[n];
                    taskStartMinuteArray[i] = temporaryStartMinuteArray[n];
                    taskStringArray[i] = temporaryStringArray[n];
                    taskCompletionArray[i] = temporaryCompletionArray[n];
                    dailyTaskArray[i] = temporaryDailyTaskArray[n];
                } else {
                    n -= 1;
                }
                n += 1;
            }
        } else {
            // Create new arrays
            taskStartHourArray = new int[TaskFragment.numberOfTasks];
            taskStartMinuteArray = new int[TaskFragment.numberOfTasks];
            taskStringArray = new String[TaskFragment.numberOfTasks];
            taskCompletionArray = new Boolean[TaskFragment.numberOfTasks];
            dailyTaskArray = new int[TaskFragment.numberOfTasks];
            // Add values
            taskStartHourArray[0] = startHour;
            taskStartMinuteArray[0] = startMinute;
            taskStringArray[0] = taskTextString;
            taskCompletionArray[0] = isChecked;
            dailyTaskArray[0] = dailyTask;
        }
    }

    public void updateValues(int arrayPosition, int startHour, int startMinute, String taskTextString, Boolean isChecked) {
        taskStartHourArray[arrayPosition] = startHour;
        taskStartMinuteArray[arrayPosition] = startMinute;
        taskStringArray[arrayPosition] = taskTextString;
        taskCompletionArray[arrayPosition] = isChecked;
    }

    // Call this method before numberOfTasks has been decremented
    public void deleteTaskValues(int arrayPosition) {
        // Create temporary arrays
        temporaryStartTimeArray = new int[TaskFragment.numberOfTasks];
        temporaryStartHourArray = new int[TaskFragment.numberOfTasks];
        temporaryStartMinuteArray = new int[TaskFragment.numberOfTasks];
        temporaryStringArray = new String[TaskFragment.numberOfTasks];
        temporaryCompletionArray = new Boolean[TaskFragment.numberOfTasks];
        temporaryDailyTaskArray = new int[TaskFragment.numberOfTasks];
        // Copy data
        System.arraycopy(taskStartTimeArray, 0, temporaryStartTimeArray, 0, TaskFragment.numberOfTasks);
        System.arraycopy(taskStartHourArray, 0, temporaryStartHourArray, 0, TaskFragment.numberOfTasks);
        System.arraycopy(taskStartMinuteArray, 0, temporaryStartMinuteArray, 0, TaskFragment.numberOfTasks);
        System.arraycopy(taskStringArray, 0, temporaryStringArray, 0, TaskFragment.numberOfTasks);
        System.arraycopy(taskCompletionArray, 0, temporaryCompletionArray, 0, TaskFragment.numberOfTasks);
        System.arraycopy(dailyTaskArray, 0, temporaryDailyTaskArray, 0, TaskFragment.numberOfTasks);
        // Create new, smaller arrays
        taskStartTimeArray = new int[TaskFragment.numberOfTasks - 1];
        taskStartHourArray = new int[TaskFragment.numberOfTasks - 1];
        taskStartMinuteArray = new int[TaskFragment.numberOfTasks - 1];
        taskStringArray = new String[TaskFragment.numberOfTasks - 1];
        taskCompletionArray = new Boolean[TaskFragment.numberOfTasks - 1];
        dailyTaskArray = new int[TaskFragment.numberOfTasks - 1];
        // Decrements numberOfCompletedTasks if checkbox was checked
        if (temporaryCompletionArray[arrayPosition] == true) {
            TaskFragment.numberOfCompletedTasks -= 1;
        }
        // Copy values from temporary arrays to actual arrays
        int n = 0;
        for (int i = 0; i != TaskFragment.numberOfTasks; i++) {
            if (i != arrayPosition) {
                taskStartTimeArray[n] = temporaryStartTimeArray[i];
                taskStartHourArray[n] = temporaryStartHourArray[i];
                taskStartMinuteArray[n] = temporaryStartMinuteArray[i];
                taskStringArray[n] = temporaryStringArray[i];
                taskCompletionArray[n] = temporaryCompletionArray[i];
                dailyTaskArray[n] = temporaryDailyTaskArray[i];
            }
            else {
                n -= 1;
            }
            n += 1;
        }
    }

    public int calculatePercentage() {
        double numberOfTasks_double = TaskFragment.numberOfTasks;
        double numberOfCompletedTasks_double = TaskFragment.numberOfCompletedTasks;
        double checkmarkPercent_double;
        checkmarkPercent_double = numberOfCompletedTasks_double/numberOfTasks_double * 100;
        int checkmarkPercent_int = (int)checkmarkPercent_double;

        return checkmarkPercent_int;
    }

    public void saveTaskValues(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("values", 0);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        SharedPreferences sharedPrefsPersistent = context.getSharedPreferences("dailyValues", 0);
        SharedPreferences.Editor persistentEditor = sharedPrefsPersistent.edit();
        // Makes room for new values
        editor.clear().apply();
        // Save values
        editor.putInt("numberOfTasks", TaskFragment.numberOfTasks);
        editor.putInt("numberOfCompletedTasks", TaskFragment.numberOfCompletedTasks);
        persistentEditor.putInt("numberOfDailyTasks", TaskFragment.numberOfDailyTasks);
        editor.putInt("percentageComplete", percentageComplete);
        editor.putBoolean("done", done);
        for (int i = 0; i != TaskFragment.numberOfTasks; i++) {
            editor.putInt("taskStartTimeArray[" + i + "]", taskStartTimeArray[i]);
            editor.putInt("taskStartHourArray[" + i + "]", taskStartHourArray[i]);
            editor.putInt("taskStartMinuteArray[" + i + "]", taskStartMinuteArray[i]);
            editor.putString("taskStringArray[" + i + "]", taskStringArray[i]);
            editor.putBoolean("taskCompletionArray[" + i + "]", taskCompletionArray[i]);
            editor.putInt("dailyTaskArray[" + i + "]", dailyTaskArray[i]);
        }
        editor.apply();
        persistentEditor.apply();
    }

    public void refreshActivityDisplay(int[] taskStartTimeArray_s, int[] taskStartHourArray_s, int[] taskStartMinuteArray_s, String[] taskStringArray_s, Boolean[] taskCompletionArray_s, int[] dailyTaskArray_s) {
        // Remove all tasks
        LinearLayout FragmentContainer = findViewById(R.id.fragment_container);
        FragmentContainer.removeAllViews();
        // Load new tasks
        for (int i = 0; i != TaskFragment.numberOfTasks; i++) {
            Bundle args = new Bundle();
            args.putBoolean("contentChecker", true);
            args.putInt("arrayPosition", i);
            args.putInt("startTime", taskStartTimeArray_s[i]);
            args.putInt("startHour", taskStartHourArray_s[i]);
            args.putInt("startMinute", taskStartMinuteArray_s[i]);
            args.putBoolean("isChecked", taskCompletionArray_s[i]);
            args.putString("taskTextString", taskStringArray_s[i]);
            args.putInt("dailyTask", dailyTaskArray_s[i]);
            TaskFragment TaskFragment = new TaskFragment();
            TaskFragment.setArguments(args);
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, TaskFragment).commit();
        }
    }

    public PendingIntent scheduleNotificationAlarm(int arrayPosition, int startTime, int startHour, int startMinute, String title, String text) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
        intent.putExtra("arrayPosition", arrayPosition);
        intent.putExtra("id", startTime);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, startTime, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        // Set the alarm start time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, startMinute);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        return alarmIntent;
    }

    public void updateNotifications(int startTime) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Cancel other alarms in order to update arrayPosition
        if (!done && startTime != 2500) {
            Calendar calendar = Calendar.getInstance();
            int mCurrentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int mCurrentMinute = calendar.get(Calendar.MINUTE);
            int mCurrentTime = (mCurrentHour * 100) + mCurrentMinute;

            for (int i = 0; i != TaskFragment.numberOfTasks; i++) {
                // Make sure only task alarms are updated
                if (taskStartTimeArray[i] != 2500 && taskStartTimeArray[i] < mCurrentTime) {
                }
            }
        }
        // Update alarms/notifications to ensure that each notification receives the proper arrayPosition (for setting checkbox and deleting task)
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Calendar calendar = Calendar.getInstance();
        int mCurrentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mCurrentMinute = calendar.get(Calendar.MINUTE);
        int mCurrentTime = (mCurrentHour * 100) + mCurrentMinute;
        StatusBarNotification[] notifications = new StatusBarNotification[TaskFragment.numberOfTasks];

        for (int i = 0; i != TaskFragment.numberOfTasks; i++) {
            if (taskStartTimeArray[i] != 2500) {
                // Update notification
                if (taskStartTimeArray[i] <= mCurrentTime) {
                    // Get active notifications
                    // See if current iteration is on the list
                    showNotification(taskStartTimeArray[i], i, getString(R.string.to_do), taskStringArray[i]);
                }
                // Update alarm
                else {
                    scheduleNotificationAlarm(i, taskStartTimeArray[i], taskStartHourArray[i], taskStartMinuteArray[i], getString(R.string.to_do), taskStringArray[i]);
                }
            }
        }
    }

    public void showNotification(int id, int arrayPosition, String title, String text) {
        // Create intent to launch MainActivity when notification is clicked
        Intent tapAction = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingTapAction = PendingIntent.getActivity(getApplicationContext(), id, tapAction, 0);
        // Create intent to respond to done button
        Intent doneIntent = new Intent(getApplicationContext(), DoneNotificationReceiver.class);
        doneIntent.putExtra("arrayPosition", arrayPosition);
        doneIntent.putExtra("id", id);
        doneIntent.putExtra("text", text);
        PendingIntent donePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), id, doneIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        // Create intent to respond to delete button
        Intent deleteIntent = new Intent(getApplicationContext(), DeleteNotificationReceiver.class);
        deleteIntent.putExtra("arrayPosition", arrayPosition);
        deleteIntent.putExtra("id", id);
        deleteIntent.putExtra("text", text);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), id, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        // Create the notification and set appearance
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_1")
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setColor(Color.rgb(33, 150, 243))
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingTapAction)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_notification_icon, getString(R.string.done), donePendingIntent)
                .addAction(R.drawable.ic_delete_icon, getString(R.string.delete), deletePendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_1_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            // Each channel allows for user control over notifications(importance, sound, etc) through the system settings
            NotificationChannel channel = new NotificationChannel("channel_1", name, importance);
            channel.setDescription(getString(R.string.channel_1_description));
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        // Show the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }

}
