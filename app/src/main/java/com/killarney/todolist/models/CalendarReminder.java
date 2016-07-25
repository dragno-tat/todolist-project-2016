package com.killarney.todolist.models;

import java.util.Calendar;

/**
 * Created by Anthony on 7/17/2016.
 */
public class CalendarReminder implements Reminder{

    public static final String TYPE = "CALENDAR";
    private Calendar calendar;

    public CalendarReminder(Calendar calendar){
        this.calendar = calendar;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    @Override
    public String toFormattedString() {
        if(calendar.get(Calendar.MINUTE)<10)
            return calendar.get(Calendar.HOUR_OF_DAY) + ":" + "0" + calendar.get(Calendar.MINUTE) + "\t" +
                    (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.DATE) + "-" + calendar.get(Calendar.YEAR);
        else
            return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + "\t" +
                    (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.DATE) + "-" + calendar.get(Calendar.YEAR);
    }

    @Override
    public String getReminderType() {
        return TYPE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CalendarReminder)) return false;

        CalendarReminder that = (CalendarReminder) o;

        if ((calendar.get(Calendar.HOUR_OF_DAY)) != that.calendar.get(Calendar.HOUR_OF_DAY)) return false;
        if ((calendar.get(Calendar.MINUTE)) != that.calendar.get(Calendar.MINUTE)) return false;
        if ((calendar.get(Calendar.DATE)) != that.calendar.get(Calendar.DATE)) return false;
        if ((calendar.get(Calendar.MONTH)) != that.calendar.get(Calendar.MONTH)) return false;
        return ((calendar.get(Calendar.YEAR)) == that.calendar.get(Calendar.YEAR));
    }

    @Override
    public int hashCode() {
        int result = ((Integer) calendar.get(Calendar.HOUR_OF_DAY)).hashCode();
        result = 31 * result + ((Integer) calendar.get(Calendar.MINUTE)).hashCode();
        result = 31 * result + ((Integer) calendar.get(Calendar.DATE)).hashCode();
        result = 31 * result + ((Integer) calendar.get(Calendar.MONTH)).hashCode();
        result = 31 * result + ((Integer) calendar.get(Calendar.YEAR)).hashCode();
        return result;
    }
}
