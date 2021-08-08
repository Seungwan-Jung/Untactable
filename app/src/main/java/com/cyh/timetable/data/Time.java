package com.cyh.timetable.data;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class Time implements Serializable {
    private int Hour = 0;
    private int Minute = 0;

    public Time(int hour, int minute) {
        this.Hour = hour;
        this.Minute = minute;
    }

    public Time() { }

    public int getHour() {
        return Hour;
    }

    public void setHour(int hour) {
        this.Hour = hour;
    }

    public int getMinute() {
        return Minute;
    }

    public void setMinute(int minute) {
        this.Minute = minute;
    }

    public int subTime(Time t){
        return (Hour * 60 + Minute) - (t.Hour * 60 + t.Minute);
    }

    @Override
    public String toString() {
        return "Time{" +
                "hour=" + Hour +
                ", minute=" + Minute +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Time time = (Time) o;
        return Hour == time.Hour &&
                Minute == time.Minute;
    }

    public String getTimeString(){
        return String.format(Locale.KOREA,"%02d:%02d", Hour , Minute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Hour, Minute);
    }
}
