package be.ac.umons.meetingmanager.meeting;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.connection.UserInfo;

/**
 * Created by SogeP on 04-08-17.
 */

public class SubjectAdapter extends ArrayAdapter<Subject> {
    private ArrayList<Subject> subjects;
    private Context context;
    private int resource;

    public SubjectAdapter(Context context, ArrayList<Subject> data, int resource){
        super(context, resource, data);
        this.context = context;
        this.subjects = data;
        this.resource = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(resource, parent, false);

        TextView name = (TextView) convertView.findViewById(R.id.textViewSubjetName);
        TextView info = (TextView) convertView.findViewById(R.id.textViewInfo);
        ImageButton up = (ImageButton) convertView.findViewById(R.id.imageButtonUP);
        ImageButton down = (ImageButton) convertView.findViewById(R.id.imageButtonDown);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapAction(position, true);
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapAction(position, false);
            }
        });


        name.setText(subjects.get(position).getName()+" - "+subjects.get(position).getDuration()+" min");
        info.setText(subjects.get(position).getInfo());
        return convertView;
    }

    public void swapAction(int position, boolean down) {
        int finalPosition = position+(down?-1:+1);
        if((position == 0 && down) ||(position == subjects.size()-1 && !down))
            return;
        Collections.swap(subjects, position, finalPosition);
        this.notifyDataSetChanged();
    }
}
