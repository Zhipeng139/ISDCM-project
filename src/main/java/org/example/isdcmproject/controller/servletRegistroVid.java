package org.example.isdcmproject.controller;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.example.isdcmproject.config.RestConfig;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "servletRegistroVid", urlPatterns = "/registroVid")
public class servletRegistroVid extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(servletRegistroVid.class.getName());
    private static final String REST_BASE = RestConfig.BASE_URL + "/videos";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/registroVid.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Map<String, String> form = extractFormData(req);
        form.put("id", UUID.randomUUID().toString());
        req.setAttribute("videoForm", form);

        Map<String, String> errors = validate(form);
        if (!errors.isEmpty()) {
            req.setAttribute("fieldErrors", errors);
            req.setAttribute("error", "Revisa els camps marcats i torna-ho a intentar.");
            req.getRequestDispatcher("/WEB-INF/views/registroVid.jsp").forward(req, resp);
            return;
        }

        String json = buildJson(form);
        try {
            httpPost(REST_BASE, json, apiKey(req));
            resp.sendRedirect(req.getContextPath() + "/listadoVid?created=1");
        } catch (IOException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("HTTP 409")) {
                req.setAttribute("error", "Ja existeix un vídeo amb aquest identificador.");
            } else {
                LOGGER.log(Level.SEVERE, "Error cridant REST registroVid", e);
                req.setAttribute("error", "No s'ha pogut registrar el vídeo: " + msg);
            }
            req.getRequestDispatcher("/WEB-INF/views/registroVid.jsp").forward(req, resp);
        }
    }

    private Map<String, String> extractFormData(HttpServletRequest req) {
        Map<String, String> form = new LinkedHashMap<>();
        for (String field : new String[]{"titulo","autor","fechaCreacion","duracion","reproducciones","formato","url","categoria","resolucion","descripcion"}) {
            String v = req.getParameter(field);
            form.put(field, v == null ? "" : v.trim());
        }
        return form;
    }

    private Map<String, String> validate(Map<String, String> form) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (String field : new String[]{"titulo","autor","fechaCreacion","duracion","formato","url","categoria","resolucion","descripcion"}) {
            if (form.getOrDefault(field, "").isBlank())
                errors.put(field, "Camp obligatori.");
        }
        if (!errors.containsKey("duracion")) {
            try { Integer.parseInt(form.get("duracion")); }
            catch (NumberFormatException e) { errors.put("duracion", "Ha de ser un número enter."); }
        }
        return errors;
    }

    private String buildJson(Map<String, String> form) {
        int duracion = Integer.parseInt(form.get("duracion"));
        int repros = form.get("reproducciones").isBlank() ? 0 : Integer.parseInt(form.get("reproducciones"));
        JsonObject obj = Json.createObjectBuilder()
                .add("id",             form.get("id"))
                .add("titulo",         form.get("titulo"))
                .add("autor",          form.get("autor"))
                .add("fechaCreacion",  form.get("fechaCreacion"))
                .add("duracion",       duracion)
                .add("reproducciones", repros)
                .add("descripcion",    form.get("descripcion"))
                .add("formato",        form.get("formato"))
                .add("url",            form.get("url"))
                .add("categoria",      form.get("categoria"))
                .add("resolucion",     form.get("resolucion"))
                .build();
        return obj.toString();
    }

    private String apiKey(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return "";
        Object k = s.getAttribute("apiKey");
        return k == null ? "" : (String) k;
    }

    private void httpPost(String urlStr, String body, String apiKey) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        if (apiKey != null && !apiKey.isBlank())
            conn.setRequestProperty("X-API-Key", apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        try {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                InputStream es = conn.getErrorStream();
                String errBody = "";
                if (es != null) {
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(es, StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) sb.append(line);
                        errBody = sb.toString();
                    }
                }
                throw new IOException("HTTP " + status + ": " + errBody);
            }
        } finally {
            conn.disconnect();
        }
    }
}
