package org.example.isdcmproject.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "videoController", urlPatterns = "/videos")
public class VideoController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        @SuppressWarnings("unchecked")
        List<String> videos = (List<String>) session.getAttribute("videos");
        if (videos == null) {
            videos = new ArrayList<>();
            session.setAttribute("videos", videos);
        }
        request.setAttribute("videos", videos);
        request.getRequestDispatcher("/WEB-INF/views/videos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        String videoNombre = request.getParameter("videoNombre");

        if (videoNombre == null || videoNombre.trim().isEmpty()) {
            request.setAttribute("error", "El nombre del video es obligatorio.");
            doGet(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        @SuppressWarnings("unchecked")
        List<String> videos = (List<String>) session.getAttribute("videos");
        if (videos == null) {
            videos = new ArrayList<>();
            session.setAttribute("videos", videos);
        }
        videos.add(videoNombre.trim());
        response.sendRedirect(request.getContextPath() + "/videos");
    }
}
