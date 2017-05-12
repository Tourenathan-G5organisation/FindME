package com.toure.findme;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    // Code used to to request for permissions
    public static final int REQUEST_PERMISSION_CODE = 1000;

    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 1004;

    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";

    // Bool to track whether the app is already resolving an error(Google play error)
    private boolean mResolvingError = false;

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    //Holds the device location
    Location mLastLocation;

    //Location update request
    LocationRequest mLocationRequest;

    //Keep record of the last time the location was available
    String mLastUpdateTime;

    MainActivityFragment MF;

    int cnt = 0;

    //Broadcast receiver for location change
    BroadcastReceiver locRecievr;

    boolean permissionAlreadyAskedOnM; // Use to keep track if the permission has already been asked to use GPS

    protected ActivityDetectionBroadcastReceiver mActivityDetectedBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the state of error resolving tracker
        mResolvingError = (savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false));

        mActivityDetectedBroadcastReceiver = new ActivityDetectionBroadcastReceiver();
        //Create the Google ApiClient instance service
        buildGoogleApiClient();
        Log.d(LOG_TAG, "Initialised the API Client");


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //THis method build the Google API Client which will be used to get services
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "the API Client is connected");

        //Request for location updates
        startLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, " the API Client connection failed");

        final int errorCode = connectionResult.getErrorCode();
        try {
            if (errorCode == ConnectionResult.SERVICE_MISSING) {
                // No google play service available
                Log.d(LOG_TAG, "onConnectionFail: SERVICE MISSING");
            } else if (errorCode == ConnectionResult.SERVICE_DISABLED) {
                Log.d(LOG_TAG, "onConnectionFail: SERVICE_DISABLED");
            } else if (errorCode == ConnectionResult.SERVICE_INVALID) {
                Log.d(LOG_TAG, "onConnectionFail: SERVICE_INVALID");
            } else if (errorCode == ConnectionResult.SERVICE_MISSING_PERMISSION) {
                Log.d(LOG_TAG, "onConnectionFail: SERVICE_MISSING_PERMISSION");
            } else if (errorCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                Log.d(LOG_TAG, "onConnectionFail: SERVICE_VERSION_UPDATE_REQUIRED");
            } else if (errorCode == ConnectionResult.SERVICE_UPDATING) {
                Log.d(LOG_TAG, "onConnectionFail: SERVICE_UPDATING");
            }

            if (!mResolvingError) {
                if (connectionResult.hasResolution()) {
                    mResolvingError = true;
                    connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                }
            }

        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, " API Client connection suspended");
        mGoogleApiClient.connect();
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.

    }



    @Override
    protected void onStart() {
        super.onStart();

        if (!mResolvingError) {
            Log.d(LOG_TAG, " API Client connecting");
            mGoogleApiClient.connect();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mActivityDetectedBroadcastReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));

        // Request the device to detect activities
        requestActivityUpdates();
    }

    @Override
    protected void onPause() {
        // Unregister the broadcast receiver that was registered during onResume
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mActivityDetectedBroadcastReceiver);

        // Stop the request of activities detection by the device
        removeActivityUpdates();
        super.onPause();

    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting())) {
            mGoogleApiClient.disconnect(); //disconnect the google play services before closing the app
            Log.d(LOG_TAG, " API Client disconnected");
        }
        super.onStop();

    }


    //This method is called after a GoogleAPi connection  problem is solved
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
        if (requestCode == REQUEST_CHECK_LOCATION_SETTINGS) {
            if (resultCode == RESULT_OK) {
                if (mGoogleApiClient.isConnected()) {
                    Log.d(LOG_TAG, "request location update");
                    startLocationUpdates();
                }
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); // 5 secs
        mLocationRequest.setFastestInterval(2000); // 2 secs
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        Log.d(LOG_TAG, "startLocationUpdates");
        createLocationRequest();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            if (!permissionAlreadyAskedOnM)
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);

            return;
        }

        Log.d(LOG_TAG, "request location update");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        LocationAvailability la = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        Log.d(LOG_TAG, Boolean.toString(la.isLocationAvailable()));
        Log.d(LOG_TAG, la.toString());

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .setAlwaysShow(true)
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(LOG_TAG, " GPS is  activated");
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Log.d(LOG_TAG, "LOCATION SETTINGS_CHANGE_UNAVAILABLE");
                        break;
                }
            }
        });

/*
        LocationUpdateReciever locReciever = new LocationUpdateReciever(MF);

        Intent intent = new Intent(this, LocationService.class);

        PendingIntent pi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, pi);*/
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        try{
            MF = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.locationFragment);

            MF.setLocation(mLastLocation);
            MF.updateUI();
            Log.d(LOG_TAG, "new location:  " + Integer.toString(++cnt) + " times");
        }
        catch(NullPointerException e){
            Log.d(LOG_TAG, "Null pointer exception2:  " + e.getMessage()+
                    "frag: " + MF.toString());
            e.getStackTrace();
        }

    }


    protected void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    //Getter method for the API Client
    public GoogleApiClient getApiClient(){
        return mGoogleApiClient;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            permissionAlreadyAskedOnM = true;
            boolean permissionState = true;

            if (permissions.length > 0) {
                for (int i = 0; i < permissions.length; i++) {

                    permissionState = permissionState && (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }
            } else {
                permissionState = false;
            }
            Log.d(LOG_TAG, Boolean.toString(permissionState));
            if (permissionState) {
                // user has given permission to access location data
                Log.d(LOG_TAG, " User has accepted the permission to access location data");
                startLocationUpdates();
            } else {
                // User has rejected the permission to access location data
                Log.d(LOG_TAG, " User has rejected the permission to access location data");
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.permission_required_title))
                        .setMessage(getString(R.string.permission_required_msg))
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish(); // close the app
                            }
                        });

                builder.show();
            }
        }
    }


    /**
     * This method is used to launch the request to detected the user current activity
     */
    public void requestActivityUpdates() {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent())
                .setResultCallback(this);
    }

    /**
     * This method is used to stop the detection of activity
     */
    public void removeActivityUpdates() {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        // Remove the activity updates for all the pending intents that was used to request it.
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient,
                getActivityDetectionPendingIntent())
                .setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.e(LOG_TAG, "Successfully added or remove activity detection");
        } else {
            Log.e(LOG_TAG, "Error adding or removing activity detection" + status.getStatusMessage());
        }
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

        protected final String TAG = ActivityDetectionBroadcastReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {

            ArrayList<DetectedActivity> detectedActivities = intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
            int[] bestOption = Constants.getbestProbableActivityType(detectedActivities);
            if (MF == null) {
                MF = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.locationFragment);
            }
            MF.setUserActivity(Constants.getActivityString(getApplicationContext(), bestOption[0]) + "\n" + String.format(getString(R.string.accuracy), bestOption[1]) + "%");
            for (DetectedActivity da : detectedActivities) {
                Log.i(TAG,
                        Constants.getActivityString(getApplicationContext(), da.getType()) + " " + da.getConfidence() + "%");
            }
        }
    }


}
