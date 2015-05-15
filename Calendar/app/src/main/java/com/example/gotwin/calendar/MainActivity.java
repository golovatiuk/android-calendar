package com.example.gotwin.calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;


public class MainActivity extends Activity implements View.OnClickListener {

    public static String eventTitle;
    public static String eventDescription;
    public static Calendar eventDate = new GregorianCalendar();
    private static int count = 0;

    private CallbackManager callbackManager;
    private GraphRequest myFriendsRequest;

    private long currDate;

    private ArrayList<String> friendsNames;
    private ArrayList<Long> friendsBirthdayDates;

    public static HashMap<Long, ArrayList<Object>> eventsInfo;
    public static ArrayList<String> events;
    public static ArrayList<Long> dates;
    public static ArrayList<String> contacts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        callbackManager = CallbackManager.Factory.create();

        final LoginButton loginButton = (LoginButton) findViewById(R.id.loginButton);
        loginButton.setReadPermissions(Collections.singletonList("user_friends"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Toast.makeText(getApplicationContext(),
                        "Login success",
                        Toast.LENGTH_SHORT).show();

                myFriendsRequest = GraphRequest.
                        newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                                new GraphRequest.GraphJSONArrayCallback() {
                                    @Override
                                    public void onCompleted(JSONArray objects,
                                                            GraphResponse response) {

                                        Log.d("---  request success", "" + objects.toString());

                                        getFriendsBirthdays(objects);
                                        callFacebookDialog();

                                    }
                                });
                Bundle bundle = new Bundle();
                bundle.putString("fields", "name, birthday");
                myFriendsRequest.setParameters(bundle);
                myFriendsRequest.executeAsync();

                Log.d("----  login sucsess", "" + friendsNames);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });

        initializeCalendar();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                switch (data.getStringExtra("result")) {
                    case "Event created":

                        createEvent();

                        Toast.makeText(getApplicationContext(), "Event created:\n " +
                                eventTitle + "\n" + eventDescription, Toast.LENGTH_LONG).show();

                        break;

                    case "Event removed":

                        removeEvent();

                        Toast.makeText(getApplicationContext(), "Event removed:\n " +
                                eventTitle + "\n" + eventDescription, Toast.LENGTH_LONG).show();

                        break;
                }
            }
        }
    }

    public void initializeCalendar() {

        final CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);

        currDate = calendarView.getDate();

        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view,
                                            int year, int month, int dayOfMonth) {

                if (calendarView.getDate() != currDate) {

                    currDate = calendarView.getDate();
                    eventDate.set(year, month, dayOfMonth);
                    callNotDialog();
                }
            }
        });
    }

    public void callNotDialog() {

        RelativeLayout view = (RelativeLayout)
                getLayoutInflater().inflate(R.layout.dialog_activity, null);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        final EditText etTitle = (EditText) view.findViewById(R.id.etTitle);
        final EditText etDescription = (EditText) view.findViewById(R.id.etDescription);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        final CheckBox cbIsInternalNotifi = (CheckBox) view.findViewById(R.id.checkBox);

        cbIsInternalNotifi.setChecked(true);

        timePicker.setIs24HourView(true);

//        alertDialog.setTitle("Event");
        alertDialog.setCancelable(false);
        alertDialog.setView(view);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                eventDescription = etDescription.getText().toString();
                eventTitle = etTitle.getText().toString();

                eventDate.set(Calendar.HOUR, timePicker.getCurrentHour());
                eventDate.set(Calendar.MINUTE, timePicker.getCurrentMinute());


                if (cbIsInternalNotifi.isChecked()) {
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

    private void callFacebookDialog() {

        final LinearLayout fBDView = (LinearLayout)
                getLayoutInflater().inflate(R.layout.facebook_dialog, null);

        final CheckBox fBCBox = (CheckBox) fBDView.findViewById(R.id.facebookCB);
        final TextView FBFriendName = (TextView) fBDView.findViewById(R.id.FBFriendName);

        if (count < friendsBirthdayDates.size()) {

            eventTitle = "BIRTHDAY";
            eventDescription = "Birthday of " + friendsNames.get(count);
            eventDate.setTimeInMillis(friendsBirthdayDates.get(count));

            FBFriendName.setText(eventDescription);

            count++;

            final AlertDialog.Builder facebookDialog = new AlertDialog.Builder(this);

            facebookDialog.setTitle("Create FB Friend's Birthdays Notifications");
            facebookDialog.setView(fBDView);
            facebookDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    createEvent();

                    if (fBCBox.isChecked()) {
                        insertEvent();
                    }

                    callFacebookDialog();

                    dialog.cancel();
                }
            });
            facebookDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.cancel();
                }
            });

            facebookDialog.create().show();
        }
    }

    protected void createEvent() {

        PendingIntent pendingIntent = createEventPIntent(getApplicationContext(),
                eventTitle,
                eventDescription,
                eventDate);

        AlarmManager alarmManager = (AlarmManager)
                getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC,
                eventDate.getTimeInMillis(),
                pendingIntent);
    }

    public void removeEvent() {

        PendingIntent pendingIntent = createEventPIntent(getApplicationContext(),
                eventTitle,
                eventDescription,
                eventDate);

        AlarmManager alarmManager = (AlarmManager)
                getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
    }

    public void insertEvent() {

        Intent intent = new Intent(Intent.ACTION_INSERT);

        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, eventTitle);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, eventDescription);
        intent.putExtra(CalendarContract.Events.DTSTART,
                eventDate.getTimeInMillis());
        intent.putExtra(CalendarContract.Events.DTEND,
                eventDate.getTimeInMillis());
        intent.putExtra(CalendarContract.Events.ALL_DAY, "false");
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "EVENT LOCATION");

        startActivity(intent);

    }

    private void getFriendsBirthdays(JSONArray objects) {

        friendsNames = new ArrayList<>();
        friendsBirthdayDates = new ArrayList<>();

        String date;
        String name;

        GregorianCalendar calendar = new GregorianCalendar();

        for (int i = 0; i < objects.length(); i++) {

            try {

                date = objects.getJSONObject(i).getString("birthday");
                name = objects.getJSONObject(i).getString("name");

                calendar.set(Calendar.DAY_OF_MONTH,
                        Integer.parseInt(date.split("/")[0]));

                calendar.set(Calendar.MONTH,
                        Integer.parseInt(date.split("/")[1]));

                if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
                    calendar.set(Calendar.YEAR, +1);
                }

                friendsBirthdayDates.add(calendar.getTimeInMillis());
                friendsNames.add(name);

            } catch (JSONException e) {

                e.printStackTrace();

            }
        }
    }

    public void readEvents() {

        long calId = 0;

        events = new ArrayList<>();
        dates = new ArrayList<>();
        contacts = new ArrayList<>();
        eventsInfo = new HashMap<>();

        final String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART };

        final String[] CALENDAR_PROJECTION = new String[] {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.OWNER_ACCOUNT };

        ContentResolver contentResolver = getContentResolver();
        Uri eventsUri = CalendarContract.Events.CONTENT_URI;
        Uri calendarsUri = CalendarContract.Calendars.CONTENT_URI;

        Cursor calendarsCursor = contentResolver.query(calendarsUri,
                CALENDAR_PROJECTION, null, null, null);

        Cursor eventsCursor = contentResolver.query(eventsUri,
                EVENT_PROJECTION, null, null, null);


        while (calendarsCursor.moveToNext()) {

            if (calendarsCursor.getString(1).equals("#contacts@group.v.calendar.google.com")) {

                calId = calendarsCursor.getLong(0);
            }
        }

        while (eventsCursor.moveToNext()) {

            if (eventsCursor.getLong(0) == calId) {

                eventsInfo.put(eventsCursor.getLong(1),
                        new ArrayList<Object>(
                                Arrays.asList(
                                        eventsCursor.getLong(1),
                                        eventsCursor.getString(2),
                                        eventsCursor.getString(3),
                                        eventsCursor.getLong(4)
                                )
                        ));

                contacts.add(eventsCursor.getString(2));
                events.add(eventsCursor.getString(1) + " " + eventsCursor.getString(2));
                dates.add(eventsCursor.getLong(4));
            }
        }

        eventsCursor.close();
        calendarsCursor.close();

    }

    public static PendingIntent createEventPIntent(Context context, String title,
                                                   String description, Calendar date) {

        Intent intent = new Intent(context, MyReceiver.class);
        intent.putExtra("Title", title);
        intent.putExtra("Message", description);
        intent.putExtra("Date", date.getTimeInMillis());

        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public void onClick(View v) {

        readEvents();

        startActivityForResult(new Intent(this, ListOfEvents.class), 1);
    }

    protected void onDestroy() {
        super.onDestroy();

        LoginManager.getInstance().logOut();

    }
}
