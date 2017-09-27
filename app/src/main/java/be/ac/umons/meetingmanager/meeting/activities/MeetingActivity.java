package be.ac.umons.meetingmanager.meeting.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.concurrent.TimeUnit;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.ActivityReceiver;
import be.ac.umons.meetingmanager.connection.VolleyConnection;
import be.ac.umons.meetingmanager.meeting.Meeting;
import be.ac.umons.meetingmanager.meeting.UserInfo;
import be.ac.umons.meetingmanager.meeting.adapters.UserAdapter;

import static be.ac.umons.meetingmanager.meeting.activities.MeetingManagerActivity.getMeetingFromServeur;

public class MeetingActivity extends AppCompatActivity {

    private Meeting meeting;
    private TextView subjectName,subjectDescription,timerTextView;
    private ListView listView;
    private Button editButton,nextButton, buttonSummon;
    private int currentSujectIndex;
    private UserAdapter adapter;
    private CountDownTimer countDownTimer;
    private long reamingTime;
    private long DELAY = 5 * 60000;
    private boolean notifcationSend = false;
    private boolean isMaster = false;
    private ActivityReceiver activityReceiver;
    private ArrayList<String> presences;
    private UserInfo user;
    private boolean end;
    private SharedPreferences sharedPreferences;
    private static boolean active = false;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert):
                new AlertDialog.Builder(this);
        builder.setTitle(R.string.leaveMeeting).setMessage(R.string.leaveMeetingCon)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(countDownTimer != null)
                            countDownTimer.cancel();
                        try {
                            sendPresence(false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(isMaster)
                        {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("index", currentSujectIndex);
                            editor.putLong("duration", reamingTime);
                            editor.commit();
                        }
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("mmm","test on destroy");
        if(isMaster && meeting != null && currentSujectIndex == meeting.getSubjects().size() - 1 && reamingTime == 0)
        {
            Log.d("mmm","test saveeeeeeee");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("index", 0);
            editor.putLong("duration", meeting.getSubjects().get(0).getDuration() * 60000);
            editor.commit();
        }

        if(countDownTimer != null)
            countDownTimer.cancel();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(activityReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        sharedPreferences = getSharedPreferences(getString(R.string.setting), MeetingActivity.MODE_PRIVATE);
        DELAY = sharedPreferences.getInt("delaySub", 5) * 60000;
        currentSujectIndex = 0;
        activityReceiver = new ActivityReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(activityReceiver, ActivityReceiver.CURRENT_ACTIVITY_RECEIVER_FILTER);
        presences = new ArrayList<String>();
        meeting = getIntent().getExtras().getParcelable("meeting");
        user = UserInfo.getUserInfoFromCache(this);
        isMaster = meeting.getMasterID().equals(user.getId());
        setTitle(meeting.getTitle());
        subjectName = (TextView) findViewById(R.id.textViewSubject);
        subjectDescription = (TextView) findViewById(R.id.textViewDescription);
        timerTextView = (TextView) findViewById(R.id.textViewTimer);
        editButton = (Button) findViewById(R.id.buttonEdit);
        editButton.setVisibility(!isMaster ? View.INVISIBLE:View.VISIBLE);
        nextButton = (Button) findViewById(R.id.buttonNext);
        nextButton.setText(R.string.startM);
        nextButton.setVisibility(!isMaster ? View.INVISIBLE:View.VISIBLE);
        buttonSummon = (Button) findViewById(R.id.buttonSummon);
        buttonSummon.setVisibility(!isMaster ? View.INVISIBLE:View.VISIBLE);
        listView = (ListView) findViewById(R.id.listPresence);

        if(isMaster)
            currentSujectIndex = sharedPreferences.getInt("index", 0);

        setDataFromSubject(false, false);
        end = false;

        if(isMaster)
        {
            try {
                sendMessageToAllParticipant(getString(R.string.send_noti_url), false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else
        {
            try {
                sendPresence(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            registerInAndOutUser(user.getEmail(),"", true, false);
        }
    }

    public void loadDate(long timeLeft, int index, ArrayList<String> presences, boolean meetingStarted) {
        currentSujectIndex = index;
        reamingTime = timeLeft;
        setDataFromSubject(true, meetingStarted);
        for(String itr : presences)
            registerInAndOutUser(itr,"", true, false);
        if(!meetingStarted && countDownTimer != null)
            countDownTimer.cancel();

        if(currentSujectIndex == meeting.getSubjects().size()-1 && reamingTime == 0)
        {
            Toast.makeText(MeetingActivity.this, R.string.masterLeave, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void sendPresence(boolean in) throws JSONException {
        user.setMeeting(meeting);
        user.setHere(in);
        user.getMeeting().setCurrentIndex(currentSujectIndex);
        Gson gson  = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, "http://sith-meetings.umons.ac.be:8080/sendPresence", new JSONObject(gson.toJson(user)),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MeetingActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    public void registerInAndOutUser(String email, String userId, boolean in, boolean fromNetwork) {
        if(fromNetwork)
        {
            if(in && !presences.contains(email))
                presences.add(email);
            else if(!in)
                presences.remove(email);
        }

        if(fromNetwork && userId.equals(meeting.getMasterID()) && !isMaster && end == false)
        {
            Toast.makeText(MeetingActivity.this, R.string.masterLeave, Toast.LENGTH_LONG).show();
            if(countDownTimer != null)
                countDownTimer.cancel();
            end = true;
            finish();
        }
        for(UserInfo itr : meeting.getSubjects().get(currentSujectIndex).getParticipants())
            if(itr.getEmail().equals(email))
                itr.setHere(in);

        adapter.notifyDataSetChanged();

        if(isMaster && in)
        {
            try {
                sendMessageToAllParticipant(getString(R.string.meeting_info_url), false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessageToAllParticipant(String url, boolean leave) throws JSONException {
        if(leave)
        {
            reamingTime = 0;
            currentSujectIndex = meeting.getSubjects().size()-1;
        }
        UserInfo user = UserInfo.getUserInfoFromCache(this);
        meeting.setCurrentIndex(currentSujectIndex);
        meeting.setCurrentSubjectTimeLeft(reamingTime);
        user.setMeeting(meeting);

        for(UserInfo itr1 : meeting.getSubjects().get(currentSujectIndex).getParticipants())
            for(String itr2 : presences)
                if(itr1.getEmail().equals(itr2))
                    itr1.setHere(true);
        meeting.setStarted(!nextButton.getText().toString().equals(getString(R.string.startM)));
        Gson gson  = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(gson.toJson(user)),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MeetingActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    public void handleNextButton() {
        if(nextButton.getText().toString().equals(getString(R.string.startM)))
        {
            nextButton.setText(R.string.next);
            setCount(reamingTime);
            try {
                sendMessageToAllParticipant(getString(R.string.meeting_info_url),false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            if (reamingTime > DELAY) {
                AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert):
                        new AlertDialog.Builder(this);
                builder.setTitle(R.string.speedup).setMessage(getString(R.string.fiveminSet)+" "+(DELAY / 60000)+" minutes ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                setCount(DELAY);
                                try {
                                    sendMessageToAllParticipant(getString(R.string.meeting_info_url), false);
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
            } else {
                if (currentSujectIndex == meeting.getSubjects().size() - 1){
                    {
                        finishMeeting();
                        return;
                    }

                }
                else
                {
                    currentSujectIndex += 1;
                    notifcationSend = false;
                }
                if (currentSujectIndex == meeting.getSubjects().size() - 1)
                    nextButton.setText(R.string.endMeeting);
                setDataFromSubject(false, true);
                try {
                    sendMessageToAllParticipant(getString(R.string.meeting_info_url),false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setDataFromSubject(boolean load, boolean meetingStarted)
    {
        subjectName.setText(meeting.getSubjects().get(currentSujectIndex).getName());
        subjectDescription.setText(meeting.getSubjects().get(currentSujectIndex).getInfo());
        adapter = new UserAdapter(this, meeting.getSubjects().get(currentSujectIndex).getParticipants(), R.layout.layout_presence_member);
        listView.setAdapter(adapter);
        long duration = load? reamingTime : meeting.getSubjects().get(currentSujectIndex).getDuration() * 60000;
        if(isMaster && !meetingStarted)
        {
            Log.d("mmm","test rem "+ sharedPreferences.getLong("duration", duration));
            duration = sharedPreferences.getLong("duration", duration);
        }


        reamingTime = duration;
        updateTimer(duration);
        if(meetingStarted && (!(nextButton.getText().toString().equals(getString(R.string.startM))) || !isMaster))
            setCount(duration);

        if(currentSujectIndex > 0)
        {
            for(String itr : presences)
                registerInAndOutUser(itr,"", true, false);
        }
    }

    public void updateTimer(long duration) {
        timerTextView.setText(getString(R.string.timeLeft) + " " +
                String.format("%d min %d sec",
                        TimeUnit.MILLISECONDS.toMinutes(duration),
                        TimeUnit.MILLISECONDS.toSeconds(duration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))));
    }

    public void setCount(long time) {
        if(countDownTimer!= null)
            countDownTimer.cancel();
        reamingTime = time;
        countDownTimer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                reamingTime = millisUntilFinished;
                updateTimer(millisUntilFinished);
                if(((reamingTime / 1000) % 60 == 0) && currentSujectIndex < meeting.getSubjects().size() - 1)
                {
                    try {
                        sendMessageToAllParticipant(getString(R.string.send_noti_sub_url), false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            public void onFinish() {
                if (currentSujectIndex == meeting.getSubjects().size() - 1)
                    Toast.makeText(MeetingActivity.this, R.string.meetingEnd, Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    public void finishMeeting() {
        AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert):
                new AlertDialog.Builder(this);
        builder.setTitle(R.string.endofMeeting).setMessage(R.string.endofmeetingCon)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(isMaster)
                        {
                            reamingTime = 0;
                            Log.d("mmmm"," test on reset");
                            try {
                                sendMessageToAllParticipant(getString(R.string.meeting_info_url), true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    public void actionOfButton(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.buttonNext: handleNextButton(); break;
            case R.id.buttonEdit:
                intent = new Intent(this, CreateMeetingActivity.class);
                intent.putExtra("meeting", meeting);
                intent.putExtra("index", currentSujectIndex);
                startActivityForResult(intent, 1); break;
            case R.id.buttonSummon:
                try {
                    sendMessageToAllParticipant(getString(R.string.send_noti_alert_url), false);
                    Toast.makeText(MeetingActivity.this, R.string.commme, Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Meeting tmp = data.getExtras().getParcelable("meetingM");
            meeting.setSubjects(tmp.getSubjects());
            for(String itr : presences)
                registerInAndOutUser(itr,"", true, false);
            adapter = new UserAdapter(this, meeting.getSubjects().get(currentSujectIndex).getParticipants(), R.layout.layout_presence_member);
            listView.setAdapter(adapter);
        }
    }

    public void updateMeeting() throws JSONException {
        UserInfo user = UserInfo.getUserInfoFromCache(this);
        sendPresence(true);
        user.setMeeting(meeting);
        ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
        jsonObjects.add(new JSONObject(gson.toJson(user)));
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST,(String) getText(R.string.get_meeting_url), new JSONArray(jsonObjects),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        meeting = getMeetingFromServeur(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MeetingActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean active) {
        MeetingActivity.active = active;
    }
}