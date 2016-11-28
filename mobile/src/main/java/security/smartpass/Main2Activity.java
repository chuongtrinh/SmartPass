package security.smartpass;

import android.content.Intent;
import android.content.res.Resources;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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

    }

    private void createBackgroundService() {
        startService(new Intent(this, BackgroundService.class));


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
