package security.smartpass;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateAccountActivity extends AppCompatActivity {
    EditText editTextUserName,editTextPassword,editTextAppName,editTextAppUrl,editTextNote,editTextConfirmPassword;
    Button btnSignUp;
    LoginDatabaseAdapter loginDataBaseAdapter;

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
        editTextAppName = (EditText)findViewById(R.id.appName);
        editTextAppUrl = (EditText)findViewById(R.id.appUrl);
        editTextNote = (EditText)findViewById(R.id.userNote);


        editTextConfirmPassword=(EditText)findViewById(R.id.userPassConfim);

        Button btnCreateAccount=(Button)findViewById(R.id.btnSignUp);
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                String appName = editTextAppName.getText().toString();
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
                    // Save the Data in Database
                    // generate password split encrypted algorithm here

                    loginDataBaseAdapter.insertEntry(userName, password, appName, appUrl,note);
                    Toast.makeText(getApplicationContext(), "Account Successfully Created ", Toast.LENGTH_LONG).show();
                    returnToMain();
                }
            }
        });
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
}
