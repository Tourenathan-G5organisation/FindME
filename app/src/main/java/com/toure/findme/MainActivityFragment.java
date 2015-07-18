package com.toure.findme;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;


/**
 * Fragment responsible for doing all the location manipulation.
 */
public class MainActivityFragment extends Fragment {

    //Holds the device location
    Location mLastLocation;

    //Keep record of the last time the location was available
    String mLastUpdateTime;

    //UI variables
    TextView lat;
    TextView lon;
    TextView alt;
    TextView time;

    //Key to keep the location object when this fragment is recreated from a save state
    private static final String LOCATION_KEY = "location_key";

    //Key to keep the location time when this fragment is recreated from a save state
    private static final String LAST_UPDATED_TIME_STRING_KEY = "last_updated_time";


    public MainActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        updateValuesFromBundle(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

//        //Initialise the UI Variables
        lat = (TextView) rootView.findViewById(R.id.latitude);
        lon = (TextView) rootView.findViewById(R.id.longitude);
        alt = (TextView) rootView.findViewById(R.id.altitude);
        time = (TextView) rootView.findViewById(R.id.time);

        return rootView;
    }

    //    @Override
//    public void onLocationChanged(Location location) {
//        mLastLocation = location;
//        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
//        updateUI();
//    }
//
    public void updateUI() {
        if(isVisible()){
            //Getting and displaying the latitude
            lat.setText(Double.toString(mLastLocation.getLatitude()));

            //Getting and displaying the longitude
            lon.setText(Double.toString(mLastLocation.getLongitude()));

            //Getting and displaying the Altitude
            alt.setText(Double.toString(mLastLocation.getAltitude()));

            //Get the current time and display
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            time.setText(mLastUpdateTime);
        }

    }

    public void setLocation(Location location) {
        mLastLocation = location;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LOCATION_KEY, mLastLocation);
        outState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(outState);
    }


    //Method used to get the values from the saved Instance state
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {


            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mLastLocation is not null.
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }

    }
}
