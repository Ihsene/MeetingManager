package be.ac.umons.meetingmanager.connection;

import java.util.Arrays;
import java.util.Set;

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

    public UserInfo(String name, String familyName, String email, String id, String token) {
        this.setName(name);
        this.setFamilyName(familyName);
        this.setEmail(email);
        this.setId(id);
        this.setToken(token);
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
}
