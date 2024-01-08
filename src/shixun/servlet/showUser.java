package shixun.servlet;

import com.google.gson.Gson;
import shixun.common.Result;
import shixun.common.ResultStatus;
import shixun.dao.UserDao;
import shixun.pojo.User;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/showUser")
public class showUser extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Result result = new Result<>();
        UserDao dao = new UserDao();
        ArrayList<User> users = dao.selectAll();

        String data = new Gson().toJson(users);

        result.setResult(ResultStatus.SUCCESS);
        resp.setContentType("application/json");
        resp.getWriter().write(data);
    }
}