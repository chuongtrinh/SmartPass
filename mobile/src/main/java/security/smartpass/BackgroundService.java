package security.smartpass;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import security.common.Constants;


/**
 * Created by Chuong on 11/9/2016.
 */

public class BackgroundService extends Service {

    LoginDatabaseAdapter loginDatabaseAdapter;
    List<AccountModel> accounts;
    private boolean FLAG_BUSY =false;
    private Set<String> currentRunningApp = new HashSet<>();
    private Set<String> previousRunningApp = new HashSet<>();

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart after 100milli seconds the service once it has been killed android

        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);

    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        loginDatabaseAdapter=new LoginDatabaseAdapter(this);
        loginDatabaseAdapter=loginDatabaseAdapter.open();

        Log.w("SmartPass: ", "Start monitoring");
        // start monitoring and detecting apps
        callAsynchronousTask(this);

        //  new detectRunningApps().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,this);
    }
    class RunTimerTask extends TimerTask {
        Context context;
        final Handler handler = new Handler();
        public RunTimerTask(Context c) {
            this.context =c;
        }
        @Override
        public void run() {
            handler.post(new Runnable() {
                public void run() {
                    try {
                        new detectRunningApps().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,context);
                    } catch (Exception e) {
                    }
                }
            });
        }
    }
    public void callAsynchronousTask(Context context) {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        RunTimerTask doAsynchronousTask = new RunTimerTask(this);
        timer.schedule(doAsynchronousTask, 0, 3000); //execute in every 3s
    }
    private class detectRunningApps extends AsyncTask<Context, Void, List<AndroidAppProcess>> {
        long startTime;

        @Override
        protected List<AndroidAppProcess> doInBackground(Context... params) {
            return AndroidProcesses.getRunningForegroundApps(params[0]);
        }

        @Override
        protected void onPostExecute(List<AndroidAppProcess> processes) {
            StringBuilder sb = new StringBuilder();
            sb.append("Running apps:\n");
            for (AndroidAppProcess app : processes) {
                String appCode = app.name;
                sb.append('\n').append(app.name);
                //previousRunningApp.add(app.name);
                if (!appCode.isEmpty()) {
                    detectTargetForSmartPassApp(appCode.toLowerCase());
                }

            }
            Log.w("SmartAppService:", sb.toString());
        }
    }
    public void detectTargetForSmartPassApp(String appCode){

        Log.w("Detected: ",appCode);
        // For notification testing
        accounts = loginDatabaseAdapter.getSingleEntryWithCode(appCode);
        Log.w("SmartPass size",String.valueOf(accounts.size()));

        if (accounts.size() > 0) {
            AccountModel account = accounts.get(0);

            // Once we can obtain the user's information
            // we can sending notification to the watch
            // -sendNotificationToWatch
            // -recievePassWordFromWatch
            Log.w("Get from wear: ",account.getAppName());
            if (!FLAG_BUSY) {
                Intent sendIntent = new Intent(this, NotificationService.class);
                sendIntent.putExtra("ACTION", Constants.ACTION_NOTIFY_WEAR);
                sendIntent.putExtra(Constants.APP_NAME, account.getAppName());
                startService(sendIntent);
                FLAG_BUSY = true;
            }
            //createNOtification is in onMessageRecieved
            // triggered when the wear user clicks yes and sends  message back

           // createNotification(account);
        }
        return;
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

