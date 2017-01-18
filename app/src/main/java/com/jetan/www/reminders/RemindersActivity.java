package com.jetan.www.reminders;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RemindersActivity extends AppCompatActivity {
    private ListView mListView;
    private RemindersDbAdapter dbAdapter;
    private RemindersSimpleCursorAdapter cursorAdapter;

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        mListView = (ListView)findViewById(R.id.reminders_list_view);
        mListView.setDivider(null);
        dbAdapter = new RemindersDbAdapter(this);
        dbAdapter.open();
        if (savedInstanceState == null) {
            dbAdapter.deleteAllReminders();
        }

        final Cursor cursor = dbAdapter.fetchAllReminders();

        String[] from = new String[]{RemindersDbAdapter.COL_CONTENT};
        int[] to = new int[]{R.id.row_text};

        cursorAdapter = new RemindersSimpleCursorAdapter(RemindersActivity.this, R.layout.reminders_row, cursor, from, to, 0);

        mListView.setAdapter(cursorAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RemindersActivity.this);
                ListView modeListView = new ListView(RemindersActivity.this);
                String[] modes = new String[]{"Edit Reminder", "Delete Reminder", "Schedule Reminder"};
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(RemindersActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int nId = getIdFromPosition(masterListPosition);
                        if (position == 0) {
                            Reminder reminder = dbAdapter.fetchReminderById(nId);
                            fireCustomDialog(reminder);
                        } else if (position == 1) {
                            dbAdapter.deleteReminderById(nId);
                            cursorAdapter.changeCursor(dbAdapter.fetchAllReminders());
                        } else {
                            final Reminder reminder = dbAdapter.fetchReminderById(nId);
                            Calendar today = Calendar.getInstance();
                            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hour, int minute) {
                                    Calendar alarmTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                                    alarmTime.set(Calendar.HOUR, hour);
                                    alarmTime.set(Calendar.MINUTE, minute);
                                    scheduleReminder(alarmTime.getTimeInMillis(), reminder.getContent());
                                }
                            };
                            new TimePickerDialog(RemindersActivity.this, listener, today.get(Calendar.HOUR), today.get(Calendar.MINUTE), false).show();
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.cam_menu, menu);
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {}

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_item_delete_reminder:
                            for (int c = cursorAdapter.getCount() - 1; c >= 0; c--) {
                                if (mListView.isItemChecked(c)) {
                                    dbAdapter.deleteReminderById(getIdFromPosition(c));
                                }
                            }
                            mode.finish();
                            cursorAdapter.changeCursor(dbAdapter.fetchAllReminders());
                            return true;
                    }
                    return false;
                }

                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {}
            });
        }
    }

    private void scheduleReminder(long time, String content) {
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, ReminderAlarmReceiver.class);
        alarmIntent.putExtra(ReminderAlarmReceiver.REMINDER_TEXT, content);
        PendingIntent broadcast = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, broadcast);
    }

    private int getIdFromPosition(int c) {
        return (int)cursorAdapter.getItemId(c);
    }

    @SuppressWarnings("deprecation")
    private void fireCustomDialog(final Reminder reminder) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);

        TextView titleView = (TextView)dialog.findViewById(R.id.custom_title);
        final EditText editCustom = (EditText)dialog.findViewById(R.id.custom_edit_reminder);
        Button commitButton = (Button)dialog.findViewById(R.id.custom_button_commit);
        final CheckBox checkBox = (CheckBox)dialog.findViewById(R.id.custom_check_box);
        LinearLayout rootLayout = (LinearLayout)dialog.findViewById(R.id.custom_root_layout);
        final boolean isEditOperation = (reminder != null);

        if (isEditOperation) {
            titleView.setText("Edit Reminder");
            checkBox.setChecked(reminder.getImportant() == 1);
            editCustom.setText(reminder.getContent());
            rootLayout.setBackgroundColor(getResources().getColor(R.color.blue));
        }

        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reminderText = editCustom.getText().toString();
                if (isEditOperation) {
                    Reminder reminderEdited = new Reminder(reminder.getId(),
                            reminderText, checkBox.isChecked() ? 1: 0);
                    dbAdapter.updateReminder(reminderEdited);
                } else {
                    dbAdapter.createReminder(reminderText, checkBox.isChecked());
                }
                cursorAdapter.changeCursor(dbAdapter.fetchAllReminders());
                dialog.dismiss();
            }
        });

        Button buttonCancel = (Button)dialog.findViewById(R.id.custom_button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_reminders, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                fireCustomDialog(null);
                return true;
            case R.id.action_exit:
                finish();
                return true;
            case R.id.action_about:
                fireAboutDialog();
                return true;
            default:
                return false;
        }
    }

    private void fireAboutDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_about);
        dialog.show();
    }
}
