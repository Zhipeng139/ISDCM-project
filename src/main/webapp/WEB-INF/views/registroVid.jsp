<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <title>Registro de Video</title>
    <style>
        * {
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #74ebd5, #ACB6E5);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
            padding: 24px 12px;
        }

        .card {
            background: #fff;
            max-width: 760px;
            width: 100%;
            padding: 30px 25px;
            border-radius: 12px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.15);
        }

        .card h2 {
            text-align: center;
            margin-bottom: 20px;
            color: #333;
            font-size: 28px;
        }

        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px 20px;
        }

        .field {
            display: flex;
            flex-direction: column;
        }

        .field.full {
            grid-column: span 2;
        }

        .field label {
            font-size: 13px;
            margin-bottom: 8px;
            font-weight: 600;
            color: #555;
        }

        .field input,
        .field textarea {
            width: 100%;
            padding: 10px 15px;
            border: 1px solid #ccc;
            border-radius: 10px;
            font-size: 14px;
            transition: border 0.3s, box-shadow 0.3s;
            font-family: inherit;
        }

        .field textarea {
            min-height: 110px;
            resize: vertical;
        }

        .field input:focus,
        .field textarea:focus {
            border-color: #74ebd5;
            box-shadow: 0 0 5px rgba(116,235,213,0.5);
            outline: none;
        }

        .error {
            color: #b00020;
            margin-bottom: 12px;
            font-weight: 500;
        }

        .fieldError {
            color: #b00020;
            margin-top: 6px;
            font-size: 12px;
            font-weight: 500;
        }

        .actions {
            margin-top: 20px;
            display: flex;
            gap: 12px;
            justify-content: flex-end;
            align-items: center;
        }

        .button {
            display: inline-block;
            border: none;
            border-radius: 8px;
            background: #74ebd5;
            color: #fff;
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
            transition: background 0.3s;
            padding: 12px 16px;
        }

        .button:hover {
            background: #4ac1b8;
        }

        .link {
            color: #666;
            font-weight: 600;
            text-decoration: none;
        }

        .topbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
        }
    </style>
</head>
<body>
<%
    Map<String, String> form = (Map<String, String>) request.getAttribute("videoForm");
    Map<String, String> fieldErrors = (Map<String, String>) request.getAttribute("fieldErrors");
    if (form == null) {
        form = new java.util.LinkedHashMap<>();
    }
    if (fieldErrors == null) {
        fieldErrors = new java.util.LinkedHashMap<>();
    }
%>
<div class="card">
    <div class="topbar">
        <a class="link" href="<%= request.getContextPath() %>/listadoVid">← Tornar al llistat</a>
        <a class="link" href="<%= request.getContextPath() %>/logout">Tancar sessió</a>
    </div>
    <h2>Registre de vídeo</h2>

    <% if (request.getAttribute("error") != null) { %>
    <div class="error"><%= request.getAttribute("error") %></div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/registroVid">
        <div class="grid">
            <div class="field">
                <label for="titulo">Títol</label>
                <input id="titulo" name="titulo" type="text" value="<%= form.getOrDefault("titulo", "") %>" />
                <% if (fieldErrors.get("titulo") != null) { %><div class="fieldError"><%= fieldErrors.get("titulo") %></div><% } %>
            </div>
            <div class="field">
                <label for="autor">Autor</label>
                <input id="autor" name="autor" type="text" value="<%= form.getOrDefault("autor", "") %>" />
                <% if (fieldErrors.get("autor") != null) { %><div class="fieldError"><%= fieldErrors.get("autor") %></div><% } %>
            </div>
            <div class="field">
                <label for="fechaCreacion">Data de creació</label>
                <input id="fechaCreacion" name="fechaCreacion" type="date" value="<%= form.getOrDefault("fechaCreacion", "") %>" />
                <% if (fieldErrors.get("fechaCreacion") != null) { %><div class="fieldError"><%= fieldErrors.get("fechaCreacion") %></div><% } %>
            </div>
            <div class="field">
                <label for="duracion">Duració (segons)</label>
                <input id="duracion" name="duracion" type="number" min="0" value="<%= form.getOrDefault("duracion", "") %>" />
                <% if (fieldErrors.get("duracion") != null) { %><div class="fieldError"><%= fieldErrors.get("duracion") %></div><% } %>
            </div>
            <div class="field">
                <label for="reproducciones">Reproduccions inicials</label>
                <input id="reproducciones" name="reproducciones" type="number" min="0" value="<%= form.getOrDefault("reproducciones", "0") %>" />
                <% if (fieldErrors.get("reproducciones") != null) { %><div class="fieldError"><%= fieldErrors.get("reproducciones") %></div><% } %>
            </div>
            <div class="field">
                <label for="formato">Format</label>
                <input id="formato" name="formato" type="text" placeholder="ex: MP4" value="<%= form.getOrDefault("formato", "") %>" />
                <% if (fieldErrors.get("formato") != null) { %><div class="fieldError"><%= fieldErrors.get("formato") %></div><% } %>
            </div>
            <div class="field">
                <label for="resolucion">Resolució</label>
                <input id="resolucion" name="resolucion" type="text" placeholder="ex: 1080p" value="<%= form.getOrDefault("resolucion", "") %>" />
                <% if (fieldErrors.get("resolucion") != null) { %><div class="fieldError"><%= fieldErrors.get("resolucion") %></div><% } %>
            </div>
            <div class="field full">
                <label for="url">URL</label>
                <input id="url" name="url" type="text" value="<%= form.getOrDefault("url", "") %>" />
                <% if (fieldErrors.get("url") != null) { %><div class="fieldError"><%= fieldErrors.get("url") %></div><% } %>
            </div>
            <div class="field full">
                <label for="categoria">Categoria</label>
                <input id="categoria" name="categoria" type="text" value="<%= form.getOrDefault("categoria", "") %>" />
                <% if (fieldErrors.get("categoria") != null) { %><div class="fieldError"><%= fieldErrors.get("categoria") %></div><% } %>
            </div>
            <div class="field full">
                <label for="descripcion">Descripció</label>
                <textarea id="descripcion" name="descripcion"><%= form.getOrDefault("descripcion", "") %></textarea>
                <% if (fieldErrors.get("descripcion") != null) { %><div class="fieldError"><%= fieldErrors.get("descripcion") %></div><% } %>
            </div>
        </div>

        <div class="actions">
            <a class="link" href="<%= request.getContextPath() %>/listadoVid">Cancel·lar</a>
            <button class="button" type="submit">Registrar vídeo</button>
        </div>
    </form>
</div>
</body>
</html>
