package be.ac.umons.meetingmanager.meeting;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
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
import be.ac.umons.meetingmanager.SeeAddFriendsActivity;
import be.ac.umons.meetingmanager.connection.UserInfo;
import be.ac.umons.meetingmanager.connection.VolleyConnection;

import static be.ac.umons.meetingmanager.R.string.noFriends;

public class CreateMeetingActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText name, location;
    private Button setDate, save;
    private TextView date;
    private ListView listViewParticipant;
    private DatePickerDialog datePickerDialog;
    private Meeting meeting;
    private Calendar calendar;
    private ArrayList<UserInfo> friends;
    private ParticipantAdapter adapter;
    private Gson gson;
    private UserInfo user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);
        meeting = new Meeting();
        calendar = Calendar.getInstance();

        name = (EditText) findViewById(R.id.nameEditText);
        location = (EditText) findViewById(R.id.locationEditText);
        date = (TextView) findViewById(R.id.textViewDateAndTime);
        setDate = (Button) findViewById(R.id.buttonSetTime);
        save = (Button) findViewById(R.id.buttonSave);
        listViewParticipant = (ListView) findViewById(R.id.participantList);
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH),day = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(this, CreateMeetingActivity.this, year, month, day);
        user = UserInfo.getUserInfoFromCache(this);
        gson = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();

        location.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    actionButton(findViewById(R.id.buttonSetTime));
                    return true;
                }
                return false;
            }
        });
        handleParticipantsList();
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

    public void handleParticipantsList() {
        friends = new ArrayList<UserInfo>();
        try {
            handleGetFriends();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new ParticipantAdapter(this, friends, R.layout.activity_create_meeting_list);
        listViewParticipant.setAdapter(adapter);
    }

    public void handleGetFriends() throws JSONException { // TODO : Duplication to remove
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
                        //noFriends.setVisibility(friends.size() == 0 ? View.VISIBLE: View.INVISIBLE);
                        listViewParticipant.setVisibility(friends.size() != 0 ? View.VISIBLE: View.INVISIBLE);
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

    public void handleSaveMeeting() {

    }

    public void actionButton(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.buttonSetTime: datePickerDialog.show(); break;
            case R.id.buttonSave: handleSaveMeeting(); break;
        }
    }
}
