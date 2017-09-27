package be.ac.umons.meetingmanager.options;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.meeting.UserInfo;
import be.ac.umons.meetingmanager.connection.VolleyConnection;
import be.ac.umons.meetingmanager.meeting.adapters.UserAdapter;

public class SeeAddFriendsActivity extends AppCompatActivity {

    private ListView listViewParticipant;
    private Dialog dialog;
    private UserInfo user;
    private TextView noFriends;
    private UserAdapter adapter;
    private ArrayList<UserInfo> friends;
    private ProgressBar progressBar;
    private Gson gson;
    private EditText searchEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_add_friends);
        searchEdit = (EditText) findViewById(R.id.search_bar_edit);
        setTitle(R.string.friendListTitle);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        noFriends = (TextView) findViewById(R.id.noFriendsTextView);
        noFriends.setVisibility(View.INVISIBLE);
        user = UserInfo.getUserInfoFromCache(this);
        gson = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);


        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_add_friends_dialog);
        final EditText editText = (EditText) dialog.findViewById(R.id.editTextFriend);

        final Button button = (Button) dialog.findViewById(R.id.addFriendButton);
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
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    button.performClick();
                    return true;
                }
                return false;
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

        adapter = new UserAdapter(this, friends, R.layout.activity_see_add_friends_list);
        listViewParticipant.setAdapter(adapter);
        listViewParticipant.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                handleActionDelete((UserInfo) adapterView.getItemAtPosition(i),i);
                return true;
            }
        });

        searchEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void handleActionDelete(final UserInfo friendToDelete, final int i) {
        AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert):
                new AlertDialog.Builder(this);
        builder.setTitle(R.string.deleteFriend).setMessage(R.string.confirmationDelete)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            handleRemoveOrAcceptFriendFromDB(user, friendToDelete, SeeAddFriendsActivity.this, gson, true);
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

    public static void handleRemoveOrAcceptFriendFromDB(UserInfo user, final UserInfo friend, final Context context, Gson gson, final boolean remove) throws JSONException {
        user.setFriend(friend.getEmail());
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,(String) context.getText(remove ?R.string.remove_friend_url:R.string.accept_friend_url), new JSONObject(gson.toJson(user)),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(context, friend.getName()+" "+friend.getFamilyName()+" "+context.getString(remove? R.string.friendsRemoved:R.string.friendAccepted), Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyConnection.getInstance(context).addToRequestQueue(req);
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
                                    "","");
                            friend.setRequest(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        friends.add(friend);
                        adapter.notifyDataSetChanged();
                        noFriends.setVisibility(friends.size() == 0 ? View.VISIBLE: View.INVISIBLE);
                        listViewParticipant.setVisibility(friends.size() != 0 ? View.VISIBLE: View.INVISIBLE);
                        Toast.makeText(SeeAddFriendsActivity.this, friend.getName()+" "+friend.getFamilyName()+" "+getString(R.string.addfriendToast), Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SeeAddFriendsActivity.this, user.getFriend()+" "+getString(R.string.dontExist), Toast.LENGTH_LONG).show();
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
                        try {
                            JSONArray itr = response.getJSONObject(0).getJSONArray("request");
                            for(int j = 0; j < itr.length(); j++)
                            {
                                user = null;
                                user = new UserInfo(itr.getJSONObject(j).getString("FIRST_NAME"),
                                        itr.getJSONObject(j).getString("LAST_NAME"),
                                        itr.getJSONObject(j).getString("EMAIL"),"","","");
                                user.setRequest(Integer.parseInt(itr.getJSONObject(j).getString("ACCEPTED")) == 0);
                                friends.add(user);
                            }
                            JSONArray itr2 = response.getJSONObject(1).getJSONArray("asked");
                            for(int j = 0; j < itr2.length(); j++)
                            {
                                user = null;
                                user = new UserInfo(itr2.getJSONObject(j).getString("FIRST_NAME"),
                                        itr2.getJSONObject(j).getString("LAST_NAME"),
                                        itr2.getJSONObject(j).getString("EMAIL"),"","","");
                                user.setAsked(Integer.parseInt(itr2.getJSONObject(j).getString("ACCEPTED")) == 0);
                                friends.add(user);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        progressBar.setVisibility(View.INVISIBLE);
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
