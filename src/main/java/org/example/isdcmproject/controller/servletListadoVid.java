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
import jakarta.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "servletListadoVid", urlPatterns = "/listadoVid")
public class servletListadoVid extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(servletListadoVid.class.getName());
    private static final String REST_BASE = RestConfig.BASE_URL + "/videos";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String titulo = param(req, "titulo");
        String autor  = param(req, "autor");
        String year   = param(req, "year");
        String month  = param(req, "month");
        String day    = param(req, "day");

        boolean hasSearch = !titulo.isBlank() || !autor.isBlank() || !year.isBlank();

        try {
            String url  = hasSearch ? buildSearchUrl(titulo, autor, year, month, day) : REST_BASE;
            String json = httpGet(url, apiKey(req));
            JsonArray arr = Json.createReader(new StringReader(json)).readArray();
            req.setAttribute("videos", toList(arr));

            if ("1".equals(req.getParameter("created"))) {
                req.setAttribute("success", "Vídeo registrat correctament.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cridant l'API REST", e);
            req.setAttribute("error", "No s'ha pogut connectar amb el servei REST: " + e.getMessage());
        }

        req.getRequestDispatcher("/WEB-INF/views/listadoVid.jsp").forward(req, resp);
    }

    private String buildSearchUrl(String titulo, String autor, String year, String month, String day) {
        StringBuilder sb = new StringBuilder(REST_BASE + "/search?");
        if (!titulo.isBlank()) {
            sb.append("titulo=").append(encode(titulo));
        } else if (!autor.isBlank()) {
            sb.append("autor=").append(encode(autor));
        } else {
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
        HttpSession s = req.getSession(false);
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
        } finally {
            conn.disconnect();
        }
    }

    private String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
