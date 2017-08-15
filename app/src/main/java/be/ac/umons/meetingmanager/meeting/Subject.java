package be.ac.umons.meetingmanager.meeting;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by SogeP on 02-08-17.
 */

public class Subject implements Parcelable {
    private int id;
    private String name, info;
    private int duration;
    private ArrayList<UserInfo> participants;
    private boolean freeze = false;

    public Subject(String name, String info, int duration, ArrayList<UserInfo> participants) {
        this.name = name;
        this.info = info;
        this.duration = duration;
        this.participants = participants;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ArrayList<UserInfo> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<UserInfo> participants) {
        this.participants = participants;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.info);
        dest.writeInt(this.duration);
        dest.writeTypedList(this.participants);
    }

    protected Subject(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.info = in.readString();
        this.duration = in.readInt();
        this.participants = in.createTypedArrayList(UserInfo.CREATOR);
    }

    public static final Parcelable.Creator<Subject> CREATOR = new Parcelable.Creator<Subject>() {
        @Override
        public Subject createFromParcel(Parcel source) {
            return new Subject(source);
        }

        @Override
        public Subject[] newArray(int size) {
            return new Subject[size];
        }
    };

    public boolean isFreeze() {
        return freeze;
    }

    public void setFreeze(boolean freeze) {
        this.freeze = freeze;
    }
}
