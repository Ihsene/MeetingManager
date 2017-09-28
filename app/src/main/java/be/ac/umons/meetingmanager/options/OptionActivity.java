package be.ac.umons.meetingmanager.options;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.Manifest;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.meeting.UserInfo;
import be.ac.umons.meetingmanager.connection.VolleyConnection;

public class OptionActivity extends AppCompatActivity {

    private EditText firstName, familyName, phoneNumber, delay, delaySub;
    private Spinner spinner;
    private SharedPreferences sharedPreferences;
    private UserInfo user;
    private ToggleButton toggleButton;
    private static final int PERMISSION_REQUEST_CODE = 1;

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
        phoneNumber = (EditText) findViewById(R.id.phoneNumber);
        phoneNumber.setText(user.getPhoneNumber());

        delay = (EditText) findViewById(R.id.DelayTime);
        delay.setText(String.valueOf(sharedPreferences.getInt("delay", 5)));
        delaySub = (EditText) findViewById(R.id.DelayTimeSub);
        delaySub.setText(String.valueOf(sharedPreferences.getInt("delaySub", 5)));
        spinner = (Spinner) findViewById(R.id.spinnerTime);
        String[] pref = new String[] {getString(R.string.minutes),getString(R.string.hours),getString(R.string.days)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, pref);
        spinner.setAdapter(adapter);
        spinner.setSelection(sharedPreferences.getInt("pref", 0));

        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setChecked(sharedPreferences.getBoolean("sms", false));
        toggleButton.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked)
            {
                if(isChecked && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                        && checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED)
                {
                    String[] permissions = {Manifest.permission.SEND_SMS};
                    ActivityCompat.requestPermissions(OptionActivity.this, permissions, PERMISSION_REQUEST_CODE);
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("sms",isChecked);
                editor.commit();
            }
        }) ;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_CODE && (grantResults.length < 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED))
        {
            toggleButton.setChecked(false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("sms",false);
            editor.commit();
        }
    }

    public void startFriendsActivity(View view) {
        Intent i = new Intent(this, SeeAddFriendsActivity.class);
        startActivity(i);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(!firstName.getText().toString().equals(user.getName()) ||
                !familyName.getText().toString().equals(user.getFamilyName())
                || !phoneNumber.getText().toString().equals(user.getPhoneNumber()))
        {
            user.setName(firstName.getText().toString());
            user.setFamilyName(familyName.getText().toString());
            user.setPhoneNumber(phoneNumber.getText().toString());

            editor.putString(getString(R.string.firstName), firstName.getText().toString());
            editor.putString(getString(R.string.familyName), familyName.getText().toString());
            editor.putString("phoneNumber", phoneNumber.getText().toString());
            editor.commit();
            Gson gson  = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
            try {
                JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,(String) getText(R.string.updateOption), new JSONObject(gson.toJson(user)),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Toast.makeText(OptionActivity.this, R.string.nameUpdate, Toast.LENGTH_LONG).show();
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
        editor.putInt("pref", spinner.getSelectedItemPosition());
        editor.putInt("delay", Integer.parseInt(delay.getText().toString()));
        editor.putInt("delaySub", Integer.parseInt(delaySub.getText().toString()));
        editor.commit();
    }
}
