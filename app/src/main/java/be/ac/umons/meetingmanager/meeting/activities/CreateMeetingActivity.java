package be.ac.umons.meetingmanager.meeting.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Calendar;
import java.util.Date;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.meeting.UserInfo;
import be.ac.umons.meetingmanager.connection.VolleyConnection;
import be.ac.umons.meetingmanager.meeting.adapters.UserAdapter;
import be.ac.umons.meetingmanager.meeting.alarm.AlarmBroadcastReceive;
import be.ac.umons.meetingmanager.meeting.alarm.AlarmNotification;
import be.ac.umons.meetingmanager.meeting.Meeting;
import be.ac.umons.meetingmanager.meeting.Subject;
import be.ac.umons.meetingmanager.meeting.adapters.SubjectAdapter;

import static be.ac.umons.meetingmanager.meeting.activities.MeetingManagerActivity.handleRemoveMeetingFromDB;
import static be.ac.umons.meetingmanager.options.SeeAddFriendsActivity.getFriends;

public class CreateMeetingActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText name, location;
    private Button setDate, save;
    private TextView date;
    private ListView listViewSubjets;
    private DatePickerDialog datePickerDialog;

    private Meeting meeting;
    private Calendar calendar;
    private ArrayList<UserInfo> friends;
    private SubjectAdapter subjectAdapter;
    private Gson gson;
    private UserInfo user;
    private DialogSubjet dialog;
    private Dialog dialogP;
    private boolean editWhileMeeting = false;
    private boolean isMaster;
    private ExpandableListView expandableListView;
    private Button fullpresenceButton;
    private ArrayList<UserInfo> partiticipantAllMeeting;
    private UserAdapter adapterPresence;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editWhileMeeting = getIntent().hasExtra("index");
        setContentView(editWhileMeeting ? R.layout.layout_edit_meeting : R.layout.activity_create_meeting);
        calendar = Calendar.getInstance();
        name = (EditText) findViewById(R.id.nameEditText);
        location = (EditText) findViewById(R.id.locationEditText);
        date = (TextView) findViewById(R.id.textViewDateAndTime);
        setDate = (Button) findViewById(R.id.buttonSetTime);
        save = (Button) findViewById(R.id.buttonSave);
        listViewSubjets = (ListView) findViewById(R.id.subjectList);
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH),day = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(this, CreateMeetingActivity.this, year, month, day);
        user = UserInfo.getUserInfoFromCache(this);
        fullpresenceButton = (Button) findViewById(R.id.buttonPresenceC);
        handleEditMeeting();
        partiticipantAllMeeting = new ArrayList<UserInfo>();
        isMaster = meeting.getMasterID() != null ? user.getId().equals(meeting.getMasterID()) : true;
        gson = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
        try {
            handleGetFriends();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!editWhileMeeting)
        {
            location.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        actionButton(findViewById(R.id.buttonSetTime));
                        return true;
                    }
                    return false;
                }
            });
        }
        handleAddSubjectList();
        createDialog();
        createDialogPresence();
        if(!isMaster)
        {
            setTitle("Réunion");
            disableEditText(name);
            disableEditText(location);
            setDate.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
            Button b = (Button) findViewById(R.id.buttonAddSubjets);
            Button b2 = (Button) findViewById(R.id.buttonPresenceC);
            b.setVisibility(View.GONE);
            b2.setVisibility(View.GONE);


        }

    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setBackgroundColor(Color.TRANSPARENT);
    }



    public  void handleAddSubjectList() {
        subjectAdapter = new SubjectAdapter(this, meeting.getSubjects(), isMaster ? R.layout.layout_subjet_list: R.layout.layout_see_subject);
        listViewSubjets.setAdapter(subjectAdapter);
        listViewSubjets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!((Subject) adapterView.getItemAtPosition(i)).isFreeze())
                {
                    dialog.load(meeting.getSubjects().get(i), i);
                    dialog.show();
                }
            }
        });
        listViewSubjets.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!((Subject) adapterView.getItemAtPosition(i)).isFreeze())
                    handleActionDelete((Subject) adapterView.getItemAtPosition(i),i);
                return true;
            }
        });
    }

    public  void handleEditMeeting() {
        if(getIntent().getExtras() != null)
        {
            Meeting m = getIntent().getExtras().getParcelable("meeting");
            meeting = m;
            if(!editWhileMeeting)
            {
                name.setText(m.getTitle());
                location.setText(m.getPlace());
                date.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(m.getDate()));
            }else
            {
                int index = getIntent().getExtras().getInt("index");
                for(int i = 0; i <= index; i++)
                    meeting.getSubjects().get(i).setFreeze(true);
            }
            handleAddSubjectList();
        }else
            meeting = new Meeting();
        setTitle(getIntent().getExtras() != null? getString(R.string.editMeetingTitle) : getString(R.string.createMeetingTitle));
    }

    public  void createDialog() {
        for(UserInfo itr : partiticipantAllMeeting)
            for(UserInfo itr2 : friends)
                if(itr.getEmail().equals(itr2.getEmail()))
                    itr2.setTaken(true);
        dialog = new DialogSubjet(this, friends, meeting);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                subjectAdapter.notifyDataSetChanged();
                for(UserInfo itr : friends)
                    itr.setTaken(false);
            }
        });
    }

    public void handleActionDelete(final Subject subject, final int i) {
        AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert):
                new AlertDialog.Builder(this);
        builder.setTitle(R.string.deleteSubject).setMessage(R.string.confirmationDeleteSubjet)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        meeting.getSubjects().remove(i);
                        subjectAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    public void handleGetFriends() throws JSONException {
        ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();
        jsonObjects.add(new JSONObject(gson.toJson(user)));
        friends = new ArrayList<UserInfo>();
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST,(String) getText(R.string.getFriends), new JSONArray(jsonObjects),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        getFriends(response, friends, true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CreateMeetingActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    @Override
    public void onDateSet(DatePicker datePicker, final int yearSet, final int monthSet, final int daySet) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY),minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourSet, int minuteSet) {
                calendar.set(Calendar.YEAR, yearSet); calendar.set(Calendar.MONTH, monthSet);
                calendar.set(Calendar.DAY_OF_MONTH, daySet); calendar.set(Calendar.HOUR_OF_DAY, hourSet);
                calendar.set(Calendar.MINUTE, minuteSet);
                meeting.setDate(calendar.getTime());
                date.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }



    public void handleSaveMeeting() throws JSONException {

        UserInfo user = UserInfo.getUserInfoFromCache(this);

        user.setMeeting(meeting);
        Gson gson  = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();

        if(editWhileMeeting)
        {
            user.getMeeting().setUpdate(true);
            final Date date = user.getMeeting().getDate();
            user.getMeeting().setDateToSend(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,getString(R.string.update_meeting_url), new JSONObject(gson.toJson(user)),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(CreateMeetingActivity.this, R.string.meetingUpdate, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent();
                            intent.putExtra("meetingM", meeting);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(CreateMeetingActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                }
            });
            VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
        }else
        {
            boolean ok = true;

            if(name.getText().toString().isEmpty()) {
                ok = false;
                name.setError(getString(R.string.nameMeetingError));
            }
            if(location.getText().toString().isEmpty()) {
                ok = false;
                location.setError(getString(R.string.locationMeetingError));
            }
            if(date.getText().toString().isEmpty()) {
                ok = false;
                date.setError(getString(R.string.pickDate));
            }

            if(meeting.getSubjects().size() == 0)
            {
                ok = false;
                Toast.makeText(CreateMeetingActivity.this, R.string.oneSubj, Toast.LENGTH_LONG).show();
            }

            if(!ok)
                return;

            meeting.setTitle(name.getText().toString());
            meeting.setPlace(location.getText().toString());
            final Date date = user.getMeeting().getDate();
            user.getMeeting().setDateToSend(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            user.getMeeting().setUpdate(getIntent().getExtras() != null);
            if(getIntent().getExtras() != null)
                handleRemoveMeetingFromDB(meeting, getApplicationContext(), true, getSharedPreferences(getString(R.string.setting), this.MODE_PRIVATE));
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,(String) getText(R.string.create_meeting_url), new JSONObject(gson.toJson(user)),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                setAlarm(true, getApplicationContext(), Integer.parseInt(response.getString("meetingID")), date, meeting, getSharedPreferences(getString(R.string.setting), getApplicationContext().MODE_PRIVATE));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(CreateMeetingActivity.this, getIntent().getExtras() != null? getString(R.string.meetingEdited) : getString(R.string.meetingCreated), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(CreateMeetingActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                }
            });
            VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
        }
    }

    public static void setAlarm(boolean on, Context c, int id, Date date, Meeting meeting, SharedPreferences sharedPreferences) {
        Intent intent = new Intent(c, AlarmBroadcastReceive.class);
        intent.putExtra("id", id);
        if(on)
        {
            if(date.after(new Date()))
            {
                int[] pref = {Calendar.SECOND, Calendar.MINUTE, Calendar.DAY_OF_WEEK};
                intent.putExtra("meeting", meeting.getTitle());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(pref[sharedPreferences.getInt("pref", 0)], -sharedPreferences.getInt("delay", 5));
                AlarmNotification.addAlarm(c, intent, id, calendar);
            }

        }else
            AlarmNotification.cancelAlarm(c,intent, id);
    }

    public void createDialogPresence() {
        dialogP = new Dialog(this);
        dialogP.setContentView(R.layout.content_see_add_friends);
        dialogP.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                partiticipantAllMeeting.clear();
                for(UserInfo itr : friends)
                    if(itr.isTaken())
                        partiticipantAllMeeting.add(itr);
            }
        });

        for(UserInfo itr : partiticipantAllMeeting)
            for(UserInfo itr2 : friends)
                if(itr.getEmail().equals(itr2.getEmail()))
                    itr2.setTaken(true);
        EditText searchEdit = (EditText) dialogP.findViewById(R.id.search_bar_edit);
        adapterPresence = new UserAdapter(this, friends, R.layout.layout_see_friends);
        searchEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapterPresence.filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ListView listView = (ListView) dialogP.findViewById(R.id.listViewFriends);
        listView.setAdapter(adapterPresence);
        ProgressBar p = (ProgressBar) dialogP.findViewById(R.id.progressBar2);
        TextView t = (TextView) dialogP.findViewById(R.id.noFriendsTextView);
        t.setVisibility(View.GONE);
        p.setVisibility(View.GONE);
    }

    public void actionButton(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.buttonSetTime: datePickerDialog.show(); break;
            case R.id.buttonSave:
                try {
                    handleSaveMeeting();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buttonAddSubjets: createDialog(); dialog.show(); break;
            case R.id.buttonPresenceC: createDialogPresence(); dialogP.show(); break;
        }
    }
}
