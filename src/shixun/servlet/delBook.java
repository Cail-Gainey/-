package shixun.servlet;

import com.google.gson.Gson;
import shixun.common.Result;
import shixun.dao.BookDao;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/delBook")
public class delBook extends HttpServlet {
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        int book_id = Integer.parseInt(req.getParameter("book_id"));

        Result result = new BookDao().delete(book_id);

        PrintWriter writer = resp.getWriter();
        writer.write(result.toJson());
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 从请求的 JSON 数据中获取要删除的图书 ID 数组
        BufferedReader reader = req.getReader();
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        Gson gson = new Gson();
        int[] bookIds = gson.fromJson(jsonBuilder.toString(), int[].class);

        // 调用 DAO 层的方法进行批量删除
        Result result = new BookDao().deleteBooks(bookIds);

        PrintWriter writer = resp.getWriter();
        writer.write(result.toJson());
    }
}