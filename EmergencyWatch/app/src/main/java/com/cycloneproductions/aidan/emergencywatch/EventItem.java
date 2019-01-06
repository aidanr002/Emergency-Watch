package com.cycloneproductions.aidan.emergencywatch;

public class EventItem {
    private String mEvent;
    private String mLocation;
    private String mTime;

    public EventItem(String event, String location, String time) {
        mEvent = event;
        mLocation = location;
        mTime = time;
    }

    public String getEvent() {
        return mEvent;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getTime() {
        return mTime;
    }
}
