package be.ac.umons.meetingmanager.meeting;

import java.util.ArrayList;

import be.ac.umons.meetingmanager.connection.UserInfo;

/**
 * Created by SogeP on 02-08-17.
 */

public class Subject {
    private String name, info;
    private int duration;
    private ArrayList<UserInfo> participants;

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
}
