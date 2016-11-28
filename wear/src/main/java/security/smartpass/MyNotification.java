package security.smartpass;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.content.Intent;

import security.common.Constants;

public class MyNotification extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String title = intent.getStringExtra(Constants.DATA_NOTIFICATION);
        String message = intent.getStringExtra(Constants.DATA_NOTIFICATION_MSG);


        Log.d("debug","created mynotification");
        setContentView(R.layout.activity_notification);
        mTextView = (TextView) findViewById(R.id.text);
        mTextView.setText(title+message);
    }
}
