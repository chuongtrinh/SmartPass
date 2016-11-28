package security.smartpass;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.List;

import security.common.Constants;

public class WearMessageListenerService extends WearableListenerService {

    private static final int NOTIF_ID = 0;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("wearlistener","" + messageEvent.getPath());
        if (messageEvent.getPath().equalsIgnoreCase(Constants.START_ACTIVITY)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
            else {
            super.onMessageReceived(messageEvent);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("wearlsitener","data changed");
        for(DataEvent dataEvent: dataEvents) {
            if(dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                Log.d("wearlist", "path" + dataEvent.getDataItem().getUri().getPath());

                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                String title = dataMapItem.getDataMap().getString(Constants.DATA_NOTIFICATION);
                String msg = dataMapItem.getDataMap().getString(Constants.DATA_NOTIFICATION_MSG);

                String path = dataEvent.getDataItem().getUri().getPath();
                if(Constants.ACTION_NOTIFY_WEAR.equals(path)){
                    launchNotification(title,msg);

                } else if(Constants.ACTION_UPLOAD_WEAR.equals(path) ||
                          Constants.ACTION_DELETE_ENTRY_WEAR.equals(path) ||
                          Constants.ACTION_DELETE_TABLE_WEAR.equals(path)){

                    Log.d("wear list", "starting upload wear action" + msg);
                    Intent sendIntent = new Intent(this, NotificationService.class);
                    sendIntent.putExtra("ACTION", path);
                    sendIntent.putExtra("MSG",msg);
                    startService(sendIntent);
                } else {
                    Log.d("wear list", "unrecongnized path:" + path +  " msg" + msg);
                }
            }
        }
    }

    private void launchNotification(String title, String message){
        Intent backIntentYes = new Intent(this, NotificationService.class);
        backIntentYes.setAction(Constants.ACTION_PASSWORD_YES);
        backIntentYes.putExtra(Constants.DATA_NOTIFICATION,title);
        backIntentYes.putExtra(Constants.DATA_NOTIFICATION_MSG,message);
        PendingIntent backPendingIntentYes = PendingIntent.getService(this, 0, backIntentYes, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent backIntentNo = new Intent(this, NotificationService.class);
        backIntentNo.setAction(Constants.ACTION_PASSWORD_NO);
        backIntentNo.putExtra(Constants.DATA_NOTIFICATION,title);
        backIntentNo.putExtra(Constants.DATA_NOTIFICATION_MSG,message);
        PendingIntent backPendingIntentNo = PendingIntent.getService(this, 0, backIntentNo, PendingIntent.FLAG_UPDATE_CURRENT);

  //      Intent displayIntent = new Intent(this, MyNotification.class);
  //      PendingIntent displayPending = PendingIntent.getActivity(this, 0, displayIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        // Create the action
        NotificationCompat.Action actionYes =
                new NotificationCompat.Action.Builder(R.drawable.ic_check_black_24dp,"Yes",backPendingIntentYes)
                        .build();

        // Create the action
        NotificationCompat.Action actionNo =
                new NotificationCompat.Action.Builder(R.drawable.ic_close_black_24dp,"No",backPendingIntentNo)
                        .build();

//        List <NotificationCompat.Action> actions = new ArrayList<NotificationCompat.Action>();
//        actions.add(actionYes);
//        actions.add(actionNo);
        // Create a WearableExtender to add functionality for wearables
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
               // .setDisplayIntent(displayPending)
                .addAction(actionYes)
                .addAction(actionNo);

        // Create a NotificationCompat.Builder to build a standard notification
// then extend it with the WearableExtender
        Notification notif = new NotificationCompat.Builder(this)
              //  .setFullScreenIntent(displayPending,true)
                .setSmallIcon(R.drawable.ic_lock_outline_black_24dp)
                .setColor(Color.RED)
                .setContentInfo("Accounts")
                .setContentTitle("Login?")
                .setContentText(title)
          //      .setContentIntent(displayPending)
                .extend(wearableExtender)
                .build();

        ((NotificationManagerCompat) NotificationManagerCompat.from(this)).notify(NOTIF_ID, notif);

    }


}