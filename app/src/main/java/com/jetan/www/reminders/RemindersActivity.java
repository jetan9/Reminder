package com.jetan.www.reminders;

import android.app.Dialog;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class RemindersActivity extends AppCompatActivity {
    private ListView mListView;
    private RemindersDbAdapter dbAdapter;
    private RemindersSimpleCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        mListView = (ListView) findViewById(R.id.reminders_list_view);
        mListView.setDivider(null);
        dbAdapter = new RemindersDbAdapter(this);
        dbAdapter.open();
        if (savedInstanceState == null) {
            dbAdapter.deleteAllReminders();
            insertSomeReminders();
        }

        Cursor cursor = dbAdapter.fetchAllReminders();

        String[] from = new String[]{RemindersDbAdapter.COL_CONTENT};
        int[] to = new int[]{R.id.row_text};

        cursorAdapter = new RemindersSimpleCursorAdapter(RemindersActivity.this, R.layout.reminders_row, cursor, from, to, 0);

        mListView.setAdapter(cursorAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RemindersActivity.this);
                ListView modelListView = new ListView(RemindersActivity.this);
                String[] models = new String[]{"Edit Reminder", "Delete Reminder"};
                ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(RemindersActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, models);
                modelListView.setAdapter(modelAdapter);
                builder.setView(modelListView);
                final Dialog dialog = builder.create();
                dialog.show();
                modelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            Toast.makeText(RemindersActivity.this, "edit " + masterListPosition, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RemindersActivity.this, "delete " + masterListPosition, Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void insertSomeReminders() {
        for (int i = 0; i < 5; i++) {
            dbAdapter.createReminder("Buy Learn Android Studio", true);
            dbAdapter.createReminder("Send Dad birthday gift", false);
            dbAdapter.createReminder("查询词典", false);
        }
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
                Log.d(getLocalClassName(), "create new Reminder");
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                return false;
        }
    }
}
