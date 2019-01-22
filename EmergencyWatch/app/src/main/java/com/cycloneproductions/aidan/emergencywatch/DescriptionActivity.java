package com.cycloneproductions.aidan.emergencywatch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

import static com.cycloneproductions.aidan.emergencywatch.HomeActivity.EXTRA_DESCRIPTION;
import static com.cycloneproductions.aidan.emergencywatch.HomeActivity.EXTRA_EVENT;
import static com.cycloneproductions.aidan.emergencywatch.HomeActivity.EXTRA_EVENTLIST;
import static com.cycloneproductions.aidan.emergencywatch.HomeActivity.EXTRA_LOCATION;
import static com.cycloneproductions.aidan.emergencywatch.HomeActivity.EXTRA_TIME;

public class DescriptionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawer;
    private ArrayList<EventItem> mEventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEventList = new ArrayList<>();
        mEventList = (ArrayList<EventItem>) getIntent().getSerializableExtra(EXTRA_EVENTLIST);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Intent intent = getIntent();
        String event = intent.getStringExtra(EXTRA_EVENT);
        String location = intent.getStringExtra(EXTRA_LOCATION);
        String time = intent.getStringExtra(EXTRA_TIME);
        String description = intent.getStringExtra(EXTRA_DESCRIPTION);

        TextView textViewEvent = findViewById(R.id.text_view_event_description);
        TextView textViewLocation = findViewById(R.id.text_view_location_description);
        TextView textViewTime = findViewById(R.id.text_view_time_description);
        TextView textViewDescription = findViewById(R.id.text_view_desciption_description);

        textViewEvent.setText(event);
        textViewLocation.setText(location);
        textViewTime.setText(time);
        textViewDescription.setText(description);
    }
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_list_of_events:
                Intent listIntent = new Intent(DescriptionActivity.this, HomeActivity.class);
                startActivity(listIntent);
                break;

            case R.id.nav_map_of_events:
                Intent mapIntent = new Intent(DescriptionActivity.this, MapActivity.class);
                mapIntent.putExtra(EXTRA_EVENTLIST, mEventList);
                startActivity(mapIntent);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
