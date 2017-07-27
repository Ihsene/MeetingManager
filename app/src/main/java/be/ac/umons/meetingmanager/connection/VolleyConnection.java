package be.ac.umons.meetingmanager.connection;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by SogeP on 26-11-16.
 */

public class VolleyConnection {
    private static VolleyConnection connection;
    private RequestQueue requestQueue;
    private static Context context;

    private  VolleyConnection(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleyConnection getInstance(Context context) {
        if (connection == null) {
            connection = new VolleyConnection(context);
        }
        return connection;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
