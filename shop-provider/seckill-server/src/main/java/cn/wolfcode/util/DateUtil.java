package cn.wolfcode.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by lanxw
 */
public class DateUtil {
    public static boolean isLegalTime(Date date, int time){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY,time);
        Long start = c.getTime().getTime();
        Long now = new Date().getTime();
        c.add(Calendar.HOUR_OF_DAY,2);
        Long end = c.getTime().getTime();
        return now>=start && now<=end;
    }
}
