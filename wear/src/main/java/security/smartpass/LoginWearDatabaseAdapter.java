package security.smartpass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chuong on 11/6/2016.
 */

public class LoginWearDatabaseAdapter {
    static final String DATABASE_NAME = "accounts.db";
    static final int DATABASE_VERSION = 1;
    public static final int NAME_COLUMN = 1;
    // TODO: Create public field for each column in your table.
    // SQL Statement to create a new database.
    static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "+"ACCOUNTS"+
            "( " +"Id"+" integer primary key autoincrement,"+ "APPID integer," +
            "APPNAME  text,PASSWORD text); ";

    public SQLiteDatabase db;
    private final Context context;
    private DatabaseHelper dbHelper;

    public LoginWearDatabaseAdapter(Context _context)
    {
        context = _context;
        dbHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public LoginWearDatabaseAdapter open() throws SQLException
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

    public void insertEntry(String appId,String appName, String password)
    {
        ContentValues newValues = new ContentValues();
        // Assign values for each row.
        newValues.put("APPID", appId);
        newValues.put("PASSWORD",password);
        newValues.put("APPNAME", appName);


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

    public List<AccountWearModel> getSinlgeEntry(String appId)
    {

        Cursor cursor=db.query("ACCOUNTS", null, "APPID=?", new String[]{appId}, null, null, null);
        if(cursor.getCount()<1) // UserName Not Exist
        {
            cursor.close();
            return null;
        }

        List<AccountWearModel> accounts = new ArrayList<>();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            AccountWearModel acc = new AccountWearModel();
            acc.setAppName(cursor.getString(cursor.getColumnIndex("APPNAME")));
            acc.setUserSecondPassword(cursor.getString(cursor.getColumnIndex("PASSWORD")));
            accounts.add(acc);
            cursor.moveToNext();
        }

        cursor.close();
        return accounts;
    }
    public void  updateEntry(String appId,String appName,  String password)
    {
        // Define the updated row content.
        ContentValues updatedValues = new ContentValues();
        // Assign values for each row.
        updatedValues.put("PASSWORD",password);
        updatedValues.put("APPNAME", appName);


        String where="APPID = ?";
        db.update("ACCOUNTS",updatedValues, where, new String[]{appId});
    }
}
