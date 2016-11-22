package security.smartpass;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class WearMessageListenerService extends WearableListenerService {
    private static final String START_ACTIVITY = "/start_activity";
    private static final String MESSAGE = "/message";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(START_ACTIVITY)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        // not needed handled by the onMessageRecieved
//         else if(messageEvent.getPath().equalsIgnoreCase(MESSAGE)){
//            final String message = new String(messageEvent.getData());
//
//            Log.d("broadcasting",message);
//            // Broadcast message to wearable activity for display
//            Intent messageIntent = new Intent();
//            messageIntent.setAction(Intent.ACTION_SEND);
//            messageIntent.putExtra("message", message);
//          //  LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
//
//        }
            else {
            super.onMessageReceived(messageEvent);
        }
    }

}