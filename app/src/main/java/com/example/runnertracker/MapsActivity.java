package com.example.runnertracker;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private long trackID;
    private Boolean isFragmentDisplayed = false;
    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle bundle = getIntent().getExtras();
        trackID = bundle.getLong("trackID");

        btnClose = findViewById(R.id.closeBtn);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFragmentDisplayed) {
                    displayFragment();
                } else {
                    closeFragment();
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // draw polyline
        Cursor c = getContentResolver().query(DatabaseHelper.LOCATION_URI,
                null, DatabaseHelper.LOCATION_JID + " = " + trackID, null, null);

        PolylineOptions line = new PolylineOptions().clickable(false);
        LatLng firstLoc = null;
        LatLng lastLoc = null;
        try {
            while(c.moveToNext()) {
                @SuppressLint("Range") LatLng loc = new LatLng(c.getDouble(c.getColumnIndex(DatabaseHelper.LOCATION_LATITUDE)),
                        c.getDouble(c.getColumnIndex(DatabaseHelper.LOCATION_LONGITUDE)));
                if(c.isFirst()) {
                    firstLoc = loc;
                }
                if(c.isLast()) {
                    lastLoc = loc;
                }
                line.add(loc);
            }
        } finally {
            c.close();
        }

        float zoom = 15.0f;
        if(lastLoc != null && firstLoc != null) {
            mMap.addMarker(new MarkerOptions().position(firstLoc).title("Start"));
            mMap.addMarker(new MarkerOptions().position(lastLoc).title("End"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLoc, zoom));
        }
        mMap.addPolyline(line);
    }

    public void displayFragment() {
        DynamicFragment simpleFragment = DynamicFragment.newInstance();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();


        // TODO: Add the SimpleFragment.
        fragmentTransaction.add(R.id.fragment_container,
                simpleFragment).addToBackStack(null).commit();
        isFragmentDisplayed = true;
    }

    public void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DynamicFragment simpleFragment = (DynamicFragment) fragmentManager
                .findFragmentById(R.id.fragment_container);
        if (simpleFragment != null) {
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            fragmentTransaction.remove(simpleFragment).commit();
        }
        isFragmentDisplayed = false;
    }
}
