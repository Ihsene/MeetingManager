package be.ac.umons.meetingmanager.meeting;

import java.util.ArrayList;

/**
 * Created by SogeP on 02-08-17.
 */

public class Subject {
    private String name, info;
    private int duration;
    private ArrayList<Integer> participantID;

    public Subject(String name, String info, int duration, ArrayList<Integer> participantID) {
        this.name = name;
        this.info = info;
        this.duration = duration;
        this.participantID = participantID;
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

    public ArrayList<Integer> getParticipantID() {
        return participantID;
    }

    public void setParticipantID(ArrayList<Integer> participantID) {
        this.participantID = participantID;
    }
}
