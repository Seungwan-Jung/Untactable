package com.cyh.timetable.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cyh.timetable.R;
import com.cyh.timetable.data.Schedule;
import com.cyh.timetable.view.TimetableView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

public class TimeTableOverviewFragment extends Fragment {

    private TimetableView timetableView = null;

    private ImageButton newButton = null;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable_main,container,false);
        timetableView = view.findViewById(R.id.timetable);
        EditFragment editFragment = new EditFragment();

        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        String saved = preferences.getString("time_table_data","");

        if(saved != null && !Objects.equals(saved, "")){
            timetableView.load(saved);
        }

        timetableView.setOnClassItemSelectEventListener((idx, s1) -> {
            Intent webView = new Intent(Intent.ACTION_VIEW, Uri.parse(s1.get(0).getWebExURI()));
            startActivity(webView);
        });

        timetableView.setOnClassItemLongSelectEventListener((idx, s2) -> {
            Bundle bundle = new Bundle();
            bundle.putString("act","edit");
            bundle.putInt("idx",idx);
            bundle.putSerializable("schedules",s2);
            editFragment.setArguments(bundle);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_down_in, R.anim.slide_down_out, R.anim.slide_up_in, R.anim.slide_up_out);
            transaction.replace(R.id.content_root, editFragment);
            transaction.addToBackStack("Edit");

            transaction.commit();

            return true;
        });


        newButton = view.findViewById(R.id.schedule_add);
        newButton.setOnClickListener(
                button -> {
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("act","new");

                    editFragment.setArguments(bundle2);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.slide_down_in, R.anim.slide_down_out, R.anim.slide_up_in, R.anim.slide_up_out);
                    transaction.replace(R.id.content_root, editFragment);
                    transaction.addToBackStack("Edit");
                    transaction.commit();
                }
        );

        String status = null;
        if (getArguments() != null) {
            status = getArguments().getString("status");
        }

        if(status != null){
            getArguments().remove("status");
            switch (status) {
                case "new":
                    timetableView.add((ArrayList<Schedule>) getArguments().getSerializable("schedules"));
                    saveTable();
                    break;
                case "edit": {
                    int idx = getArguments().getInt("idx");
                    timetableView.edit(idx, (ArrayList<Schedule>) getArguments().getSerializable("schedules"));
                    saveTable();
                    break;
                }
                case "remove": {
                    int idx = getArguments().getInt("idx");
                    timetableView.remove(idx);
                    saveTable();
                    break;
                }
            }
        }

        return view;
    }

    private void saveTable(){
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("time_table_data",timetableView.createSaveData());

        editor.apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveTable();
    }
}
