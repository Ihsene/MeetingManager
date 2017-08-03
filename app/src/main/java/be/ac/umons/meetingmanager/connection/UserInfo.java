package be.ac.umons.meetingmanager.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Set;

import be.ac.umons.meetingmanager.MainActivity;
import be.ac.umons.meetingmanager.R;

/**
 * Created by SogeP on 27-07-17.
 */

public class UserInfo {
    private String name;
    private String familyName;
    private String email;
    private String id;
    private String token;
    private int value;
    private String friend;

    public UserInfo(String name, String familyName, String email, String id, String token) {
        this.setName(name);
        this.setFamilyName(familyName);
        this.setEmail(email);
        this.setId(id);
        this.setToken(token);
    }

    public static UserInfo getUserInfoFromCache(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(c.getString(R.string.setting), c.MODE_PRIVATE);
        UserInfo user = new UserInfo(
                sharedPreferences.getString(c.getString(R.string.firstName),""),
                sharedPreferences.getString(c.getString(R.string.familyName),""),
                sharedPreferences.getString(c.getString(R.string.email),""),
                sharedPreferences.getString(c.getString(R.string.accountID),""),
                sharedPreferences.getString(c.getString(R.string.accountToken),""));
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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }
}
