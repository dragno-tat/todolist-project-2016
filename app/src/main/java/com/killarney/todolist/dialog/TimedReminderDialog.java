package com.killarney.todolist.dialog;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.widget.TextView;

/**
 * Created by Anthony on 7/18/2016.
 */
public abstract class TimedReminderDialog extends ReminderDialog implements TimePickerFragment.TimePickerListener, DatePickerFragment.DatePickerListener {
    protected TextView time;
    protected TextView date;
    protected int year;
    protected int month;
    protected int day;
    protected int hourOfDay;
    protected int minute;

    /**
     * Allows user to select a date
     */
    protected void showDatePickerDialog() {
        FragmentManager manager = getFragmentManager();
        DatePickerFragment dialog = new DatePickerFragment();
        dialog.setListener(this);
        dialog.show(manager, "dateDialog");
    }

    /**
     * Allows user to select a time
     */
    protected void showTimePickerDialog() {
        FragmentManager manager = getFragmentManager();
        TimePickerFragment dialog = new TimePickerFragment();
        dialog.setListener(this);
        dialog.show(manager, "timeDialog");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setTime(int hourOfDay, int minute) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
        //format the displayed time
        if(minute >= 10)
            time.setText(this.hourOfDay + ":" + this.minute);
        else
            time.setText(this.hourOfDay + ":" + "0" + this.minute);
    }

    @Override
    public void setDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        //format the displayed text
        date.setText((this.month + 1) + "-" + this.day + "-" + this.year, TextView.BufferType.EDITABLE);
    }
}
