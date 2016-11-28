package security.smartpass;

import android.provider.ContactsContract;

/**
 * Created by Chuong on 11/1/2016.
 */

public class AccountModel {
    private  String AppName="";
    private  String Image="";
    private  String UserName="";
    private  String AppUrl ="";
    private  String AppId = "";
    private  String AppCode ="";
    private  String userFirstPassword ="";
    private String Note ="";

    /*********** Set Methods ******************/

    public void setAppUrl(String appUrl) {
        this.AppUrl = appUrl;
    }
    public void setAppName(String AppName)
    {
        this.AppName = AppName;
    }

    public void setImage(String Image)
    {
        this.Image = Image;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }
    public void setAppId(String appId) {
        AppId = appId;
    }
    /*********** Get Methods ****************/

    public String getAppName()
    {
        return this.AppName;
    }

    public String getImage()
    {
        return this.Image;
    }

    public String getUserName() {
        return UserName;
    }

    public String getAppUrl() {
        return AppUrl;
    }
    public String getAppId() {
        return AppId;
    }


    public String getUserFirstPassword() {
        return userFirstPassword;
    }

    public void setUserFirstPassword(String userFirstPassword) {
        this.userFirstPassword = userFirstPassword;
    }

    public String getNote() {
        return Note;
    }

    public void setNote(String note) {
        Note = note;
    }

    public String getAppCode() {
        return AppCode;
    }

    public void setAppCode(String appCode) {
        AppCode = appCode;
    }
}
