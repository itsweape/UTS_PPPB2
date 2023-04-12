package com.example.runnertracker;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;


public class DynamicFragment extends Fragment {

    private static final int YES = 0;
    private static final int NO = 1;

    public DynamicFragment() {
    }

    public static DynamicFragment newInstance() {
        return new DynamicFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dynamic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RadioGroup radioGroup = view.findViewById(R.id.radio_group);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                View radioButton = radioGroup.findViewById(i);
                int index = radioGroup.indexOfChild(radioButton);
                TextView textView = view.findViewById(R.id.fragment_header);

                switch (index){
                    case YES :
                        Intent intent = new Intent(getActivity(), RecordTrack.class);
                        startActivity(intent);
                        break;
                    case NO :
                        Intent intents = new Intent(getActivity(), ViewTrack.class);
                        startActivity(intents);
                        break;
                    default:
                        break;
                }
            }
        });
    }
}