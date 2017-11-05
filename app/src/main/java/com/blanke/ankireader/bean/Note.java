package com.blanke.ankireader.bean;

import android.text.Html;
import android.util.Log;

import com.blanke.ankireader.utils.HtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by on 2016/10/14.
 */

public class Note {
    private long id;
    private String tags;
    private CharSequence front;
    private CharSequence back;
    private List<String> mediaPaths;
    private String primaryMediaPath;

    public Note() {
        mediaPaths = new ArrayList<>();
    }

    public void addMediaPath(String path) {
        this.mediaPaths.add(path);
    }

    public String getPrimaryMediaPath() {
        if (primaryMediaPath == null && mediaPaths.size() > 0) {
            primaryMediaPath = mediaPaths.get(0);
        }
        return primaryMediaPath;
    }

    public CharSequence getFullContent() {
        return Html.fromHtml(front.toString() + "<br/>" + back.toString());
    }

    @Override
    public String toString() {
        return "Note{" +
                "back='" + back + '\'' +
                ", id=" + id +
                ", tags='" + tags + '\'' +
                ", front='" + front + '\'' +
                ", mediaPath='" + mediaPaths + '\'' +
                '}';
    }

    public CharSequence getBack() {
        return back;
    }

    public void setBack(CharSequence back) {
        this.back = back;
    }

    public CharSequence getFront() {
        return front;
    }

    public String getSimpleTextFront() {
        return HtmlUtils.removeAllTags(front.toString()).trim();
    }

    public String getSimpleTextBack() {
        return HtmlUtils.removeAllTags(back.toString()).trim();
    }

    public String getSimpleTextWordBack() {
        String back = getSimpleTextBack();
        Log.d("note", back);
        back = back.replaceAll("英.{0,3}\\[.*?\\].*美.{0,3}\\[.*?\\]", "");//去掉音标
        back = back.replaceAll("[\\.0-9a-zA-Z\\(\\)\\s]", "");//去掉数字 英文
        back = back.trim();
        Pattern r = Pattern.compile("(^.{0,12}[^\\u4e00-\\u9fa5])");// 取出最多前十位,且结尾是非汉字字符
        Matcher m = r.matcher(back);
        if (m.find()) {
            back = m.group(1);
        } else {
            if (back.length() > 10) {
                back = back.substring(0, 10);
            }
        }
        Log.d("note after=", back);
        back = back.replaceAll("[^\\u4e00-\\u9fa5][^\\u4e00-\\u9fa5.]*$", "");//去掉尾部非汉字字符
        Log.d("note end=", back);
        return back;
    }

    public void setFront(CharSequence front) {
        this.front = front;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getMediaPaths() {
        return mediaPaths;
    }

    public void setMediaPaths(List<String> mediaPaths) {
        this.mediaPaths = mediaPaths;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

}
