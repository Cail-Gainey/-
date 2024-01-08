package shixun.servlet;

import com.google.gson.Gson;
import shixun.dao.BorrowedDao;
import shixun.pojo.Borrowed;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/user_borrowAll")
public class user_borrowAll extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Integer userID = Integer.valueOf(req.getParameter("userID"));

        BorrowedDao dao = new BorrowedDao();
        ArrayList<Borrowed> list = dao.user_borrowAll(userID);

        String data = new Gson().toJson(list);

        resp.setContentType("application/json");
        resp.getWriter().write(data);
    }
}