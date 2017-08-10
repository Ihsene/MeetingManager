package be.ac.umons.meetingmanager.meeting.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.UserInfo;
import be.ac.umons.meetingmanager.meeting.Meeting;
import be.ac.umons.meetingmanager.meeting.Subject;
import be.ac.umons.meetingmanager.meeting.UserAdapter;
import be.ac.umons.meetingmanager.options.OptionActivity;

public class MeetingActivity extends AppCompatActivity {

    private Meeting meeting;
    private TextView subjectName,subjectDescription,timerTextView;
    private ListView listView;
    private Button editButton,nextButton;
    private int currentSujectIndex;
    private UserAdapter adapter;
    private CountDownTimer countDownTimer;
    private long reamingTime;
    private final long FIVE_MIN = 5 * 60000;

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert):
                new AlertDialog.Builder(this);
        builder.setTitle(R.string.leaveMeeting).setMessage(R.string.leaveMeetingCon)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        currentSujectIndex = 0;
        meeting = getIntent().getExtras().getParcelable("meeting");
        setTitle(meeting.getTitle());
        subjectName = (TextView) findViewById(R.id.textViewSubject);
        subjectDescription = (TextView) findViewById(R.id.textViewDescription);
        timerTextView = (TextView) findViewById(R.id.textViewTimer);
        editButton = (Button) findViewById(R.id.buttonEdit);
        nextButton = (Button) findViewById(R.id.buttonNext);
        nextButton.setText(R.string.startM);
        listView = (ListView) findViewById(R.id.listPresence);
        setDateFromSubject();
    }

    public void handleNextButton() {
        if(nextButton.getText().toString().equals(getString(R.string.startM)))
        {
            nextButton.setText(R.string.next);
            setCount(meeting.getSubjects().get(currentSujectIndex).getDuration() * 60000);
        }
        else
        {
            if (reamingTime > FIVE_MIN) {
                AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert):
                        new AlertDialog.Builder(this);
                builder.setTitle(R.string.speedup).setMessage(R.string.fiveminSet)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                setCount(FIVE_MIN);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
            } else {
                if (currentSujectIndex == meeting.getSubjects().size() - 1){
                    finishMeeting();
                    return;
                }
                else
                    currentSujectIndex += 1;
                if (currentSujectIndex == meeting.getSubjects().size() - 1)
                    nextButton.setText(R.string.endMeeting);
                setDateFromSubject();
            }
        }
    }

    public void setDateFromSubject()
    {
        subjectName.setText(meeting.getSubjects().get(currentSujectIndex).getName());
        subjectDescription.setText(meeting.getSubjects().get(currentSujectIndex).getInfo());
        adapter = new UserAdapter(this, meeting.getSubjects().get(currentSujectIndex).getParticipants(), R.layout.layout_presence_member);
        listView.setAdapter(adapter);
        int duration = meeting.getSubjects().get(currentSujectIndex).getDuration() * 60000;
        updateTimer(duration);
        if(!(nextButton.getText().toString().equals(getString(R.string.startM))))
            setCount(duration);
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
        countDownTimer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                reamingTime = millisUntilFinished;
                updateTimer(millisUntilFinished);
            }
            public void onFinish() {
                finishMeeting();
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
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Meeting tmp = data.getExtras().getParcelable("meetingM");
            meeting.setSubjects(tmp.getSubjects());
        }
    }
}
