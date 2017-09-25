package be.ac.umons.meetingmanager.meeting.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.meeting.UserInfo;
import be.ac.umons.meetingmanager.connection.VolleyConnection;
import be.ac.umons.meetingmanager.meeting.alarm.AlarmBroadcastReceive;
import be.ac.umons.meetingmanager.meeting.alarm.AlarmNotification;
import be.ac.umons.meetingmanager.meeting.Meeting;
import be.ac.umons.meetingmanager.meeting.adapters.MeetingAdapter;
import be.ac.umons.meetingmanager.meeting.Subject;

import static be.ac.umons.meetingmanager.meeting.alarm.AlarmNotification.cancelAllAlarms;
import static be.ac.umons.meetingmanager.meeting.activities.CreateMeetingActivity.setAlarm;

public class MeetingManagerActivity extends AppCompatActivity {

    private MeetingAdapter adapter;
    private ArrayList<Meeting> meetings;
    private ListView listView;
    private ProgressBar progressBar;
    private TextView textViewNoMeeting;
    private SharedPreferences sharedPreferences;
    private UserInfo user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.meetingListTitle);
        user = UserInfo.getUserInfoFromCache(this);
        sharedPreferences = getSharedPreferences(getString(R.string.setting), this.MODE_PRIVATE);

        textViewNoMeeting = (TextView) findViewById(R.id.coucou);
        textViewNoMeeting.setVisibility(View.INVISIBLE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreateMeetingActivity.class);
                startActivity(intent);
            }
        });

        if(getIntent().getExtras() != null && getIntent().getExtras().getBoolean("join"))
        {
            setTitle(R.string.startMeeting);
            FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
            floatingActionButton.setVisibility(View.INVISIBLE);
        }

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
        if(!(getIntent().getExtras() != null && getIntent().getExtras().getBoolean("join")))
        {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Meeting meeting = (Meeting) adapterView.getItemAtPosition(i);
                    if(meeting.getMasterID().equals(user.getId()))
                    {
                        handleActionDelete(meeting,i);
                        return true;
                    }
                    return  false;
                }
            });
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Meeting meeting = (Meeting) adapterView.getItemAtPosition(i);
                if(getIntent().getExtras() != null && getIntent().getExtras().getBoolean("join"))
                    showComfirmation(meeting);
                else {
                    try {
                        loadMeeting(meeting);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    public void showComfirmation(final Meeting meeting) {
        UserInfo user = UserInfo.getUserInfoFromCache(this);
        boolean start = meeting.getMasterID().equals(user.getId());
        AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert):
                new AlertDialog.Builder(this);
        builder.setTitle(start? getString(R.string.startMeetingCom):getString(R.string.joinMeet))
                .setMessage(start? getText(R.string.startingMeeting):getString(R.string.joinMeetingCom))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            loadMeeting(meeting);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
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
                        meeting = getMeetingFromServeur(response);
                        Intent i = new Intent(MeetingManagerActivity.this, !(getIntent().getExtras() != null && getIntent().getExtras().getBoolean("join")) ? CreateMeetingActivity.class : MeetingActivity.class);
                        i.putExtra("meeting", meeting);
                        startActivity(i);
                        
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MeetingManagerActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
    }
    
    public static Meeting getMeetingFromServeur(JSONArray response) {
        Meeting meeting = null;

        try {
            meeting = new Meeting(
                    response.getJSONObject(0).getJSONObject("meeting").getString("TITLE"),
                    response.getJSONObject(0).getJSONObject("meeting").getString("PLACE"),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(response.getJSONObject(0).getJSONObject("meeting").getString("MDATE"))
                    , new ArrayList<Subject>());
            meeting.setId(response.getJSONObject(0).getJSONObject("meeting").getString("ID"));
            meeting.setMasterID(response.getJSONObject(0).getJSONObject("meeting").getString("MASTER_ID"));
            Subject subject;
            for (int i = 0; i < response.getJSONObject(1).getJSONArray("subjects").length(); i++) {
                subject = new Subject(response.getJSONObject(1).getJSONArray("subjects").getJSONObject(i).getString("NAME"),
                        response.getJSONObject(1).getJSONArray("subjects").getJSONObject(i).getString("INFO"),
                        Integer.parseInt(response.getJSONObject(1).getJSONArray("subjects").getJSONObject(i).getString("DURATION")),
                        new ArrayList<UserInfo>());
                subject.setId(Integer.parseInt(response.getJSONObject(1).getJSONArray("subjects").getJSONObject(i).getString("ID")));

                for (int j = 0; j < +response.getJSONObject(2).getJSONArray("participants").length(); j++)
                    if (Integer.parseInt(response.getJSONObject(2).getJSONArray("participants").getJSONObject(j).getString("SUBJET_ID")) == subject.getId())
                        subject.getParticipants().add(new UserInfo(response.getJSONObject(2).getJSONArray("participants").getJSONObject(j).getString("FIRST_NAME"),
                                response.getJSONObject(2).getJSONArray("participants").getJSONObject(j).getString("LAST_NAME"),
                                response.getJSONObject(2).getJSONArray("participants").getJSONObject(j).getString("EMAIL"),
                                "", "", ""));
                meeting.getSubjects().add(subject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return meeting;
    }

    public void handleActionDelete(final Meeting meeting, final int i) {
        AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert):
                new AlertDialog.Builder(this);
        builder.setTitle(R.string.deleteMeeting).setMessage(R.string.confirmationDeleteMeeting)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            handleRemoveMeetingFromDB(meeting, getApplicationContext(), false, getSharedPreferences(getString(R.string.setting), getApplicationContext().MODE_PRIVATE));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        meetings.remove(i);
                        textViewNoMeeting.setVisibility(meetings.size() == 0 ? View.VISIBLE: View.INVISIBLE);
                        listView.setVisibility(meetings.size() != 0 ? View.VISIBLE: View.INVISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    public static void handleRemoveMeetingFromDB(Meeting meeting, final Context c, boolean update, SharedPreferences sharedPreferences) throws JSONException {
        UserInfo user = UserInfo.getUserInfoFromCache(c);
        user.setMeeting(meeting);
        meeting.setUpdate(update);
        final int id =  Integer.parseInt(meeting.getId());
        setAlarm(false, c, id, null, user.getMeeting(), sharedPreferences);
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
                        progressBar.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(getApplicationContext(), AlarmBroadcastReceive.class);
                        cancelAllAlarms(getApplicationContext(), intent);
                        for(int i = 0; i < response.length(); i++)
                        {
                            try {
                                meeting = new Meeting(response.getJSONObject(i).getString("TITLE"),
                                        response.getJSONObject(i).getString("PLACE"),
                                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(response
                                                .getJSONObject(i).getString("MDATE")), null);
                                meeting.setId(response.getJSONObject(i).getString("ID"));
                                meeting.setMasterName(response.getJSONObject(i).getString("FIRST_NAME")+
                                        response.getJSONObject(i).getString("LAST_NAME"));
                                meeting.setMasterID(response.getJSONObject(i).getString("MASTER_ID"));
                                meetings.add(meeting);

                                if(meeting.getDate().after(new Date()))
                                {
                                    intent.putExtra("meeting", meeting.getTitle());
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(meeting.getDate());
                                    int[] pref = {Calendar.MINUTE, Calendar.HOUR, Calendar.DAY_OF_WEEK};
                                    calendar.add(pref[sharedPreferences.getInt("pref", 0)], -sharedPreferences.getInt("delay", 5));
                                    Log.d("mmm","test : "+calendar.getTime());
                                    AlarmNotification.addAlarm(getApplicationContext(), intent, Integer.parseInt(meeting.getId()), calendar);
                                }
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        Collections.sort(meetings);
                        textViewNoMeeting.setVisibility(meetings.size() == 0 ? View.VISIBLE: View.INVISIBLE);
                        listView.setVisibility(meetings.size() != 0 ? View.VISIBLE: View.INVISIBLE);
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
