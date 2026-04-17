package org.example.isdcmproject.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.isdcmproject.model.video;
import org.example.isdcmproject.model.videoRepository;
import org.example.isdcmproject.model.videoValidator;

@WebServlet(name = "servletRegistroVid", urlPatterns = "/registroVid")
public class servletRegistroVid extends HttpServlet {
    private final videoRepository repository = new videoRepository();
    private final videoValidator videoValidator = new videoValidator();

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
        request.getRequestDispatcher("/WEB-INF/views/registroVid.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Map<String, String> formData = extractFormData(request);
        request.setAttribute("videoForm", formData);

        Map<String, String> fieldErrors = videoValidator.validate(formData);
        if (!fieldErrors.isEmpty()) {
            request.setAttribute("fieldErrors", fieldErrors);
            request.setAttribute("error", "Revisa los campos marcados e inténtalo nuevamente.");
            request.getRequestDispatcher("/WEB-INF/views/registroVid.jsp").forward(request, response);
            return;
        }

        video video = videoValidator.toVideo(formData);
        try {
            videoRepository.SaveVideoResult result = repository.save(video);
            if (result == videoRepository.SaveVideoResult.DUPLICATE_ID) {
                fieldErrors.put("identificador", "El identificador ya existe. Debe ser único.");
                request.setAttribute("fieldErrors", fieldErrors);
                request.setAttribute("error", "No fue posible registrar el video.");
                request.getRequestDispatcher("/WEB-INF/views/registroVid.jsp").forward(request, response);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/listadoVid?created=1");
        } catch (SQLException e) {
            request.setAttribute("error", "No fue posible registrar el video. Inténtalo nuevamente.");
            request.getRequestDispatcher("/WEB-INF/views/registroVid.jsp").forward(request, response);
        }
    }

    private Map<String, String> extractFormData(HttpServletRequest request) {
        Map<String, String> formData = new LinkedHashMap<>();
        formData.put("identificador", sanitize(request.getParameter("identificador")));
        formData.put("titulo", sanitize(request.getParameter("titulo")));
        formData.put("fechaCreacion", sanitize(request.getParameter("fechaCreacion")));
        formData.put("duracion", sanitize(request.getParameter("duracion")));
        formData.put("reproducciones", sanitize(request.getParameter("reproducciones")));
        formData.put("descripcion", sanitize(request.getParameter("descripcion")));
        formData.put("formato", sanitize(request.getParameter("formato")));
        formData.put("url", sanitize(request.getParameter("url")));
        formData.put("categoria", sanitize(request.getParameter("categoria")));
        return formData;
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
