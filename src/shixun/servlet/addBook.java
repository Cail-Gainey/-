package shixun.servlet;

import com.google.gson.Gson;
import shixun.common.Result;
import shixun.dao.BookDao;
import shixun.pojo.Book;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/addBook")
public class addBook extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String json = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);

        Gson gson = new Gson();
        Book book = gson.fromJson(json,Book.class);

        String book_name = book.getBook_name();
        String book_author = book.getBook_author();
        Double book_price = book.getBook_price();
        String book_publisher = book.getBook_publisher();
        String add_time = book.getAdd_time();
        Result result = new BookDao().add(book_name,book_author,book_price,book_publisher,add_time);
        PrintWriter writer = resp.getWriter();
        writer.write(result.toJson());
    }
}