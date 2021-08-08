package com.cyh.timetable.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.cyh.timetable.R;
import com.cyh.timetable.WebExRedirectWorker;
import com.cyh.timetable.data.Schedule;
import com.cyh.timetable.data.Time;
import com.cyh.timetable.view.ScheduleSearchDialog;
import com.cyh.timetable.view.TimePlaceBox;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class EditFragment extends Fragment {

    private List<String> enqueuedInfoTag;
    private WorkManager manager;

    private View view;

    private LinearLayout timePlaceList;

    private int scheduleNumber;

    private EditText classTitle;
    private EditText classProf;
    private EditText classUri;

    private SwitchCompat alarmOnOff;
    private ToggleButton vibeToggle;
    private NumberPicker alarmTime;

    private int currentAlarmTime = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_class,container,false);

        initCommon();

        String status = Objects.requireNonNull(getArguments()).getString("act");
        getArguments().remove("act");

        if(status.equals("new")){
            initNew();
        }
        else if(status.equals("edit")){

            initEdit();
        }

        return view;

    }


    private void initCommon(){
        manager = WorkManager.getInstance(getContext());

        Button addTimePlace = view.findViewById(R.id.button_add_time_place_box);
        addTimePlace.setOnClickListener(view1 -> {
            onTimePlaceAdd();
        });

        Button cancelEdit = view.findViewById(R.id.edit_cancel);
        cancelEdit.setOnClickListener(view1 -> {
            onCancel();
        });

        classTitle = view.findViewById(R.id.edit_class_title);
        classProf = view.findViewById(R.id.edit_class_prof);
        classUri = view.findViewById(R.id.edit_class_uri);
        timePlaceList = view.findViewById(R.id.time_place_list);
        alarmOnOff = view.findViewById(R.id.alarm_on_off);
        vibeToggle = view.findViewById(R.id.vibe_toggle_Button);
        alarmTime = view.findViewById(R.id.alarm_time);

        vibeToggle.setVisibility(View.GONE);
        alarmTime.setVisibility(View.GONE);
        alarmOnOff.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if(isChecked){
                vibeToggle.setVisibility(View.VISIBLE);
                alarmTime.setVisibility(View.VISIBLE);
            }
            else{
                vibeToggle.setVisibility(View.GONE);
                alarmTime.setVisibility(View.GONE);
            }
        });

        String[] displayTimes = new String[60];
        for (int i = 0; i<60; ++i){
            displayTimes[i] = i+"분 전 알림";
        }
        alarmTime.setMinValue(0);
        alarmTime.setMaxValue(59);
        alarmTime.setValue(5);
        alarmTime.setDisplayedValues(displayTimes);

        scheduleNumber = 0;
    }

    private void initNew(){
        TextView title = view.findViewById(R.id.edit_class_main_title);
        title.setText("과목 추가");


        LinearLayout buttonList = view.findViewById(R.id.edit_button_list);
        Button searchButton = new Button(getContext());
        searchButton.setLayoutParams(view.findViewById(R.id.edit_confirm).getLayoutParams());
        searchButton.setText("검색");
        searchButton.setTextColor(Color.BLACK);
        searchButton.setOnClickListener(view1 -> {
            onSearch();
        });

        buttonList.addView(searchButton, 1);

        Button confirmButton = view.findViewById(R.id.edit_confirm);
        confirmButton.setText("추가");
        confirmButton.setOnClickListener(view1 -> {
            onNew();
        });
    }

    private void onSearch() {
        ScheduleSearchDialog searchDialog = new ScheduleSearchDialog(getContext());
        searchDialog.setItemSelectedListener(this::setUpExistSchedule);
        searchDialog.setCanceledOnTouchOutside(true);
        searchDialog.setCancelable(true);
        searchDialog.show();
        searchDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

    private void initEdit(){
        ArrayList<Schedule> schedules = (ArrayList<Schedule>) getArguments().getSerializable("schedules");

        TextView title = view.findViewById(R.id.edit_class_main_title);
        title.setText("과목 수정");

        setUpExistSchedule(schedules);

        LinearLayout buttonList = view.findViewById(R.id.edit_button_list);
        Button removeButton = new Button(getContext());
        removeButton.setLayoutParams(view.findViewById(R.id.edit_confirm).getLayoutParams());
        removeButton.setText("제거");
        removeButton.setTextColor(Color.BLACK);
        removeButton.setOnClickListener(view1 -> {
            onRemove();
        });

        buttonList.addView(removeButton, 1);

        Button confirmButton = view.findViewById(R.id.edit_confirm);
        confirmButton.setText("수정");
        confirmButton.setOnClickListener(view1 -> {
            onEdit();
        });

        enqueuedInfoTag = new ArrayList<>();
        for(int i = 0; i < scheduleNumber; ++i){
            TimePlaceBox timePlaceBox = (TimePlaceBox) timePlaceList.getChildAt(i);
            int weekDay = timePlaceBox.getWeekday();
            Time startTime =timePlaceBox.getStartTime();
            String tag = classTitle.getText().toString()
                    + classProf.getText().toString()
                    + weekDay
                    + startTime.getTimeString();
            try {
                List<WorkInfo> info = manager.getWorkInfosByTag(tag).get();
                int size = info.size();
                Log.d("TAG", "initEdit: "+size);

                if(size>0){
                    for (WorkInfo workInfo : info) {
                        if(workInfo.getState() == WorkInfo.State.ENQUEUED){
                            enqueuedInfoTag.add(tag);
                            alarmOnOff.setChecked(true);
                            vibeToggle.setVisibility(View.VISIBLE);
                            alarmTime.setVisibility(View.VISIBLE);
                            alarmTime.setValue(schedules.get(0).getRegisteredAlarmTime());
                        }
                    }
                }

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void setUpExistSchedule(ArrayList<Schedule> schedules){

        classTitle.setText(schedules.get(0).getClassTitle());
        classProf.setText(schedules.get(0).getProfessorName());
        classUri.setText(schedules.get(0).getWebExURI());
        timePlaceList.removeAllViews();
        scheduleNumber = 0;
        for (Schedule schedule : schedules) {
            TimePlaceBox timePlaceBox = createNewTimePlaceBox(schedule.getDay(), schedule.getStartTime(), schedule.getEndTime(), schedule.getClassPlace());
            timePlaceList.addView(timePlaceBox,timePlaceList.getChildCount() - 1);
            scheduleNumber += 1;
        }
    }

    private void onTimePlaceAdd(){
        TimePlaceBox timePlaceBox = createNewTimePlaceBox(0, new Time(9,0), new Time(10,0), "");
        timePlaceList.addView(timePlaceBox,timePlaceList.getChildCount() - 1);
        scheduleNumber += 1;
    }

    private void onCancel(){
        Bundle bundle = new Bundle();
        bundle.putString("status","cancel");

        TimeTableOverviewFragment t = new TimeTableOverviewFragment();
        t.setArguments(bundle);

        doTransaction(t);

    }

    private void onRemove(){
        Bundle bundle = new Bundle();
        bundle.putString("status","remove");
        removeAlarm();
        int idx = getArguments().getInt("idx");
        bundle.putInt("idx",idx);

        TimeTableOverviewFragment t = new TimeTableOverviewFragment();
        t.setArguments(bundle);

        doTransaction(t);
    }

    private void onNew(){
        if(isInvalidSchedule()){
            return;
        }

        if(alarmOnOff.isChecked()){
            registerAlarm();
        }
        ArrayList<Schedule> schedules = makeScheduleList();
        Bundle bundle = new Bundle();
        bundle.putString("status","new");
        bundle.putSerializable("schedules",schedules);

        TimeTableOverviewFragment t = new TimeTableOverviewFragment();
        t.setArguments(bundle);

        doTransaction(t);
    }

    private void onEdit(){

        if(isInvalidSchedule()){
            return;
        }
        removeAlarm();
        if(alarmOnOff.isChecked()){
            registerAlarm();
        }
        ArrayList<Schedule> schedules = makeScheduleList();
        Bundle bundle = new Bundle();
        bundle.putString("status","edit");
        bundle.putInt("idx", getArguments().getInt("idx"));
        bundle.putSerializable("schedules",schedules);

        TimeTableOverviewFragment t = new TimeTableOverviewFragment();
        t.setArguments(bundle);

        doTransaction(t);
    }

    private boolean isInvalidSchedule(){
        if(invalidTitle()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("과목명 정보 없음");
            builder.setMessage("과목명이 등록되지 않았습니다.")
                    .setCancelable(true)
                    .setPositiveButton("확인",(dialogInterface, i) -> {});
            builder.show();
            return true;
        }

        if(invalidProf()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("교수 정보 없음");
            builder.setMessage("교수가 등록되지 않았습니다.")
                    .setCancelable(true)
                    .setPositiveButton("확인",(dialogInterface, i) -> {});
            builder.show();
            return true;
        }

        if(invalidURI()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("올바르지 않은 WebEX 주소");
            builder.setMessage("WebEx 주소가 올바르지 않습니다.")
                    .setCancelable(true)
                    .setPositiveButton("확인",(dialogInterface, i) -> {
                        classUri.post(() -> {
                            classUri.setFocusableInTouchMode(true);
                            classUri.requestFocus();
                        });
                    });
            builder.show();
            return true;
        }

        if(emptyTimeSchedule()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("시간/장소 정보 없음");
            builder.setMessage("시간/장소 정보가 등록되지 않았습니다.")
                    .setCancelable(true)
                    .setPositiveButton("확인",(dialogInterface, i) -> {});
            builder.show();
            return true;
        }
        if(invalidTimeSchedule()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("시간 정보 오류");
            builder.setMessage("입력된 시작 시간보다 종료시간이 빠릅니다.")
                    .setCancelable(true)
                    .setPositiveButton("확인",(dialogInterface, i) -> {});
            builder.show();
            return true;
        }

        return false;
    }

    private boolean invalidURI(){
        return !URLUtil.isValidUrl(String.valueOf(classUri.getText()));
    }

    private boolean invalidTitle(){
        return String.valueOf(classTitle.getText()).equals("");
    }

    private boolean invalidProf(){
        return String.valueOf(classProf.getText()).equals("");
    }

    private boolean emptyTimeSchedule(){
        return scheduleNumber <= 0;
    }

    private boolean invalidTimeSchedule(){
        for(int i = 0; i<scheduleNumber; ++i) {
            TimePlaceBox timePlaceBox = (TimePlaceBox) timePlaceList.getChildAt(i);
            Time start = timePlaceBox.getStartTime();
            Time end = timePlaceBox.getEndTime();
            if(start.subTime(end) <= 0){
                return false;
            }
        }
        return true;
    }

    private void registerAlarm(){
        for(int i = 0; i<scheduleNumber; ++i) {

            TimePlaceBox timePlaceBox = (TimePlaceBox) timePlaceList.getChildAt(i);
            int weekDay = timePlaceBox.getWeekday();
            Time startTime = timePlaceBox.getStartTime();

            Calendar current = Calendar.getInstance();

            Calendar future = Calendar.getInstance();
            future.set(Calendar.DAY_OF_WEEK, weekDay+2);
            future.set(Calendar.HOUR_OF_DAY, startTime.getHour());
            future.set(Calendar.MINUTE, startTime.getMinute() - alarmTime.getValue());
            future.set(Calendar.SECOND,0);

            //현재가 등록예정보다 미래라면
            if(current.getTime().compareTo(future.getTime()) > 0){
                //다음주로 등록
                future.set(Calendar.WEEK_OF_YEAR, current.get(Calendar.WEEK_OF_YEAR) + 1);
            }


            Log.e("alarm", "registerAlarm: "+future.getTime());
            long mil = future.getTimeInMillis() - current.getTimeInMillis();


            Data notifyData = new Data.Builder()
                    .putString("WebExURI",classUri.getText().toString())
                    .putString("Title", classTitle.getText().toString())
                    .putInt("Time", alarmTime.getValue())
                    .putBoolean("mode", vibeToggle.isChecked())
                    .build();

            PeriodicWorkRequest workRequest =
                    new PeriodicWorkRequest
                            .Builder(WebExRedirectWorker.class, 7, TimeUnit.DAYS)
                            .setInitialDelay(mil, TimeUnit.MILLISECONDS)
                            .addTag(classTitle.getText().toString()
                                    + classProf.getText().toString()
                                    + weekDay
                                    + startTime.getTimeString())
                            .setInputData(notifyData)
                            .build();
            manager.enqueue(workRequest);
            currentAlarmTime = alarmTime.getValue();
        }

    }

    private void removeAlarm(){
        for (String tag : enqueuedInfoTag) {
            Log.d("TAG", "removeAlarm: "+tag);
            manager.cancelAllWorkByTag(tag);
            currentAlarmTime = -1;
        }
    }

    private void doTransaction(TimeTableOverviewFragment timeTableOverviewFragment){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_up_in, R.anim.slide_up_out);
        transaction.replace(R.id.content_root, timeTableOverviewFragment);
        transaction.commit();
        getFragmentManager().popBackStack();
    }

    private TimePlaceBox createNewTimePlaceBox(int day, Time start, Time end, String place){
        TimePlaceBox timePlaceBox = new TimePlaceBox(getContext());
        timePlaceBox.setWeekDay(day);
        timePlaceBox.setStartTime(start);
        timePlaceBox.setEndTime(end);
        timePlaceBox.setPlace(place);
        timePlaceBox.setOnRemoveButtonClicked(() -> {
            timePlaceList.removeView(timePlaceBox);
            scheduleNumber -= 1;
        });

        return timePlaceBox;
    }

    private ArrayList<Schedule> makeScheduleList(){
        ArrayList<Schedule> schedules = new ArrayList<>();

        for(int i = 0; i<scheduleNumber; ++i){
            Schedule schedule = new Schedule();
            schedule.setClassTitle(String.valueOf(classTitle.getText()));
            schedule.setProfessorName(String.valueOf(classProf.getText()));
            schedule.setWebExURI(String.valueOf(classUri.getText()));

            TimePlaceBox timePlaceBox = (TimePlaceBox) timePlaceList.getChildAt(i);
            schedule.setDay(timePlaceBox.getWeekday());
            schedule.setStartTime(timePlaceBox.getStartTime());
            schedule.setEndTime(timePlaceBox.getEndTime());
            schedule.setClassPlace(timePlaceBox.getPlace());
            schedule.setRegisteredAlarmTime(currentAlarmTime);

            schedules.add(schedule);
        }
        return schedules;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}
