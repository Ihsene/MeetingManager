package be.ac.umons.meetingmanager.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.options.OptionActivity;

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

    public boolean checkVPN() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = cm.getAllNetworks();
            for (int i = 0; i < networks.length; i++) {
                NetworkCapabilities caps = null;
                caps = cm.getNetworkCapabilities(networks[i]);
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                    return true;
            }
        }
        return false;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        //if(checkVPN())
            getRequestQueue().add(req);
       /*else
            Toast.makeText(context, R.string.vpn, Toast.LENGTH_LONG).show();*/
    }
}
