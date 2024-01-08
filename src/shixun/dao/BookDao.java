package shixun.dao;

import shixun.common.Result;

import shixun.common.ResultStatus;
import shixun.pojo.Book;
import shixun.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BookDao {
    static Connection coon = null;
    static PreparedStatement ps = null;
    static ResultSet rs = null;
    public ArrayList<Book> selectAll() {
        ArrayList<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM book";

        try {
            coon = JDBCUtils.getConnection();
            ps = coon.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("book_id");
                String name = rs.getString("book_name");
                String author = rs.getString("book_author");
                Double price = rs.getDouble("book_price");
                String publisher = rs.getString("book_publisher");
                int state = rs.getInt("book_state");
                String time = rs.getString("add_time");

                Book book = new Book(id,name,author,price,publisher,state,time);
                list.add(book);
            }JDBCUtils.releaseAll(coon,ps,rs);
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Result add(String name, String author, Double price, String publisher, String time) {
        Result result = new Result<>();
        String sql = "INSERT INTO book (book_name, book_author, book_price, book_publisher, add_time) VALUES (?, ?, ?, ?, ?)";

        try {
            coon = JDBCUtils.getConnection();
            ps = coon.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, author);
            ps.setDouble(3, price);
            ps.setString(4, publisher);
            ps.setString(5, time);

            int row = ps.executeUpdate();
            if (row > 0) {
                result.setResult(ResultStatus.SUCCESS);
            } else {
                result.setResult(ResultStatus.ERROR);
            }
            JDBCUtils.releaseAll(coon, ps, rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    public Result update(int book_id, String book_name, String book_author, Double book_price, Integer book_state, String book_publisher) {
        Result result = new Result<>();
        String sql = "UPDATE book SET book_name = ?, book_author = ?, book_price = ?, book_state = ?, book_publisher = ? WHERE book_id = ?";

        try {
            coon = JDBCUtils.getConnection();
            ps = coon.prepareStatement(sql);
            ps.setString(1, book_name);
            ps.setString(2, book_author);
            ps.setDouble(3, book_price);
            ps.setInt(4,book_state);
            ps.setString(5, book_publisher);
            ps.setInt(6, book_id);

            int row = ps.executeUpdate();
            if (row > 0) {
                result.setResult(ResultStatus.SUCCESS);
            } else {
                result.setResult(ResultStatus.ERROR);
            }
            JDBCUtils.releaseAll(coon, ps, rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Result delete(int book_id) {
        Result result = new Result<>();
        try {
            Connection connection = JDBCUtils.getConnection();
            String sql = "DELETE FROM book WHERE book_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, book_id);
            int row = ps.executeUpdate();

            if (row > 0) {
                result.setResult(ResultStatus.SUCCESS);
            } else {
                result.setResult(ResultStatus.ERROR);
            }

            JDBCUtils.releaseAll(connection, ps, null);
        } catch (SQLException e) {
            e.printStackTrace();
            result.setResult(ResultStatus.ERROR);
        }
        return result;
    }
    public Result deleteBooks(int[] bookIds) {
        Result result = new Result<>();
        try {
            Connection connection = JDBCUtils.getConnection();
            String sql = "DELETE FROM book WHERE book_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);

            for (int bookId : bookIds) {
                ps.setInt(1, bookId);
                ps.addBatch();
            }

            int[] rows = ps.executeBatch();

            if (rows.length == bookIds.length) {
                result.setResult(ResultStatus.SUCCESS);
            } else {
                result.setResult(ResultStatus.ERROR);
            }

            JDBCUtils.releaseAll(connection, ps, null);
        } catch (SQLException e) {
            e.printStackTrace();
            result.setResult(ResultStatus.ERROR);
        }
        return result;
    }
}
