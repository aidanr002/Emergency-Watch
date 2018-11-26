package com.cycloneproductions.aidan.emergencywatch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void eventButtonClicked(View view) {
        TextView fireHeading = (TextView) findViewById(R.id.fireHeading);
        fireHeading.setText("Pressed");
    }
    public void mapButtonClicked(View view) {
        TextView mapButton = (TextView) findViewById(R.id.mapButton);
        mapButton.setText("Pressed");
        TextView listButton = (TextView) findViewById(R.id.listButton);
        listButton.setText("List");
    }
    public void menuButtonClicked(View view) {
        TextView menuButton = (TextView) findViewById(R.id.menuButton);
        menuButton.setText("Pressed");
    }
    public void listButtonClicked(View view) {
        TextView listButton = (TextView) findViewById(R.id.listButton);
        listButton.setText("Pressed");
        TextView mapButton = (TextView) findViewById(R.id.mapButton);
        mapButton.setText("Map");
    }
}
