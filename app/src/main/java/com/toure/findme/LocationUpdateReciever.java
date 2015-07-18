package com.toure.findme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

public class LocationUpdateReciever extends BroadcastReceiver {
    MainActivityFragment MF = null;
    public LocationUpdateReciever(MainActivityFragment MF) {
        this.MF = MF;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving

        // Key of the updated location.
        String key = LocationManager.KEY_LOCATION_CHANGED;
        Location location = (Location) intent.getExtras().get(key);

        //Check if the fragment is active and update UI
       if(MF != null && (MF.isVisible() || MF.isResumed())){
            MF.setLocation(location);
            MF.updateUI();
        }
        else{
            //IF the application is running in background

        }

    }
}
