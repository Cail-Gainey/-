package shixun.pojo;

public class Borrowed {
    private int id;
    private int userID;
    private int book_id;
    private String book_name;
    private String book_author;
    private String book_publisher;
    private int status;
    private int oldStatus;
    private String borrow_time;
    private String repaid_time;
    public Borrowed() {
    }

    public Borrowed(int id, int userID, int book_id, String book_name, String book_author, String book_publisher, int status) {
        this.id = id;
        this.userID = userID;
        this.book_id = book_id;
        this.book_name = book_name;
        this.book_author = book_author;
        this.book_publisher = book_publisher;
        this.status = status;
    }

    public Borrowed(int id, int book_id, String book_name, String book_author, String book_publisher, String borrow_time) {
        this.id = id;
        this.book_id = book_id;
        this.book_name = book_name;
        this.book_author = book_author;
        this.book_publisher = book_publisher;
        this.borrow_time = borrow_time;
    }




    @Override
    public String toString() {
        return "Borrowed{" +
                "id=" + id +
                ", userID=" + userID +
                ", book_id=" + book_id +
                ", book_name='" + book_name + '\'' +
                ", book_author='" + book_author + '\'' +
                ", book_publisher='" + book_publisher + '\'' +
                ", status=" + status +
                ", oldStatus=" + oldStatus +
                ", borrow_time='" + borrow_time + '\'' +
                ", repaid_time='" + repaid_time + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getBook_id() {
        return book_id;
    }

    public void setBook_id(int book_id) {
        this.book_id = book_id;
    }

    public String getBook_name() {
        return book_name;
    }

    public void setBook_name(String book_name) {
        this.book_name = book_name;
    }

    public String getBook_author() {
        return book_author;
    }

    public void setBook_author(String book_author) {
        this.book_author = book_author;
    }

    public String getBook_publisher() {
        return book_publisher;
    }

    public void setBook_publisher(String book_publisher) {
        this.book_publisher = book_publisher;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(int oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getBorrow_time() {
        return borrow_time;
    }

    public void setBorrow_time(String borrow_time) {
        this.borrow_time = borrow_time;
    }

    public String getRepaid_time() {
        return repaid_time;
    }

    public void setRepaid_time(String repaid_time) {
        this.repaid_time = repaid_time;
    }
}
