package security.smartpass;

/**
 * Created by Chuong on 11/21/2016.
 */

public class App {

    private String name = "";
    private String code = "";

    public App(String name,String code) {
        this.name = name;
        this.code =code;
    }

    public String getCode() {
        return code;
    }

    public String toString() {
        return name;
    }
}
