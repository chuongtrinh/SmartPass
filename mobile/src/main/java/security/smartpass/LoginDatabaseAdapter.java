package security.smartpass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chuong on 11/2/2016.
 */

public class LoginDatabaseAdapter {
    static final String DATABASE_NAME = "accounts.db";
    static final int DATABASE_VERSION = 1;
    public static final int NAME_COLUMN = 1;
    // TODO: Create public field for each column in your table.
    // SQL Statement to create a new database.
    static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "+"ACCOUNTS"+
            "( " +"APPID"+" integer primary key autoincrement,"+ "APPNAME text," +
                "USERNAME  text,PASSWORD text, APPURL text, NOTE text); ";

    public  SQLiteDatabase db;
    private final Context context;
    private DatabaseHelper dbHelper;

    public LoginDatabaseAdapter(Context _context)
    {
        context = _context;
        dbHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public LoginDatabaseAdapter open() throws SQLException
    {
        db = dbHelper.getWritableDatabase();
        return this;
    }
    public void close()
    {
        db.close();
    }

    public  SQLiteDatabase getDatabaseInstance()
    {
        return db;
    }

    public void insertEntry(String userName,String password,String appName, String appUrl, String note)
    {
        ContentValues newValues = new ContentValues();
        // Assign values for each row.
        newValues.put("USERNAME", userName);
        newValues.put("PASSWORD",password);
        newValues.put("APPNAME", appName);
        newValues.put("APPURL",appUrl);
        newValues.put("NOTE", note);


        db.insert("ACCOUNTS", null, newValues);
       // Toast.makeText(context, "New Account Is Successfully Saved", Toast.LENGTH_LONG).show();

    }
    public void dropTable() {
        db.execSQL("DROP TABLE IF EXISTS ACCOUNTS");
        Toast.makeText(context, "All accounts have been deleted", Toast.LENGTH_LONG).show();
    }
    public int deleteEntry(String appId)
    {
        String where="APPID=?";
        int numberOFEntriesDeleted= db.delete("ACCOUNTS", where, new String[]{appId}) ;
        return numberOFEntriesDeleted;
    }
    // Note: Chuong - This is where we need to consider two same app accounts cases
    private boolean checkTableExistence() {

        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+"ACCOUNTS"+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }
    public List<AccountModel> getAllAccounts() {

        List<AccountModel> accounts = new ArrayList<AccountModel>();

        if (!checkTableExistence()) {
            db.execSQL(DATABASE_CREATE);
            return accounts;
        }

        Cursor cursor = db.rawQuery("SELECT * FROM ACCOUNTS",new String[]{});
        //Cursor cursor=db.query("ACCOUNTS", null, "*", null, null, null, null);


        if(cursor.getCount()<1)
        {
            cursor.close();
            return accounts;
        }


        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            AccountModel acc = new AccountModel();
            acc.setAppId(cursor.getString(cursor.getColumnIndex("APPID")));
            acc.setAppName(cursor.getString(cursor.getColumnIndex("APPNAME")));
            acc.setAppUrl(cursor.getString(cursor.getColumnIndex("APPURL")));
           // acc.setImage(cursor.getString(cursor.getColumnIndex("IMAGE")));
            acc.setUserName(cursor.getString(cursor.getColumnIndex("USERNAME")));
            acc.setUserFirstPassword(cursor.getString(cursor.getColumnIndex("PASSWORD")));

            Log.w("SmartPass",acc.getAppId() + " " + acc.getAppName() + " " + acc.getUserName() + " " + acc.getUserFirstPassword());
            accounts.add(acc);
            cursor.moveToNext();
        }

        cursor.close();
        return accounts;
    }

    public List<AccountModel> getSinlgeEntry(String appName)
    {

        Cursor cursor=db.query("ACCOUNTS", null, "APPNAME=?", new String[]{appName}, null, null, null);
        if(cursor.getCount()<1) // UserName Not Exist
        {
            cursor.close();
            return null;
        }

        List<AccountModel> accounts = new ArrayList<>();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            AccountModel acc = new AccountModel();
            acc.setUserName(cursor.getString(cursor.getColumnIndex("USERNAME")));
            acc.setAppUrl(cursor.getString(cursor.getColumnIndex("APPURL")));
            acc.setNote(cursor.getString(cursor.getColumnIndex("NOTE")));
            acc.setAppName(cursor.getString(cursor.getColumnIndex("APPNAME")));
            acc.setUserFirstPassword(cursor.getString(cursor.getColumnIndex("PASSWORD")));
            accounts.add(acc);
            cursor.moveToNext();
        }

        cursor.close();
        return accounts;
    }
    public void  updateEntry(String userName,String password,String appId, String appName,String note, String appUrl)
    {
        // Define the updated row content.
        ContentValues updatedValues = new ContentValues();
        // Assign values for each row.
        updatedValues.put("USERNAME", userName);
        updatedValues.put("PASSWORD",password);
        updatedValues.put("APPNAME", appName);
        updatedValues.put("NOTE",note);
        updatedValues.put("APPURL",appUrl);

        String where="APPID = ?";
        db.update("ACCOUNTS",updatedValues, where, new String[]{appId});
    }
}
