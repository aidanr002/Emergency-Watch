package com.cycloneproductions.aidan.emergencywatch;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: Oncreate is starting for HomeActivity");

        if (isServicesOk()) {
            Log.d(TAG, "onCreate: Services are ok, initialising map activity");
            init();
        }
    }

    private void init() {
        Log.d(TAG, "init: Initialising onclicklistener for events map button from home activity");

        Button buttontomap = (Button) findViewById(R.id.mapButton);
        buttontomap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked button to map");

                Intent intent = new Intent(HomeActivity.this, MapActivity.class);
                startActivity(intent);
                Log.d(TAG, "onClick: MapActivity should have/be starting");
            }
        });
    }

    public void menuButtonClicked(View view) {
        TextView menuButton = (TextView) findViewById(R.id.menuButton);
        menuButton.setText("Pressed");
    }

    public boolean isServicesOk() {
        Log.d(TAG, "isServicesOk: Checking Google Services Version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(HomeActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //Everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOk: Google play services is working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //An error occured but it can be resolved
            Log.d(TAG, "isServicesOk: An error occured but it can be resolved");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(HomeActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
