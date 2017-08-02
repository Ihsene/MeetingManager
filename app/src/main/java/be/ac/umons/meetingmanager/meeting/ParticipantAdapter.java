package be.ac.umons.meetingmanager.meeting;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.UserInfo;

/**
 * Created by SogeP on 02-08-17.
 */

public class ParticipantAdapter extends ArrayAdapter<UserInfo> {
    UserInfo[] friends;
    Context context;

    public ParticipantAdapter(Context context, UserInfo[] data){
        super(context, R.layout.activity_create_meeting_list, data);
        this.context = context;
        this.friends = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((CreateMeetingActivity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.activity_create_meeting_list, parent, false);

        TextView name = (TextView) convertView.findViewById(R.id.textViewName);
        TextView email = (TextView) convertView.findViewById(R.id.textViewEmail);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBoxParticipant);
        name.setText(friends[position].getName()+" "+friends[position].getFamilyName());
        email.setText(friends[position].getEmail());
        if(friends[position].getValue() == 1)
            cb.setChecked(true);
        else
            cb.setChecked(false);
        return convertView;
    }

}
