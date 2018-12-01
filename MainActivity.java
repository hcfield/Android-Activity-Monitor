package com.example.user.activitymonitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by User on 08/01/2018.
 */

public class MainActivity extends AppCompatActivity {

    // Once app is ran, main menu is displayed
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_gui);
    }

    // When GPS Location Viewer button is clicked, this method is ran
    // Opening the ActivityMonitor activity to view GPS locations
    public void openMap(View view) {
        Intent myIntent = new Intent(this,ActivityMonitor.class);
        startActivity(myIntent);
    }

    // When Step Count button is clicked, this method is ran
    // Opening the Steps activity to view a list of the step counts and upload times
    public void openSteps(View view) {
        Intent myIntent = new Intent(this,StepsActivity.class);
        startActivity(myIntent);
    }
}
