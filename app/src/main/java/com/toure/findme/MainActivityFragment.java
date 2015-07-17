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

    public MainActivityFragment() {

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
        //Getting and displaying the latitude
        lat.setText(Double.toString(mLastLocation.getLatitude()));

        //Getting and displaying the longitude
        lon.setText(Double.toString(mLastLocation.getLongitude()));

        //Getting and displaying the Altitude
        alt.setText(Double.toString(mLastLocation.getAltitude()));

        //Get the current time and display
        time.setText(DateFormat.getTimeInstance().format(new Date()));
    }

    public void setLocation(Location location){
        mLastLocation = location;
    }




}
