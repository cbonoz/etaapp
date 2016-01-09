package com.etame.etame;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity  implements OnContactSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String SELECTED_CONTACT_ID 	= "contact_id";
    public static final String KEY_PHONE_NUMBER 	= "phone_number";
    public static final String KEY_CONTACT_NAME 	= "contact_name";
    private static final String TAG = "MainActivity";

    protected GoogleApiClient mGoogleApiClient;
    private Location currentLocation;


    /**
     * Starting point
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        FragmentManager fragmentManager 	= this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ContactsListFragment 	fragment 			= new ContactsListFragment();

        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Select contact");
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "MainActivity onDestroy");

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }


    }

        /**
         * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
         */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    /**
     * Callback when the contact is selected from the list of contacts.
     * Loads the {@link ContactDetailsFragment}
     */
    @Override
    public void onContactNameSelected(long contactId) {
		/* Now that we know which Contact was selected we can go to the details fragment */

        ContactDetailsFragment detailsFragment = new ContactDetailsFragment();
        Bundle 		args 			= new Bundle();
        args.putLong(MainActivity.SELECTED_CONTACT_ID, contactId);
        if (currentLocation!=null) {
            args.putDouble("Lat", currentLocation.getLatitude());
            args.putDouble("Long", currentLocation.getLongitude());
         } else {
            //no location found (default values)
            args.putDouble("Lat", 0);
            args.putDouble("Long", 0);
        }


        detailsFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.fragment_container, detailsFragment);

        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }

    /**
     * Callback when the contact number is selected from the contact details view
     * Sets the activity result with the contact information and finishes
     */
    @Override
    public void onContactNumberSelected(String contactNumber, String contactName) {
        Intent intent = new Intent();
        intent.putExtra(KEY_PHONE_NUMBER, contactNumber);
        intent.putExtra(KEY_CONTACT_NAME, contactName);

        setResult(RESULT_OK, intent);
        finish();
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


    //Location



    //Location

    private static final Integer UPDATE_INTERVAL_MS = 1000 * 60;
    private static final Integer FASTEST_INTERVAL_MS = UPDATE_INTERVAL_MS;
    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS);

        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {
                        if (status.getStatus().isSuccess()) {
                            if (Log.isLoggable(TAG, Log.DEBUG)) {
                                Log.d(TAG, "Successfully requested location updates");
                            }
                        } else {
                            Log.e(TAG,
                                    "Failed in requesting location updates, "
                                            + "status code: "
                                            + status.getStatusCode()
                                            + ", message: "
                                            + status.getStatusMessage());
                        }
                    }
                });
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (ClientPaths.currentLocation==null) {
//            Toast.makeText(this, "No Location Detected", Toast.LENGTH_SHORT).show();
//            Log.i(TAG, "No Location Detected");
//        } else {
//            Toast.makeText(this,"Location Found", Toast.LENGTH_SHORT).show();
//            Log.i(TAG, "Location Found: " + ClientPaths.currentLocation.getLatitude() + "," + ClientPaths.currentLocation.getLongitude());
//        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "connection to location client suspended");
        }
        mGoogleApiClient.connect();
    }

}
