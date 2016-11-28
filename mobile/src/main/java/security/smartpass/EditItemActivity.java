package security.smartpass;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Set;

import security.common.Constants;


public class EditItemActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {
    EditText editTextUserName,editTextPassword,editTextAppName,editTextAppUrl,editTextNote,editTextConfirmPassword;
    Button btnCancel,btnUpdate;
    LoginDatabaseAdapter loginDatabaseAdapter;
    TextView appNameView;
    List<AccountModel> accounts;
    String appId;
    App selectedApp;
    static Spinner dropdown = null;
    final App[] apps = new App[] {
            new App("Bank Of America", "com.infonow.bofa"),
            new App("Chase", "com.chase.sig.android"),
            new App("Facebook","com.facebook.pages.app"),
            new App("Gmail", "com.google.android.gm")
    };
    String selectedAppStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);



        Intent intent = getIntent();
        String appName = intent.getStringExtra("AppName");
        appId = intent.getStringExtra("AppId");


        loginDatabaseAdapter=new LoginDatabaseAdapter(this);
        loginDatabaseAdapter=loginDatabaseAdapter.open();

        Log.w("SmartPass",appName);

        // For notification testing
        accounts = loginDatabaseAdapter.getSingleEntry(appName);
        Log.w("SmartPass size",String.valueOf(accounts.size()));

        if (accounts.size() > 0) {
            AccountModel account = accounts.get(0);





            editTextUserName = (EditText) findViewById(R.id.userName);
            editTextPassword = (EditText) findViewById(R.id.userPass);
            editTextAppUrl = (EditText) findViewById(R.id.appUrl);
            editTextNote = (EditText) findViewById(R.id.userNote);
            editTextConfirmPassword = (EditText) findViewById(R.id.userPassConfim);


            editTextUserName.setText(account.getUserName(), TextView.BufferType.EDITABLE);
            editTextPassword.setText(account.getUserFirstPassword(), TextView.BufferType.EDITABLE);

             appNameView = (TextView) findViewById(R.id.appName);

            appNameView.setText(account.getAppName());
            selectedAppStr =appName;
            editTextAppUrl.setText(account.getAppUrl(), TextView.BufferType.EDITABLE);
            editTextNote.setText(account.getNote(), TextView.BufferType.EDITABLE);

            Button btnCancel = (Button) findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });

            Button btnUpdateAccount = (Button) findViewById(R.id.btnUpdate);
            btnUpdateAccount.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    String appName = selectedAppStr;
                    String appUrl = editTextAppUrl.getText().toString();
                    String note = editTextNote.getText().toString();
                    String userName = editTextUserName.getText().toString();
                    String password = editTextPassword.getText().toString();
                    String confirmPassword = editTextConfirmPassword.getText().toString();

                    // check if any of the fields are vaccant
                    if (userName.equals("") || password.equals("") || confirmPassword.equals("") || appName.equals("")) {
                        Toast.makeText(getApplicationContext(), "Empty Field", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        // update
                        // generate password split encrypted algorithm here
                        int split_index = password.length() / 2;
                        String mobilePass = password.substring(0, split_index);
                        String wearPass = password.substring(split_index);
                        Log.d("update acct", "password" + password + "combinded split:" + mobilePass + wearPass);
                        if (!isWearAvailable()) {
                            Toast.makeText(getApplicationContext(), "Please try again - Wear is not connected ", Toast.LENGTH_LONG).show();
                        } else {
                            loginDatabaseAdapter.updateEntry(userName, mobilePass, appId, appName, note, appUrl);
                            Intent sendIntent = new Intent(EditItemActivity.this, NotificationService.class);
                            sendIntent.putExtra("ACTION", Constants.ACTION_UPLOAD_WEAR);

                            sendIntent.putExtra(Constants.APP_NAME, appName);
                            sendIntent.putExtra(Constants.WEAR_PASS, wearPass);

                            startService(sendIntent);


                            Toast.makeText(getApplicationContext(), "Account Successfully Created ", Toast.LENGTH_LONG).show();
                            returnToMain();
                        }
                    }
                }
            });
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        // Get the currently selected State object from the spinner
        selectedApp = (App)dropdown.getSelectedItem();
    }

    public void onNothingSelected(AdapterView<?> parent )
    {
    }
    public void returnToMain() {
        Intent intent = new Intent(this, Main2Activity.class);
        finish();
        startActivity(intent);
    }
    private boolean isWearAvailable() {
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            return true;
        } else {
            Toast.makeText(this, "No paired bluetooth devices found", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void createNotification(AccountModel account) {

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

        intent = new Intent(this, ClipBoardBroadcastReceiver.class);
        intent.putExtra("credential",account.getUserFirstPassword());
        pIntent = PendingIntent.getBroadcast(this,1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification userPassNoti = new Notification.Builder(this)
                .setContentTitle("SmartPass")
                .setContentText("Click to copy your password").setSmallIcon(R.drawable.ic_notify)
                .setContentIntent(pIntent).build();
        // hide the notification after its selected
        userPassNoti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(1, userPassNoti);



    }
}
