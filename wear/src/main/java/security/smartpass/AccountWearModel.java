package security.smartpass;

/**
 * Created by Chuong on 11/1/2016.
 */

public class AccountWearModel {
    private  String AppName="";
    private  Integer AppId;
    private  String userSecondPassword ="";

    /*********** Set Methods ******************/

    public void setAppName(String AppName)
    {
        this.AppName = AppName;
    }

    public void setAppId(int appId) {
        AppId = appId;
    }
    public void setUserSecondPassword(String userSecondPassword) {
        this.userSecondPassword = userSecondPassword;
    }
    /*********** Get Methods ****************/

    public String getAppName()
    {
        return this.AppName;
    }

    public Integer getAppId() {
        return AppId;
    }


    public String getUserSecondPassword() {
        return userSecondPassword;
    }



}
