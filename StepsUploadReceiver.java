package com.example.user.activitymonitor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

/**
 * Created by User on 09/01/2018.
 */


public class StepsUploadReceiver extends BroadcastReceiver {
    public float stepCount;

    @Override
    public void onReceive(Context context, Intent intent) {
        // On receival of the intent the method reads the current step count from the stepsActivity class
        // Due to the initial intent being ran before the stepCount can be updated the first value will always be 0 on application start
        // To avoid uploading a step count of 0 each time is occurs, a step count will only be uploaded if the value isn't 0
        // Step count uploaded with information regarding date/time of upload
        stepCount = StepsActivity.stepNum;
        if (stepCount != 0) {
            Date now = new Date();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String ID = (String.valueOf(now.getTime()));
            DatabaseReference myRef = database.getReference("Steps");
            DatabaseReference stepsRef = myRef.child(ID);
            stepsRef.setValue(stepCount);
        }

    }
}
