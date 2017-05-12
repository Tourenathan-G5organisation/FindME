package com.toure.findme;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * <p/>
 * helper methods.
 */
public class DetectedActivitiesIntentService extends IntentService {

    public static final String LOC_TAG = DetectedActivitiesIntentService.class.getSimpleName();


    public DetectedActivitiesIntentService() {
        super("DetectedActivitiesIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION);

            // Get the list of the probable activities associated with the current state of the
            // device. Each activity is associated with a confidence level, which is an int between
            // 0 and 100.
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            // Broadcast the list of detected activities.
            localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);


        }
    }


}
