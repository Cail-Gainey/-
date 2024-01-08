package shixun.servlet;

import com.google.gson.Gson;
import shixun.common.Result;
import shixun.dao.UserDao;
import shixun.pojo.User;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/addUser")
public class addUser extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String json = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);

        Gson gson = new Gson();
        User user = gson.fromJson(json, User.class);

        Integer userID = user.getUserID();
        String password = user.getPassword();
        Integer type = user.getType();

        Result result = new UserDao().add(userID,password,type);
        PrintWriter writer = resp.getWriter();
        writer.write(result.toJson());
    }
}