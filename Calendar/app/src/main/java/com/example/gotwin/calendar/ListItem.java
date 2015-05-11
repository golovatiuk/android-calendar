package com.example.gotwin.calendar;

import java.util.HashMap;


public class ListItem extends HashMap<String, String> {

    public static final String EVENT_TITLE = "title";
    public static final String DATE_OF_EVENT = "date";

    public ListItem(String title, String date){
        super();
        super.put(EVENT_TITLE, title);
        super.put(DATE_OF_EVENT, date);
    }
}
