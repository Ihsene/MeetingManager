package be.ac.umons.meetingmanager.meeting.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.UserInfo;
import be.ac.umons.meetingmanager.connection.VolleyConnection;
import be.ac.umons.meetingmanager.meeting.Meeting;
import be.ac.umons.meetingmanager.meeting.MeetingAdapter;
import be.ac.umons.meetingmanager.meeting.Subject;
import be.ac.umons.meetingmanager.meeting.SubjectAdapter;
import be.ac.umons.meetingmanager.options.SeeAddFriendsActivity;

public class MeetingManagerActivity extends AppCompatActivity {

    private MeetingAdapter adapter;
    private ArrayList<Meeting> meetings;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.meetingListTitle);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreateMeetingActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            setMeetingList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter = new MeetingAdapter(this, meetings, R.layout.layout_meeting_list);
        listView = (ListView) findViewById(R.id.meeting_list);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                handleActionDelete((Meeting) adapterView.getItemAtPosition(i),i);
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    loadMeeting((Meeting) adapterView.getItemAtPosition(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadMeeting(Meeting meeting) throws JSONException {
        UserInfo user = UserInfo.getUserInfoFromCache(this);
        user.setMeeting(meeting);
        ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
        jsonObjects.add(new JSONObject(gson.toJson(user)));
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST,(String) getText(R.string.get_meeting_url), new JSONArray(jsonObjects),
                new Response.Listener<JSONArray>() {
                    Meeting meeting;
                    @Override
                    public void onResponse(JSONArray response) {
                        Meeting meeting;

                        try {
                            meeting = new Meeting(
                                    response.getJSONObject(0).getJSONObject("meeting").getString("TITLE"),
                                    response.getJSONObject(0).getJSONObject("meeting").getString("PLACE"),
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(response.getJSONObject(0).getJSONObject("meeting").getString("MDATE"))
                                    , new ArrayList<Subject>());
                            meeting.setId(response.getJSONObject(0).getJSONObject("meeting").getString("ID"));
                            Subject subject;
                            for(int i = 0; i < response.getJSONObject(1).getJSONArray("subjects").length(); i++)
                            {
                                subject = new Subject(response.getJSONObject(1).getJSONArray("subjects").getJSONObject(i).getString("NAME"),
                                        response.getJSONObject(1).getJSONArray("subjects").getJSONObject(i).getString("INFO"),
                                        Integer.parseInt(response.getJSONObject(1).getJSONArray("subjects").getJSONObject(i).getString("DURATION")),
                                        new ArrayList<UserInfo>());
                                subject.setId(Integer.parseInt(response.getJSONObject(1).getJSONArray("subjects").getJSONObject(i).getString("ID")));

                                for(int j = 0; j < +response.getJSONObject(2).getJSONArray("participants").length(); j++)
                                    if(Integer.parseInt(response.getJSONObject(2).getJSONArray("participants").getJSONObject(j).getString("SUBJET_ID")) == subject.getId())
                                        subject.getParticipants().add(new UserInfo("","",response.getJSONObject(2).getJSONArray("participants").getJSONObject(j).getString("EMAIL"),
                                                "",""));
                                meeting.getSubjects().add(subject);
                            }

                            Intent i = new Intent(MeetingManagerActivity.this, CreateMeetingActivity.class);
                            i.putExtra("meeting", meeting);
                            startActivity(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MeetingManagerActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    public void handleActionDelete(final Meeting meeting, final int i) {
        AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert):
                new AlertDialog.Builder(this);
        builder.setTitle(R.string.deleteMeeting).setMessage(R.string.confirmationDeleteMeeting)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            handleRemoveFriendFromDB(meeting, getApplicationContext());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        meetings.remove(i);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    public static void handleRemoveFriendFromDB(Meeting meeting, final Context c) throws JSONException {
        UserInfo user = UserInfo.getUserInfoFromCache(c);
        user.setMeeting(meeting);
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,(String) c.getText(R.string.remove_meeting_url), new JSONObject(gson.toJson(user)),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(c, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyConnection.getInstance(c).addToRequestQueue(req);
    }

    public void setMeetingList() throws JSONException {
        meetings = new ArrayList<Meeting>();
        UserInfo user = UserInfo.getUserInfoFromCache(this);
        ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
        jsonObjects.add(new JSONObject(gson.toJson(user)));
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST,(String) getText(R.string.get_meeting_list_url), new JSONArray(jsonObjects),
                new Response.Listener<JSONArray>() {
                    Meeting meeting;
                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i = 0; i < response.length(); i++)
                        {
                            try {
                                meeting = new Meeting(response.getJSONObject(i).getString("TITLE"),
                                        response.getJSONObject(i).getString("PLACE"),
                                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(response
                                                .getJSONObject(i).getString("MDATE")), null);
                                meeting.setId(response.getJSONObject(i).getString("ID"));
                                meetings.add(meeting);
                                Collections.sort(meetings);
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MeetingManagerActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

}
