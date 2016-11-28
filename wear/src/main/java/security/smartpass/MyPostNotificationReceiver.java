package security.smartpass;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.ConfirmationOverlay;
import android.widget.Toast;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;

//**************************
// NOT BEING USED CURRENTLY
//**************************
public class MyPostNotificationReceiver extends BroadcastReceiver {
    public static final String CONTENT_KEY = "contentText";


    public MyPostNotificationReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        Intent smartpassIntent = new Intent(context, MainActivity.class);
        PendingIntent smartpassPendingIntent = PendingIntent.getActivity(context, 0, smartpassIntent, 0);
        Intent displayIntent = new Intent(context, MyNotification.class);
        String text = intent.getStringExtra(CONTENT_KEY);

        // Create the action
        NotificationCompat.Action actionYes =
                new NotificationCompat.Action.Builder(R.drawable.ic_check_black_24dp,"Yes",smartpassPendingIntent)
                        .build();

        // Create the action
        NotificationCompat.Action actionNo =
                new NotificationCompat.Action.Builder(R.drawable.ic_close_black_24dp,"No",smartpassPendingIntent)
                        .build();

        // Create a WearableExtender to add functionality for wearables
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setDisplayIntent(PendingIntent.getActivity(context, 0, displayIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(actionYes)
                .addAction(actionNo);

        // Create a NotificationCompat.Builder to build a standard notification
// then extend it with the WearableExtender
        Notification notif = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_lock_outline_black_24dp)
                .setContentTitle("Login?")
                .extend(wearableExtender)
                .build();

//        NotificationCompat.WearableExtender notification = new NotificationCompat.WearableExtender()
//                .setContentIcon(R.mipmap.ic_launcher)
//                .set(PendingIntent.getActivity(context, 0, displayIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT))
////                .extend(new Notification.WearableExtender()
////                        .setDisplayIntent(PendingIntent.getActivity(context, 0, displayIntent,
////                                PendingIntent.FLAG_UPDATE_CURRENT)))
//                .setContentAction(R.drawable.ic_launcher,"smartpass",smartpassPendingIntent)
//                .build();

        ((NotificationManagerCompat) NotificationManagerCompat.from(context)).notify(0, notif);

        Toast.makeText(context, context.getString(R.string.notification_posted), Toast.LENGTH_SHORT).show();
    }
}
