package com.cycloneproductions.aidan.emergencywatch;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements EventAdapter.OnItemClickListener{
    public static final String EXTRA_EVENT = "event";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_TIME = "time";

    private RecyclerView mRecyclerView;
    private EventAdapter mEventAdapter;
    private ArrayList<EventItem> mEventList;
    private RequestQueue mRequestQueue;


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

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mEventList = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(this);
        parseJSON();

    }

    private void parseJSON() {
        String url = "https://api.myjson.com/bins/lomw8";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("events");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject event = jsonArray.getJSONObject(i);

                                String eventHeading = event.getString("event_heading");
                                String location = event.getString("location");
                                String time = event.getString("time");

                                mEventList.add(new EventItem(eventHeading, location, time));
                            }

                            mEventAdapter = new EventAdapter(HomeActivity.this, mEventList);
                            mRecyclerView.setAdapter(mEventAdapter);
                            mEventAdapter.setOnItemClickListener(HomeActivity.this);

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

    @Override
    public void onItemClick(int position) {
        Intent descriptionIntent = new Intent(this, DescriptionActivity.class);
        EventItem clickedItem = mEventList.get(position);

        descriptionIntent.putExtra(EXTRA_EVENT, clickedItem.getEvent());
        descriptionIntent.putExtra(EXTRA_LOCATION, clickedItem.getLocation());
        descriptionIntent.putExtra(EXTRA_TIME, clickedItem.getTime());

        startActivity(descriptionIntent);
    }
}
