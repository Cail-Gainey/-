package shixun.servlet;

import shixun.common.Result;
import shixun.dao.UserDao;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/sign")
public class sign extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Integer userID = Integer.valueOf(req.getParameter("userID"));
        String password = req.getParameter("password");
        Integer type = Integer.valueOf(req.getParameter("type"));

        Result result = UserDao.sign(userID,password,type);
        PrintWriter writer = resp.getWriter();
        writer.write(result.toJson());
    }
}