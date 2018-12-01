package com.example.user.activitymonitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import static com.example.user.activitymonitor.R.*;


/**
 * Created by User on 08/01/2018.
 */

public class StepsActivity extends AppCompatActivity implements SensorEventListener{

    public ListView listView;
    public SensorManager sensorManager;
    public ArrayList<String> stepCountList= new ArrayList();
    public ArrayList<String> stepCountArray= new ArrayList();
    public static final String TAG = "Step Count";
    public static float stepNum;

    // When activity is created the Steps GUI is loaded firstly
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.steps_gui);
        listView = (ListView) findViewById(id.listView);

        // The in-built step counter in Android phones is accessed
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
        else {
            Toast.makeText(this, "Sensor not found!", Toast.LENGTH_SHORT).show();
        }

        // The Firebase database is accessed and all records under the "Steps" child are printed to the listView in the Steps GUI
        // When a new step count is uploaded and "onDataChange()" is ran the stepCountList and stepCountArray are cleared to avoid reuploading data
        // The listView is also cleared so the full updated dataset can be printed
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Steps");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                stepCountList.clear();
                stepCountArray.clear();;
                listView.setAdapter(null);
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    stepCountList.add(String.valueOf(messageSnapshot.getKey()) + ", " + (String.valueOf(messageSnapshot.getValue())));
                }
                for (String i: stepCountList) {
                    Log.d(TAG, "Value is: " + i);
                    String ss[] = i.split(",");
                    String Millis = ss[0];
                    String stepCount = ss[1];

                    Date date = new Date(Long.parseLong(Millis));
                    String dateString = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(date);
                    String stepPrint = "Step Count: " + stepCount + " Uploaded at: " + dateString + "\n";
                    stepCountArray.add(stepPrint);
                }
                listView.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, stepCountArray));
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // An intent is created to run the stepsUploadReceiver service
        // Then a pending intent is created the run the same intent as before every hour
        // Thus uploading the current step count every hour
        Context context = this;
        AlarmManager alarmManager= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, StepsUploadReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),3600000,
                pendingIntent);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Step count uploaded is the step count since either the first run of the app or the last reboot
        // The count will only reset when the system is rebooted
        stepNum = sensorEvent.values[0];
    }

    // Default method inclusion
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
