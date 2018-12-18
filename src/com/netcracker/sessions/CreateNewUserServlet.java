package com.netcracker.sessions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CreateNewUserServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reqLogin = req.getParameter("login");
        String reqPassword = req.getParameter("password");

        BufferedWriter bw = new BufferedWriter(new FileWriter(getServletContext().getRealPath("/users.csv"),true));
        bw.write(reqLogin + "," + reqPassword + "\n");
        bw.close();
        resp.sendRedirect("/login.html");
    }
}
