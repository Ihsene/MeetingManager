package be.ac.umons.meetingmanager.connection;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import be.ac.umons.meetingmanager.MainActivity;
import be.ac.umons.meetingmanager.R;
import be.ac.umons.meetingmanager.meeting.activities.MeetingActivity;

public class MeetingFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";

    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Intent localMessage = new Intent(ActivityReceiver.CURRENT_ACTIVITY_ACTION);
            localMessage.putExtra("user", remoteMessage.getData().get("user"));
            localMessage.putExtra("userId", remoteMessage.getData().get("userId"));
            localMessage.putExtra("modif", remoteMessage.getData().get("modif"));
            localMessage.putExtra("timeLeft", remoteMessage.getData().get("timeLeft"));
            localMessage.putExtra("currentIndex", remoteMessage.getData().get("currentIndex"));
            localMessage.putExtra("presence", remoteMessage.getData().get("presence"));
            localMessage.putExtra("started", remoteMessage.getData().get("meetStarted"));
            LocalBroadcastManager.getInstance(this).sendBroadcast(localMessage);

            if(remoteMessage.getData().get("modif").equals("icommingSubect"))
            {
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.setting), this.MODE_PRIVATE);
                int delay = sharedPreferences.getInt("delaySub", 5);
                if(Math.round(Integer.parseInt(remoteMessage.getData().get("timeLeft")) / 60000) > delay || MeetingActivity.isActive())
                    return;
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());
        }
    }

    private void sendNotification(String messageBody) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo_notification)
                .setContentTitle("Meeting Manager")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}