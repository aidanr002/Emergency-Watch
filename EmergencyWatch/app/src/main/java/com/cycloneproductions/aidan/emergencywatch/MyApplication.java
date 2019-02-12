package com.cycloneproductions.aidan.emergencywatch;

import android.app.Application;

public class MyApplication extends Application {
    private Object globalDisclaimer; //make getter and setter
    private static boolean disclaimerGiven = false;

    public static boolean getDisclaimerGiven()
    {
        return disclaimerGiven;
    }

    public static void setDisclaimer(boolean state) {
        disclaimerGiven = state;
    }

}
