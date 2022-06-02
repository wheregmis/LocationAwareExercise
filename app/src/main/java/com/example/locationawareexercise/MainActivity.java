package com.example.locationawareexercise;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.location.Address;
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1;

    // Location Demo with FUSED LOCATION PROVIDER CLIENT

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int UPDATE_INTERVAL = 1000;
    private static final int FASTEST_INTERVAL = 1000;

    private List<String> permissionsToRequest;
    private List<String> permissions = new ArrayList<>();
    private List<String> permissionsRejected = new ArrayList<>();

    private TextView lblLongitude;
    private TextView lblLatitude;
    private TextView lblAltitude;
    private TextView lblAddress;
    private TextView lblAccuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lblLatitude = findViewById(R.id.lblLatitude);
        lblLongitude = findViewById(R.id.lblLongitude);
        lblAltitude = findViewById(R.id.lblAltitude);
        lblAddress = findViewById(R.id.lblAddress);
        lblAccuracy = findViewById(R.id.lblAccuracy);

        // instantiate the fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // add permissions
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);
        if (permissionsToRequest.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: ");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // this is a proper place to check the google play services availability

        int errorCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, errorCode, errorCode, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Toast.makeText(MainActivity.this, "No Services", Toast.LENGTH_SHORT).show();
                        }
                    });
            errorDialog.show();
        } else {
            Log.i(TAG, "onPostResume: ");
            findLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            for (String perm: permissions) {
                if (!hasPermission(perm))
                    permissionsRejected.add(perm);
            }

            if (permissionsRejected.size() > 0 ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("The location permission is mandatory")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), REQUEST_CODE);
                                        }

                                    }
                                }).setNegativeButton("Cancel", null)
                                .create()
                                .show();
                    }
                }
            }
        }
    }

    private void findLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                   //     location_tv.setText(String.format("Lat: %s, Lng: %s", location.getLatitude(), location.getLongitude()));
                    // place where you append your values
                        Log.d(TAG, "onSuccess: "+ location.getLongitude());
                        String address = getAddress(MainActivity.this, location.getLatitude(), location.getLongitude());
                        appendValuesToActivity(String.format("%s",location.getLatitude()), String.format("%s",location.getLongitude()), String.format("%s",location.getAltitude()), String.format("%s",location.getAccuracy()), address);
                    }
                }
            });
        }

        startUpdateLocation();
    }

    private void startUpdateLocation() {
        Log.d(TAG, "startUpdateLocation: ");
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                   // location_tv.setText(String.format("Lat: %s, Lng: %s", location.getLatitude(), location.getLongitude()));
                    // place to append value when location is updated

                    String address = getAddress(MainActivity.this, location.getLatitude(), location.getLongitude());
                    appendValuesToActivity(String.format("%s",location.getLatitude()), String.format("%s",location.getLongitude()), String.format("%s",location.getAltitude()), String.format("%s",location.getAccuracy()), address);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }


    private List<String> permissionsToRequest(List<String> permissions) {
        ArrayList<String> results = new ArrayList<>();
        for (String perm : permissions) {
            if (!hasPermission(perm))
                results.add(perm);
        }

        return results;
    }

    private boolean hasPermission(String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void appendValuesToActivity(String latitude, String longitude, String altitude, String accuracy, String address){
        Log.d(TAG, "appendValuesToActivity: "+latitude + "and long is "+longitude);
        lblLongitude.setText(longitude);
        lblLatitude.setText(latitude);
        lblAltitude.setText(altitude);
        lblAccuracy.setText(accuracy);
        lblAddress.setText(address);
    }

    public String getAddress(Context ctx, double lat, double lng){
        String fullAdd=null;
        try{
            Geocoder geocoder= new Geocoder(ctx, Locale.getDefault());
            List<android.location.Address> addresses = geocoder.getFromLocation(lat,lng,1);
            if(addresses.size()>0){
                Address address = addresses.get(0);
                fullAdd = address.getAddressLine(0);
                // if you want only city or pin code use following code //
            /* String Location = address.getLocality();
            String zip = address.getPostalCode();
            String Country = address.getCountryName(); */
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
        return fullAdd;
    }

}