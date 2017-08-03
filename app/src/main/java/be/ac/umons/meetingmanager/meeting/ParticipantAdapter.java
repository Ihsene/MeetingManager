package be.ac.umons.meetingmanager.meeting;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.UserInfo;

/**
 * Created by SogeP on 02-08-17.
 */

public class ParticipantAdapter extends ArrayAdapter<UserInfo> {
    private ArrayList<UserInfo> friends;
    private Context context;
    private int resource;

    public ParticipantAdapter(Context context, ArrayList<UserInfo> data, int resource){
        super(context, resource, data);
        this.context = context;
        this.friends = data;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(resource, parent, false);

        TextView name = (TextView) convertView.findViewById(R.id.textViewName);
        TextView email = (TextView) convertView.findViewById(R.id.textViewEmail);

        name.setText(friends.get(position).getName()+" "+friends.get(position).getFamilyName());
        email.setText(friends.get(position).getEmail());

        if(resource == R.layout.activity_create_meeting_list)
        {
            CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBoxParticipant);
            if(friends.get(position).getValue() == 1)
                cb.setChecked(true);
            else
                cb.setChecked(false);
        }
        return convertView;
    }

}
