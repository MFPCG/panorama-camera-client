package pers.liufushihai.panocamclient.util;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/08/02
 *     desc  : utils about time
 * </pre>
 */

public class TimeUtils {
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");


    /**
     * Milliseconds to the formatted time string.
     * <p>The pattern is {@code yyyy-MM-dd-HH-mm-ss}.</p>
     *
     * @param millis The milliseconds.
     * @return the formatted time string
     */
    public static String millis2String(final long millis) {
        return millis2String(millis, DEFAULT_FORMAT);
    }

    /**
     * Milliseconds to the formatted time string.
     *
     * @param millis The milliseconds.
     * @param format The format.
     * @return the formatted time string
     */
    public static String millis2String(final long millis, final DateFormat format) {
        return format.format(new Date(millis));
    }
}
