package com.example.gotwin.calendar;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;


public class ListOfEvents extends ListActivity {

    private TextView eventInfo;

    private static String[] menuItems = {
            "Create notification", "Remove notification",
            "BACK"};

    private ArrayAdapter<String> eventAdapter;
    private ArrayAdapter<String> menuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item);

        String[] eventsArr = new String[MainActivity.events.size()];
        HashSet<String> contactOfEvent = new HashSet<>();

        for (int i = 0; i < MainActivity.events.size(); i++) {

            eventsArr[i] = MainActivity.events.get(i) +
                    ". \n" + new Date(MainActivity.dates.get(i));
            contactOfEvent.add(MainActivity.contacts.get(i));
        }

        Log.d("##### -- contacts", "" + contactOfEvent);

        eventInfo = (TextView) findViewById(R.id.eventInfo);

        eventAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1,
                eventsArr);

        menuAdapter  = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1,
                menuItems);

        setListAdapter(eventAdapter);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (getListAdapter() == eventAdapter){
            eventInfo.setText((String)getListView().getItemAtPosition(position));
            setListAdapter(menuAdapter);
        } else {
            switch ((String)getListView().getItemAtPosition(position)) {
                case "Create notification":

                    addEvent(Long.parseLong(eventInfo.getText().toString().split(". ")[0]));

                    break;

                case "Remove notification":

                    dellEvent(Long.parseLong(eventInfo.getText().toString().split(". ")[0]));

                    break;

                case "BACK":
                    setListAdapter(eventAdapter);
                    eventInfo.setText("");
                    break;

                default:
                    break;
            }
        }
    }

    private void addEvent(long id) {

        onEventClick(id);

        setResult(RESULT_OK, new Intent().putExtra("result", "Event created"));
        finish();
    }

    private void dellEvent(long id) {

        onEventClick(id);

        setResult(RESULT_OK, new Intent().putExtra("result", "Event removed"));

        finish();
    }

    private void onEventClick (long id) {

        Calendar date = new GregorianCalendar();
        date.setTimeInMillis((Long)(MainActivity.eventsInfo.get(id)).get(3));

        MainActivity.eventTitle = (String)(MainActivity.eventsInfo.get(id)).get(1);
        MainActivity.eventDescription = (String)(MainActivity.eventsInfo.get(id)).get(2);

        MainActivity.eventDate.set(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH));
    }
}
