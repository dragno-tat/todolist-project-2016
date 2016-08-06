package com.killarney.todolist.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.killarney.todolist.models.Day;
import com.killarney.todolist.models.Event;
import com.killarney.todolist.models.reminder.AbstractRepeatReminder;
import com.killarney.todolist.models.reminder.LocationReminder;
import com.killarney.todolist.models.reminder.OneTimeCalendarReminder;
import com.killarney.todolist.models.reminder.DailyReminder;
import com.killarney.todolist.models.reminder.MonthlyReminder;
import com.killarney.todolist.models.reminder.Reminder;
import com.killarney.todolist.models.reminder.ShortDurationReminder;
import com.killarney.todolist.models.reminder.WeeklyReminder;
import com.killarney.todolist.models.reminder.YearlyReminder;

import java.util.Calendar;
import java.util.Set;

/**
 * Utility class to set up the alarm and notification for reminders
 *
 * Created by Anthony on 7/24/2016.
 */
public final class ReminderManager {

    //prevent instantiation
    private ReminderManager(){
    }

    /**
     *
     * @param context
     * @param event
     * @param depths
     * @return true if successfully set
     */
    public static boolean setAlarm(Context context, Event event, int[] depths){
        Reminder reminder = event.getReminder();
        Bundle bundle = new Bundle();
        bundle.putString("title", event.getTitle());
        bundle.putString("desc", event.getDescription());
        bundle.putInt("id", event.hashCode());
        bundle.putIntArray("depths", depths);
        if(reminder!=null){
            switch(reminder.getReminderType()){
                case OneTimeCalendarReminder.TYPE: {
                    bundle.putString("calendar", CalendarParser.parseCalendar(((OneTimeCalendarReminder) reminder).getCalendar()));
                    break;
                }
                case AbstractRepeatReminder.TYPE: {
                    AbstractRepeatReminder repeatReminder = ((AbstractRepeatReminder) reminder);
                    String[] strings = new String[4];
                    strings[0] = repeatReminder.getRepeatType();
                    switch(repeatReminder.getRepeatType()){
                        case DailyReminder.REPEAT:
                            strings[1] = CalendarParser.parseTime(repeatReminder.getCalendar());
                            break;
                        case WeeklyReminder.REPEAT:
                            strings[1] = CalendarParser.parseTime(repeatReminder.getCalendar());
                            strings[2] = CalendarParser.parseDays(((WeeklyReminder) repeatReminder).getDays());
                            break;
                        case MonthlyReminder.REPEAT:
                            strings[1] = CalendarParser.parseCalendar(repeatReminder.getCalendar());
                            break;
                        case YearlyReminder.REPEAT:
                            strings[1] = CalendarParser.parseCalendar(repeatReminder.getCalendar());
                            break;
                        case ShortDurationReminder.REPEAT:
                            strings[1] = CalendarParser.parseCalendar(repeatReminder.getCalendar());
                            strings[2] = Integer.toString(((ShortDurationReminder) repeatReminder).getHourlyRepeat());
                            strings[3] = Integer.toString(((ShortDurationReminder) repeatReminder).getMinuteRepeat());
                    }
                    bundle.putStringArray("repeat", strings);
                    break;
                }
                case LocationReminder.TYPE:{
                    LocationReminder locationReminder =  ((LocationReminder) reminder);
                    bundle.putBoolean("entering", locationReminder.isEntering());
                    bundle.putInt("radius", locationReminder.getRadius());
                    bundle.putParcelable("latLng", locationReminder.getLatLng());
                    break;
                }
            }
        }
        return setAlarm(context, bundle);
    }

    public static void cancelAlarm(Context context, Event event){
        Intent intent = new Intent(context, NotifyService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, event.hashCode(), intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        LocationManager locationManager = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);
        alarmManager.cancel(pendingIntent);
        try{
            locationManager.removeProximityAlert(pendingIntent);
        }
        catch (SecurityException e){
            Toast.makeText(context, "Location Permissions have not been granted", Toast.LENGTH_SHORT).show();
        }
    }

    protected static boolean setAlarm(Context context, Bundle bundle){
        Intent intent = new Intent(context, NotifyService.class);
        intent.putExtra("title", (String) bundle.get("title"));
        intent.putExtra("desc",(String) bundle.get("desc"));
        intent.putExtra("id",(int) bundle.get("id"));
        intent.putExtra("depths",(int[]) bundle.get("depths"));

        String temp = (String) bundle.get("calendar");
        if(temp!=null){
            Calendar calendar = CalendarParser.unparseCalendar(temp);
            if(calendar!=null){
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getService(context, (int) bundle.get("id"), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                return true;
            }
        }

        String[] strings = (String[]) bundle.get("repeat");
        if(strings!=null){
            intent.putExtra("repeat", strings);
            PendingIntent pendingIntent = PendingIntent.getService(context, (int) bundle.get("id"), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Calendar now = Calendar.getInstance();
            Calendar reminderCalendar;
            switch(strings[0]){
                case DailyReminder.REPEAT:
                    reminderCalendar = CalendarParser.unparseTime(strings[1]);
                    reminderCalendar.set(Calendar.SECOND, 0); //prevent notification loop during the initial minute
                    if(reminderCalendar.before(now)){
                        //use the fact that reminderCalendar.getInstance is called to parse, so time will always be today
                        reminderCalendar.add(Calendar.DATE, 1);
                    }
                    break;
                case WeeklyReminder.REPEAT:
                    reminderCalendar = CalendarParser.unparseTime(strings[1]);
                    reminderCalendar.set(Calendar.SECOND, 0); //prevent notification loop during the initial minute
                    Set<Day> days = CalendarParser.unparseDays(strings[2]);
                    Day today = CalendarParser.calendarDaytoDay(reminderCalendar.get(Calendar.DAY_OF_WEEK));

                    while(reminderCalendar.before(now) || !days.contains(today)){
                        reminderCalendar.add(Calendar.DATE, 1);
                        today = CalendarParser.calendarDaytoDay(reminderCalendar.get(Calendar.DAY_OF_WEEK));
                    }
                    break;
                case MonthlyReminder.REPEAT:
                    reminderCalendar = CalendarParser.unparseCalendar(strings[1]);
                    reminderCalendar.set(Calendar.SECOND, 0); //prevent notification loop during the initial minute
                    while(reminderCalendar.before(now)){
                        reminderCalendar.add(Calendar.MONTH, 1);
                    }
                    break;
                case YearlyReminder.REPEAT:
                    reminderCalendar = CalendarParser.unparseCalendar(strings[1]);
                    reminderCalendar.set(Calendar.SECOND, 0); //prevent notification loop during the initial minute
                    while(reminderCalendar.before(now)){
                        reminderCalendar.add(Calendar.YEAR, 1);
                    }
                    break;
                case ShortDurationReminder.REPEAT:
                    reminderCalendar = CalendarParser.unparseCalendar(strings[1]);
                    reminderCalendar.set(Calendar.SECOND, 0); //prevent notification loop during the initial minute
                    int hourlyRepeat = Integer.parseInt(strings[2]);
                    int minuteRepeat = Integer.parseInt(strings[3]);
                    while(reminderCalendar.before(now)){
                        reminderCalendar.add(Calendar.HOUR_OF_DAY, hourlyRepeat);
                        reminderCalendar.add(Calendar.MINUTE, minuteRepeat);
                    }
                    break;
                default:
                    throw new IllegalArgumentException();

            }
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
            return true;
        }

        LatLng latLng = (LatLng) bundle.get("latLng");
        if(latLng!=null){
            try{
                LocationManager locationManager = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);
                intent.putExtra("entering", bundle.getBoolean("entering"));
                PendingIntent pendingIntent = PendingIntent.getService(context, (int) bundle.get("id"), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                locationManager.addProximityAlert(latLng.latitude, latLng.longitude, bundle.getInt("radius"), -1, pendingIntent);
                Log.d("location", "successfully created" + latLng);
            }
            catch (SecurityException e){
                Toast.makeText(context, "Location Permissions have not been granted", Toast.LENGTH_SHORT).show();
            }
        }

        return false;
    }

}
