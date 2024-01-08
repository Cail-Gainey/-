package shixun.pojo;

public class Book {
    private int book_id;
    private String book_name;
    private String book_author;
    private Double book_price;
    private String book_publisher;
    private int book_state;
    private String add_time;

    public Book() {
    }

    public Book(int book_id, String book_name, String book_author, Double book_price, String book_publisher, int book_state, String add_time) {
        this.book_id = book_id;
        this.book_name = book_name;
        this.book_author = book_author;
        this.book_price = book_price;
        this.book_publisher = book_publisher;
        this.book_state = book_state;
        this.add_time = add_time;
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

    public Double getBook_price() {
        return book_price;
    }

    public void setBook_price(Double book_price) {
        this.book_price = book_price;
    }

    public String getBook_publisher() {
        return book_publisher;
    }

    public void setBook_publisher(String book_publisher) {
        this.book_publisher = book_publisher;
    }

    public int getBook_state() {
        return book_state;
    }

    public void setBook_state(int book_state) {
        this.book_state = book_state;
    }

    public String getAdd_time() {
        return add_time;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }
}
