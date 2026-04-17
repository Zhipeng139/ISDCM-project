<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.isdcmproject.model.video" %>
<!DOCTYPE html>
<html>
<head>
    <title>Listado de Videos</title>
    <style>
        * {
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f7f9fc;
            margin: 0;
            padding: 24px;
        }

        .container {
            max-width: 1100px;
            margin: 0 auto;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
        }

        .header h2 {
            margin: 0;
            color: #223;
        }

        .actions {
            display: flex;
            gap: 10px;
            align-items: center;
        }

        .button {
            display: inline-block;
            text-decoration: none;
            border: none;
            border-radius: 8px;
            background: #74ebd5;
            color: #fff;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.3s;
            padding: 10px 14px;
        }

        .button:hover {
            background: #4ac1b8;
        }

        .link {
            color: #4d5b73;
            text-decoration: none;
            font-weight: 600;
        }

        .card {
            background: #fff;
            border-radius: 12px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.08);
            overflow: hidden;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            text-align: left;
            padding: 12px;
            border-bottom: 1px solid #eef2f7;
            vertical-align: top;
            font-size: 14px;
        }

        th {
            background: #f0f5fb;
            font-size: 13px;
            color: #3a4963;
        }

        .muted {
            color: #75839a;
            font-size: 13px;
        }

        .error {
            color: #b00020;
            margin-bottom: 12px;
            font-weight: 500;
        }

        .success {
            color: #006400;
            margin-bottom: 12px;
            font-weight: 500;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h2>Listado de videos</h2>
        <div class="actions">
            <a class="button" href="<%= request.getContextPath() %>/registroVid">Nuevo video</a>
            <a class="link" href="<%= request.getContextPath() %>/logout">Cerrar sesión</a>
        </div>
    </div>
    <p class="muted">Usuario activo: <strong><%= session.getAttribute("usuarioLogueado") %></strong></p>

    <% if (request.getAttribute("error") != null) { %>
    <div class="error"><%= request.getAttribute("error") %></div>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
    <div class="success"><%= request.getAttribute("success") %></div>
    <% } %>

    <div class="card">
        <table>
            <thead>
            <tr>
                <th>Identificador</th>
                <th>Título</th>
                <th>Descripción</th>
                <th>Categoría / tags</th>
                <th>Formato</th>
                <th>Fecha creación</th>
                <th>Duración</th>
                <th>Reproducciones</th>
                <th>URL</th>
            </tr>
            </thead>
            <tbody>
            <%
                List<video> videos = (List<video>) request.getAttribute("videos");
                if (videos != null && !videos.isEmpty()) {
                    for (video item : videos) {
            %>
            <tr>
                <td><%= item.getIdentificador() %></td>
                <td><%= item.getTitulo() %></td>
                <td><%= item.getDescripcion() %></td>
                <td><%= item.getCategoria() %></td>
                <td><%= item.getFormato() %></td>
                <td><%= item.getFechaCreacion() %></td>
                <td><%= item.getDuracion() %> s</td>
                <td><%= item.getReproducciones() %></td>
                <td><a href="<%= item.getUrl() %>" target="_blank"><%= item.getUrl() %></a></td>
            </tr>
            <%
                    }
                } else {
            %>
            <tr>
                <td colspan="9">No hay videos registrados.</td>
            </tr>
            <%
                }
            %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
