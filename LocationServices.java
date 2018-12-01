package com.example.user.activitymonitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by User on 08/01/2018.
 */

public class LocationServices extends Service implements android.location.LocationListener {

    public LocationServices() {

    }
    // Once the service is ran, the onCreate method runs the startGettingLocations() method
    @Override
    public void onCreate() {
        startGettingLocations();
    }

    private void startGettingLocations() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean canGetLocation = ActivityMonitor.canGetLocation;
        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;// Minimum distance of 5 metres between updates
        long MIN_TIME_BW_UPDATES = 900000;// 15 minutes between each update

        // Starts requesting location updates if canGetLocation is true
        // Attempts the get location information from GPS and if unavailable the network
        if (canGetLocation) {
            if (isGPS) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
            } else if (isNetwork) {
                        lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
            }
        }
        else{
            Toast.makeText(this, "Can't get location", Toast.LENGTH_SHORT).show();
        }
    }

    // When the location is updated a new database entry is created containing information about
    // the latitude, longitude and date/time of upload
    public void onLocationChanged(Location location) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Date now = new Date();
        String ID = (String.valueOf(now.getTime()));
        DatabaseReference myRef = database.getReference("Locations");
        DatabaseReference locationRef = myRef.child(ID);
        LocationData currentData = new LocationData(location.getLatitude(),location.getLongitude());
        locationRef.setValue(currentData);
    }

    // All these methods must be included by default but aren't utilised
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onDestroy() {

    }

}
