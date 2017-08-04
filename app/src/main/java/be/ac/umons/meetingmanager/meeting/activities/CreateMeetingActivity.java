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
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.UserInfo;
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
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);
        setTitle(R.string.createMeetingTitle);
        meeting = new Meeting();
        calendar = Calendar.getInstance();



        dialogAddSubjet();

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

        location.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    actionButton(findViewById(R.id.buttonSetTime));
                    return true;
                }
                return false;
            }
        });
        meeting.getSubjects().add(new Subject("Introduction","qu'est ce qu'on va faire?",15,new ArrayList<Integer>()));
        meeting.getSubjects().add(new Subject("L'eau","On fait quoi après l'eau ?",10,new ArrayList<Integer>()));
        meeting.getSubjects().add(new Subject("Le gateau","On mange le gateau",25,new ArrayList<Integer>()));
        meeting.getSubjects().add(new Subject("Conclusion","Je suis remplit",5,new ArrayList<Integer>()));


        subjectAdapter = new SubjectAdapter(this, meeting.getSubjects(), R.layout.layout_subjet_list);
        listViewSubjets.setAdapter(subjectAdapter);
        listViewSubjets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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

    public void dialogAddSubjet() {

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_add_subjet_dialog);

        String[] meetings = {"Android","IPhone","WindowsMobile","Blackberry",
                "WebOS","Ubuntu","Windows7","Max OS X"};
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_meeting_manager_listview, meetings);

        ListView listView = (ListView) dialog.findViewById(R.id.listviewFriends);
        listView.setAdapter(adapter);
    }

    public void actionButton(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.buttonSetTime: datePickerDialog.show(); break;
            case R.id.buttonSave: handleSaveMeeting(); break;
            case R.id.buttonAddSubjets: dialog.show(); break;
        }
    }
}
