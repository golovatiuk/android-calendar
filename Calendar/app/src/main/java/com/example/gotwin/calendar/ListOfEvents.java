package com.example.gotwin.calendar;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.HashSet;


public class ListOfEvents extends ListActivity {

    private TextView eventInfo;

    private static String[] eventsArr;
    private static HashSet<String> contactOfEvent;
    private static String[] menuItems = {
            "Manage Menu Item 1", "Manage Menu Item 2",
            "Manage Menu Item 3", "Manage Menu Item 4",
            "BACK"};

    private ArrayAdapter<String> eventAdapter;
    private ArrayAdapter<String> menuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item);

        eventsArr = new String[MainActivity.events.size()];
        contactOfEvent = new HashSet<>();

        for (int i = 0; i < MainActivity.events.size(); i++) {

            eventsArr[i] = MainActivity.events.get(i) +
                    "\n" + new Date(MainActivity.dates.get(i));
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
                case "Manage Menu Item 1":
                    /// do smth
                    Toast.makeText(this, "Manage Menu Item 1", Toast.LENGTH_SHORT).show();
                    break;
                case "Manage Menu Item 2":
                    Toast.makeText(this, "Manage Menu Item 2", Toast.LENGTH_SHORT).show();
                    /// do smth
                    break;
                case "Manage Menu Item 3":
                    Toast.makeText(this, "Manage Menu Item 3", Toast.LENGTH_SHORT).show();
                    /// do smth
                    break;
                case "Manage Menu Item 4":
                    Toast.makeText(this, "Manage Menu Item 4", Toast.LENGTH_SHORT).show();
                    /// do smth
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
}
