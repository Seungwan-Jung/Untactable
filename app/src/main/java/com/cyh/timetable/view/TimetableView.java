package com.cyh.timetable.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.cyh.timetable.R;
import com.cyh.timetable.data.ClassItem;
import com.cyh.timetable.data.Schedule;
import com.cyh.timetable.data.Time;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class TimetableView extends LinearLayout {
    private static final int DEFAULT_ROW_COUNT = 16;
    private static final int DEFAULT_COLUMN_COUNT = 6;
    private static final int DEFAULT_CELL_HEIGHT_DP = 50;
    private static final int DEFAULT_SIDE_CELL_WIDTH_DP = 30;
    private static final int DEFAULT_START_TIME = 9;

    private static final int DEFAULT_SIDE_HEADER_FONT_SIZE_DP = 13;
    private static final int DEFAULT_HEADER_FONT_SIZE_DP = 15;
    private static final int DEFAULT_CLASSITEM_FONT_SIZE_DP = 13;


    private int rowCount;
    private int columnCount;
    private int cellHeight;
    private int sideCellWidth;
    private String[] headerTitle;
    private String[] classItemColors;
    private int startTime;

    private RelativeLayout classItemBox;
    TableLayout tableHeader;
    TableLayout tableBox;


    HashMap<Integer, ClassItem> classItems = new HashMap<>();
    private int classItemCount = -1;

    private OnClassItemSelectedListener classItemSelectedListener = null;
    private OnClassItemLongSelectedListener classItemLongSelectedListener = null;

    public TimetableView(Context context) {
        super(context, null);
    }

    public TimetableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimetableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrs(attrs);
        init();
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TimetableView);
        rowCount = a.getInt(R.styleable.TimetableView_row_count, DEFAULT_ROW_COUNT) - 1;
        columnCount = a.getInt(R.styleable.TimetableView_column_count, DEFAULT_COLUMN_COUNT);
        cellHeight = a.getDimensionPixelSize(R.styleable.TimetableView_cell_height, dp2Px(DEFAULT_CELL_HEIGHT_DP));
        sideCellWidth = a.getDimensionPixelSize(R.styleable.TimetableView_side_cell_width, dp2Px(DEFAULT_SIDE_CELL_WIDTH_DP));
        int titlesId = a.getResourceId(R.styleable.TimetableView_header_title, R.array.default_header_title);
        headerTitle = a.getResources().getStringArray(titlesId);
        int colorsId = a.getResourceId(R.styleable.TimetableView_classItem_colors, R.array.default_classItem_color);
        classItemColors = a.getResources().getStringArray(colorsId);
        startTime = a.getInt(R.styleable.TimetableView_start_time, DEFAULT_START_TIME);
        a.recycle();
    }

    private void init() {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.view_timetable, this, false);
        addView(view);

        classItemBox = view.findViewById(R.id.classItem_box);
        tableHeader = view.findViewById(R.id.table_header);
        tableBox = view.findViewById(R.id.table_box);

        createTable();
    }

    public void add(ArrayList<Schedule> schedules) {
        add(schedules, -1);
    }

    private void add(final ArrayList<Schedule> schedules, int specIdx) {
        final int count = specIdx < 0 ? ++classItemCount : specIdx;
        ClassItem classItem = new ClassItem();
        for (Schedule schedule : schedules) {
            TextView tv = new TextView(getContext());

            RelativeLayout.LayoutParams param = createClassItemParam(schedule);
            tv.setLayoutParams(param);
            tv.setPadding(10, 0, 10, 0);
            tv.setText(MessageFormat.format("{0}\n{1}\n{2}", schedule.getClassTitle(), schedule.getClassPlace(), schedule.getProfessorName()));
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CLASSITEM_FONT_SIZE_DP);
            tv.setTypeface(null, Typeface.BOLD);

            tv.setOnClickListener(v -> {
                if(classItemSelectedListener != null)
                    classItemSelectedListener.OnClassItemSelected(count, schedules);
            });


            tv.setOnLongClickListener(v -> {
                if(classItemLongSelectedListener != null)
                    return classItemLongSelectedListener.OnClassItemLongSelected(count, schedules);
                return false;
            });

            classItem.addTextView(tv);
            classItem.addSchedule(schedule);
            classItems.put(count, classItem);
            classItemBox.addView(tv);
        }
        setClassItemColor();
    }


    public void edit(int idx, ArrayList<Schedule> schedules) {
        remove(idx);
        add(schedules, idx);
    }

    public void removeAll() {
        for (int key : classItems.keySet()) {
            ClassItem classItem = classItems.get(key);
            for (TextView tv : Objects.requireNonNull(classItem).getView()) {
                classItemBox.removeView(tv);
            }
        }
        classItems.clear();
    }

    public void remove(int idx) {
        ClassItem classItem = classItems.get(idx);
        for (TextView tv : Objects.requireNonNull(classItem).getView()) {
            classItemBox.removeView(tv);
        }
        classItems.remove(idx);
        setClassItemColor();
    }

    private void setClassItemColor() {
        int size = classItems.size();
        int[] orders = new int[size];
        int i = 0;
        for (int key : classItems.keySet()) {
            orders[i++] = key;
        }
        Arrays.sort(orders);

        int colorSize = classItemColors.length;

        for (i = 0; i < size; i++) {
            for (TextView v : Objects.requireNonNull(classItems.get(orders[i])).getView()) {
                v.setBackgroundColor(Color.parseColor(classItemColors[i % (colorSize)]));
            }
        }

    }

    private void createTable() {
        createTableHeader();
        for (int i = 0; i < rowCount; i++) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setLayoutParams(createTableLayoutParam());

            for (int k = 0; k < columnCount; k++) {
                TextView tv = new TextView(getContext());
                tv.setLayoutParams(createTableRowParam(cellHeight));
                if (k == 0) {
                    tv.setText(getHeaderTime(i));
                    tv.setTextColor(ResourcesCompat.getColor(getResources(),R.color.colorHeaderText,null));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SIDE_HEADER_FONT_SIZE_DP);
                    tv.setBackgroundColor(ResourcesCompat.getColor(getResources(),R.color.colorHeader,null));
                    tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                    tv.setLayoutParams(createTableRowParam(sideCellWidth, cellHeight));
                } else {
                    tv.setText("");
                    tv.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.item_border,null));
                    tv.setGravity(Gravity.END);
                }
                tableRow.addView(tv);
            }
            tableBox.addView(tableRow);
        }
    }

    private void createTableHeader() {
        TableRow tableRow = new TableRow(getContext());
        tableRow.setLayoutParams(createTableLayoutParam());

        for (int i = 0; i < columnCount; i++) {
            TextView tv = new TextView(getContext());
            if (i == 0) {
                tv.setLayoutParams(createTableRowParam(sideCellWidth, cellHeight));
            } else {
                tv.setLayoutParams(createTableRowParam(cellHeight));
            }
            tv.setTextColor(ResourcesCompat.getColor(getResources(),R.color.colorHeaderText,null));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_HEADER_FONT_SIZE_DP);
            tv.setText(headerTitle[i]);
            tv.setGravity(Gravity.CENTER);

            tableRow.addView(tv);
        }
        tableHeader.addView(tableRow);
    }

    private RelativeLayout.LayoutParams createClassItemParam(Schedule schedule) {
        int cell_w = calCellWidth();

        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(cell_w, calClassItemHeightPx(schedule));
        param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        param.setMargins(sideCellWidth + cell_w * schedule.getDay(), calClassItemTopPxByTime(schedule.getStartTime()), 0, 0);

        return param;
    }

    private int calCellWidth(){
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return (size.x-getPaddingLeft() - getPaddingRight()- sideCellWidth) / (columnCount - 1);
    }

    private int calClassItemHeightPx(Schedule schedule) {
        int startTopPx = calClassItemTopPxByTime(schedule.getStartTime());
        int endTopPx = calClassItemTopPxByTime(schedule.getEndTime());
        return endTopPx - startTopPx;
    }

    private int calClassItemTopPxByTime(Time time) {
        return (time.getHour() - startTime) * cellHeight + (int) ((time.getMinute() / 60.0f) * cellHeight);
    }

    private TableLayout.LayoutParams createTableLayoutParam() {
        return new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
    }

    private TableRow.LayoutParams createTableRowParam(int h_px) {
        return new TableRow.LayoutParams(calCellWidth(), h_px);
    }

    private TableRow.LayoutParams createTableRowParam(int w_px, int h_px) {
        return new TableRow.LayoutParams(w_px, h_px);
    }

    private String getHeaderTime(int i) {
        int p = (startTime + i) % 24;
        int res = p <= 12 ? p : p - 12;
        return res + "";
    }

    static private int dp2Px(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public interface OnClassItemSelectedListener {
        void OnClassItemSelected(int idx, ArrayList<Schedule> schedules);
    }
    public void setOnClassItemSelectEventListener(OnClassItemSelectedListener listener) {
        classItemSelectedListener = listener;
    }

    public interface OnClassItemLongSelectedListener {
        boolean OnClassItemLongSelected(int idx, ArrayList<Schedule> schedules);
    }
    public void setOnClassItemLongSelectEventListener(OnClassItemLongSelectedListener listener) {
        classItemLongSelectedListener = listener;
    }

    public void load(String data) {
        removeAll();
        classItems = loadClassItem(data);
        int maxKey = 0;
        for (int key : classItems.keySet()) {
            ArrayList<Schedule> schedules = Objects.requireNonNull(classItems.get(key)).getSchedules();
            add(schedules, key);
            if (maxKey < key) maxKey = key;
        }
        classItemCount = maxKey + 1;
        setClassItemColor();
    }

    private HashMap<Integer, ClassItem> loadClassItem(String json) {
        HashMap<Integer, ClassItem> classItems = new HashMap<>();
        JsonParser parser = new JsonParser();
        JsonObject rootObject = (JsonObject) parser.parse(json);
        JsonArray arr1 = rootObject.getAsJsonArray("class");
        for (int i = 0; i < arr1.size(); i++) {
            ClassItem classItem = new ClassItem();
            JsonObject indexedObject = (JsonObject) arr1.get(i);
            int idx = indexedObject.get("idx").getAsInt();
            JsonArray indexedArray = (JsonArray) indexedObject.get("schedule");
            for (int k = 0; k < indexedArray.size(); k++) {
                Schedule schedule = new Schedule();
                JsonObject scheduleObject = (JsonObject) indexedArray.get(k);
                schedule.setClassTitle(scheduleObject.get("classTitle").getAsString());
                schedule.setClassPlace(scheduleObject.get("classPlace").getAsString());
                schedule.setProfessorName(scheduleObject.get("professorName").getAsString());
                schedule.setDay(scheduleObject.get("day").getAsInt());
                schedule.setWebExURI(scheduleObject.get("webExURI").getAsString());
                schedule.setRegisteredAlarmTime(scheduleObject.get("alarmTime").getAsInt());

                Time startTime = new Time();
                JsonObject startTimeObject = (JsonObject) scheduleObject.get("startTime");
                startTime.setHour(startTimeObject.get("hour").getAsInt());
                startTime.setMinute(startTimeObject.get("minute").getAsInt());

                Time endTime = new Time();
                JsonObject endTimeObject = (JsonObject) scheduleObject.get("endTime");
                endTime.setHour(endTimeObject.get("hour").getAsInt());
                endTime.setMinute(endTimeObject.get("minute").getAsInt());

                schedule.setStartTime(startTime);
                schedule.setEndTime(endTime);
                classItem.addSchedule(schedule);
            }
            classItems.put(idx, classItem);
        }
        return classItems;
    }

    public String createSaveData() {
        JsonObject rootObject = new JsonObject();
        JsonArray arr1 = new JsonArray();
        int[] orders = getSortedKeySet(classItems);
        for (int order : orders) {
            JsonObject indexedObject = new JsonObject();
            indexedObject.addProperty("idx", order);
            JsonArray indexedArray = new JsonArray();
            ArrayList<Schedule> schedules = Objects.requireNonNull(classItems.get(order)).getSchedules();
            for (Schedule schedule : schedules) {

                JsonObject scheduleObject = new JsonObject();
                scheduleObject.addProperty("classTitle", schedule.getClassTitle());
                scheduleObject.addProperty("classPlace", schedule.getClassPlace());
                scheduleObject.addProperty("professorName", schedule.getProfessorName());
                scheduleObject.addProperty("day", schedule.getDay());
                scheduleObject.addProperty("webExURI", schedule.getWebExURI());
                scheduleObject.addProperty("alarmTime",schedule.getRegisteredAlarmTime());

                JsonObject startTimeObject = new JsonObject();
                startTimeObject.addProperty("hour", schedule.getStartTime().getHour());
                startTimeObject.addProperty("minute", schedule.getStartTime().getMinute());
                scheduleObject.add("startTime", startTimeObject);

                JsonObject endTimeObject = new JsonObject();
                endTimeObject.addProperty("hour", schedule.getEndTime().getHour());
                endTimeObject.addProperty("minute", schedule.getEndTime().getMinute());
                scheduleObject.add("endTime", endTimeObject);

                indexedArray.add(scheduleObject);
            }
            indexedObject.add("schedule", indexedArray);
            arr1.add(indexedObject);
        }
        rootObject.add("class", arr1);
        return rootObject.toString();
    }

    private int[] getSortedKeySet(HashMap<Integer, ClassItem> classItems) {
        int[] orders = new int[classItems.size()];
        int i = 0;
        for (int key : classItems.keySet()) {
            orders[i++] = key;
        }
        Arrays.sort(orders);
        return orders;
    }
}
