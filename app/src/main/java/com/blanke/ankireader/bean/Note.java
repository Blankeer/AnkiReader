package com.blanke.ankireader.bean;

import android.text.Html;

import java.util.ArrayList;
import java.util.List;

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
