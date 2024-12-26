package com.schnurritv.sigmacals.util;

import android.graphics.Bitmap;

public class ImageUtil {

    public static final int GREEN_SCREEN_COLOR = 3079936;

    public static void replaceColor(Bitmap map, int oldColor, int newColor) {
        int width = map.getWidth();
        int height = map.getHeight();

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if(map.getPixel(x, y) == oldColor)
                    map.setPixel(x, y, newColor);
    }
}
