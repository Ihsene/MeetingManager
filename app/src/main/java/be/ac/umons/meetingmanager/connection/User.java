package be.ac.umons.meetingmanager.connection;

/**
 * Created by SogeP on 27-07-17.
 */

public class User {
    private String name, familyName, email, id, token;

    public User(String name, String familyName, String email, String id, String token) {
        this.name = name;
        this.familyName = familyName;
        this.email = email;
        this.id = id;
        this.token = token;
    }
}
