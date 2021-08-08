package com.cyh.timetable.data;

import java.io.Serializable;
import java.util.Objects;

public class Schedule implements Serializable {

    private String ClassTitle ="";
    private String ClassPlace ="";
    private String ProfessorName ="";
    private int Day = 0;
    private Time StartTime;
    private Time EndTime;
    private String WebExURI;

    private int registeredAlarmTime = -1;

    public Schedule() {
        this.StartTime = new Time();
        this.EndTime = new Time();
    }

    public String getProfessorName() {
        return ProfessorName;
    }

    public void setProfessorName(String professorName) {
        this.ProfessorName = professorName;
    }

    public String getClassTitle() {
        return ClassTitle;
    }

    public void setClassTitle(String classTitle) {
        this.ClassTitle = classTitle;
    }

    public String getClassPlace() {
        return ClassPlace;
    }

    public void setClassPlace(String classPlace) {
        this.ClassPlace = classPlace;
    }

    public int getDay() {
        return Day;
    }

    public void setDay(int day) {
        this.Day = day;
    }

    public Time getStartTime() {
        return StartTime;
    }

    public void setStartTime(Time startTime) {
        this.StartTime = startTime;
    }

    public Time getEndTime() {
        return EndTime;
    }

    public void setEndTime(Time endTime) {
        this.EndTime = endTime;
    }

    public String getWebExURI() {
        return WebExURI;
    }

    public void setWebExURI(String webExURI) {
        this.WebExURI = webExURI;
    }

    public int getRegisteredAlarmTime() {
        return registeredAlarmTime;
    }

    public void setRegisteredAlarmTime(int minute) {
        this.registeredAlarmTime = minute;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "classTitle='" + ClassTitle + '\'' +
                ", classPlace='" + ClassPlace + '\'' +
                ", professorName='" + ProfessorName + '\'' +
                ", day=" + Day +
                ", startTime=" + StartTime +
                ", endTime=" + EndTime +
                ", webExURI='" + WebExURI + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return Day == schedule.Day &&
                ClassTitle.equals(schedule.ClassTitle) &&
                Objects.equals(ClassPlace, schedule.ClassPlace) &&
                Objects.equals(ProfessorName, schedule.ProfessorName) &&
                Objects.equals(StartTime, schedule.StartTime) &&
                Objects.equals(EndTime, schedule.EndTime) &&
                Objects.equals(WebExURI, schedule.WebExURI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ClassTitle, ClassPlace, ProfessorName, Day, StartTime, EndTime, WebExURI);
    }


}