package org.example.isdcmproject.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.isdcmproject.model.video;
import org.example.isdcmproject.model.videoRepository;
import org.example.isdcmproject.model.videoSearchCriteria;
import org.example.isdcmproject.service.VideoService;

@WebServlet(name = "servletListadoVid", urlPatterns = "/listadoVid")
public class servletListadoVid extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(servletListadoVid.class.getName());
    private final videoRepository repository = new videoRepository();
    private final VideoService videoService = new VideoService();

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
            videoSearchCriteria criteria = extractCriteria(request);
            request.setAttribute("searchCriteria", criteria);
            validateCriteria(criteria, request);
            List<video> videos = videoService.search(criteria);
            request.setAttribute("videos", videos);
            request.setAttribute("suggestions", videoService.suggest(criteria.getConsultaLibre()));
            if ("1".equals(request.getParameter("created"))) {
                String videoId = sanitize(request.getParameter("videoId"));
                if (videoId.isBlank()) {
                    request.setAttribute("success", "Video registrado correctamente.");
                } else {
                    request.setAttribute("success", "Video registrado correctamente con ID " + videoId + ".");
                }
            }
            request.getRequestDispatcher("/WEB-INF/views/listadoVid.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "No fue posible cargar el listado de videos.", e);
            request.setAttribute("error", "No fue posible cargar el listado de videos.");
            request.getRequestDispatcher("/WEB-INF/views/listadoVid.jsp").forward(request, response);
        }
    }

    private videoSearchCriteria extractCriteria(HttpServletRequest request) {
        videoSearchCriteria criteria = new videoSearchCriteria();
        criteria.setConsultaLibre(sanitize(request.getParameter("q")));
        criteria.setTitulo(sanitize(request.getParameter("titulo")));
        criteria.setCategoria(sanitize(request.getParameter("categoria")));
        criteria.setResolucion(sanitize(request.getParameter("resolucion")));
        criteria.setFechaDesde(parseDate(request.getParameter("fechaDesde")));
        criteria.setFechaHasta(parseDate(request.getParameter("fechaHasta")));
        criteria.setDuracionMin(parseInteger(request.getParameter("duracionMin")));
        criteria.setDuracionMax(parseInteger(request.getParameter("duracionMax")));
        return criteria;
    }

    private void validateCriteria(videoSearchCriteria criteria, HttpServletRequest request) {
        if (criteria.getDuracionMin() != null && criteria.getDuracionMax() != null
                && criteria.getDuracionMin() > criteria.getDuracionMax()) {
            request.setAttribute("error", "La duración mínima no puede ser mayor que la duración máxima.");
            criteria.setDuracionMax(criteria.getDuracionMin());
        }
        if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null
                && criteria.getFechaDesde().isAfter(criteria.getFechaHasta())) {
            request.setAttribute("error", "La fecha desde no puede ser mayor que la fecha hasta.");
            criteria.setFechaHasta(criteria.getFechaDesde());
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return Math.max(0, parsed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
