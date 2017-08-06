package be.ac.umons.meetingmanager.meeting.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.UserInfo;
import be.ac.umons.meetingmanager.connection.VolleyConnection;
import be.ac.umons.meetingmanager.meeting.Meeting;
import be.ac.umons.meetingmanager.meeting.Subject;
import be.ac.umons.meetingmanager.meeting.SubjectAdapter;

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

    //Dialog
    private DialogSubjet dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);
        setTitle(R.string.createMeetingTitle);
        meeting = new Meeting();
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
        gson = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
        try {
            handleGetFriends();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        location.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    actionButton(findViewById(R.id.buttonSetTime));
                    return true;
                }
                return false;
            }
        });

        subjectAdapter = new SubjectAdapter(this, meeting.getSubjects(), R.layout.layout_subjet_list);
        listViewSubjets.setAdapter(subjectAdapter);
        listViewSubjets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog.load(meeting.getSubjects().get(i), i);
                dialog.show();
            }
        });
        listViewSubjets.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                handleActionDelete((Subject) adapterView.getItemAtPosition(i),i);
                return true;
            }
        });

        createDialog();
    }

    public  void createDialog() {
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

    public void handleGetFriends() throws JSONException { // TODO : Duplication to remove
        friends = new ArrayList<UserInfo>();
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
                                user = new UserInfo(response.getJSONObject(i).getString("FIRST_NAME"),
                                        response.getJSONObject(i).getString("LAST_NAME"),
                                        response.getJSONObject(i).getString("EMAIL"),"",
                                        "");
                                friends.add(user);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
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
                date.setText(meeting.getDate().toString());
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }



    public void handleSaveMeeting() {

    }

    public void actionButton(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.buttonSetTime: datePickerDialog.show(); break;
            case R.id.buttonSave: handleSaveMeeting(); break;
            case R.id.buttonAddSubjets: createDialog(); dialog.show(); break;
        }
    }
}
