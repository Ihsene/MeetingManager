package be.ac.umons.meetingmanager.meeting.activities;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.meeting.UserInfo;
import be.ac.umons.meetingmanager.meeting.Meeting;
import be.ac.umons.meetingmanager.meeting.Subject;
import be.ac.umons.meetingmanager.meeting.adapters.UserAdapter;

/**
 * Created by SogeP on 06-08-17.
 */

public class DialogSubjet extends Dialog {
    private ListView listView;
    private EditText nameSubjet;
    private EditText info;
    private EditText searchEdit;
    private TextView durationText;
    private SeekBar seekBar;
    private Button saveButton;
    private UserAdapter adapter;
    private ArrayList<UserInfo> friends;
    private boolean load;
    private int positionLoaded;
    private Meeting meeting;
    private Context context;
    private TextView noFriendsOption;
    private boolean isMaster;

    public DialogSubjet(@NonNull Context context, final ArrayList<UserInfo> friends, final Meeting meeting) {
        super(context);
        setContentView(R.layout.layout_add_subjet_dialog);
        this.friends = friends;
        load = false;
        this.meeting = meeting;
        this.context = context;

        UserInfo user = UserInfo.getUserInfoFromCache(context);
        isMaster = meeting.getMasterID() != null ? meeting.getMasterID().equals(user.getId()) : true;
        adapter = new UserAdapter(context, friends, R.layout.layout_see_friends);
        listView = (ListView) findViewById(R.id.listviewFriends);
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);
        nameSubjet = (EditText) findViewById(R.id.nameSubjet);
        info = (EditText) findViewById(R.id.infoSubjet);
        durationText = (TextView) findViewById(R.id.textViewDur);
        durationText.setText("20 min");
        noFriendsOption = (TextView) findViewById(R.id.textViewCoucou);
        noFriendsOption.setText(R.string.noFriendsOption);
        seekBar = (SeekBar) findViewById(R.id.seekBarDuration);
        seekBar.setMax(20);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                durationText.setText((i+1)*5+" min");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        saveButton = (Button) findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAction();
            }
        });

        if(!friends.isEmpty())
            noFriendsOption.setVisibility(View.INVISIBLE);

        if(!isMaster)
        {
            TextView t = (TextView) findViewById(R.id.textViewDuration);
            TextView friendsText = (TextView) findViewById(R.id.textViewFriend);
            friendsText.setText(R.string.participants);
            t.setVisibility(View.GONE);
            nameSubjet.setVisibility(View.GONE);
            info.setVisibility(View.GONE);
            durationText.setVisibility(View.GONE);
            seekBar.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
        }

        searchEdit = (EditText) findViewById(R.id.search_bar_edit);
        searchEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    public void saveAction() {
        if(nameSubjet.getText().toString().isEmpty()) {
            nameSubjet.setError(context.getString(R.string.nameSubError));
            return;
        }

        ArrayList<UserInfo> participants = new ArrayList<UserInfo>();
        for(UserInfo itr : friends)
            if(itr.isTaken())
            {
                participants.add(itr);
                itr.setTaken(false);
            }
        if(participants.isEmpty())
        {
            Toast.makeText(context, R.string.oneFriendError, Toast.LENGTH_LONG).show();
            return;
        }

        if(load) {
            meeting.getSubjects().get(positionLoaded).setName(nameSubjet.getText().toString());
            meeting.getSubjects().get(positionLoaded).setInfo(info.getText().toString());
            meeting.getSubjects().get(positionLoaded).setDuration((seekBar.getProgress()+1)*5);
            meeting.getSubjects().get(positionLoaded).setParticipants(participants);
            load = false;

        }else
        {
            meeting.getSubjects().add(new Subject(nameSubjet.getText().toString(), info.getText().toString(),(seekBar.getProgress()+1)*5,participants));
        }
        dismiss();
    }

    public void load(Subject subject, int positionLoaded) {
        this.positionLoaded = positionLoaded;
        load = true;
        nameSubjet.setText(subject.getName());
        info.setText(subject.getInfo());
        seekBar.setProgress((subject.getDuration() / 5)-1);
        if(!isMaster)
        {
            adapter = new UserAdapter(context, subject.getParticipants(), R.layout.activity_see_add_friends_list);
            listView.setAdapter(adapter);
        }else
        {
            for(UserInfo itr : friends)
                for(UserInfo itr2 : subject.getParticipants())
                    if(itr2.getEmail().equals(itr.getEmail()))
                        itr.setTaken(true);
        }

        adapter.notifyDataSetChanged();

        if(!friends.isEmpty())
            noFriendsOption.setVisibility(View.INVISIBLE);
    }
}