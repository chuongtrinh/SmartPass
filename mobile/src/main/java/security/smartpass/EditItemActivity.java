package security.smartpass;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class EditItemActivity extends AppCompatActivity {
    EditText editTextUserName,editTextPassword,editTextAppName,editTextAppUrl,editTextNote,editTextConfirmPassword;
    Button btnCancel,btnUpdate;
    LoginDatabaseAdapter loginDatabaseAdapter;
    List<AccountModel> accounts;
    String appId;
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
        accounts = loginDatabaseAdapter.getSinlgeEntry(appName);
        Log.w("SmartPass size",String.valueOf(accounts.size()));

        if (accounts.size() > 0) {
            AccountModel account = accounts.get(0);

            // Once we can obtain the user's information
            // we can sending notification to the watch
            // -sendNotificationToWatch
            // -recievePassWordFromWatch
            createNotification(account);


            editTextUserName = (EditText) findViewById(R.id.userName);
            editTextPassword = (EditText) findViewById(R.id.userPass);
            editTextAppName = (EditText) findViewById(R.id.appName);
            editTextAppUrl = (EditText) findViewById(R.id.appUrl);
            editTextNote = (EditText) findViewById(R.id.userNote);
            editTextConfirmPassword = (EditText) findViewById(R.id.userPassConfim);


            editTextUserName.setText(account.getUserName(), TextView.BufferType.EDITABLE);
            editTextPassword.setText(account.getUserFirstPassword(), TextView.BufferType.EDITABLE);
            editTextAppName.setText(account.getAppName(), TextView.BufferType.EDITABLE);
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

                    String appName = editTextAppName.getText().toString();
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
                        loginDatabaseAdapter.updateEntry(userName, password, appId, appName, note, appUrl);
                        Toast.makeText(getApplicationContext(), "Account Successfully Created ", Toast.LENGTH_LONG).show();
                        returnToMain();
                    }
                }
            });
        }
    }
    public void returnToMain() {
        Intent intent = new Intent(this, Main2Activity.class);
        finish();
        startActivity(intent);
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
