package security.smartpass;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.IntentFilter;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import security.common.Constants;


public class MainActivity2 extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mApiClient;
    private ArrayAdapter<String> mAdapter;
    private BroadcastReceiver mBroadcastReceiver;
    private LoginWearDatabaseAdapter loginWearDatabaseAdapter;

    private ListView mListView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.list);


        loginWearDatabaseAdapter=new LoginWearDatabaseAdapter(this);
        loginWearDatabaseAdapter=loginWearDatabaseAdapter.open();

        loginWearDatabaseAdapter.insertEntry("2","BOA","boaHalfPass");
        Log.d("debug","Save in the database");


        mAdapter = new ArrayAdapter<String>(this, R.layout.list_item);
        mListView.setAdapter(mAdapter);


        AccountWearModel acc;
        List<AccountWearModel> accs = loginWearDatabaseAdapter.getSingleEntry("1");
        if (accs.size() > 0) {
            acc = accs.get(0);
            mAdapter.add(acc.getAppName() + " : " + acc.getUserSecondPassword());
            Log.d("debug",acc.getAppId()+ " : " + acc.getAppName() + " : " + acc.getUserSecondPassword());

        } else {
            mAdapter.add("Empty");
            Log.d("debug","empty");

        }

// not needed handled by the onMessageRecieved
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("ACTION_SEND");
//
//        mBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, final Intent intent) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String message = intent.getStringExtra("message");
//                        if (!message.isEmpty()) {
//                            Log.d("debug","adding :" + message);
//                            mAdapter.add(message);
//                            mAdapter.notifyDataSetChanged();
//                        }
//                    }
//                });
//            }
//        };
//
//        registerReceiver(mBroadcastReceiver, filter);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initGoogleApiClient();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        Log.d("debug","created main activity");
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        if (mApiClient != null && !(mApiClient.isConnected() || mApiClient.isConnecting()))
            mApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mApiClient != null && !(mApiClient.isConnected() || mApiClient.isConnecting()))
            mApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getPath().equalsIgnoreCase(Constants.WEAR_MESSAGE_PATH)) {
                    Log.d("debug","message recieved adding :" + new String(messageEvent.getData()));
                    mAdapter.add(new String(messageEvent.getData()));
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d("D","connected");
        Wearable.MessageApi.addListener(mApiClient, this);
    }

    @Override
    protected void onStop() {
        if (mApiClient != null) {
            Wearable.MessageApi.removeListener(mApiClient, this);
            if (mApiClient.isConnected()) {
                mApiClient.disconnect();
            }
        }
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        if (mApiClient != null)
            mApiClient.unregisterConnectionCallbacks(this);
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
}