package org.example.isdcmproject.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.isdcmproject.model.video;
import org.example.isdcmproject.model.videoRepository;

@WebServlet(name = "servletListadoVid", urlPatterns = "/listadoVid")
public class servletListadoVid extends HttpServlet {
    private final videoRepository repository = new videoRepository();

    @Override
    public void init() throws ServletException {
        try {
            repository.initializeTable();
        } catch (SQLException e) {
            throw new ServletException("No se pudo inicializar la tabla de videos.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<video> videos = repository.findAll();
            request.setAttribute("videos", videos);
            if ("1".equals(request.getParameter("created"))) {
                request.setAttribute("success", "Video registrado correctamente.");
            }
            request.getRequestDispatcher("/WEB-INF/views/listadoVid.jsp").forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("error", "No fue posible cargar el listado de videos.");
            request.getRequestDispatcher("/WEB-INF/views/listadoVid.jsp").forward(request, response);
        }
    }
}
