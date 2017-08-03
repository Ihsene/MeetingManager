package be.ac.umons.meetingmanager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import be.ac.umons.meetingmanager.connection.UserInfo;
import be.ac.umons.meetingmanager.connection.VolleyConnection;
import be.ac.umons.meetingmanager.meeting.ParticipantAdapter;

public class SeeAddFriendsActivity extends AppCompatActivity {

    private ListView listViewParticipant;
    private Dialog dialog;
    private UserInfo user;
    private TextView noFriends;
    private ParticipantAdapter adapter;
    private ArrayList<UserInfo> friends;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_add_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        noFriends = (TextView) findViewById(R.id.noFriendsTextView);
        user = UserInfo.getUserInfoFromCache(this);
        gson = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();


        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_add_friends_dialog);
        final EditText editText = (EditText) dialog.findViewById(R.id.editTextFriend);
        Button button = (Button) dialog.findViewById(R.id.addFriendButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.setFriend(editText.getText().toString());
                try {
                    handleSaveFriend();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        listViewParticipant = (ListView) findViewById(R.id.listViewFriends);

        friends = new ArrayList<UserInfo>();
        try {
            handleGetFriends();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter = new ParticipantAdapter(this, friends, R.layout.activity_see_add_friends_list);
        listViewParticipant.setAdapter(adapter);
        listViewParticipant.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                handleActionDelete((UserInfo) adapterView.getItemAtPosition(i),i);
                return true;
            }
        });
    }

    public void handleActionDelete(final UserInfo user, final int i) {
        AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert):
                new AlertDialog.Builder(this);
        builder.setTitle(R.string.deleteFriend).setMessage(R.string.confirmationDelete)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            handleRemoveFriendFromDB(user);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        friends.remove(i);
                        adapter.notifyDataSetChanged();
                        noFriends.setVisibility(friends.size() == 0 ? View.VISIBLE: View.INVISIBLE);
                        listViewParticipant.setVisibility(friends.size() != 0 ? View.VISIBLE: View.INVISIBLE);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    public void handleRemoveFriendFromDB(UserInfo friend) throws JSONException {
        user.setFriend(friend.getEmail());
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,(String) getText(R.string.remove_friend_url), new JSONObject(gson.toJson(user)),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SeeAddFriendsActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    public void handleSaveFriend () throws JSONException {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,(String) getText(R.string.add_friend_url), new JSONObject(gson.toJson(user)),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        UserInfo friend = null;
                        try {
                            friend = new UserInfo(response.getString("FIRST_NAME"),response.getString("LAST_NAME"),
                                    response.getString("EMAIL"),"",
                                    "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        friends.add(friend);
                        adapter.notifyDataSetChanged();
                        noFriends.setVisibility(friends.size() == 0 ? View.VISIBLE: View.INVISIBLE);
                        listViewParticipant.setVisibility(friends.size() != 0 ? View.VISIBLE: View.INVISIBLE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SeeAddFriendsActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    public void handleGetFriends() throws JSONException {
        ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();
        jsonObjects.add(new JSONObject(gson.toJson(user)));
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST,(String) getText(R.string.getFriends), new JSONArray(jsonObjects),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        UserInfo user = null;
                        for(int i = 0; i < response.length(); i++)
                        {
                            try {
                                user = new UserInfo(response.getJSONObject(i).getString("FIRST_NAME"),response.getJSONObject(i).getString("LAST_NAME"),
                                        response.getJSONObject(i).getString("EMAIL"),"",
                                        "");
                                friends.add(user);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                        noFriends.setVisibility(friends.size() == 0 ? View.VISIBLE: View.INVISIBLE);
                        listViewParticipant.setVisibility(friends.size() != 0 ? View.VISIBLE: View.INVISIBLE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SeeAddFriendsActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);

    }
}
