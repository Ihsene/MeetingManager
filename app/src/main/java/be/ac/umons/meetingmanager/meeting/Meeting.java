package be.ac.umons.meetingmanager.meeting;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by SogeP on 02-08-17.
 */

/*

TODO
empecher n'importe qui de modifier la réunion d'un autre
afficher le nom de l'utilisateur dans la page principale
ajouter l'envoi de token au refresh dans la classe FirebaseInstanceIDService
 */

public class Meeting implements Comparable<Meeting>,Parcelable {
    private String id;
    private String masterID;
    private String masterName;
    private String title, place, dateToSend;
    private Date date;
    private ArrayList<Subject> subjects;
    private boolean update;

    public Meeting() {
        subjects = new ArrayList<Subject>();
    }

    public Meeting(String title, String place, Date date, ArrayList<Subject> subjects) {
        this.title = title;
        this.place = place;
        this.date = date;
        this.subjects = subjects;
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

    public String getDateToSend() {
        return dateToSend;
    }

    public void setDateToSend(String dateToSend) {
        this.dateToSend = dateToSend;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(@NonNull Meeting meeting) {
        return getDate().compareTo(meeting.getDate());
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.place);
        dest.writeString(this.dateToSend);
        dest.writeLong(this.date != null ? this.date.getTime() : -1);
        dest.writeTypedList(this.subjects);
        dest.writeByte(this.update ? (byte) 1 : (byte) 0);
    }

    protected Meeting(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.place = in.readString();
        this.dateToSend = in.readString();
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
        this.subjects = in.createTypedArrayList(Subject.CREATOR);
        this.update = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Meeting> CREATOR = new Parcelable.Creator<Meeting>() {
        @Override
        public Meeting createFromParcel(Parcel source) {
            return new Meeting(source);
        }

        @Override
        public Meeting[] newArray(int size) {
            return new Meeting[size];
        }
    };

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public String getMasterID() {
        return masterID;
    }

    public void setMasterID(String masterID) {
        this.masterID = masterID;
    }
}
