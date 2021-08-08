package com.cyh.timetable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.cyh.timetable.data.Schedule;
import com.cyh.timetable.data.DBSchedules;
import com.cyh.timetable.fragment.TimeTableOverviewFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        Uri redirect = getIntent().getData();
        if(redirect != null){
            Log.d("redirect to", redirect.toString());
            Intent webView = new Intent(Intent.ACTION_VIEW, redirect);
            startActivity(webView);
            finish();
        }
        else{
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("schedules/");

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<ArrayList<Schedule>> allSchedules = new ArrayList<>();
                    //schedules
                    for (DataSnapshot schedules : snapshot.getChildren()) {
                        ArrayList<Schedule> scheduleData = new ArrayList<>();
                        for (DataSnapshot schedule : schedules.child("schedule").getChildren()) {
                            Schedule value = schedule.getValue(Schedule.class);
                            scheduleData.add(value);
                        }
                        allSchedules.add(scheduleData);
                    }
                    DBSchedules.getInstance().setDBSchedules(allSchedules);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            getSupportFragmentManager().beginTransaction().replace(R.id.content_root, new TimeTableOverviewFragment()).commit();
        }
    }

}