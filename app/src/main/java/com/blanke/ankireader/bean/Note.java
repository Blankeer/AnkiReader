package com.blanke.ankireader.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by on 2016/10/14.
 */

public class Note implements Parcelable {
    private long id;
    private String tags;
    private String front;
    private String back;
    private List<String> mediaPaths;
    private List<File> medias;

    public Note() {
        mediaPaths = new ArrayList<>();
        medias = new ArrayList<>();
    }

    public List<File> getMedias() {
        return medias;
    }

    public void setMedias(List<File> medias) {
        this.medias = medias;
    }

    public void addMedia(File media) {
        this.medias.add(media);
    }

    public void addMediaPath(String path) {
        this.mediaPaths.add(path);
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

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public String getFront() {
        return front;
    }

    public void setFront(String front) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.tags);
        dest.writeString(this.front);
        dest.writeString(this.back);
        dest.writeStringList(this.mediaPaths);
        dest.writeList(this.medias);
    }

    protected Note(Parcel in) {
        this.id = in.readLong();
        this.tags = in.readString();
        this.front = in.readString();
        this.back = in.readString();
        this.mediaPaths = in.createStringArrayList();
        this.medias = new ArrayList<File>();
        in.readList(this.medias, File.class.getClassLoader());
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel source) {
            return new Note(source);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };
}
