package be.ac.umons.meetingmanager.meeting;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import be.ac.umons.meetingmanager.meeting.activities.MeetingActivity;
import be.ac.umons.meetingmanager.meeting.activities.MeetingManagerActivity;

/**
 * Created by SogeP on 11-08-17.
 */

public class ActivityReceiver extends BroadcastReceiver {

    private Activity activity;
    public static final String CURRENT_ACTIVITY_ACTION = "current.activity.action";
    public static final IntentFilter CURRENT_ACTIVITY_RECEIVER_FILTER = new IntentFilter(CURRENT_ACTIVITY_ACTION);
    public ActivityReceiver(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getStringExtra("modif").equals("joinFriend") || intent.getStringExtra("modif").equals("leaveFriend"))
            ((MeetingActivity) activity).registerInAndOutUser(intent.getStringExtra("user"),
                    intent.getStringExtra("userId"), intent.getStringExtra("modif").equals("joinFriend"), true);
        else if(intent.getStringExtra("modif").equals("infoMeeting")) {
            ((MeetingActivity) activity).loadDate(Long.valueOf(intent.getStringExtra("timeLeft")).longValue(),
                    Integer.parseInt(intent.getStringExtra("currentIndex")),
                    new ArrayList<>(Arrays.asList(intent.getStringExtra("presence").split(" "))),
                    intent.getStringExtra("started").equals("true"));
        }
    }
}
