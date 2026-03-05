<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>Videos</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .card { max-width: 640px; padding: 20px; border: 1px solid #ccc; border-radius: 8px; }
        .field { margin-bottom: 12px; }
        .field label { display: block; margin-bottom: 6px; }
        .field input { width: 100%; padding: 8px; box-sizing: border-box; }
        .error { color: #b00020; margin-bottom: 12px; }
    </style>
</head>
<body>
<div class="card">
    <h2>Registro y listado de videos</h2>
    <p>Usuario activo: <strong><%= session.getAttribute("usuarioLogueado") %></strong></p>
    <% if (request.getAttribute("error") != null) { %>
    <div class="error"><%= request.getAttribute("error") %></div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/videos">
        <div class="field">
            <label for="videoNombre">Nombre del video</label>
            <input id="videoNombre" name="videoNombre" type="text" />
        </div>
        <button type="submit">Registrar video</button>
    </form>

    <h3>Listado</h3>
    <ul>
        <%
            List<String> videos = (List<String>) request.getAttribute("videos");
            if (videos != null && !videos.isEmpty()) {
                for (String video : videos) {
        %>
        <li><%= video %></li>
        <%
                }
            } else {
        %>
        <li>No hay videos registrados.</li>
        <%
            }
        %>
    </ul>

    <p><a href="<%= request.getContextPath() %>/logout">Cerrar sesión</a></p>
</div>
</body>
</html>
