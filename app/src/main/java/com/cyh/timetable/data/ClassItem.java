package com.cyh.timetable.data;

import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class ClassItem implements Serializable {
    private ArrayList<TextView> view;
    private ArrayList<Schedule> schedules;

    public ClassItem() {
        this.view = new ArrayList<>();
        this.schedules = new ArrayList<>();
    }

    public void addTextView(TextView v){
        view.add(v);
    }

    public void addSchedule(Schedule schedule){
        schedules.add(schedule);
    }

    public void addSchedules(ArrayList<Schedule> schedules){
        this.schedules.addAll(schedules);
    }

    public ArrayList<TextView> getView() {
        return view;
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassItem classItem = (ClassItem) o;
        return Objects.equals(view, classItem.view) &&
                Objects.equals(schedules, classItem.schedules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(view, schedules);
    }
}
