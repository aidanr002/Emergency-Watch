package com.cycloneproductions.aidan.emergencywatch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import static com.cycloneproductions.aidan.emergencywatch.HomeActivity.EXTRA_EVENT;
import static com.cycloneproductions.aidan.emergencywatch.HomeActivity.EXTRA_LOCATION;
import static com.cycloneproductions.aidan.emergencywatch.HomeActivity.EXTRA_TIME;

public class DescriptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        Intent intent = getIntent();
        String event = intent.getStringExtra(EXTRA_EVENT);
        String location = intent.getStringExtra(EXTRA_LOCATION);
        String time = intent.getStringExtra(EXTRA_TIME);

        TextView textViewEvent = findViewById(R.id.text_view_event_description);
        TextView textViewLocation = findViewById(R.id.text_view_location_description);
        TextView textViewTime = findViewById(R.id.text_view_time_description);

        textViewEvent.setText(event);
        textViewLocation.setText(location);
        textViewTime.setText(time);
    }
}
