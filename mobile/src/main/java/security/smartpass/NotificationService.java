package security.smartpass;

import android.accounts.Account;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import se.simbio.encryption.Encryption;

import security.common.Constants;

public class NotificationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    private GoogleApiClient mApiClient;
    private LoginDatabaseAdapter loginDatabaseAdapter;

    public NotificationService() {
        super("NotificationService");
    }

    private void initGoogleApiClient() {
        Log.d("notif service", "trying to connect");
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        if (mApiClient != null && !(mApiClient.isConnected() || mApiClient.isConnecting())) {
            ConnectionResult result = mApiClient.blockingConnect();
            if(result.isSuccess()) {
                Log.d("notif mobile", "connected successfully");
            } else {
                Log.d("notif mobile", "unsuccessful google api");
            }
        }

        Wearable.MessageApi.addListener(mApiClient, this);
    }

    private void initDatabase() {
        loginDatabaseAdapter = new LoginDatabaseAdapter(this);
        loginDatabaseAdapter = loginDatabaseAdapter.open();

      //  loginDatabaseAdapater.insertEntry("2","boaHalfPass", "BOA", "url", "note");
        Log.d("debug","Save default entry in the database");

    }

    public void createNotification(AccountModel account, String wearPassPermuted) {

        // Setting up broadcastReceiver for CopyToClipBoard

        Intent intent = new Intent(this, ClipBoardBroadcastReceiver.class);
        intent.putExtra("credential",account.getUserName());
        PendingIntent pIntent = PendingIntent.getBroadcast(this,0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification userNameNoti = new Notification.Builder(this)
                .setContentTitle("SmartPass")
                .setContentText("Click to copy your username").setSmallIcon(R.drawable.ic_notify)
                .setContentIntent(pIntent).build();


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        userNameNoti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, userNameNoti);

        String permuted_password = account.getUserFirstPassword() + wearPassPermuted;

        ArrayList<Pair> letters = new ArrayList<Pair>();
        String[] index_list = account.getNote().split(",");
        Log.d("notif mobile", "notes" + account.getNote());
        String combinedPass = "";

        Log.d("permute", "length password:" + permuted_password.length());
        for(int i = 0; i < permuted_password.length(); i++) {
            Pair pair = new Pair(Integer.parseInt(index_list[i]), permuted_password.charAt(i));
            Log.d("permute", "index: " + index_list[i] + " letter:" + permuted_password.charAt(i));
            letters.add(pair);
        }

        Collections.sort(letters);

        for(int i = 0; i < letters.size(); i++) {
            combinedPass += letters.get(i).letter;
        }

        Log.d("notif mobile", "permuted pass: " + permuted_password + " combineded pass:" + combinedPass + " length:" + letters.size());

        intent = new Intent(this, ClipBoardBroadcastReceiver.class);
        intent.putExtra("credential",combinedPass);
        pIntent = PendingIntent.getBroadcast(this,1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification userPassNoti = new Notification.Builder(this)
                .setContentTitle("SmartPass")
                .setContentText("Click to copy your password").setSmallIcon(R.drawable.ic_notify)
                .setContentIntent(pIntent).build();
        // hide the notification after its selected
        userPassNoti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(1, userPassNoti);



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

    private void sendNotification(String path, String title, String msg) {
        Log.d("mobile","sending notification path:" + path);
        if (mApiClient.isConnected()) {
            PutDataMapRequest dataMapRequest = PutDataMapRequest.create(path);

            // Make sure the data item is unique. Usually, this will not be required, as the payload
            dataMapRequest.getDataMap().putDouble(Constants.DATA_NOTIFICATION_TIMESTAMP, System.currentTimeMillis());
            dataMapRequest.getDataMap().putString(Constants.DATA_NOTIFICATION, title);
            dataMapRequest.getDataMap().putString(Constants.DATA_NOTIFICATION_MSG, msg);
            PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(mApiClient, putDataRequest);
        }
        else {
            Log.e("mobile", "No connection to wearable available!");
        }
    }

    private void sendMessage( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                if(mApiClient.isConnected()) {
                    Log.d("notif mobile", "nodes");
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        Log.d("send message mobile", text);
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                mApiClient, node.getId(), path, text.getBytes()).await();
                        if (result.getRequestId() == MessageApi.UNKNOWN_REQUEST_ID) {
                            Log.d("mobile", "unable to send message");
                        }
                    }
                } else {
                    Log.d("mobile notif", "google api not connected.....");
                }
            }
        }).start();
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.d("Mobile", "recieved message:" + new String(messageEvent.getData()));

        //wear sent back its half of password to mobile phone
        if(messageEvent.getPath().equals(Constants.WEAR_MESSAGE_PASSWORD)) {
            String msg = new String(messageEvent.getData());
            String[] elems = msg.split(",");
            String appName = elems[0];
            String wearPassEncrypted = elems[1];



            AccountModel account = loginDatabaseAdapter.getSingleEntry(appName).get(0);
            String wearPassPermuted = decryptWearPass(account, wearPassEncrypted);
            createNotification(account, wearPassPermuted);

        }
    }

    private String decryptWearPass(AccountModel account, String wearPassEncrypted) {

        String wearPass = "";
        String encryptedKey = "securesecuresecu";
        byte[] iv = "[B@5c79df3fghjkl".getBytes();
      //  byte[] iv = getIV();

        Encryption encryption = Encryption.getDefault(encryptedKey, account.getAppId(), iv);
        wearPass =  encryption.decryptOrNull(wearPassEncrypted);

        Log.d("decrypt wear pass", "wear pass encrypted:" + wearPassEncrypted + " decrypted:" + wearPass);

        return wearPass;
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
            final String action = intent.getStringExtra("ACTION");

            if (action.equals(Constants.ACTION_DELETE_TABLE_WEAR)) {
                sendNotification(Constants.ACTION_DELETE_TABLE_WEAR, "", "");
            } else {
                final String appName = intent.getStringExtra(Constants.APP_NAME);

                Log.d("notif service", "action:" + action);
                Log.d("notif service", "appName:" + appName);

                List<AccountModel> accounts = loginDatabaseAdapter.getSingleEntry(appName);
                AccountModel account;
                if (accounts.size() == 0) {
                    Log.d("notif mobile", "accounts empty for appName" + appName);
                } else {
                    account = accounts.get(0);

                    if (action.equals(Constants.ACTION_NOTIFY_WEAR)) {

                        sendNotification(Constants.ACTION_NOTIFY_WEAR, account.getAppName(), account.getAppId());

                    } else if (action.equals(Constants.ACTION_UPLOAD_WEAR)) {
                        String msg = "";
                        final String wearPass = intent.getStringExtra(Constants.WEAR_PASS);

                        msg = account.getAppId() + "," + account.getAppName() + "," + wearPass;
                        sendNotification(Constants.ACTION_UPLOAD_WEAR, "", msg);

                    } else {
                        Log.d("notif mobile", "unknown action" + action);
                    }
                }
            }

        }


    }

    private byte[] getIV() {

        try {
            FileInputStream fileIn = openFileInput("iv");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] data = new byte[16];
            int bytes = 0;

            while (  ( bytes = fileIn.read(data)) != -1 ){
                bos.write(data, 0, bytes);
            }
            byte[] res = bos.toByteArray();
            return res;
        }catch (Exception e) {
            e.printStackTrace();
            return new byte[16];
        }

    }
    private String getKey() {
        try {
            FileInputStream fileIn=openFileInput("key.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[16];
            String s="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
            Log.d("key", "key:" + s);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return "defaultKey";
        }
    }

}
