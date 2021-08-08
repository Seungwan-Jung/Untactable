package com.cyh.timetable.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cyh.timetable.R;
import com.cyh.timetable.data.DBSchedules;
import com.cyh.timetable.data.Schedule;

import java.util.ArrayList;

public class ScheduleSearchDialog extends Dialog {

    private LinearLayout searchResult;
    private EditText searchData;
    private Button cancelButton;
    private OnItemSelectedListener itemSelectedListener;

    public ScheduleSearchDialog(@NonNull Context context) {
        super(context);
    }

    public ScheduleSearchDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ScheduleSearchDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setItemSelectedListener(OnItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_search_class);
        searchData = findViewById(R.id.search_data);
        searchResult = findViewById(R.id.search_results);
        cancelButton = findViewById(R.id.search_cancel);
        String[] weekdays = getContext().getResources().getStringArray(R.array.weekdays);

        searchData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                searchResult.removeAllViews();
                ArrayList<ArrayList<Schedule>> allSchedules = DBSchedules.getInstance().findByClassTitle(String.valueOf(charSequence));

                for (ArrayList<Schedule> schedules : allSchedules) {

                    TextView textView = new TextView(getContext());
                    textView.setBackgroundResource(R.drawable.item_border);

                    textView.setOnClickListener(view -> {
                        itemSelectedListener.onItemSelected(schedules);
                        dismiss();
                    });
                    StringBuilder timeSchedule = new StringBuilder();

                    timeSchedule.append(schedules.get(0).getClassTitle()).append("\n")
                            .append(schedules.get(0).getClassPlace()).append("\n")
                            .append(schedules.get(0).getProfessorName());

                    for (Schedule schedule : schedules) {
                        timeSchedule.append("\n")
                                    .append(weekdays[schedule.getDay()])
                                    .append(" ")
                                    .append(schedule.getStartTime().getTimeString())
                                    .append(" ~ ")
                                    .append(schedule.getEndTime().getTimeString());
                    }
                    textView.setText(timeSchedule.toString());
                    searchResult.addView(textView);
                }

            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        cancelButton.setOnClickListener(view -> dismiss());
    }

    public interface OnItemSelectedListener {
        void onItemSelected(ArrayList<Schedule> selectedSchedules);
    }


}
