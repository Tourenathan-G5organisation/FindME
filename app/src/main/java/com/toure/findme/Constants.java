package com.toure.findme;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * Created by Toure Nathan on 4/23/2017.
 */
public class Constants {

    private Constants() {
    }


    public static final String PACKAGE_NAME = "com.toure.findme";

    public static final String BROADCAST_ACTION = PACKAGE_NAME + ".activityrecognition.BROADCAST_ACTION";
    public static final String ACTIVITY_EXTRA = PACKAGE_NAME + ".ACTIVITY_EXTRA";


    /**
     * The desired time between activity detections. Larger values result in fewer activity
     * detections while improving battery life. A value of 0 results in activity detections at the
     * fastest possible rate. Getting frequent updates negatively impact battery life and a real
     * app may prefer to request less frequent updates.
     */
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 5000;

    /**
     * List of DetectedActivity types that we are monitoring in this app.
     */
    protected static final int[] MONITORED_ACTIVITIES = {
            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.TILTING,
            DetectedActivity.UNKNOWN
    };

    /**
     * Returns a human readable String corresponding to a detected activity type.
     */
    public static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return String.format(context.getString(R.string.current_activity), resources.getString(R.string.in_vehicle));
            case DetectedActivity.ON_BICYCLE:
                return String.format(context.getString(R.string.current_activity), resources.getString(R.string.on_bicycle));
            case DetectedActivity.ON_FOOT:
                return String.format(context.getString(R.string.current_activity), resources.getString(R.string.on_foot));
            case DetectedActivity.RUNNING:
                return String.format(context.getString(R.string.current_activity), resources.getString(R.string.running));
            case DetectedActivity.STILL:
                return String.format(context.getString(R.string.current_activity), resources.getString(R.string.still));
            case DetectedActivity.TILTING:
                return String.format(context.getString(R.string.current_activity), resources.getString(R.string.tilting));
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
        }

    }

    /**
     * Get the best probable detected activity type based on the confidence percentage of each type
     *
     * @param detectedActivities
     * @return
     */
    public static int[] getbestProbableActivityType(ArrayList<DetectedActivity> detectedActivities) {
        int type = DetectedActivity.UNKNOWN, confidence = 0;
        for (DetectedActivity da : detectedActivities) {
            if (da.getConfidence() > confidence) {
                type = da.getType();
                confidence = da.getConfidence();
            }

        }
        return new int[]{type, confidence};
    }
}
