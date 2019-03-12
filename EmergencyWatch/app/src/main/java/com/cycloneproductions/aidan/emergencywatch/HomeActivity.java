package com.cycloneproductions.aidan.emergencywatch;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.Serializable;
import java.util.ArrayList;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, EventAdapter.OnItemClickListener, Serializable {
    public static final String EXTRA_EVENT = "event";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_TIME = "time";
    public static final String EXTRA_DESCRIPTION = "description";
    public static final String EXTRA_EVENTLIST = "eventlist";
    public static final String EXTRA_EVENTICON = "eventIcon";

    private boolean disclaimerGiven = false;
    private RecyclerView mRecyclerView;
    private EventAdapter mEventAdapter;
    private ArrayList<EventItem> mEventList;
    private RequestQueue mRequestQueue;

    private DrawerLayout drawer;

    private static final String TAG = "HomeActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean disclaimerGiven = MyApplication.getDisclaimerGiven();

        if (disclaimerGiven != true) {
            final TextView textView = new TextView(this);
            textView.setText(R.string.disclaimer);
            textView.setMovementMethod(LinkMovementMethod.getInstance()); // this is important to make the links clickable

            final AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom))
                    .setPositiveButton("OK", null)
                    .setView(textView)
                    .create();
            alertDialog.show();
        }

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_list_of_events);
        }

        Log.d(TAG, "onCreate: Oncreate is starting for HomeActivity");

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mEventList = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(this);
        parseJSON();

        if (isServicesOk()) {
            Log.d(TAG, "onCreate: Services are ok, initialising map activity");
            init();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void parseJSON() {
        String url = "http://emergencywatch.pythonanywhere.com/static/";
        Log.d(TAG, "parseJSON: Attempting to parse json");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "onResponse: Trying primary JSON parser");
                            JSONArray jsonArray = response.getJSONArray("events");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject event = jsonArray.getJSONObject(i);

                                String eventHeading = event.getString("event_heading");
                                String location = event.getString("location");
                                String time = event.getString("time");
                                String description = event.getString("description");
                                String eventIcon = event.getString("event_icon");
                                String eventLat = event.getString("event_lat");
                                String eventLng = event.getString("event_lng");

                                mEventList.add(new EventItem(eventHeading, location, time, description, eventIcon, eventLat, eventLng));
                            }

                            mEventAdapter = new EventAdapter(HomeActivity.this, mEventList);
                            mRecyclerView.setAdapter(mEventAdapter);
                            mEventAdapter.setOnItemClickListener(HomeActivity.this);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            Log.d(TAG, "onResponse: Trying secondary JSON parser");
                            JSONArray jsonArray = response.getJSONArray("last_updated");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject last_updated_array = jsonArray.getJSONObject(i);

                                Log.d(TAG, "onResponse: got object");

                                String last_updated = last_updated_array.getString("last_updated");
                                TextView last_update_textView = findViewById(R.id.last_updated_textview);
                                last_update_textView.setText(last_updated);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mRequestQueue.add(request);
    }

    public void refresh(View v) {
        parseJSON();
        Log.d(TAG, "refresh: Refreshed");
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

    @Override
    public void onItemClick(int position) {
        Intent descriptionIntent = new Intent(this, DescriptionActivity.class);
        EventItem clickedItem = mEventList.get(position);

        descriptionIntent.putExtra(EXTRA_EVENT, clickedItem.getEvent());
        descriptionIntent.putExtra(EXTRA_LOCATION, clickedItem.getLocation());
        descriptionIntent.putExtra(EXTRA_TIME, clickedItem.getTime());
        descriptionIntent.putExtra(EXTRA_DESCRIPTION, clickedItem.getDescription());
        descriptionIntent.putExtra(EXTRA_EVENTICON, clickedItem.getEventIcon());

        startActivity(descriptionIntent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_list_of_events:
                Intent listIntent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(listIntent);
                break;

            case R.id.nav_map_of_events:
                Intent mapIntent = new Intent(HomeActivity.this, MapActivity.class);
                startActivity(mapIntent);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
