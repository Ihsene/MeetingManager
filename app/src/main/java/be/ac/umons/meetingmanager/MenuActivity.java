package be.ac.umons.meetingmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import be.ac.umons.meetingmanager.meeting.activities.MeetingManagerActivity;
import be.ac.umons.meetingmanager.options.OptionActivity;

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
            case R.id.buttonJoinM:  intent = new Intent(this, MeetingManagerActivity.class); intent.putExtra("join", true); break;
            case R.id.buttonOptions:  intent = new Intent(this, OptionActivity.class); break;
            case R.id.buttonLogout:  handleLogout(); break;
        }
        if(view.getId() != R.id.buttonLogout)
            startActivity(intent);
    }

    public void handleLogout() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.setting), this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
