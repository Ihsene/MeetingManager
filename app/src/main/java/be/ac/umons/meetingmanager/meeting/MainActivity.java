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
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.User;
import be.ac.umons.meetingmanager.connection.VolleyConnection;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

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
        if (result.isSuccess()) {
            try {
                onSucessLogin(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            pd.dismiss();
            Toast.makeText(MainActivity.this, R.string.error_login, Toast.LENGTH_LONG).show();
        }
    }

    private void onSucessLogin(GoogleSignInResult result) throws JSONException {
        final GoogleSignInAccount acct = result.getSignInAccount();
        User user = new User(acct.getGivenName(), acct.getFamilyName(), acct.getEmail(), acct.getId(), acct.getIdToken());
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,(String) getText(R.string.login_url), new JSONObject(new Gson().toJson(user)),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(acct.getIdToken().equals(response.getString("token"))) {
                                startMenuActivity();
                                finish();
                            }
                            else
                                Toast.makeText(MainActivity.this, R.string.conn_error, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        pd.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, R.string.server_reachability, Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        });
        VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
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
