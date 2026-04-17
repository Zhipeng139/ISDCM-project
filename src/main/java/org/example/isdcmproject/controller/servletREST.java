package org.example.isdcmproject.controller;

import jakarta.json.*;
import jakarta.servlet.ServletException;
import org.example.isdcmproject.config.RestConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/busqueda", "/reproduccion"})
public class servletREST extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(servletREST.class.getName());
    private static final String REST_BASE = RestConfig.BASE_URL + "/videos";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/busqueda"     -> handleBusqueda(req, resp);
            case "/reproduccion" -> handleMostrarVideo(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleReproducir(req, resp);
    }

    private void handleBusqueda(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String titulo = param(req, "titulo");
        String autor  = param(req, "autor");
        String year   = param(req, "year");
        String month  = param(req, "month");
        String day    = param(req, "day");

        if (!titulo.isBlank() || !autor.isBlank() || !year.isBlank()) {
            try {
                String json = httpGet(buildSearchUrl(titulo, autor, year, month, day), apiKey(req));
                req.setAttribute("videos", toList(Json.createReader(new StringReader(json)).readArray()));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error REST busqueda", e);
                req.setAttribute("error", "No s'ha pogut connectar amb el servei REST: " + e.getMessage());
            }
        }
        req.getRequestDispatcher("/WEB-INF/views/busqueda.jsp").forward(req, resp);
    }

    private void handleMostrarVideo(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = param(req, "id");
        if (id.isBlank()) { resp.sendRedirect(req.getContextPath() + "/busqueda"); return; }
        try {
            String json = httpGet(REST_BASE + "/" + encode(id), apiKey(req));
            req.setAttribute("video", toMap(Json.createReader(new StringReader(json)).readObject()));
            if ("1".equals(req.getParameter("reproduit")))
                req.setAttribute("success", "Reproducció registrada correctament.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error REST reproduccion", e);
            req.setAttribute("error", "No s'ha pogut carregar el vídeo: " + e.getMessage());
        }
        req.getRequestDispatcher("/WEB-INF/views/reproduccion.jsp").forward(req, resp);
    }

    private void handleReproducir(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = param(req, "id");
        String body = "";
        try { body = httpPost(REST_BASE + "/" + encode(id) + "/view", apiKey(req)); }
        catch (Exception e) { LOGGER.log(Level.WARNING, "Error incrementant reproduccions", e); }

        if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
            String count = "0";
            if (!body.isEmpty()) {
                try {
                    JsonObject obj = Json.createReader(new StringReader(body)).readObject();
                    count = obj.get("reproducciones").toString();
                } catch (Exception ignored) {}
            }
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"reproducciones\":" + count + "}");
        } else {
            resp.sendRedirect(req.getContextPath() + "/reproduccion?id=" + encode(id) + "&reproduit=1");
        }
    }

    private String buildSearchUrl(String titulo, String autor, String year, String month, String day) {
        StringBuilder sb = new StringBuilder(REST_BASE + "/search?");
        if (!titulo.isBlank())     sb.append("titulo=").append(encode(titulo));
        else if (!autor.isBlank()) sb.append("autor=").append(encode(autor));
        else {
            sb.append("year=").append(year);
            if (!month.isBlank()) sb.append("&month=").append(month);
            if (!day.isBlank())   sb.append("&day=").append(day);
        }
        return sb.toString();
    }

    private List<Map<String, String>> toList(JsonArray arr) {
        List<Map<String, String>> list = new ArrayList<>();
        for (JsonValue val : arr) list.add(toMap(val.asJsonObject()));
        return list;
    }

    private Map<String, String> toMap(JsonObject obj) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : obj.keySet()) {
            JsonValue v = obj.get(key);
            map.put(key, v.getValueType() == JsonValue.ValueType.STRING
                    ? ((JsonString) v).getString() : v.toString());
        }
        return map;
    }

    private String apiKey(HttpServletRequest req) {
        jakarta.servlet.http.HttpSession s = req.getSession(false);
        if (s == null) return "";
        Object k = s.getAttribute("apiKey");
        return k == null ? "" : (String) k;
    }

    private String httpGet(String urlStr, String apiKey) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (apiKey != null && !apiKey.isBlank())
            conn.setRequestProperty("X-API-Key", apiKey);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        try {
            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) throw new IOException("HTTP " + status + " from " + urlStr);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                if (status < 200 || status >= 300) throw new IOException("HTTP " + status + ": " + sb);
                return sb.toString();
            }
        } finally { conn.disconnect(); }
    }

    private String httpPost(String urlStr, String apiKey) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        if (apiKey != null && !apiKey.isBlank())
            conn.setRequestProperty("X-API-Key", apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        try {
            conn.getOutputStream().close();
            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) return "";
            try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
                return sb.toString();
            }
        } finally { conn.disconnect(); }
    }

    private String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
