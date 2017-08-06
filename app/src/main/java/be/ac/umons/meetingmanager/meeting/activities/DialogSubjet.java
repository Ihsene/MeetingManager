package be.ac.umons.meetingmanager.meeting.activities;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.UserInfo;
import be.ac.umons.meetingmanager.meeting.Meeting;
import be.ac.umons.meetingmanager.meeting.Subject;
import be.ac.umons.meetingmanager.meeting.UserAdapter;

/**
 * Created by SogeP on 06-08-17.
 */

public class DialogSubjet extends Dialog {
    private ListView listView;
    private EditText nameSubjet;
    private EditText info;
    private TextView durationText;
    private SeekBar seekBar;
    private Button saveButton;
    private UserAdapter adapter;
    private ArrayList<UserInfo> friends;
    private boolean load;
    private int positionLoaded;

    public DialogSubjet(@NonNull Context context, final ArrayList<UserInfo> friends, final Meeting meeting) {
        super(context);
        setContentView(R.layout.layout_add_subjet_dialog);
        this.friends = friends;
        load = false;

        adapter = new UserAdapter(context, friends, R.layout.layout_see_friends);
        listView = (ListView) findViewById(R.id.listviewFriends);
        listView.setAdapter(adapter);
        nameSubjet = (EditText) findViewById(R.id.nameSubjet);
        info = (EditText) findViewById(R.id.infoSubjet);
        durationText = (TextView) findViewById(R.id.textViewDur);
        durationText.setText("20 min");

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
                ArrayList<UserInfo> participants = new ArrayList<UserInfo>();
                for(UserInfo itr : friends)
                    if(itr.isTaken())
                    {
                        participants.add(itr);
                        itr.setTaken(false);
                    }

                if(load) {
                    meeting.getSubjects().remove(positionLoaded);
                    meeting.getSubjects().add(positionLoaded, new Subject(nameSubjet.getText().toString(), info.getText().toString(),(seekBar.getProgress()+1)*5,participants));
                    load = false;
                }else
                    meeting.getSubjects().add(new Subject(nameSubjet.getText().toString(), info.getText().toString(),(seekBar.getProgress()+1)*5,participants));
                dismiss();
            }
        });

    }

    public void load(Subject subject, int positionLoaded) {
        load = true;
        nameSubjet.setText(subject.getName());
        info.setText(subject.getInfo());
        seekBar.setProgress((subject.getDuration() / 5)-1);
        for(UserInfo itr : friends)
            for(UserInfo itr2 : subject.getParticipants())
                if(itr2.getEmail().equals(itr.getEmail()))
                    itr.setTaken(true);
        adapter.notifyDataSetChanged();
    }
}