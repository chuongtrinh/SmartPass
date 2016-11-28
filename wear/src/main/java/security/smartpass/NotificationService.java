package security.smartpass;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

import security.common.Constants;


public class NotificationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {


    private static final int NOTIF_ID = 0;
    private GoogleApiClient mApiClient;
    private LoginWearDatabaseAdapter loginWearDatabaseAdapter;

    public NotificationService() {
        super("NotificationService");
    }

    private void initGoogleApiClient() {
        Log.d("notif service", "trying to connect");
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        if (mApiClient != null && !(mApiClient.isConnected() || mApiClient.isConnecting()))
            mApiClient.blockingConnect();
    }

    private void initDatabase() {
        loginWearDatabaseAdapter = new LoginWearDatabaseAdapter(this);
        loginWearDatabaseAdapter = loginWearDatabaseAdapter.open();

        Log.d("wear notif","database");
       // for(AccountWearModel acc : loginWearDatabaseAdapter.get)

       // loginWearDatabaseAdapter.insertEntry("2","BOA","boaHalfPass");
       // Log.d("debug","Save in the database");

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("D","connected");
        Wearable.MessageApi.addListener(mApiClient, this);
        //resolveNode();

        //  sendMessage(START_ACTIVITY,"");
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    private void sendMessage( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    Log.d("send message wear",text);
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes() ).await();
                    if(result.getRequestId() == MessageApi.UNKNOWN_REQUEST_ID) {
                        Log.d("wear", "unable to send message");
                    }
                }
            }
        }).start();
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        sendMessage(Constants.WEAR_MESSAGE_PATH,"echo:"+ new String(messageEvent.getData()));
    }



    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionPassword(Context context, String param1, String param2) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(Constants.ACTION_PASSWORD_YES);
        intent.putExtra(Constants.DATA_NOTIFICATION, param1);
        intent.putExtra(Constants.DATA_NOTIFICATION_MSG, param2);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        initGoogleApiClient();
        initDatabase();
        Log.d("notif service", "connected");

        if (intent != null) {
            final String action = intent.getAction();
            final String actionListener = intent.getStringExtra("ACTION");
            Log.d("notif service", "action:" + action);

            if (Constants.ACTION_PASSWORD_YES.equals(action)) {
                String appName = intent.getStringExtra(Constants.DATA_NOTIFICATION);
                String appId = intent.getStringExtra(Constants.DATA_NOTIFICATION_MSG);

                Log.d("notif wear", "appName" + appName);
                Log.d("notif wear", "appId" + appId);


                String wearPassword = "";

                AccountWearModel acc;
                List<AccountWearModel> accs = loginWearDatabaseAdapter.getSingleEntry(appId);
                if(accs.isEmpty()) {
                    Log.d("notif service", "error didn't find in wear database");
                    appName = "error";
                    wearPassword = "error app not found in wear database";
                } else {

                    wearPassword = accs.get(0).getUserSecondPassword();
                }

                handleActionPassword(appName, wearPassword);

                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(NOTIF_ID);

            } else if(Constants.ACTION_PASSWORD_NO.equals(action)) {
                Log.d("notif service", "clicked no");

                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(NOTIF_ID);

            } else if(Constants.ACTION_UPLOAD_WEAR.equals(actionListener)) {

                String msg = intent.getStringExtra("MSG");
                Log.d("notif wear", "action upload wear msg" + msg);
                String[] elems = msg.split(",");

                // If the appid is already taken delete and then insert
                List<AccountWearModel> accounts = loginWearDatabaseAdapter.getSingleEntry(elems[0]);
                if (accounts != null) {
                    for (AccountWearModel account : accounts) {
                        Log.d("notif wear", "Caution deleting existing appid:" + elems[0] + " app name" + account.getAppName());
                        loginWearDatabaseAdapter.deleteEntry(account.getAppId() + "");
                    }
                }


                loginWearDatabaseAdapter.insertEntry(elems[0], elems[1], elems[2]);

                Log.d("notif wear", "added app to db" + elems[0] + elems[1] + elems[2]);

            } else if(Constants.ACTION_DELETE_ENTRY_WEAR.equals(actionListener)) {
                String msg = intent.getStringExtra("MSG");
                loginWearDatabaseAdapter.deleteEntry(msg);
                Log.d("notif wear", "deleted entry with app id" + msg);
            } else if(Constants.ACTION_DELETE_TABLE_WEAR.equals(actionListener)) {
                loginWearDatabaseAdapter.deleteTable();
                Log.d("notif wear", "deleted table");
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPassword(String appName, String wearPass) {
        sendMessage(Constants.WEAR_MESSAGE_PASSWORD,appName + "," + wearPass);
    }

}
