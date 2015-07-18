package com.toure.findme;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * Customize class - This service recieves updated location from the fused requestupdate method
 * helper methods.
 */
public class LocationService extends IntentService {
    public static String LOG_TAG = LocationService.class.getSimpleName();



    public LocationService() {
        super("LocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            // Key of the updated location.
            String key = LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED;
            Location location = (Location) intent.getExtras().get(key);
            if(location != null){
                SharedPreferences sp = getSharedPreferences("Location", 0);
                sp.edit().putFloat("Loclatitude", (float)location.getLatitude()).commit(); //save the latitude
                sp.edit().putFloat("Loclongitude", (float)location.getLongitude()).commit(); //save the longitude
                sp.edit().putString("Loctime", DateFormat.getTimeInstance().format(new Date())).commit(); //save the time of location

                Log.d(LOG_TAG, location.toString());
            }else {
                Log.d(LOG_TAG, "Location service object is null");
            }

            final String action = intent.getAction();

        }
    }


}
