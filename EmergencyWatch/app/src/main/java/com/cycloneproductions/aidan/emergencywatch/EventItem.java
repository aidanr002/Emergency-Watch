package com.cycloneproductions.aidan.emergencywatch;

import java.io.Serializable;

public class EventItem implements Serializable {
    private String mEvent;
    private String mLocation;
    private String mTime;
    private String mDescription;

    public EventItem(String event, String location, String time, String description) {
        mEvent = event;
        mLocation = location;
        mTime = time;
        mDescription = description;
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

    public String getDescription() {return mDescription; }

    public String toString(){
        return "\n" + "Event: " + mEvent + "\n" + "Location:  " + mLocation + "\n" + "Time: " + mTime  + "\n" + "Description: " + mDescription;
    }
}
