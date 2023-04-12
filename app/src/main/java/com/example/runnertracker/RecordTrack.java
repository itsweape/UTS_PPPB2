package com.example.runnertracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RecordTrack extends AppCompatActivity {
    private LocationService.LocationServiceBinder locationService;

    private TextView mDistance;
    private TextView mAvgSpeed;
    private TextView mDuration;
    private TextView mStep;

    private ImageButton playButton;
    private ImageButton stopButton;
    private static final int PERMISSION_GPS_CODE = 1;

    private SensorManager sensorManager;
    private double MagnitudePrevious = 0;
    private Integer stepcount = 0;



    BottomNavigationView bottomNavigationView;
    private static final String pref_name = "mysteps";
    private static final String KEY_STEP = "step";

    private Handler postBack = new Handler();

    private ServiceConnection lsc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            locationService = (LocationService.LocationServiceBinder) iBinder;

            initButtons();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (locationService != null) {
                        // Mendapatkan jarak dan durasi saat berlari
                        float d = (float) locationService.getDuration();
                        long duration = (long) d;  // in seconds
                        float distance = locationService.getDistance();

                        long hours = duration / 3600;
                        long minutes = (duration % 3600) / 60;
                        long seconds = duration % 60;

                        float avgSpeed = 0;
                        if(d != 0) {
                            avgSpeed = distance / (d / 3600);
                        }

                        final String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        final String dist = String.format("%.2f KM", distance);
                        final String avgs = String.format("%.2f KM/H", avgSpeed);

                        postBack.post(new Runnable() {
                            @Override
                            public void run() {
                                mDuration.setText(time);
                                mAvgSpeed.setText(avgs);
                                mDistance.setText(dist);
                            }
                        });

                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            locationService = null;
        }
    };

    //inisiasi button dari locationservice
    private void initButtons() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopButton.setEnabled(false);
            playButton.setEnabled(false);
            return;
        }

        if(locationService != null && locationService.currentlyTracking()) {
            stopButton.setEnabled(true);
            playButton.setEnabled(false);
        } else {
            stopButton.setEnabled(false);
            playButton.setEnabled(true);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_track);

        mDistance = findViewById(R.id.distanceText);
        mDuration = findViewById(R.id.durationText);
        mAvgSpeed = findViewById(R.id.avgSpeedText);
        mStep = findViewById(R.id.stepText);

        playButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        stopButton.setEnabled(false);
        playButton.setEnabled(false);

        //handel permission gps
        handlePermissions();

        //start service
        startService(new Intent(this, LocationService.class));
        bindService(
                new Intent(this, LocationService.class), lsc, Context.BIND_AUTO_CREATE);

        //membuat menu di bawah
        bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.track:
                        startActivity(new Intent(getApplicationContext(), ViewTrack.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        return true;
                    case R.id.action_language:
                        Intent languageIntent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                        startActivity(languageIntent);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("stepCount", stepcount);
        editor.apply();
    }


    public void onClickPlay(View view) {
        locationService.playTrack();

        playButton.setEnabled(false);
        stopButton.setEnabled(true);

        //sensor untuk langkah kaki saat berlari
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener stepDetector = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent!=null){
                    float x_acceleration = sensorEvent.values[0];
                    float y_acceleration = sensorEvent.values[1];
                    float z_acceleration = sensorEvent.values[2];

                    double Magnitude = Math.sqrt(x_acceleration*x_acceleration + y_acceleration*y_acceleration + z_acceleration*z_acceleration);
                    double MagnitudeDelta = Magnitude - MagnitudePrevious;
                    MagnitudePrevious = Magnitude;

                    if (MagnitudeDelta > 6){
                        stepcount++;
                    }
                    mStep.setText(stepcount.toString());
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorManager.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onClickStop(View view) {
        locationService.saveTrack();

        playButton.setEnabled(false);
        stopButton.setEnabled(false);

        //menyimpan total step ke shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(pref_name, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_STEP, String.valueOf(stepcount));
        editor.apply();

        showAlertDialog();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(lsc != null) {
            unbindService(lsc);
            lsc = null;
        }
    }

    //permission untuk GPS
    @Override
    public void onRequestPermissionsResult(int reqCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(reqCode, permissions, results);
        switch (reqCode) {
            case PERMISSION_GPS_CODE:
                if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                    initButtons();
                    if (locationService != null) {
                        locationService.notifyGPSEnabled();
                    }
                } else {
                    stopButton.setEnabled(false);
                    playButton.setEnabled(false);
                }
                return;
        }
    }

    public static class NoPermissionDialogue extends DialogFragment {
        public static  NoPermissionDialogue newInstance() {
            NoPermissionDialogue frag = new NoPermissionDialogue();
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("GPS is required to track your run!")
                    .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GPS_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            return builder.create();
        }
    }

    private void handlePermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                DialogFragment modal = NoPermissionDialogue.newInstance();
                modal.show(getSupportFragmentManager(), "Permissions");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GPS_CODE);
            }

        }
    }

    //menampilkan allert dialog
    public void showAlertDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(RecordTrack.this);
        alert.setMessage(R.string.dialog);
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recreate();
            }
        });
        alert.show();
    }
}
