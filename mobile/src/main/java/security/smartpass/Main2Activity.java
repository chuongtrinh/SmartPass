package security.smartpass;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import security.common.Constants;

public class Main2Activity extends AppCompatActivity {

    ListView list;
    AccountModelViewAdapter adapter;
    List<AccountModel> accountModels = new ArrayList<AccountModel>();
    LoginDatabaseAdapter loginDatabaseAdapter;
    Button creatBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.accounts_main);



        loginDatabaseAdapter=new LoginDatabaseAdapter(this);
        loginDatabaseAdapter=loginDatabaseAdapter.open();


        accountModels = loginDatabaseAdapter.getAllAccounts();

        Resources res =getResources();
        list= ( ListView )findViewById( R.id.listAccounts );  // List defined in XML ( See Below )


        Button btnRegister= (Button)findViewById(R.id.createBtn);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

        Button btnDeleteAll= (Button)findViewById(R.id.deleteBtn);
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //drops table
                //then creates table
                // then opens table
                loginDatabaseAdapter.deleteTable();
                Intent intent = new Intent(Main2Activity.this, NotificationService.class);
                intent.putExtra("ACTION", Constants.ACTION_DELETE_TABLE_WEAR);
                startService(intent);
                Log.d("main activity", "deleting table entries");
            }
        });


        Log.w("smartpass:",String.valueOf(accountModels.size()));
        adapter=new AccountModelViewAdapter( this, accountModels,res );
        list.setAdapter( adapter );


        //Create background service - for temporary testing only
        startService(new Intent(this, BackgroundService.class));
        Log.w("smartpass:","created service");

        establishEncryptionKey();


    }

    private void establishEncryptionKey() {
        File file = new File("iv");
        if(!file.exists()) {
            try {
                byte[] iv = {32, 12, -11, 100, -32, 94, 11, -34, 5, 114, -61, 57, -87, 9, -110, 42};
                FileOutputStream fileout=openFileOutput("iv", MODE_PRIVATE);
                BufferedOutputStream bos = new BufferedOutputStream(fileout);
                bos.write(iv);
                bos.flush();
                bos.close();
                Log.w("SaveIV:","Successfully save with: " + iv.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        file = new File("key.txt");
        if (!file.exists()) {
            Random rnd = new Random();
            int numLetters = 16;

            String randomLetters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String key = "";
            for (int n = 0; n < numLetters; n++){
                key += randomLetters.charAt(rnd.nextInt(randomLetters.length()));
            }
            try {
                FileOutputStream fileout=openFileOutput("key.txt", MODE_PRIVATE);
                OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                outputWriter.write(key);
                outputWriter.close();
                Log.w("SaveKey:","Successfully save with: " + key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public void createNewAccount() {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
      //  this.recreate();
    }

    /*****************  This function used by adapter ****************/
    public void onItemClick(int mPosition)
    {
        AccountModel tempValues = (AccountModel) accountModels.get(mPosition);
        Log.w("SmartPassMess","Clicked: " + tempValues.getAppName() + " with user_id: " + tempValues.getUserName());

        Intent intent = new Intent(this, EditItemActivity.class);

        intent.putExtra("AppId", tempValues.getAppId());
        intent.putExtra("AppName",tempValues.getAppName());
        startActivity(intent);

    }
}
