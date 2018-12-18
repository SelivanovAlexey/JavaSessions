package com.netcracker.sessions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;


public class LoginServlet extends HttpServlet {

    private HttpServletRequest req;
    private HttpServletResponse resp;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.req=req;
        this.resp=resp;

        String login = req.getParameter("login");
        String password = req.getParameter("password");

        signIn(login,password);
    }

    public void signIn(String login, String password) throws IOException {
        checkActiveSession(login, password);
    }

    public void toWelclomePage(String login) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print("<html><body>");
        out.print("<h1>Welcome, " + login + " </h1></br>");
        out.print("</body></html>");
        out.close();
    }

    public boolean checkValid(String login, String password) throws IOException{

        boolean hasLogin = false;

        BufferedReader br = new BufferedReader(new FileReader(getServletContext().getRealPath("/users.csv")));
        String line;

        while ((line = br.readLine()) != null) {
            String[] user = line.split(",");
            if (login.equals(user[0])) {
                hasLogin = true;
                if (password.equals(user[1])) {
                    return true;
                }
                else{
                    resp.sendError(404,"Password not correct");
                }
                break;
            }
        }
        if(!hasLogin){
            addUser();
        }
        return false;
    }

    public void addUser() throws IOException{
        resp.sendRedirect("/createNewUser.html");
    }

    public void checkActiveSession(String login, String password) throws IOException{
        HttpSession session = req.getSession(true);


        if (!session.isNew() && password==""){
            toWelclomePage((String) session.getAttribute("login"));
        }
        else if (checkValid(login,password)){
                session.setAttribute("login",login);
                session.setAttribute("password",password);
                toWelclomePage(login);
        }
    }
}


