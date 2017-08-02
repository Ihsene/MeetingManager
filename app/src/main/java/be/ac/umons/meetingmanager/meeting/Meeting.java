package be.ac.umons.meetingmanager.meeting;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by SogeP on 02-08-17.
 */

public class Meeting {
    private int masterID;
    private String title, place;
    private Date date;
    private ArrayList<Subject> subjects;

    public Meeting() {

    }

    public Meeting(int masterID, String title, String place, Date date, ArrayList<Subject> subjects) {
        this.masterID = masterID;
        this.title = title;
        this.place = place;
        this.date = date;
        this.subjects = subjects;
    }

    public int getMasterID() {
        return masterID;
    }

    public void setMasterID(int masterID) {
        this.masterID = masterID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ArrayList<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(ArrayList<Subject> subjects) {
        this.subjects = subjects;
    }
}
