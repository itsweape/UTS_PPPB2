package com.example.runnertracker;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class LocationsListener implements LocationListener {
    ArrayList<Location> locations;
    boolean recordLocations;

    public LocationsListener() {
        newTrack();
        recordLocations = false;
    }

    public void newTrack() {
        locations = new ArrayList<Location>();
    }

    public float getDistanceOfTrack() {
        if(locations.size() <= 1) {
            return 0;
        }

        return locations.get(0).distanceTo(locations.get(locations.size() - 1)) / 1000;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }


    @Override
    public void onLocationChanged(Location location) {
        if(recordLocations) {
            locations.add(location);
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("mdp", "onStatusChanged: " + provider + " " + status);
    }
    @Override
    public void onProviderEnabled(String provider) {
        Log.d("mdp", "onProviderEnabled: " + provider);
    }
    @Override
    public void onProviderDisabled(String provider) {
        Log.d("mdp", "onProviderDisabled: " + provider);
    }
}