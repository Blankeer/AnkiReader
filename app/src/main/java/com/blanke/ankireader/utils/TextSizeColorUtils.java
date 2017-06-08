package com.blanke.ankireader.utils;

/**
 * Created by blanke on 2017/6/9.
 */

public class TextSizeColorUtils {

    public static int getSize(String sizeColor) {
        String temp[] = sizeColor.split("\\+");
        return Integer.parseInt(temp[0]);
    }

    public static int getColor(String sizeColor) {
        String temp[] = sizeColor.split("\\+");
        return Integer.parseInt(temp[1]);
    }

    public static String getSizeColor(int size, int color) {
        return size + "+" + color;
    }

}
