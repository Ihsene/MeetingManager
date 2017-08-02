package be.ac.umons.meetingmanager.meeting.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import be.ac.umons.meetingmanager.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void actionMenu(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.buttonCreateM:  intent = new Intent(this, MeetingManagerActivity.class); break;
            case R.id.buttonJoinM:  intent = new Intent(this, MeetingManagerActivity.class); break;
            case R.id.buttonOptions:  intent = new Intent(this, MeetingManagerActivity.class); break;
            case R.id.buttonLogout:  intent = new Intent(this, MeetingManagerActivity.class); break;
        }
        startActivity(intent);
    }
}
