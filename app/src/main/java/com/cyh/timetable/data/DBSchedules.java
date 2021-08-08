package com.cyh.timetable.data;

import java.util.ArrayList;

public class DBSchedules {

    private final static DBSchedules instance = new DBSchedules();

    private ArrayList<ArrayList<Schedule>> allSchedule;

    private DBSchedules(){
    }

    public static DBSchedules getInstance() {
        return instance;
    }

    public void setDBSchedules(ArrayList<ArrayList<Schedule>> dbSchedules){
        this.allSchedule = new ArrayList<>(dbSchedules);
    }

    public ArrayList<ArrayList<Schedule>> findByClassTitle(String classTitle){
        ArrayList<ArrayList<Schedule>> foundSchedules = new ArrayList<>();
        for (ArrayList<Schedule> schedules : allSchedule) {
            if(schedules.get(0).getClassTitle().contains(classTitle)){
                foundSchedules.add(schedules);
            }
        }
        return foundSchedules;
    }

}
