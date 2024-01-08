package shixun.servlet;

import com.google.gson.Gson;
import shixun.dao.BorrowedDao;
import shixun.pojo.Borrowed;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/borrowAll")
public class borrowAll extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        BorrowedDao dao = new BorrowedDao();
        ArrayList<Borrowed> list = dao.borrowALl();

        String data = new Gson().toJson(list);

        resp.setContentType("application/json");
        resp.getWriter().write(data);
    }
}