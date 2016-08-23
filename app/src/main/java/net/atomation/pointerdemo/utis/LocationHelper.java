package net.atomation.pointerdemo.utis;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import net.atomation.pointerdemo.R;

/**
 * Helper class to ask the user to enable location services
 * no need to request location permission as the Atomation SDK already does that
 * Created by eyal on 21/08/2016.
 */
public class LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient
        .OnConnectionFailedListener {

    private static final String TAG = LocationHelper.class.getSimpleName();

    private static LocationHelper sInstance;
    private final Context mContext;
    private final GoogleApiClient mGoogleApiClient;
    private Location mLastKnown;

    public static LocationHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LocationHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    private LocationHelper(Context context){
        mContext = context;
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLastKnown = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void askForLocationIfNeeded(AppCompatActivity activity) {
        boolean needToAskForLocation = needToAskForLocation(activity);
        Log.d(TAG, "onResume: needToAskForLocation = " + needToAskForLocation);
        if (needToAskForLocation(activity)) {
            askForLocation(activity);
        }
    }

    private void askForLocation(final AppCompatActivity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(activity.getString(R.string.location_dialog_text))
                .setCancelable(false)
                .setPositiveButton(activity.getString(R.string.location_dialog_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(activity.getString(R.string.location_dialog_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean needToAskForLocation(AppCompatActivity activity) {
        final LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        boolean hasPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasLocation = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.d(TAG, String.format("needToAskForLocation: hasPermission = %s, hasLocation = %s", hasPermission, hasLocation));
        return hasPermission
                &&  !hasLocation;
    }

    public Location getLastKnownLocation(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        mLastKnown = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        return mLastKnown;
    }
}
