package be.ac.umons.meetingmanager.meeting;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import be.ac.umons.meetingmanager.R;

/**
 * Created by SogeP on 08-08-17.
 */

public class MeetingAdapter extends ArrayAdapter<Meeting> {
    private ArrayList<Meeting> meetings;
    private Context context;
    private int resource;

    public MeetingAdapter(Context context, ArrayList<Meeting> data, int resource){
        super(context, resource, data);
        this.context = context;
        this.meetings = data;
        this.resource = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(resource, parent, false);

        TextView name = (TextView) convertView.findViewById(R.id.textViewTitle);
        TextView place = (TextView) convertView.findViewById(R.id.textViewPlace);
        TextView date =  (TextView) convertView.findViewById(R.id.textViewDate);


        name.setText(meetings.get(position).getTitle());
        place.setText(meetings.get(position).getPlace());
        date.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(meetings.get(position).getDate()));
        return convertView;
    }
}