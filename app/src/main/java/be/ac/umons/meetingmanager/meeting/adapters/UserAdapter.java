package be.ac.umons.meetingmanager.meeting.adapters;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.meeting.UserInfo;

/**
 * Created by SogeP on 02-08-17.
 */

public class UserAdapter extends ArrayAdapter<UserInfo>  {
    private ArrayList<UserInfo> friends;
    private ArrayList<Integer> hidden;
    private Context context;
    private int resource;

    public UserAdapter(Context context, ArrayList<UserInfo> data, int resource){
        super(context, resource, data);
        this.context = context;
        this.friends = data;
        this.resource = resource;
        hidden = new ArrayList<Integer>();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(resource, parent, false);

        TextView name = (TextView) convertView.findViewById(R.id.textViewName);
        TextView email = (TextView) convertView.findViewById(R.id.textViewEmail);

        name.setText(friends.get(position).getName()+" "+friends.get(position).getFamilyName());
        if(email != null)
            email.setText(friends.get(position).getEmail());

        for(Integer itr : hidden)
            if(itr.equals(position))
            {
                convertView.setLayoutParams(new AbsListView.LayoutParams(-1,1));
                convertView.setVisibility(View.GONE);
            }else
            {
                convertView.setVisibility(View.VISIBLE);
                convertView.setLayoutParams(new AbsListView.LayoutParams(-1,-2));
            }


        if(resource == R.layout.layout_see_friends)
        {
            CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox);
            cb.setChecked(friends.get(position).isTaken());
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    friends.get(position).setTaken(isChecked);

                }
            });
        }

        if(resource == R.layout.layout_presence_member)
        {
            ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.presence);
            imageButton.setImageResource(friends.get(position).isHere()? android.R.drawable.checkbox_on_background : android.R.drawable.ic_delete);
        }
        return convertView;
    }
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        hidden.clear();
        if(charText.length() != 0)
        {
            for(int i = 0; i < friends.size(); i++)
                if (!friends.get(i).getName().toLowerCase(Locale.getDefault()).contains(charText) &&
                        !friends.get(i).getFamilyName().toLowerCase(Locale.getDefault()).contains(charText) &&
                        !friends.get(i).getEmail().toLowerCase(Locale.getDefault()).contains(charText))
                    hidden.add(i);
        }
        notifyDataSetChanged();
    }

}
