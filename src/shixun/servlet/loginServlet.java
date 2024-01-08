package shixun.servlet;

import shixun.common.Result;
import shixun.dao.UserDao;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/login")
public class loginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Integer userID = Integer.valueOf(req.getParameter("userID"));
        String password = req.getParameter("password");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String login_time = sdf.format(now);

        Result result = UserDao.login(userID, password, login_time);

        PrintWriter writer = resp.getWriter();
        writer.write(result.toJson());
    }
}