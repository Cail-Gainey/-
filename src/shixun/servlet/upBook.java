package shixun.servlet;

import shixun.common.Result;
import shixun.dao.BookDao;
import com.google.gson.Gson;
import shixun.pojo.Book;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/upBook")
public class upBook extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 从请求体中获取JSON字符串
        String json = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);

        Gson gson = new Gson();
        Book bookForm = gson.fromJson(json, Book.class);

        int book_id = bookForm.getBook_id();
        String book_name = bookForm.getBook_name();
        String book_author = bookForm.getBook_author();
        Double book_price = bookForm.getBook_price();
        Integer book_state = bookForm.getBook_state();
        String book_publisher = bookForm.getBook_publisher();

        Result result = new BookDao().update(book_id, book_name, book_author, book_price, book_state, book_publisher);

        PrintWriter writer = resp.getWriter();
        writer.write(result.toJson());
    }
}