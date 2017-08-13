package be.ac.umons.meetingmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

import be.ac.umons.meetingmanager.meeting.activities.MeetingManagerActivity;
import be.ac.umons.meetingmanager.options.OptionActivity;

public class MenuActivity extends AppCompatActivity {

    private GoogleApiClient googleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        googleApiClient.connect();
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
        Toast.makeText(MenuActivity.this, R.string.goodbye, Toast.LENGTH_LONG).show();
        Auth.GoogleSignInApi.signOut(googleApiClient);
        finish();
    }
}
