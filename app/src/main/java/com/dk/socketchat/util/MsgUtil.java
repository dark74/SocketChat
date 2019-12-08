package com.dk.socketchat.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MsgUtil {
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static String timeFormat(long timeMills) {
        try {
            format.format(new Date(timeMills));
        } catch (Exception e) {
            return timeMills + "-error";
        }
        return "";
    }
}
