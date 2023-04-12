package com.example.runnertracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ViewSingleTrack extends AppCompatActivity {
    private ImageView trackImg;
    private TextView mDistance;
    private TextView mAvgSpeed;
    private TextView mTime;
    private TextView mDate;
    private TextView mTitle;
    private TextView mStep;

    private long trackID;

    private static final String pref_name = "mysteps";
    private static final String KEY_STEP = "step";

    private Handler handler = new Handler();

    BottomNavigationView bottomNavigationView;

    protected class MyObserver extends ContentObserver {

        public MyObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            populateView();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_track);

        Bundle bundle = getIntent().getExtras();

        trackImg = findViewById(R.id.trackImg);
        mDistance = findViewById(R.id.recordDistance);
        mAvgSpeed = findViewById(R.id.avgSpeed);
        mTime     = findViewById(R.id.duration);
        mDate     = findViewById(R.id.dateText);
        mTitle    = findViewById(R.id.titleText);
        mStep = findViewById(R.id.totalStep);
        trackID  = bundle.getLong("trackID");

        populateView();
        getContentResolver().registerContentObserver(
                DatabaseHelper.ALL_URI, true, new MyObserver(handler));

        //membuat menu di bawah
        bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.track);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.track:
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), RecordTrack.class));
                        overridePendingTransition(0,0);
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

    //membuka maps
    public void onClickMap(View v) {
        Intent map = new Intent(ViewSingleTrack.this, MapsActivity.class);
        Bundle b = new Bundle();
        b.putLong("trackID", trackID);
        map.putExtras(b);
        startActivity(map);
    }

    //method untuk set data rincian track
    @SuppressLint("Range")
    private void populateView() {
        Cursor c = getContentResolver().query(Uri.withAppendedPath(DatabaseHelper.TRACK_URI,
                trackID + ""), null, null, null, null);

        if(c.moveToFirst()) {
            double distance = c.getDouble(c.getColumnIndex(DatabaseHelper.TRACK_DISTANCE));
            long time       = c.getLong(c.getColumnIndex(DatabaseHelper.TRACK_DURATION));
            double avgSpeed = 0;

            if(time != 0) {
                avgSpeed = distance / (time / 3600.0);
            }

            long hours = time / 3600;
            long minutes = (time % 3600) / 60;
            long seconds = time % 60;


            mDistance.setText(String.format("%.2f KM", distance));
            mAvgSpeed.setText(String.format("%.2f KM/H", avgSpeed));
            mTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

            SharedPreferences sharedPreferences = getSharedPreferences(pref_name, MODE_PRIVATE);
            String step = sharedPreferences.getString(KEY_STEP, null);
            mStep.setText(step);

            String date = c.getString(c.getColumnIndex(DatabaseHelper.TRACK_DATE));
            String[] dateParts = date.split("-");
            date = dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];

            mDate.setText(date);
            mTitle.setText(c.getString(c.getColumnIndex(DatabaseHelper.TRACK_NAME)));
            trackImg.setImageDrawable(getResources().getDrawable(R.drawable.run));
        }
    }
}
