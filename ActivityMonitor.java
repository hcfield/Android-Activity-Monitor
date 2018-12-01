package com.example.user.activitymonitor;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityMonitor extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    public ArrayList <String> locationList = new ArrayList();
    public static final String TAG = "Locations";
    public static boolean canGetLocation = true;

    // When activity is ran the Google Maps view is opened
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Firebase database is accessed and all records under within the reference "Locations" are accessed
        // Each record is placed on the map as a pointer and its heading is set as date and time of upload
        // Each record is added to the pointerList to be used connecting each pointer on the map in the order they were uploaded
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Locations");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    locationList.add(String.valueOf(messageSnapshot.getKey()) + ", " + (String.valueOf(messageSnapshot.getValue())));
                }
                Double[][] pointerList = new Double[locationList.size()][2];
                int j = 0;
                for (String i: locationList){
                    Log.d(TAG, "Value is: " + i);
                    String ss[] = i.split(",");
                    String Millis = ss[0];
                    String latt = ss[1];
                    String longi = ss[2];

                    latt = latt.replace("{", "");
                    longi = longi.replace("}", "");

                    latt = latt.substring(10);
                    longi = longi.substring(11);
                    double latitudeDB = Double.parseDouble(latt);
                    double longitudeDB = Double.parseDouble(longi);

                    Date date=new Date(Long.parseLong(Millis));
                    String dateString = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(date);

                    LatLng marker = new LatLng(latitudeDB, longitudeDB);
                    mMap.addMarker(new MarkerOptions()
                            .position(marker)
                            .title(dateString)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    pointerList[j][0] = latitudeDB;
                    pointerList[j][1] = longitudeDB;
                    j++;
                    }

                List<LatLng> path = new ArrayList();
                for (int i=0; i<pointerList.length-2; i++ ) {
                    //Implementation to connect each pointer using simple straight lines
                    /*
                    Polyline line = mMap.addPolyline(new PolylineOptions()
                            .add(new LatLng(pointerList[i][0], pointerList[i][1]), new LatLng(pointerList[(i+1)][0], pointerList[(i+1)][1]))
                            .width(5)
                            .color(Color.RED));
                            */

                    // Implementation to connect each pointer using Google's Directions to plot a recommended route between each
                    GeoApiContext context = new GeoApiContext.Builder()
                            .apiKey("AIzaSyC0bbWhdLM1dSFe1qTK26X2Dpk776YXi-k")
                            .build();
                    DirectionsApiRequest request = DirectionsApi
                            .getDirections(context, (pointerList[i][0]+","+pointerList[i][1]), (pointerList[(i+1)][0]+","+pointerList[(i+1)][1]));
                    try {
                        DirectionsResult dirResult = request.await();
                        if (dirResult.routes != null && dirResult.routes.length > 0) {
                            DirectionsRoute retRoute = dirResult.routes[0];
                            if (retRoute.legs !=null) {
                                for(int k=0; k<retRoute.legs.length; k++) {
                                    DirectionsLeg dirLeg = retRoute.legs[k];
                                    if (dirLeg.steps != null) {
                                        for (int l=0; l<dirLeg.steps.length;l++){
                                            DirectionsStep dirStep = dirLeg.steps[l];
                                            if (dirStep.steps != null && dirStep.steps.length >0) {
                                                for (int m=0; m<dirStep.steps.length;m++){
                                                    DirectionsStep dirStep1 = dirStep.steps[m];
                                                    EncodedPolyline points = dirStep1.polyline;
                                                    if (points != null) {
                                                        //Add points to list of route coordinates
                                                        List<com.google.maps.model.LatLng> coordList = points.decodePath();
                                                        for (com.google.maps.model.LatLng coord : coordList) {
                                                            path.add(new LatLng(coord.lat, coord.lng));
                                                        }
                                                    }
                                                }
                                            } else {
                                                EncodedPolyline points = dirStep.polyline;
                                                if (points != null) {
                                                    //Add points to list of route coordinates
                                                    List<com.google.maps.model.LatLng> coordList1 = points.decodePath();
                                                    for (com.google.maps.model.LatLng coord1 : coordList1) {
                                                        path.add(new LatLng(coord1.lat, coord1.lng));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch(Exception ex) {
                        Log.e(TAG, ex.getLocalizedMessage());
                    }
                    //Plot the polyline between each pointer on the map
                    if (path.size() > 0) {
                        PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.RED).width(5);
                        mMap.addPolyline(opts);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // Ensures all permissions for the completion of the activity are available to the app
        // If any are unavailable the global variable "canGetLocation" is set to false
        // This variable is utilised in the locationServices class to check if it is able to access location services
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        int ALL_PERMISSIONS_RESULT = 101;
        ArrayList<String> permissions = new ArrayList<>();
        ArrayList<String> permissionsToRequest;
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (!isGPS && !isNetwork) {
            showSettingsAlert();
        } else {
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                    canGetLocation = false;
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show();
            return;
        }
        // Starts the background service LocationServices to begin requesting location updates every 15 minutes
        startService(new Intent(this, LocationServices.class));
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Places marker on the Engineering Building
        LatLng engBuilding = new LatLng(53.283912, -9.063874);
        mMap.addMarker(new MarkerOptions().position(engBuilding).title("Marker on Engineering Building"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(engBuilding, 15));
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    // Methods utilised to ensure all permissions required are available to the app
    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }
    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }
}
