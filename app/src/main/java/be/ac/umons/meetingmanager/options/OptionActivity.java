package be.ac.umons.meetingmanager.options;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.UserInfo;
import be.ac.umons.meetingmanager.connection.VolleyConnection;

public class OptionActivity extends AppCompatActivity {

    private EditText firstName, familyName;
    private SharedPreferences sharedPreferences;
    private UserInfo user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        setTitle(R.string.buttonOption);

        sharedPreferences = getSharedPreferences(getString(R.string.setting), this.MODE_PRIVATE);
        user = UserInfo.getUserInfoFromCache(this);

        firstName = (EditText) findViewById(R.id.firstName);
        firstName.setText(user.getName());
        familyName = (EditText) findViewById(R.id.familyName);
        familyName.setText(user.getFamilyName());
    }

    public void startFriendsActivity(View view) {
        Intent i = new Intent(this, SeeAddFriendsActivity.class);
        startActivity(i);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(!firstName.getText().toString().equals(user.getName()) ||
                !familyName.getText().toString().equals(user.getFamilyName()))
        {
            user.setName(firstName.getText().toString());
            user.setFamilyName(familyName.getText().toString());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.firstName), firstName.getText().toString());
            editor.putString(getString(R.string.familyName), familyName.getText().toString());
            editor.commit();
            Gson gson  = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
            try {
                JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,(String) getText(R.string.updateOption), new JSONObject(gson.toJson(user)),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(OptionActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
                VolleyConnection.getInstance(getApplicationContext()).addToRequestQueue(req);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
