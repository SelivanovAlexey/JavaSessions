package com.netcracker.cookies;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;

public class CookieServlet extends HttpServlet {

    private HttpServletRequest req;
    private HttpServletResponse resp;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.req = req;
        this.resp = resp;

        String login = req.getParameter("login");
        String password = req.getParameter("password");

        signIn(login, password);
    }

    public void signIn(String login, String password) throws IOException {
        if (isPasswordRemember()) {
            if (isInCookies(login)) toWelclomePage(login);
            else if (checkValid(login, password)) {
                resp.addCookie(new Cookie(login, password));
                toWelclomePage(login);
            }
        } else {
            if (checkValid(login, password)) toWelclomePage(login);
        }
    }

    public void toWelclomePage(String login) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print("<html><body>");
        out.print("<h1>Welcome, " + login + " </h1></br>");
        out.print("</body></html>");
        out.close();
    }

    public boolean checkValid(String login, String password) {
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
                        checkErrorsInCookies(login);
                    }
                    break;
                }
            }
            if (!hasLogin) {
                addUser();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void checkErrorsInCookies(String login) throws IOException {
        Integer errors = 3;
        if (isInCookies(login + "Fails")) {
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

        if (errors.intValue() == 0) {
            deleteUser(login);
            renameFile();
            resp.sendRedirect("/createNewUser.html");
        } else {
            resp.sendError(404, "Password not correct. " + errors + " attemtps remaining");
        }
    }

    public void addUser() throws IOException {
        resp.sendRedirect("/createNewUser.html");
    }

    public boolean isInCookies(String item) {
        Cookie[] cookies = req.getCookies();
        for (Cookie ck : cookies) {
            if (ck.getName().equals(item)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPasswordRemember() {
        String flag = req.getParameter("savePassword");
        if (flag == null) return false;
        else return true;
    }

    public void deleteUser(String login) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(getServletContext().getRealPath("/users.csv")));
             BufferedWriter bw = new BufferedWriter(new FileWriter(getServletContext().getRealPath("/~users.csv"), false))){
            String line;

            while ((line = br.readLine()) != null) {
                String[] user = line.split(",");
                if (!login.equals(user[0])) {
                    bw.write(line + "\n");
                }
            }
        }
    }

    public void renameFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(getServletContext().getRealPath("/~users.csv")));
             BufferedWriter writer = new BufferedWriter(new FileWriter(getServletContext().getRealPath("/users.csv"), false))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line+"\n");
            }
        }
    }
}

