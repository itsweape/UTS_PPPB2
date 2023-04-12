package com.example.runnertracker;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class ViewTrack extends ListActivity {
    private TextView dateText;
    private DatePickerDialog.OnDateSetListener dateListener;

    private ListView trackList;
    private ViewTrack.TrackAdapter adapter;
    private ArrayList<ViewTrack.TrackItem> trackNames;

    BottomNavigationView bottomNavigationView;
    private class TrackItem {
        private String name;
        private String strUri;

        private long id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void set_id(long id) {
            this.id = id;
        }

        public long get_id() {
            return id;
        }
    }

    private class TrackAdapter extends ArrayAdapter<ViewTrack.TrackItem> {
        private ArrayList<ViewTrack.TrackItem> items;

        public TrackAdapter(Context context, int textViewResourceId, ArrayList<ViewTrack.TrackItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        //get view dari tracklist xml
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.tracklist, null);
            }

            ViewTrack.TrackItem item = items.get(position);
            if (item != null) {
                TextView text = view.findViewById(R.id.singleTrack);
                ImageView img = view.findViewById(R.id.trackImg);
                if (text != null) {
                    text.setText(item.getName());
                }
                img.setImageDrawable(getResources().getDrawable(R.drawable.run));
            }
            return view;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_track);

        trackNames = new ArrayList<ViewTrack.TrackItem>();
        adapter = new TrackAdapter(this, R.layout.tracklist, trackNames);
        setListAdapter(adapter);
        setUpDateDialogue();

        trackList.setClickable(true);
        //berpindah ke activity single track ketika item list track di klik
        trackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            ViewTrack.TrackItem o = (ViewTrack.TrackItem) trackList.getItemAtPosition(position);
            long trackID = o.get_id();

            Bundle b = new Bundle();
            b.putLong("trackID", trackID);
            Intent singleTrack = new Intent(ViewTrack.this, ViewSingleTrack.class);
            singleTrack.putExtras(b);
            startActivity(singleTrack);
            }
        });

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

    @Override
    public void onResume() {
        super.onResume();
        String date = dateText.getText().toString();
        if(!date.toLowerCase().equals("select date")) {
            listTrack(date);
        }
    }

    //select tanggal
    private void setUpDateDialogue() {
        dateText = findViewById(R.id.selectDateText);
        trackList = getListView();

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year;
                int month;
                int day;

                if(dateText.getText().toString().toLowerCase().equals("select date")) {
                    Calendar calender = Calendar.getInstance();
                    year = calender.get(Calendar.YEAR);
                    month = calender.get(Calendar.MONTH);
                    day = calender.get(Calendar.DAY_OF_MONTH);
                } else {
                    String[] dateParts = dateText.getText().toString().split("/");
                    year = Integer.parseInt(dateParts[2]);
                    month = Integer.parseInt(dateParts[1]) - 1;
                    day = Integer.parseInt(dateParts[0]);
                }

                DatePickerDialog dialog = new DatePickerDialog(
                        ViewTrack.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int yyyy, int mm, int dd) {
                mm = mm + 1;
                String date;

                if(mm < 10) {
                    date = dd + "/0" + mm + "/" + yyyy;
                } else {
                    date = dd + "/" + mm + "/" + yyyy;
                }

                if(dd < 10) {
                    date = "0" + date;
                }

                dateText.setText(date);

                listTrack(date);
            }
        };
    }

    //menampilkan list track berdasarkan tanggal yang di pilih
    @SuppressLint("Range")
    private void listTrack(String date) {
        String[] dateParts = date.split("/");
        date = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];

        Cursor cursor = getContentResolver().query(DatabaseHelper.TRACK_URI,
                new String[] {DatabaseHelper.TRACK_ID + " id", DatabaseHelper.TRACK_NAME}, DatabaseHelper.TRACK_DATE+ " = ?", new String[] {date}, DatabaseHelper.TRACK_NAME + " ASC");

        trackNames = new ArrayList<TrackItem>();
        adapter.notifyDataSetChanged();
        adapter.clear();
        adapter.notifyDataSetChanged();
        try {
            while(cursor.moveToNext()) {
                TrackItem i = new TrackItem();
                i.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRACK_NAME)));
                i.set_id(cursor.getLong(cursor.getColumnIndex("id")));
                trackNames.add(i);
            }
        } finally {
            if(trackNames != null && trackNames.size() > 0) {
                adapter.notifyDataSetChanged();
                for(int i = 0; i < trackNames.size(); i++) {
                    adapter.add(trackNames.get(i));
                }
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        }
    }
}