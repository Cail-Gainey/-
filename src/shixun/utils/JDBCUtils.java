package shixun.utils;

import java.sql.*;

public class JDBCUtils {
        private static final String url = "jdbc:mysql://localhost:3306/shixun_2?serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String username = "root";
    private static final String password = "123456";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return DriverManager.getConnection(url, username, password);
    }

    public static void releaseAll(Connection conn, PreparedStatement ps, ResultSet rs) throws SQLException {

        if (rs != null) {
            rs.close();
        }
        if (ps != null) {
            ps.close();
        }
        if (conn != null) {
            conn.close();
        }
    }
}
