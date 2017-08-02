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

import java.util.Calendar;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.UserInfo;

public class CreateMeetingActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText name, location;
    private Button setDate, goBack, save;
    private TextView date;
    private ListView listViewParticipant;
    private DatePickerDialog datePickerDialog;
    private Meeting meeting;
    private Calendar calendar;
    private UserInfo[] friends;

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
        goBack = (Button) findViewById(R.id.buttonReturn);
        save = (Button) findViewById(R.id.buttonSave);
        listViewParticipant = (ListView) findViewById(R.id.participantList);
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH),day = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(this, CreateMeetingActivity.this, year, month, day);

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
        friends = new UserInfo[12];
        friends[0] = new UserInfo("Ihsene","Jemaiel","sogepix@live.fr","","");
        friends[1] = new UserInfo("sefsef","sefsef","sefsefse@live.fr","","");
        friends[2] = new UserInfo("sfesefsf","sfsefsef","sfsefsefsef@live.fr","","");
        friends[3] = new UserInfo("sefsfsef","Jemaiele(gerg","sfsfsf@live.fr","","");
        friends[4] = new UserInfo("Ihsene","Jemaiel","sogepix@live.fr","","");
        friends[5] = new UserInfo("sefsef","sefsef","sefsefse@live.fr","","");
        friends[6] = new UserInfo("sfesefsf","sfsefsef","sfsefsefsef@live.fr","","");
        friends[7] = new UserInfo("sefsfsef","Jemaiele(gerg","sfsfsf@live.fr","","");
        friends[8] = new UserInfo("Ihsene","Jemaiel","sogepix@live.fr","","");
        friends[9] = new UserInfo("sefsef","sefsef","sefsefse@live.fr","","");
        friends[10] = new UserInfo("sfesefsf","sfsefsef","sfsefsefsef@live.fr","","");
        friends[11] = new UserInfo("sefsfsef","Jemaiele(gerg","sfsfsf@live.fr","","");
        ParticipantAdapter adapter = new ParticipantAdapter(this, friends);
        listViewParticipant.setAdapter(adapter);
    }

    public void handleSaveMeeting() {

    }

    public void actionButton(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.buttonSetTime: datePickerDialog.show(); break;
            case R.id.buttonReturn: finish(); break;
            case R.id.buttonSave: handleSaveMeeting(); break;
        }
    }
}
