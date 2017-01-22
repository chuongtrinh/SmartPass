package security.smartpass;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Collections;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Set;


import se.simbio.encryption.Encryption;
import security.common.Constants;

public class CreateAccountActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText editTextUserName,editTextPassword,editTextAppName,editTextAppUrl,editTextNote,editTextConfirmPassword;
    Button btnSignUp;
    LoginDatabaseAdapter loginDataBaseAdapter;
    static Spinner dropdown = null;
    final App[] apps = new App[] {
            new App("Bank Of America", "com.infonow.bofa"),
            new App("Chase", "com.chase.sig.android"),
            new App("Facebook","com.facebook.katana"),
            new App("Gmail", "com.google.android.gm")
    };
    App selectedApp;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // create a instance of SQLite Database
       // loginDataBaseAdapter=new LoginDataBaseAdapter(this);
        loginDataBaseAdapter=new LoginDatabaseAdapter(this);
        loginDataBaseAdapter=loginDataBaseAdapter.open();

        // Get Refferences of Views
        editTextUserName=(EditText)findViewById(R.id.userName);
        editTextPassword=(EditText)findViewById(R.id.userPass);
        editTextAppUrl = (EditText)findViewById(R.id.appUrl);
        editTextNote = (EditText)findViewById(R.id.userNote);

        editTextConfirmPassword=(EditText)findViewById(R.id.userPassConfim);


        dropdown = (Spinner)findViewById(R.id.spinner1);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, apps);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);



        Button btnCreateAccount=(Button)findViewById(R.id.btnSignUp);
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                String appName = (selectedApp == null)? apps[0].toString(): selectedApp.toString();
                String appCode = (selectedApp == null)? apps[0].getCode(): selectedApp.getCode();
                String appUrl = editTextAppUrl.getText().toString();
                String note = editTextNote.getText().toString();
                String userName=editTextUserName.getText().toString();
                String password=editTextPassword.getText().toString();
                String confirmPassword=editTextConfirmPassword.getText().toString();

                // check if any of the fields are vaccant
                if(userName.equals("")||password.equals("")||confirmPassword.equals("") || appName.equals(""))
                {
                    Toast.makeText(getApplicationContext(), "Empty Field", Toast.LENGTH_LONG).show();
                    return;
                }
                // check if both password matches
                if(!password.equals(confirmPassword))
                {
                    Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                {
                    ArrayList<Pair> letters = new ArrayList<Pair>();
                    String permuted_password = "";
                    String index_list = "";

                    for(int i = 0; i < password.length(); i++) {
                        Pair pair = new Pair(i, password.charAt(i));
                        letters.add(pair);
                    }

                    Collections.shuffle(letters);

                    for(int i = 0; i < letters.size(); i++) {
                        permuted_password += letters.get(i).letter;
                        index_list += letters.get(i).index + ",";
                    }


                    // Save the Data in Database
                    // generate password split encrypted algorithm here
                    int split_index = permuted_password.length()/2;
                    String mobilePass = permuted_password.substring(0,split_index);
                    String wearPass = permuted_password.substring(split_index);
                    Log.w("CREATE ACCT", "name: " + appName + " original password: " + password + " permuted password: " + permuted_password + "\nindex list: " + index_list + " combinded split:" + mobilePass +" + " + wearPass);

                    if (!isWearAvailable()) {
                        Toast.makeText(getApplicationContext(), "Please try again - Wear is not connected ", Toast.LENGTH_LONG).show();
                    } else {
                        String appId = loginDataBaseAdapter.insertEntry(userName, mobilePass, appName, appCode, appUrl, index_list);


                        //String decrypted = encryption.decryptOrNull(encrypted);

                        String encryptedKey = "securesecuresecu";
                        byte[] iv = "[B@5c79df3fghjkl".getBytes();
                    //    byte[] iv = getIV();

                        //Encryption encryption = Encryption.getDefault("Key", "Salt", new byte[16]);
                        Encryption encryption = Encryption.getDefault(encryptedKey, appId, iv);


                        String encryptedWearPwd = encryption.encryptOrNull(wearPass);

                        Log.w("Encryption:",encryptedWearPwd);
                        Intent sendIntent = new Intent(CreateAccountActivity.this, NotificationService.class);
                        sendIntent.putExtra("ACTION", Constants.ACTION_UPLOAD_WEAR);
                        sendIntent.putExtra(Constants.APP_ID, appId);
                        sendIntent.putExtra(Constants.APP_NAME, appName);
                        sendIntent.putExtra(Constants.WEAR_PASS, encryptedWearPwd);

                        startService(sendIntent);

                        Toast.makeText(getApplicationContext(), "Account Successfully Created ", Toast.LENGTH_LONG).show();

                        String decryptedWearPwd = encryption.decryptOrNull(encryptedWearPwd);
                        Log.w("Decryption:",decryptedWearPwd);


                        returnToMain();
                    }
                }
            }
        });
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
            Log.d("key create", "key:" + s);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return "defaultKey";
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        // Get the currently selected State object from the spinner
        selectedApp = (App)dropdown.getSelectedItem();
        Log.w("Select app: ", selectedApp.toString());
    }

    public void onNothingSelected(AdapterView<?> parent )
    {
    }


    public void returnToMain() {
        Intent intent = new Intent(this, Main2Activity.class);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close The Database
        loginDataBaseAdapter.close();
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
}
