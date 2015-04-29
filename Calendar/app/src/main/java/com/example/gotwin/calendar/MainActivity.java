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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.GregorianCalendar;


public class MainActivity extends Activity {

    private static String eventTitle;
    private static String eventDescription;
    private static GregorianCalendar eventDate = new GregorianCalendar();
    private static int count = 0;

    private CallbackManager callbackManager;
    private GraphRequest myFriendsRequest;

    private long currDate;

    private ArrayList<String> friendsNames;
    private ArrayList<Long> friendsBirthdayDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        callbackManager = CallbackManager.Factory.create();

        final LoginButton loginButton = (LoginButton) findViewById(R.id.loginButton);
        loginButton.setReadPermissions(Arrays.asList("user_friends"));

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

                                        getFriendsBirthdays(objects);
                                        callFacebookDialog();

                                    }
                                });
                Bundle bundle = new Bundle();
                bundle.putString("fields", "name,birthday");
                myFriendsRequest.setParameters(bundle);
                myFriendsRequest.executeAsync();

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

                }

                callNotDialog();
            }
        });
    }

    public void callNotDialog() {

        LinearLayout view = (LinearLayout)
                getLayoutInflater().inflate(R.layout.dialog_activity, null);

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
                eventDate.getTimeInMillis(),
                pendingIntent);
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

    protected void onDestroy() {
        super.onDestroy();

        LoginManager.getInstance().logOut();

    }
}
