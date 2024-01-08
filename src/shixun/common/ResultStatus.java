package shixun.common;


public enum ResultStatus {
    SUCCESS(200, "success"), //成功
    USER(301,"user"),
    ROOT(302,"root"),
    LOGIN_INVAILE_USER(401, "无效用户"),
    LOGIN_INVAILE_PWD(402,"密码错误"),
    UPDATE_PASS(403, "输入的原密码不符,请重试!"),
    USER_EXIST(4001,"用户已存在"),
    BORROW_STATUS_BOWWOWED(501,"尚未归还该图书"),
    BORROW_STATUS_RETURN(502,"尚未借阅该图书"),

    ERROR(4000, "Error"),       //程序报错

    INVALID_SERVICE(199, "invalid service"),//服务不可用

    SERVICE_EXCEPTION(5000, "service exception"),

    UNKNOWN(999, "unknown");//未知错误




    private final int value;

    private final String reasonPhrase;

    private ResultStatus(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }


    public int value() {
        return this.value;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}

