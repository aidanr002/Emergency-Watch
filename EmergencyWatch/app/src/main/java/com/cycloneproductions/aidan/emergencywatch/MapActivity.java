package com.cycloneproductions.aidan.emergencywatch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.cycloneproductions.aidan.emergencywatch.HomeActivity.EXTRA_EVENTLIST;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener, Serializable {
    private static final String TAG = "MapActivity";
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 9f;

    // Variables
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ArrayList<EventItem> mEventList;
    private Marker focusMarker;
    public static final String EXTRA_EVENT = "event";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_TIME = "time";
    public static final String EXTRA_DESCRIPTION = "description";
    private DrawerLayout drawer;
    private RequestQueue mRequestQueue;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(this);
        parseJSON();
        setContentView(R.layout.activity_map);
        Log.d(TAG, "onCreate: Oncreate is starting for MapActivity");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_map_of_events);
        }

        mEventList = new ArrayList<>();

        Button buttontolist = findViewById(R.id.listButton);

        buttontolist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked button to list");

                Intent intent = new Intent(MapActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    public void refresh(View v) {
        parseJSON();
        Log.d(TAG, "refresh: Refreshed");
    }

    private void parseJSON() {
        String url = "http://emergencywatch.pythonanywhere.com/static/";
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
                                String description = event.getString("description");
                                String eventIcon = event.getString("event_icon");

                                mEventList.add(new EventItem(eventHeading, location, time, description, eventIcon));
                            }
                            getLocationPermission();
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");
        Toast.makeText(this, "Map Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        List<Marker> markerList = new ArrayList<>();

        for (int i = 0; i < mEventList.size(); i++) {
            EventItem focusItem = mEventList.get(i);
            String focusAddress = focusItem.getLocation();
            String focusIcon = focusItem.getEventIcon();
            if (getLocationFromAddress(this, focusAddress) != null) {
                focusMarker = mMap.addMarker(new MarkerOptions().position(getLocationFromAddress(this, focusAddress)));
                focusMarker.setIcon(BitmapDescriptorFactory.fromPath(focusIcon));
                focusMarker.setTag(i);
                markerList.add(focusMarker);
            }
        }

        Log.d(TAG, "onMapReady: Got the latlng and made a marker. Marker added to list");

        mMap.setOnMarkerClickListener(this);

        for (Marker m : markerList) {
            LatLng latLng = new LatLng(m.getPosition().latitude, m.getPosition().longitude);
            mMap.addMarker(new MarkerOptions().position(latLng));
            Log.d(TAG, "onMapReady: Displaying marker at:" + latLng);
            moveCamera(latLng, DEFAULT_ZOOM);
        }

        if (mLocationPermissionGranted) {
            getDeviceLocation();

        }
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return p1;
    }


    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: Gettingt the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Found Location");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText((MapActivity.this), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: Security Exception:" + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving camera to: Lat:" + latLng.latitude + ", Lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap() {
        Log.d(TAG, "initMap: Initialising map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.eventMap);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: Getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
                Log.d(TAG, "getLocationPermission: Permissions Granted");
            } else {
                ActivityCompat.requestPermissions(this, permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Called");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: Permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: Permission Granted");
                    mLocationPermissionGranted = true;
                    //initialise the map
                    initMap();
                }
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Integer focusId = (Integer) marker.getTag();

        Intent descriptionIntent = new Intent(this, DescriptionActivity.class);
        EventItem clickedItem = mEventList.get(focusId);

        descriptionIntent.putExtra(EXTRA_EVENT, clickedItem.getEvent());
        descriptionIntent.putExtra(EXTRA_LOCATION, clickedItem.getLocation());
        descriptionIntent.putExtra(EXTRA_TIME, clickedItem.getTime());
        descriptionIntent.putExtra(EXTRA_DESCRIPTION, clickedItem.getDescription());

        startActivity(descriptionIntent);
        return false;
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
        switch (menuItem.getItemId()) {
            case R.id.nav_list_of_events:
                Intent listIntent = new Intent(MapActivity.this, HomeActivity.class);
                startActivity(listIntent);
                break;

            case R.id.nav_map_of_events:
                Intent mapIntent = new Intent(MapActivity.this, MapActivity.class);
                mapIntent.putExtra(EXTRA_EVENTLIST, mEventList);
                startActivity(mapIntent);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}