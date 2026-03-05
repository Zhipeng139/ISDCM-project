package org.example.isdcmproject.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.isdcmproject.model.usuario;

@WebServlet(name = "servletUsuarios", urlPatterns = {"/login", "/registroUsu", "/logout"})
public class servletUsuarios extends HttpServlet {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9._-]{4,20}$");

    private final usuario usuarioModel = new usuario();

    @Override
    public void init() throws ServletException {
        try {
            usuarioModel.initializeTable();
        } catch (SQLException e) {
            throw new ServletException("No se pudo inicializar la tabla de usuarios.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        HttpSession session = request.getSession(false);
        if ("/logout".equals(path)) {
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if ("/registroUsu".equals(path)) {
            request.getRequestDispatcher("/WEB-INF/views/registroUsu.jsp").forward(request, response);
            return;
        }
        if (session != null && session.getAttribute("usuarioLogueado") != null) {
            response.sendRedirect(request.getContextPath() + "/videos");
            return;
        }
        if ("1".equals(request.getParameter("registered"))) {
            request.setAttribute("success", "Registro completado. Ahora puedes iniciar sesión.");
        }
        if ("required".equals(request.getParameter("auth"))) {
            request.setAttribute("error", "Debes iniciar sesión para acceder a la gestión de videos.");
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        if ("/registroUsu".equals(path)) {
            handleRegister(request, response);
            return;
        }
        handleLogin(request, response);
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nombre = sanitize(request.getParameter("nombre"));
        String apellido = sanitize(request.getParameter("apellido"));
        String email = sanitize(request.getParameter("email"));
        String username = sanitize(request.getParameter("username"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        request.setAttribute("nombre", nombre);
        request.setAttribute("apellido", apellido);
        request.setAttribute("email", email);
        request.setAttribute("username", username);

        String error = validateRegisterFields(nombre, apellido, email, username, password, confirmPassword);
        if (error != null) {
            request.setAttribute("error", error);
            request.getRequestDispatcher("/WEB-INF/views/registroUsu.jsp").forward(request, response);
            return;
        }

        try {
            boolean created = usuarioModel.createUser(nombre, apellido, email, username, password);
            if (!created) {
                request.setAttribute("error", "El nombre de usuario ya está en uso.");
                request.getRequestDispatcher("/WEB-INF/views/registroUsu.jsp").forward(request, response);
                return;
            }
        } catch (SQLException e) {
            request.setAttribute("error", "No fue posible registrar el usuario. Intenta nuevamente.");
            request.getRequestDispatcher("/WEB-INF/views/registroUsu.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login?registered=1");
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = sanitize(request.getParameter("username"));
        String password = request.getParameter("password");

        request.setAttribute("username", username);

        if (isBlank(username) || isBlank(password)) {
            request.setAttribute("error", "Debes completar usuario y contraseña.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        try {
            boolean valid = usuarioModel.validateCredentials(username, password);
            if (!valid) {
                request.setAttribute("error", "Credenciales inválidas.");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("usuarioLogueado", username);
            response.sendRedirect(request.getContextPath() + "/videos");
        } catch (SQLException e) {
            request.setAttribute("error", "No fue posible iniciar sesión. Intenta nuevamente.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }

    private String validateRegisterFields(String nombre, String apellido, String email, String username, String password, String confirmPassword) {
        if (isBlank(nombre) || isBlank(apellido) || isBlank(email) || isBlank(username) || isBlank(password) || isBlank(confirmPassword)) {
            return "Todos los campos son obligatorios.";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "El formato del correo electrónico es inválido.";
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "El usuario debe tener entre 4 y 20 caracteres alfanuméricos.";
        }
        if (password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }
        if (!password.equals(confirmPassword)) {
            return "Las contraseñas no coinciden.";
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
