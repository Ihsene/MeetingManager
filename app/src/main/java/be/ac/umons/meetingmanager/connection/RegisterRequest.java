package be.ac.umons.meetingmanager.connection;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SogeP on 26-11-16.
 */

public class RegisterRequest extends StringRequest {
    private User user;

    public RegisterRequest(User user, int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.user = user;
    }

    @Override
    protected Map<String,String> getParams() {
        Map<String,String> params = new HashMap<String, String>();
        params.put("", new Gson().toJson(user));
        return params;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
