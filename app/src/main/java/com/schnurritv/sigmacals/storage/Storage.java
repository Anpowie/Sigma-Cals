package com.schnurritv.sigmacals.storage;

import android.content.Context;
import com.schnurritv.sigmacals.util.DateUtil;
import com.schnurritv.sigmacals.util.Debug;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Storage {

    static final int BUFFER_SIZE = 64; // the files should be around 100-200 bytes, so I guess that's a good size
    static Day today, yesterDay, twoDaysAgo, threeDaysAgo;

    public static void init(Context context){
        yesterDay = loadDay(DateUtil.getDate(-1), context);
        twoDaysAgo = loadDay(DateUtil.getDate(-2), context);
        threeDaysAgo = loadDay(DateUtil.getDate(-3), context);

        today = loadDay(DateUtil.getDate(0), context);
        if(today == null)
            today = new Day();
    }

    public static void deleteAllFilesInDirectory(Context context) {
        File directory = context.getFilesDir();
        File[] files = directory.listFiles();

        if (files != null)
            for (File file : files)
                Debug.log("deleted " + file.toString() + "? " + file.delete());
    }

    public static void save(Context context) {
        try {
            FileOutputStream saveFile = context.openFileOutput(DateUtil.getDate(0), Context.MODE_PRIVATE);
            saveFile.write(today.serialize());
            saveFile.close();
        } catch (Exception e) {
            Debug.error(e.toString());
        }
    }

    public static void debugSaveDay(Context context, Day day, int dayOffset) {
        try {
            FileOutputStream saveFile = context.openFileOutput(DateUtil.getDate(dayOffset), Context.MODE_PRIVATE);
            saveFile.write(day.serialize());
            saveFile.close();
        } catch (Exception e) {
            Debug.error(e.toString());
        }
    }

    static Day loadDay(String date, Context context) {

        byte[] data = null;

        try {

            FileInputStream fis = context.openFileInput(date);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1)
                byteArrayOutputStream.write(buffer, 0, bytesRead);

            data = byteArrayOutputStream.toByteArray();
        }catch (Exception e) {
            Debug.error(e.toString());
        }

        if(data == null)
            return null;

        return Day.deserialize(data);
    }
    

    public static Day getToday() {
        return today;
    }

    public static Day getYesterDay() {
        return yesterDay;
    }

    public static Day getTwoDaysAgo() {
        return twoDaysAgo;
    }

    public static Day getThreeDaysAgo() {
        return threeDaysAgo;
    }


}
