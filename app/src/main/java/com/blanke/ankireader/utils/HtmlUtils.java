package com.blanke.ankireader.utils;

import android.text.Html;

/**
 * Created by blanke on 2017/6/11.
 */

public class HtmlUtils {
    public static String removeAllTags(String htmltext) {
        return Html.fromHtml(htmltext).toString().replaceAll("\n", "").trim();
    }

    public static String removeNoBrTags(String htmltext) {
        return Html.fromHtml(htmltext).toString().trim();
    }
}
