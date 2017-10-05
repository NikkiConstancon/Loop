package com.zetta.android.browse;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.zetta.android.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Hristian Vitrychenko on 30/08/2017.
 */

public class NotificationsObject implements Parcelable {
    private String noteTitle;
    private String noteContent;
    private int imageSource;
    private int severity;

    public NotificationsObject(String title, String content, int source, int sev)
    {
        noteTitle = title;
        noteContent = content;
        imageSource = source;
        severity = sev;
    }

    protected NotificationsObject(Parcel in) {
        noteTitle = in.readString();
        noteContent = in.readString();
        imageSource = in.readInt();
        severity = in.readInt();
    }

    public static final Creator<NotificationsObject> CREATOR = new Creator<NotificationsObject>() {
        @Override
        public NotificationsObject createFromParcel(Parcel in) {
            return new NotificationsObject(in);
        }

        @Override
        public NotificationsObject[] newArray(int size) {
            return new NotificationsObject[size];
        }
    };

    public String getNoteTitle() {
        return noteTitle;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public int getImageSource() {
        return imageSource;
    }

    public int getSeverity() {
        return severity;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public void setImageSource(int imageSource) {
        this.imageSource = imageSource;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(noteTitle);
        parcel.writeString(noteContent);
        parcel.writeInt(imageSource);
        parcel.writeInt(severity);
    }
}
