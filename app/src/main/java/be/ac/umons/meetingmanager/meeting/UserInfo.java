package be.ac.umons.meetingmanager.meeting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import be.ac.umons.meetingmanager.R;

/**
 * Created by SogeP on 27-07-17.
 */

public class UserInfo implements Parcelable {
    private String name;
    private String familyName;
    private String email;
    private String id;
    private String token, tokenFire;
    private boolean taken;
    private String friend;
    private Meeting meeting;
    private boolean isHere;
    private boolean request, asked;

    public UserInfo(String name, String familyName, String email, String id, String token, String tokenFire) {
        this.setName(name);
        this.setFamilyName(familyName);
        this.setEmail(email);
        this.setId(id);
        this.setToken(token);
        this.setTokenFire(tokenFire);
    }

    public static UserInfo getUserInfoFromCache(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(c.getString(R.string.setting), c.MODE_PRIVATE);
        UserInfo user = new UserInfo(
                sharedPreferences.getString(c.getString(R.string.firstName),""),
                sharedPreferences.getString(c.getString(R.string.familyName),""),
                sharedPreferences.getString(c.getString(R.string.email),""),
                sharedPreferences.getString(c.getString(R.string.accountID),""),
                sharedPreferences.getString(c.getString(R.string.accountToken),""),
                sharedPreferences.getString(c.getString(R.string.tokenFire),""));
        return  user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public String getTokenFire() {
        return tokenFire;
    }

    public void setTokenFire(String tokenFire) {
        this.tokenFire = tokenFire;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.familyName);
        dest.writeString(this.email);
        dest.writeString(this.id);
        dest.writeString(this.token);
        dest.writeString(this.tokenFire);
        dest.writeByte(this.taken ? (byte) 1 : (byte) 0);
        dest.writeString(this.friend);
        dest.writeParcelable(this.meeting, flags);
    }

    protected UserInfo(Parcel in) {
        this.name = in.readString();
        this.familyName = in.readString();
        this.email = in.readString();
        this.id = in.readString();
        this.token = in.readString();
        this.tokenFire = in.readString();
        this.taken = in.readByte() != 0;
        this.friend = in.readString();
        this.meeting = in.readParcelable(Meeting.class.getClassLoader());
    }

    public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public boolean isHere() {
        return isHere;
    }

    public void setHere(boolean here) {
        isHere = here;
    }

    public boolean isRequest() {
        return request;
    }

    public void setRequest(boolean request) {
        this.request = request;
    }

    public boolean isAsked() {
        return asked;
    }

    public void setAsked(boolean asked) {
        this.asked = asked;
    }
}
