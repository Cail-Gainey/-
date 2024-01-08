package shixun.dao;

import shixun.common.Result;
import shixun.common.ResultStatus;
import shixun.pojo.Borrowed;
import shixun.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BorrowedDao {
    static Connection coon = null;
    static PreparedStatement ps = null;
    static ResultSet rs = null;
       public ArrayList<Borrowed> notReturn(Integer userID) {
        ArrayList<Borrowed> list = new ArrayList<>();
        String sql = "SELECT * FROM borrowed WHERE status = 0 AND userID = ?";
        try {
            Connection conn = JDBCUtils.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int book_id = rs.getInt("book_id");
                String book_name = rs.getString("book_name");
                String book_author = rs.getString("book_author");
                String book_publisher = rs.getString("book_publisher");
                String borrow_time = rs.getString("borrow_time");

                Borrowed borrowed = new Borrowed(id, book_id, book_name, book_author, book_publisher, borrow_time);
                list.add(borrowed);
            }
            JDBCUtils.releaseAll(conn, ps, rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Result addBorrow(Integer  userID, int book_id, String book_name, String book_author, String book_publisher, int status, String borrow_time) {
        Result result = new Result<>();
        String querySql = "SELECT * FROM borrowed WHERE userID = ? AND book_id = ? AND status = 0";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(querySql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, book_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // 数据库中存在与传入的userID和book_id相同且status为0的记录
                result.setResult(ResultStatus.BORROW_STATUS_BOWWOWED);
            } else {
                // 数据库中不存在与传入的userID和book_id相同且status为0的记录，进行插入操作
                String insertSql = "INSERT INTO borrowed(userID, book_id, book_name, book_author, book_publisher, status,borrow_time) VALUES (?, ?, ?, ?, ?, ?,?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, userID);
                    insertStmt.setInt(2, book_id);
                    insertStmt.setString(3, book_name);
                    insertStmt.setString(4, book_author);
                    insertStmt.setString(5, book_publisher);
                    insertStmt.setInt(6, status);
                    insertStmt.setString(7,borrow_time);
                    int row = insertStmt.executeUpdate();
                    if (row > 0) {
                        result.setResult(ResultStatus.SUCCESS);
                    } else {
                        result.setResult(ResultStatus.ERROR);
                    }
                }
            }
            JDBCUtils.releaseAll(conn,ps,rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
    public Result addReturn(Integer userID, int book_id, String book_name, String book_author, String book_publisher, int status, String return_time) {
        Result result = new Result<>();

        // 判断borrowed表中是否存在与传入的userID和book_id相同且status为0的记录
        String querySql = "SELECT * FROM borrowed WHERE userID = ? AND book_id = ? AND status = 0";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(querySql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, book_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // 存在记录，将status设置为1，oldStatus设置为0
                String updateSql = "UPDATE borrowed SET status = 1, oldStatus = 0, repaid_time = ? WHERE userID = ? AND book_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1,return_time);
                    updateStmt.setInt(2, userID);
                    updateStmt.setInt(3, book_id);

                    updateStmt.executeUpdate();
                }
                result.setResult(ResultStatus.SUCCESS);
            } else {
                result.setResult(ResultStatus.BORROW_STATUS_RETURN);
            }
            JDBCUtils.releaseAll(conn, stmt, rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    public ArrayList<Borrowed> user_borrowAll(Integer userID) {
        ArrayList<Borrowed> list = new ArrayList<>();
        String sql = "select * from borrowed where userID = ?";
        try {
            coon = JDBCUtils.getConnection();
            ps = coon.prepareStatement(sql);
            ps.setInt(1,userID);
            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int book_id = rs.getInt("book_id");
                String book_name = rs.getString("book_name");
                String book_author = rs.getString("book_author");
                String book_publisher = rs.getString("book_publisher");
                String borrow_time = rs.getString("borrow_time");

                Borrowed borrowed = new Borrowed(id, book_id, book_name, book_author, book_publisher, borrow_time);
                list.add(borrowed);
            }
            JDBCUtils.releaseAll(coon,ps,rs);
        } catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }
    public ArrayList<Borrowed> user_repaidAll(Integer userID) {
        ArrayList<Borrowed> list = new ArrayList<>();
        String sql = "select id, book_id, book_name, book_author, book_publisher, repaid_time from borrowed where (status = 1 and userID = ?)";

        try {
            coon = JDBCUtils.getConnection();
            ps = coon.prepareStatement(sql);
            ps.setInt(1,userID);
            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int book_id = rs.getInt("book_id");
                String book_name = rs.getString("book_name");
                String book_author = rs.getString("book_author");
                String book_publisher = rs.getString("book_publisher");
                String repaid_time= rs.getString("repaid_time");

                Borrowed borrowed = new Borrowed();
                borrowed.setId(id);
                borrowed.setBook_id(book_id);
                borrowed.setBook_name(book_name);
                borrowed.setBook_author(book_author);
                borrowed.setBook_publisher(book_publisher);
                borrowed.setRepaid_time(repaid_time);
                list.add(borrowed);
            }

        } catch (SQLException e){
            e.printStackTrace();
        }finally {
            try {
                JDBCUtils.releaseAll(coon,ps,rs);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
    public ArrayList<Borrowed> borrowALl() {
        ArrayList<Borrowed> list = new ArrayList<>();
        String sql = "select * from borrowed";

        try {
            coon = JDBCUtils.getConnection();
            ps = coon.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int userID = rs.getInt("userID");
                int book_id = rs.getInt("book_id");
                String book_name = rs.getString("book_name");
                String book_author = rs.getString("book_author");
                String book_publisher = rs.getString("book_publisher");
                String borrow_time = rs.getString("borrow_time");

                Borrowed borrowed = new Borrowed();
                borrowed.setBook_id(id);
                borrowed.setUserID(userID);
                borrowed.setBook_id(book_id);
                borrowed.setBook_name(book_name);
                borrowed.setBook_author(book_author);
                borrowed.setBook_publisher(book_publisher);
                borrowed.setBorrow_time(borrow_time);
                list.add(borrowed);
            }JDBCUtils.releaseAll(coon,ps,rs);
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public ArrayList<Borrowed> repaidAll() {
        ArrayList<Borrowed> list = new ArrayList<>();
        String sql = "select * from borrowed where (status = 1 and oldStatus = 0)";

        try {
            coon = JDBCUtils.getConnection();
            ps = coon.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int userID = rs.getInt("userID");
                int book_id = rs.getInt("book_id");
                String book_name = rs.getString("book_name");
                String book_author = rs.getString("book_author");
                String book_publisher = rs.getString("book_publisher");
                String repaid_time = rs.getString("repaid_time");

                Borrowed borrowed = new Borrowed();
                borrowed.setBook_id(id);
                borrowed.setUserID(userID);
                borrowed.setBook_id(book_id);
                borrowed.setBook_name(book_name);
                borrowed.setBook_author(book_author);
                borrowed.setBook_publisher(book_publisher);
                borrowed.setRepaid_time(repaid_time);
                list.add(borrowed);
            }JDBCUtils.releaseAll(coon,ps,rs);
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


}
