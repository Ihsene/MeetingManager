package be.ac.umons.meetingmanager.meeting;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import be.ac.umons.meetingmanager.MainActivity;
import be.ac.umons.meetingmanager.R;

/**
 * Created by SogeP on 12-08-17.
 */

public class AlarmBroadcastReceive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String m = intent.getStringExtra("meeting");

        Intent intent2 = new Intent(context, MainActivity.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent2,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_logo_notification)
                .setContentTitle("Meeting Manager")
                .setContentText(context.getString(R.string.dontforget)+" "+m)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
