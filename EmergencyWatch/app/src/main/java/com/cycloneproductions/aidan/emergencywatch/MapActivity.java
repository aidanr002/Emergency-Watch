package com.cycloneproductions.aidan.emergencywatch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.cycloneproductions.aidan.emergencywatch.HomeActivity.EXTRA_EVENTICON;
import static com.cycloneproductions.aidan.emergencywatch.HomeActivity.EXTRA_EVENTLIST;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener, Serializable {
    private static final String TAG = "MapActivity";
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 8f;

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
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
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
                                String eventLat = event.getString("event_lat");
                                String eventLng = event.getString("event_lng");

                                mEventList.add(new EventItem(eventHeading, location, time, description, eventIcon, eventLat, eventLng));
                            }
                            getLocationPermission();
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        List<Marker> markerList = new ArrayList<>();

        for (int i = 0; i < mEventList.size(); i++) {
            EventItem focusItem = mEventList.get(i);
            String focusAddress = focusItem.getLocation();
            String focusIcon = focusItem.getEventIcon();
            String focusLat = focusItem.getEventLat();
            String focusLng = focusItem.getEventLng();
            if (getLocationFromCoords(this, focusLat, focusLng ) != null) {

                if (focusItem.getEventIcon().equals("http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/yellowfire.png")) {
                    focusMarker = mMap.addMarker(new MarkerOptions()
                            .position(getLocationFromCoords(this, focusLat, focusLng))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.advice)));
                    focusMarker.setTag(i);
                    markerList.add(focusMarker);
                }
                if (focusItem.getEventIcon().equals("http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/orangefire.png")) {
                    focusMarker = mMap.addMarker(new MarkerOptions()
                            .position(getLocationFromCoords(this, focusLat, focusLng))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.watchact)));
                    focusMarker.setTag(i);
                    markerList.add(focusMarker);
                }
                if (focusItem.getEventIcon().equals("http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/redfire.png")) {
                    focusMarker = mMap.addMarker(new MarkerOptions()
                            .position(getLocationFromCoords(this, focusLat, focusLng))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.emergency)));
                    focusMarker.setTag(i);
                    markerList.add(focusMarker);
                }
                if (focusItem.getEventIcon().equals("http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/information.png")) {
                    focusMarker = mMap.addMarker(new MarkerOptions()
                            .position(getLocationFromCoords(this, focusLat, focusLng))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.information)));
                    focusMarker.setTag(i);
                    markerList.add(focusMarker);
                }
            }
            }


        Log.d(TAG, "onMapReady: Got the latlng and made a marker. Marker added to list");

        mMap.setOnMarkerClickListener(this);

        for (Marker m : markerList) {
            LatLng latLng = new LatLng(m.getPosition().latitude, m.getPosition().longitude);
            int tagID = (int) m.getTag();
            EventItem clickedItem = mEventList.get(tagID);
            if (clickedItem.getEventIcon().equals("http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/yellowfire.png")) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.advice)));
            }
            if (clickedItem.getEventIcon().equals("http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/orangefire.png")) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.watchact)));
            }
            if (clickedItem.getEventIcon().equals("http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/redfire.png")) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.emergency)));
            }
            if (clickedItem.getEventIcon().equals("http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/information.png")) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.information)));
            }
            Log.d(TAG, "onMapReady: Displaying marker at:" + latLng);
            moveCamera(latLng, DEFAULT_ZOOM);
        }
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);

    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            if(mMap != null){
                moveCamera(loc, DEFAULT_ZOOM);
            }
        }
    };

    public LatLng getLocationFromCoords(Context context, String eventLat, String eventLng) {
        LatLng location = null;
        try {
            double latitude = Double.parseDouble(eventLat);
            double longitude = Double.parseDouble(eventLng);
            location = new LatLng(latitude, longitude);
        } catch (IndexOutOfBoundsException ex){
            ex.printStackTrace();
            Toast.makeText(this, "An error occured: Not all items were displayed", Toast.LENGTH_LONG).show();
        }
        return location;
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
        } catch (IndexOutOfBoundsException ex){
            ex.printStackTrace();
        }
        return p1;
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
        try {
            Integer focusId = (Integer) marker.getTag();

            Intent descriptionIntent = new Intent(this, DescriptionActivity.class);
            EventItem clickedItem = mEventList.get(focusId);

            descriptionIntent.putExtra(EXTRA_EVENT, clickedItem.getEvent());
            descriptionIntent.putExtra(EXTRA_LOCATION, clickedItem.getLocation());
            descriptionIntent.putExtra(EXTRA_TIME, clickedItem.getTime());
            descriptionIntent.putExtra(EXTRA_DESCRIPTION, clickedItem.getDescription());
            descriptionIntent.putExtra(EXTRA_EVENTICON, clickedItem.getEventIcon());
            startActivity(descriptionIntent);

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            Toast.makeText(this, "An Error Occured", Toast.LENGTH_LONG).show();


        } catch (NullPointerException ex) {
            ex.printStackTrace();
            Toast.makeText(this, "An Error Occured", Toast.LENGTH_LONG).show();

        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            Toast.makeText(this, "An error occured", Toast.LENGTH_LONG).show();
        }
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