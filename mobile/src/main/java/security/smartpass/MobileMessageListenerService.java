package security.smartpass;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import security.common.Constants;

public class MobileMessageListenerService extends WearableListenerService {


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("mobile listener", new String(messageEvent.getData()));
        if (messageEvent.getPath().equalsIgnoreCase(Constants.START_ACTIVITY)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

}