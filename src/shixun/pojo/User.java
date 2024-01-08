package shixun.pojo;

public class User {
    private Integer userID;
    private String password;
    private int type;
    String login_time;
    public User() {
    }

    public User(Integer userID, String password, int type, String login_time) {
        this.userID = userID;
        this.password = password;
        this.type = type;
        this.login_time = login_time;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLogin_time() {
        return login_time;
    }

    public void setLogin_time(String login_time) {
        this.login_time = login_time;
    }
}
