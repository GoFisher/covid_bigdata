package cn.zjdf.util;

import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Author itcast
 * Date 2020/5/11 14:00
 * Desc
 */
public abstract class TimeUtils {
    public static String format(Long timestamp,String pattern){
        return FastDateFormat.getInstance(pattern).format(timestamp);
    }

    public static void main(String[] args) {
        String format = TimeUtils.format(System.currentTimeMillis(), "yyyy-MM-dd");
        System.out.println(format);
    }
}