package com.schnurritv.sigmacals.util;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ScrollView;
import com.schnurritv.sigmacals.R;
import com.schnurritv.sigmacals.ui.meals.MealUi;

import java.io.File;

public class Util {

    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }
    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }
    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }


    public static int lerp(int start, int end, float p) {

        return (int) (start + (end - start) * p);
    }

    public static float lerp(float start, float end, float p) {

        return start + (end - start) * p;
    }

    public static void lerpScale(Rect target, Rect start, Rect end, float p) {

        int widthStart = Math.abs(start.right - start.left);
        int widthEnd = Math.abs(end.right - end.left);
        int targetWidth = lerp(widthStart, widthEnd, p);

        int centerX = lerp(end.left, end.right, 0.5f);
        int centerY = lerp(end.top, end.bottom, 0.5f);


        target.top = centerY - targetWidth/2;
        target.bottom = centerY + targetWidth/2;
        target.left = centerX - targetWidth/2;
        target.right = centerX + targetWidth/2;
    }

    public static void resizeRect(Rect target, Rect ref, float p) {
        float centerX = Util.lerp((float)ref.left, ref.right, 0.5f);
        float centerY = Util.lerp((float)ref.top, ref.bottom, 0.5f);

        target.top = (int) (centerY - p * (MealUi.MARGIN / 2f));
        target.bottom = (int) (centerY + p * (MealUi.MARGIN / 2f));
        target.left = (int) (centerX - p * (MealUi.MARGIN / 2f));
        target.right = (int) (centerX + p * (MealUi.MARGIN / 2f));
    }

    public static void runAfter(int millis, Runnable code) {
        new Thread(()-> {
            try {Thread.sleep(millis);} catch (Exception ignored) {}
            new Handler(Looper.getMainLooper()).post(code);
        }).start();


    }

    public static int roundToNearestHundred(int number) {
        int quotient = number / 100;
        int rounded = quotient * 100;

        if (number - rounded >= 50)
            rounded += 100;

        return rounded;
    }

}
