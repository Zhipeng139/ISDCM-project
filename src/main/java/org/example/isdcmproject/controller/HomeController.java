package org.example.isdcmproject.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.isdcmproject.model.Greeting;
import org.example.isdcmproject.service.GreetingService;

@WebServlet(name = "homeController", value = "/home")
public class HomeController extends HttpServlet {
    private final GreetingService greetingService = new GreetingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Greeting greeting = greetingService.getGreeting();
        request.setAttribute("greeting", greeting);
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
