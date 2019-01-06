package com.netcracker.cookies;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;

public class CookieServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {


        String login = req.getParameter("login");
        String password = req.getParameter("password");

        signIn(login, password, req, resp);
    }

    private void signIn(String login, String password, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isPasswordRemember(req)) {
            if (isInCookies(login, req)) toWelclomePage(login,resp);
            else if (checkValid(login, password, req, resp)) {
                resp.addCookie(new Cookie(login, password));
                toWelclomePage(login,resp);
            }
        } else {
            if (checkValid(login, password, req, resp)) toWelclomePage(login,resp);
        }
    }

    private void toWelclomePage(String login, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print("<html><body>");
        out.print("<h1>Welcome, " + login + " </h1></br>");
        out.print("</body></html>");
        out.close();
    }

    private boolean checkValid(String login, String password, HttpServletRequest req, HttpServletResponse resp) {
        boolean hasLogin = false;
        try (BufferedReader br = new BufferedReader(new FileReader(getServletContext().getRealPath("/users.csv")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] user = line.split(",");
                if (login.equals(user[0])) {
                    hasLogin = true;
                    if (password.equals(user[1])) {
                        return true;
                    } else {
                        checkErrorsInCookies(login, req, resp);
                    }
                    break;
                }
            }
            if (!hasLogin) {
                addUser(resp);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void checkErrorsInCookies(String login, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer errors = 3;
        if (isInCookies(login + "Fails", req)) {
            Cookie[] cookies = req.getCookies();
            for (Cookie ck : cookies) {
                if (ck.getName().equals(login + "Fails")) {
                    errors = Integer.parseInt(ck.getValue());
                    errors--;
                    ck.setValue(errors.toString());
                    resp.addCookie(ck);
                }
            }
        } else {
            resp.addCookie(new Cookie(login + "Fails", errors.toString()));
        }

        if (errors == 0) {
            deleteUser(login);
            renameFile();
            resp.sendRedirect("/createNewUser.html");
        } else {
            resp.sendError(404, "Password not correct. " + errors + " attempts remaining");
        }
    }

    private void addUser(HttpServletResponse resp) throws IOException {
        resp.sendRedirect("/createNewUser.html");
    }

    private boolean isInCookies(String item, HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        for (Cookie ck : cookies) {
            if (ck.getName().equals(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPasswordRemember(HttpServletRequest req) {
        String flag = req.getParameter("savePassword");
        return flag != null;
    }

    private void deleteUser(String login) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(getServletContext().getRealPath("/users.csv")));
             BufferedWriter bw = new BufferedWriter(new FileWriter(getServletContext().getRealPath("/~users.csv"), false))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] user = line.split(",");
                if (!login.equals(user[0])) {
                    bw.write(line + "\n");
                }
            }
        }
    }

    private void renameFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(getServletContext().getRealPath("/~users.csv")));
             BufferedWriter writer = new BufferedWriter(new FileWriter(getServletContext().getRealPath("/users.csv"), false))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }
        }
    }
}

