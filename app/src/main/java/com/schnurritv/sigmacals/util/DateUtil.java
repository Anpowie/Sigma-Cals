package com.schnurritv.sigmacals.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtil {

    static final Calendar CALENDAR = Calendar.getInstance();
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public static Weekday getCurrentWeekday() {
        return Weekday.getDay(CALENDAR.get(Calendar.DAY_OF_WEEK));
    }
    public static String getDate(int dayOffset) {
        CALENDAR.add(Calendar.DAY_OF_YEAR, dayOffset);
        String date = DATE_FORMAT.format(CALENDAR.getTime());
        CALENDAR.add(Calendar.DAY_OF_YEAR, -dayOffset);
        return date;
    }

    public enum Weekday {

        SUNDAY(1),
        MONDAY(2),
        TUESDAY(3),
        WEDNESDAY(4),
        THURSDAY(5),
        FRIDAY(6),
        SATURDAY(7);

        private final int n;

        Weekday(int n) {
            this.n = n;
        }

        public static String getAbb(Weekday weekday) {
            return weekday.name().substring(0, 3);
        }

        public static Weekday getDay(int day) {
            for(Weekday weekday : values())
                if(weekday.n == day)
                    return weekday;

            Debug.error(String.format("there is no %s'th day of the week wtf", day));
            return MONDAY;
        }

        public static Weekday yesterdayOf(Weekday day) {

            int yesterday = day.n-1;
            if(yesterday == 0) // the day before sunday is saturday
                yesterday = 7;

            return getDay(yesterday);
        }
    }
}
