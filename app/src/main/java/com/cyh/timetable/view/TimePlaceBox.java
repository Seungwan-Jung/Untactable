package com.cyh.timetable.view;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import com.cyh.timetable.R;
import com.cyh.timetable.data.Time;

import java.util.Locale;

public class TimePlaceBox extends LinearLayout {

    private Spinner weekDaySpinner;
    private Button startTime;
    private Button endTime;
    private EditText place;

    private Time startTimeValue;
    private Time endTimeValue;

    private OnRemoveButtonClick onRemoveButtonClick;

    public TimePlaceBox(Context context) {
        super(context);
        init();
    }

    public TimePlaceBox(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public TimePlaceBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.time_place_box, this, false);

        startTimeValue = new Time(9,0);
        endTimeValue = new Time(10,0);

        weekDaySpinner = view.findViewById(R.id.button_weekday_select);

        startTime = view.findViewById(R.id.time_set_start);
        startTime.setOnClickListener(view1 -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                    (timePicker1, h, m) -> {
                        startTime.setText(String.format(Locale.KOREA,"%02d:%02d", h, m));
            }, startTimeValue.getHour(),startTimeValue.getMinute(), true);
            timePicker.setTitle("시작 시각");
            timePicker.show();
        });

        endTime = view.findViewById(R.id.time_set_end);
        endTime.setOnClickListener(view1 -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                    (timePicker1, h, m) -> {
                        endTime.setText(String.format(Locale.KOREA,"%02d:%02d", h, m));
                    }, endTimeValue.getHour(),endTimeValue.getMinute(), true);
            timePicker.setTitle("종료 시각");
            timePicker.show();
        });

        place = view.findViewById(R.id.place_text);

        Button removeButton = view.findViewById(R.id.button_remove_time_place);
        removeButton.setOnClickListener(view1 -> onRemoveButtonClick.onRemove());

        addView(view);
    }

    public void setWeekDay(int weekDay){
        weekDaySpinner.setSelection(weekDay);
    }

    public void setStartTime(Time time){
        startTime.setText(time.getTimeString());
    }

    public void setEndTime(Time time){
        endTime.setText(time.getTimeString());
    }

    public void setPlace(String place){
        this.place.setText(place);
    }

    public int getWeekday(){
        return weekDaySpinner.getSelectedItemPosition();
    }

    public Time getStartTime(){
        String[] timeValue = String.valueOf(startTime.getText()).split(":");
        startTimeValue.setHour(Integer.parseInt(timeValue[0]));
        startTimeValue.setMinute(Integer.parseInt(timeValue[1]));
        return startTimeValue;
    }


    public Time getEndTime(){
        String[] timeValue = String.valueOf(endTime.getText()).split(":");
        endTimeValue.setHour(Integer.parseInt(timeValue[0]));
        endTimeValue.setMinute(Integer.parseInt(timeValue[1]));
        return endTimeValue;
    }

    public String getPlace(){
        return String.valueOf(place.getText());
    }

    public interface OnRemoveButtonClick {
        void onRemove();
    }
    public void setOnRemoveButtonClicked(OnRemoveButtonClick removeButtonClicked){
        onRemoveButtonClick = removeButtonClicked;
    }

}
