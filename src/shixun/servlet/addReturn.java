



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

@WebServlet("/addReturn")
public class addReturn extends HttpServlet {
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
        String repaid_time = borrowed.getRepaid_time();

        Result result = new BorrowedDao().addReturn(userID, book_id, book_name, book_author, book_publisher, status, repaid_time);
        PrintWriter writer = resp.getWriter();
        writer.write(result.toJson());
    }
}

    
	