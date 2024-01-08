package shixun.servlet;

import shixun.common.Result;
import shixun.dao.UserDao;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/upPwd")
public class upPwd extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String nPwd = req.getParameter("nPwd");
        Integer userID = Integer.valueOf(req.getParameter("userID"));
        String oPwd = req.getParameter("oPwd");

        Result result = new UserDao().upPwd(userID,oPwd,nPwd);
        PrintWriter writer = resp.getWriter();
        writer.write(result.toJson());
    }
}