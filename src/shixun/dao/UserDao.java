package shixun.dao;

import shixun.common.Result;
import shixun.common.ResultStatus;
import shixun.pojo.User;
import shixun.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class UserDao {
    static Connection coon = null;
    static PreparedStatement ps = null;
    static ResultSet rs = null;
   public static Result login(Integer userID, String password, String login_time) {
    Result result = new Result();
    try (Connection conn = JDBCUtils.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE userID = ?")) {

        ps.setInt(1, userID);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String pwd = rs.getString("password");
                int type = rs.getInt("type");
                if (password.equals(pwd)) {
                    if (type == 0) {
                        result.setResult(ResultStatus.ROOT);
                        String last_time = rs.getString("login_time");
                        String updateSql = "UPDATE user SET last_time = ?,login_time = ? WHERE userID = ?";
                        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                            updatePs.setString(1, last_time);
                            updatePs.setString(2,login_time);
                            updatePs.setInt(3, userID);
                            updatePs.executeUpdate();
                        }
                        // 将数据作为 data 字段返回
                        result.setData(new HashMap<String, Object>() {{
                            put("userID", userID);
                            put("last_time", last_time);
                        }});
                        return result;
                    } else if (type == 1) {
                        result.setResult(ResultStatus.USER);

                        String last_time = rs.getString("login_time");
                        String updateSql = "UPDATE user SET last_time = ?,login_time = ? WHERE userID = ?";
                        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                            updatePs.setString(1, last_time);
                            updatePs.setString(2,login_time);
                            updatePs.setInt(3, userID);
                            updatePs.executeUpdate();
                        }
                        // 将数据作为 data 字段返回
                        result.setData(new HashMap<String, Object>() {{
                            put("userID", userID);
                            put("last_time", last_time);
                        }});
                        return result;
                    }
                } else {
                    result.setResult(ResultStatus.LOGIN_INVAILE_PWD);
                    return result;
                }
            } else {
                result.setResult(ResultStatus.LOGIN_INVAILE_USER);
                return result;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return result;
}

    public static Result sign(Integer userID, String password, Integer type) {
    Result result = new Result<>();
    String selectSql = "SELECT * FROM user WHERE userID = ?";
    String insertSql = "INSERT INTO user(userID,password,type) VALUES (?, ?,?)";
    try {
        coon = JDBCUtils.getConnection();
        ps = coon.prepareStatement(selectSql);
        ps.setInt(1, userID);
        rs = ps.executeQuery();
        if (rs.next()) {
            result.setResult(ResultStatus.USER_EXIST);
        } else {
            ps = coon.prepareStatement(insertSql);
            ps.setInt(1, userID);
            ps.setString(2, password);
            ps.setInt(3,type);
            int row = ps.executeUpdate();
            if (row > 0) {
                result.setResult(ResultStatus.SUCCESS);
            } else {
                result.setResult(ResultStatus.ERROR);
            }
        }
        JDBCUtils.releaseAll(coon, ps, rs);
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return result;
}
    public Result upPwd(Integer userID, String oPwd, String nPwd) {
        Result result = new Result<>();
        String sql = "update user set password = ? where userID = ?";
        String password = null;
        try {
            coon = JDBCUtils.getConnection();
            ps = coon.prepareStatement("select password from user where userID = ?");
            ps.setInt(1, userID);
            rs = ps.executeQuery();
            if (rs.next()) {
                password = rs.getString("password");
            }
            JDBCUtils.releaseAll(coon, ps, rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (password != null && password.equals(oPwd)) { // 判断原密码是否相同
            try {
                coon = JDBCUtils.getConnection();
                ps = coon.prepareStatement(sql);
                ps.setString(1, nPwd);
                ps.setInt(2, userID);

                int row = ps.executeUpdate();
                if (row > 0) {
                    result.setResult(ResultStatus.SUCCESS);
                } else {
                    result.setResult(ResultStatus.UPDATE_PASS);
                }
                JDBCUtils.releaseAll(coon, ps, rs);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            result.setResult(ResultStatus.UPDATE_PASS); // 原密码不相同
        }
        return result;
    }
    public ArrayList<User> selectAll() {
        ArrayList<User> list = new ArrayList<>();
        String sql = "select * from user";

        try {
            coon = JDBCUtils.getConnection();
            ps = coon.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Integer userID = rs.getInt("userId");
                String password = rs.getString("password");
                int type = rs.getInt("type");
                String last_time = rs.getString("last_time");
                User user = new User(userID,password,type,last_time);
                list.add(user);
            }
            JDBCUtils.releaseAll(coon,ps,rs);
        }catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }

    public Result add(Integer userID, String password, Integer type) {
    Result result = new Result<>();
    String sql = "insert into user (userID, password, type) values (?, ?, ?)";

    try {
        coon = JDBCUtils.getConnection();
        ps = coon.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, password);
        ps.setInt(3, type);

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

    public Result update(String password, int type ,Integer userID) {
        Result result = new Result<>();
        String sql = "update user set password = ?, type = ? where userID = ?";

        try {
            coon = JDBCUtils.getConnection();
            ps = coon.prepareStatement(sql);
            ps.setString(1,password);
            ps.setInt(2,type);
            ps.setInt(3,userID);

            int row = ps.executeUpdate();
            if (row > 0) {
                result.setResult(ResultStatus.SUCCESS);
            } else {
                result.setResult(ResultStatus.ERROR);
            }
            JDBCUtils.releaseAll(coon,ps,rs);
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Result delete(Integer userID) {
        Result result = new Result();
        String sql = "delete from user where userID = ?";
        try {
            coon = JDBCUtils.getConnection();
            ps = coon.prepareStatement(sql);
            ps.setInt(1,userID);

            int row = ps.executeUpdate();
            if (row > 0) {
                result.setResult(ResultStatus.SUCCESS);
            } else {
                result.setResult(ResultStatus.ERROR);
            }
            JDBCUtils.releaseAll(coon,ps,rs);
        }catch (SQLException e){
            e.printStackTrace();
        }
        return result;
    }
    public Result deleteUsers(int[] users) {
        Result result = new Result<>();
        try {
            Connection connection = JDBCUtils.getConnection();
            String sql = "DELETE FROM user WHERE userID= ?";
            PreparedStatement ps = connection.prepareStatement(sql);

            for (int userID : users) {
                ps.setInt(1, userID);
                ps.addBatch();
            }

            int[] rows = ps.executeBatch();

            if (rows.length == users.length) {
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
