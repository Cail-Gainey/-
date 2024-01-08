package shixun.servlet;

import com.google.gson.Gson;
import shixun.common.Result;
import shixun.dao.BorrowedDao;
import shixun.pojo.Borrowed;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/addBorrow")
public class addBorrow extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String json = req.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);

        Gson gson = new Gson();
        Borrowed borrowed = gson.fromJson(json, Borrowed.class);

        Integer userID = Integer.valueOf(borrowed.getUserID());
        int book_id = borrowed.getBook_id();
        String book_name = borrowed.getBook_name();
        String book_author = borrowed.getBook_author();
        String book_publisher = borrowed.getBook_publisher();
        int status = borrowed.getStatus();
        String borrow_time = borrowed.getBorrow_time();

        Result result = new BorrowedDao().addBorrow(userID, book_id, book_name, book_author, book_publisher, status, borrow_time);
        PrintWriter writer = resp.getWriter();
        writer.write(result.toJson());
    }
}