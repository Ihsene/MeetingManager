package be.ac.umons.meetingmanager.meeting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.User;
import be.ac.umons.meetingmanager.connection.RegisterRequest;
import be.ac.umons.meetingmanager.connection.VolleyConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 1234;
    private static final String TAG = "MM";

    private GoogleApiClient googleApiClient;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail().build();

        googleApiClient = new GoogleApiClient.Builder(this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        pd = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            pd.show();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            onSucessLogin(result);
        } else {
            Toast.makeText(MainActivity.this, R.string.error_login, Toast.LENGTH_LONG).show();
        }
        pd.dismiss();
    }

    private void onSucessLogin(GoogleSignInResult result) {
        GoogleSignInAccount acct = result.getSignInAccount();
        User user = new User(acct.getGivenName(), acct.getFamilyName(), acct.getEmail(), acct.getId(), acct.getIdToken());

        RegisterRequest registerRequest = new RegisterRequest(user, Request.Method.POST, (String) getText(R.string.login_url),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        startMenuActivity();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, R.string.server_reachability, Toast.LENGTH_LONG).show();
                    }
                });
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(registerRequest);
    }
    private void startMenuActivity() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }
}
