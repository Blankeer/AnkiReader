package com.blanke.ankireader.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by on 2016/10/14.
 */

public class Note implements Parcelable {
    private long id;
    private String tags;
    private String front;
    private String back;
    private String mediaPath;
    private File media;

    public Note(String back, String front, long id, String mediaPath, String tags) {
        this.back = back;
        this.front = front;
        this.id = id;
        this.mediaPath = mediaPath;
        this.tags = tags;
    }

    public Note() {

    }

    public File getMedia() {
        return media;
    }

    public void setMedia(File media) {
        this.media = media;
    }

    @Override
    public String toString() {
        return "Note{" +
                "back='" + back + '\'' +
                ", id=" + id +
                ", tags='" + tags + '\'' +
                ", front='" + front + '\'' +
                ", mediaPath='" + mediaPath + '\'' +
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

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
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
        dest.writeString(this.mediaPath);
        dest.writeSerializable(this.media);
    }

    protected Note(Parcel in) {
        this.id = in.readLong();
        this.tags = in.readString();
        this.front = in.readString();
        this.back = in.readString();
        this.mediaPath = in.readString();
        this.media = (File) in.readSerializable();
    }

    public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {
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
