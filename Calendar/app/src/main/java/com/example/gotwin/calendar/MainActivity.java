package com.example.gotwin.calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class MainActivity extends Activity {

    private static String eventTitle;
    private static String eventDescription;
    private static GregorianCalendar eventDate = new GregorianCalendar();

    private TextView textView1;
    private TextView textView2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView1 = (TextView) findViewById(R.id.textDate);
        textView2 = (TextView) findViewById(R.id.textView);

        initializeCalendar();


    }

    public void initializeCalendar() {

        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);

        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view,
                                            int year, int month, int dayOfMonth) {

                eventDate.set(year, month, dayOfMonth);

                callNotDialog();
            }
        });
    }

    public void callNotDialog() {

        LinearLayout view = (LinearLayout)
                getLayoutInflater().inflate(R.layout.dialog_activity, null);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy, k:m");

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        final EditText etTitle = (EditText) view.findViewById(R.id.etTitle);
        final EditText etDescription = (EditText) view.findViewById(R.id.etDescription);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        final CheckBox cbIsInternalNotifi = (CheckBox) view.findViewById(R.id.checkBox);

        cbIsInternalNotifi.setChecked(true);

        timePicker.setIs24HourView(true);

        alertDialog.setTitle("Event");
        alertDialog.setCancelable(false);
        alertDialog.setView(view);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                eventDescription = etDescription.getText().toString();
                eventTitle = etTitle.getText().toString();
                textView2.setText(eventTitle + "\n" + eventDescription);

                eventDate.set(Calendar.HOUR, timePicker.getCurrentHour());
                eventDate.set(Calendar.MINUTE, timePicker.getCurrentMinute());

                textView1.setText(simpleDateFormat.format(eventDate.getTime()));

                if (cbIsInternalNotifi.isChecked()){
                    createEvent();
                } else {
                    insertEvent();
                }

                dialog.cancel();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.create();
        alertDialog.show();
    }

    public void createEvent() {

        Intent intent = new Intent(this, MyReceiver.class);
        intent.putExtra("Title", eventTitle);
        intent.putExtra("Message", eventDescription);
        intent.putExtra("Date", eventDate.getTimeInMillis());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)
                getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC,
                eventDate.getTimeInMillis() - 5 * 60 * 1000, pendingIntent);
    }

    public void insertEvent() {

        Intent intent = new Intent(Intent.ACTION_INSERT);

        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, eventTitle);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, eventDescription);
        intent.putExtra(CalendarContract.Events.DTSTART,
                eventDate.getTimeInMillis() - 5 * 60 * 1000);
        intent.putExtra(CalendarContract.Events.DTEND,
                eventDate.getTimeInMillis() + 5 * 60 * 1000);
        intent.putExtra(CalendarContract.Events.ALL_DAY, "false");
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "EVENT LOCATION");

        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
