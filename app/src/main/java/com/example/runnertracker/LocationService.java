package com.example.runnertracker;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationService extends Service {
    private LocationManager locationManager;
    private LocationsListener locationListener;
    private final IBinder binder = new LocationServiceBinder();

    private final int NOTIFICATION_ID = 001;
    private long startTime = 0;
    private long stopTime = 0;

    final int TIME_INTERVAL = 3;
    final int DIST_INTERVAL = 3;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("mdp", "Location Service created");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationsListener();
        locationListener.recordLocations = false;


        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, TIME_INTERVAL, DIST_INTERVAL, locationListener);
        } catch(SecurityException e) {
            Log.d("mdp", "No Permissions for GPS");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        locationListener = null;
        locationManager = null;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        Log.d("mdp", "Location Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected float getDistance() {
        return locationListener.getDistanceOfTrack();
    }

    protected double getDuration() {
        if(startTime == 0) {
            return 0.0;
        }

        long endTime = SystemClock.elapsedRealtime();

        if(stopTime != 0) {
            endTime = stopTime;
        }

        long elapsedMilliSeconds = endTime - startTime;
        return elapsedMilliSeconds / 1000.0;
    }

    protected boolean currentlyTracking() {
        return startTime != 0;
    }

    protected void playTrack() {
        locationListener.newTrack();
        locationListener.recordLocations = true;
        startTime = SystemClock.elapsedRealtime();
        stopTime = 0;
    }

    protected void saveTrack() {
        ContentValues trackData = new ContentValues();
        trackData.put(DatabaseHelper.TRACK_DISTANCE, getDistance());
        trackData.put(DatabaseHelper.TRACK_DURATION, (long) getDuration());
        trackData.put(DatabaseHelper.TRACK_DATE, getDateTime());

        long trackID = Long.parseLong(getContentResolver().insert(DatabaseHelper.TRACK_URI, trackData).getLastPathSegment());

        for(Location location : locationListener.getLocations()) {
            ContentValues locationData = new ContentValues();
            locationData.put(DatabaseHelper.LOCATION_JID, trackID);
            locationData.put(DatabaseHelper.LOCATION_ALTITUDE, location.getAltitude());
            locationData.put(DatabaseHelper.LOCATION_LATITUDE, location.getLatitude());
            locationData.put(DatabaseHelper.LOCATION_LONGITUDE, location.getLongitude());

            getContentResolver().insert(DatabaseHelper.LOCATION_URI, locationData);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        locationListener.recordLocations = false;
        stopTime = SystemClock.elapsedRealtime();
        startTime = 0;
        locationListener.newTrack();

        Log.d("mdp", "Track saved with id = " + trackID);
    }


    protected void notifyGPSEnabled() {
        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 3, 3, locationListener);
        } catch(SecurityException e) {
            Log.d("mdp", "No Permissions for GPS");
        }
    }

    private String getDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return formatter.format(date);
    }

    public class LocationServiceBinder extends Binder {
        public float getDistance() {
            return LocationService.this.getDistance();
        }

        public double getDuration() {
            return LocationService.this.getDuration();
        }

        public boolean currentlyTracking() {return LocationService.this.currentlyTracking();}

        public void playTrack() {
            LocationService.this.playTrack();
        }

        public void saveTrack() {
            LocationService.this.saveTrack();
        }

        public void notifyGPSEnabled() { LocationService.this.notifyGPSEnabled();}
    }
}
