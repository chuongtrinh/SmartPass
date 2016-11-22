package security.smartpass;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.widget.Toast;

/**
 * Created by Chuong on 11/2/2016.
 */

public class ClipBoardBroadcastReceiver  extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        String credential = intent.getStringExtra("credential");

        ClipData clip = ClipData.newPlainText("info", credential);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();


        // Vibrartion for fun
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);

    }
}
