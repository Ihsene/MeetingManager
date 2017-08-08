package be.ac.umons.meetingmanager.meeting.activities;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.UserInfo;
import be.ac.umons.meetingmanager.meeting.Meeting;
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
        listView = (ListView) findViewById(R.id.listPresence);
        setDateFromSubject();
    }

    public void handleNextButton() {
        if (reamingTime > FIVE_MIN) {
            setCount(FIVE_MIN);
        } else {
            currentSujectIndex += 1;
            if (currentSujectIndex == meeting.getSubjects().size()) {
                finish();
            } else {
                Log.d("mmmm","test : "+reamingTime);

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
        setCount(meeting.getSubjects().get(currentSujectIndex).getDuration() * 60000);
    }

    public void setCount(long time) {
        if(countDownTimer!= null)
            countDownTimer.cancel();
        countDownTimer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                reamingTime = millisUntilFinished;
                timerTextView.setText(getString(R.string.timeLeft) + " " +
                        String.format("%d min %d sec",
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
            }
        }.start();
    }

    public void actionOfButton(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.buttonNext: handleNextButton(); break;
            case R.id.buttonEdit:
                intent = new Intent(this, CreateMeetingActivity.class);
                intent.putExtra("meeting", meeting);
                startActivity(intent); break;
        }
    }
}
