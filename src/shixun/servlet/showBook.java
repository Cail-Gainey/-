package shixun.servlet;

import com.google.gson.Gson;
import shixun.common.Result;
import shixun.common.ResultStatus;
import shixun.dao.BookDao;
import shixun.pojo.Book;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/showBook")
public class showBook extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Result result = new Result<>();
        BookDao dao = new BookDao();
        ArrayList<Book> books = dao.selectAll();

        String data = new Gson().toJson(books);

        result.setResult(ResultStatus.SUCCESS);
        resp.setContentType("application/json");
        resp.getWriter().write(data);
    }
}