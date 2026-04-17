package org.example.isdcmproject.controller;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.example.isdcmproject.config.RestConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet(name = "servletUsuarios", urlPatterns = {"/login", "/registroUsu", "/logout"})
public class servletUsuarios extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(servletUsuarios.class.getName());
    private static final String REST_BASE = RestConfig.BASE_URL + "/usuaris";

    private static final Pattern EMAIL_PATTERN   = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{4,20}$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);

        if ("/logout".equals(path)) {
            if (session != null) session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if ("/registroUsu".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/registroUsu.jsp").forward(req, resp);
            return;
        }
        if (session != null && session.getAttribute("usuarioLogueado") != null) {
            resp.sendRedirect(req.getContextPath() + "/listadoVid");
            return;
        }
        if ("1".equals(req.getParameter("registered")))
            req.setAttribute("success", "Registro completado. Ahora puedes iniciar sesión.");
        if ("required".equals(req.getParameter("auth")))
            req.setAttribute("error", "Debes iniciar sesión para acceder a la gestión de vídeos.");
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if ("/registroUsu".equals(req.getServletPath())) {
            handleRegister(req, resp);
        } else {
            handleLogin(req, resp);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = sanitize(req.getParameter("username")).toLowerCase();
        String password = req.getParameter("password");

        req.setAttribute("username", username);

        if (username.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Debes completar usuario y contraseña.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        String json = Json.createObjectBuilder()
                .add("username", username)
                .add("password", password)
                .build().toString();
        try {
            String body = httpPost(REST_BASE + "/login", json);
            HttpSession session = req.getSession(true);
            session.setAttribute("usuarioLogueado", username);
            try (JsonReader jr = Json.createReader(new StringReader(body))) {
                JsonObject obj = jr.readObject();
                session.setAttribute("apiKey", obj.getString("apiKey", ""));
            }
            resp.sendRedirect(req.getContextPath() + "/listadoVid");
        } catch (IOException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("HTTP 401")) {
                req.setAttribute("error", "Credenciales incorrectas.");
            } else {
                LOGGER.log(Level.SEVERE, "Error REST login", e);
                req.setAttribute("error", "No fue posible iniciar sesión. Inténtalo de nuevo.");
            }
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String nombre   = sanitize(req.getParameter("nombre"));
        String apellido = sanitize(req.getParameter("apellido"));
        String email    = sanitize(req.getParameter("email"));
        String username = sanitize(req.getParameter("username"));
        String password = req.getParameter("password");
        String confirm  = req.getParameter("confirmPassword");

        req.setAttribute("nombre",   nombre);
        req.setAttribute("apellido", apellido);
        req.setAttribute("email",    email);
        req.setAttribute("username", username);

        String validationError = validateRegister(nombre, apellido, email, username, password, confirm);
        if (validationError != null) {
            req.setAttribute("error", validationError);
            req.getRequestDispatcher("/WEB-INF/views/registroUsu.jsp").forward(req, resp);
            return;
        }

        String json = Json.createObjectBuilder()
                .add("nombre",   nombre)
                .add("apellido", apellido)
                .add("email",    email)
                .add("username", username)
                .add("password", password)
                .build().toString();
        try {
            httpPost(REST_BASE, json);
            resp.sendRedirect(req.getContextPath() + "/login?registered=1");
        } catch (IOException e) {
            String msg = e.getMessage();
            String error;
            if (msg != null && msg.contains("HTTP 409")) {
                if (msg.toLowerCase().contains("username")) {
                    error = "El nombre de usuario ya está en uso.";
                } else if (msg.toLowerCase().contains("email")) {
                    error = "El correo electrónico ya está en uso.";
                } else {
                    error = "El usuario o el correo ya existen.";
                }
            } else {
                LOGGER.log(Level.SEVERE, "Error REST registre", e);
                error = "No fue posible crear la cuenta. Inténtalo de nuevo.";
            }
            req.setAttribute("error", error);
            req.getRequestDispatcher("/WEB-INF/views/registroUsu.jsp").forward(req, resp);
        }
    }

    private String validateRegister(String nombre, String apellido, String email,
                                    String username, String password, String confirm) {
        if (nombre.isBlank() || apellido.isBlank() || email.isBlank() ||
            username.isBlank() || password == null || password.isBlank() ||
            confirm == null || confirm.isBlank())
            return "Todos los campos son obligatorios.";
        if (!EMAIL_PATTERN.matcher(email).matches())
            return "El formato del correo electrónico no es válido.";
        if (!USERNAME_PATTERN.matcher(username).matches())
            return "El usuario debe tener entre 4 y 20 caracteres alfanuméricos.";
        if (password.length() < 6)
            return "La contraseña debe tener al menos 6 caracteres.";
        if (!password.equals(confirm))
            return "Las contraseñas no coinciden.";
        return null;
    }

    private String httpPost(String urlStr, String body) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        try {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
            StringBuilder sb = new StringBuilder();
            if (is != null) {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line);
                }
            }
            if (status < 200 || status >= 300)
                throw new IOException("HTTP " + status + ": " + sb);
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    private String sanitize(String v) {
        return v == null ? "" : v.trim();
    }
}
