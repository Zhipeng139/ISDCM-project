<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.isdcmproject.model.video" %>
<%@ page import="org.example.isdcmproject.model.videoSearchCriteria" %>
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
            overflow: auto;
            margin-top: 16px;
        }

        .searchCard {
            background: #fff;
            border-radius: 12px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.08);
            padding: 14px;
        }

        .searchGrid {
            display: grid;
            grid-template-columns: repeat(4, minmax(160px, 1fr));
            gap: 10px;
        }

        .searchGrid label {
            display: block;
            font-size: 12px;
            color: #4d5b73;
            margin-bottom: 6px;
            font-weight: 600;
        }

        .searchGrid input {
            width: 100%;
            border: 1px solid #d9e1ec;
            border-radius: 8px;
            padding: 8px 10px;
            font-size: 13px;
        }

        .searchActions {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            margin-top: 10px;
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

        @media (max-width: 960px) {
            .searchGrid {
                grid-template-columns: repeat(2, minmax(160px, 1fr));
            }
        }
    </style>
</head>
<body>
<%
    List<video> videos = (List<video>) request.getAttribute("videos");
    List<String> suggestions = (List<String>) request.getAttribute("suggestions");
    videoSearchCriteria criteria = (videoSearchCriteria) request.getAttribute("searchCriteria");
    if (criteria == null) {
        criteria = new videoSearchCriteria();
    }
%>
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
    <div class="searchCard">
        <form method="get" action="<%= request.getContextPath() %>/listadoVid">
            <div class="searchGrid">
                <div>
                    <label for="q">Búsqueda full-text</label>
                    <input id="q" name="q" type="text" list="suggestionsList" value="<%= criteria.getConsultaLibre() %>" />
                    <datalist id="suggestionsList">
                        <%
                            if (suggestions != null) {
                                for (String suggestion : suggestions) {
                        %>
                        <option value="<%= suggestion %>"></option>
                        <%
                                }
                            }
                        %>
                    </datalist>
                </div>
                <div>
                    <label for="titulo">Título</label>
                    <input id="titulo" name="titulo" type="text" value="<%= criteria.getTitulo() %>" />
                </div>
                <div>
                    <label for="categoria">Categoría</label>
                    <input id="categoria" name="categoria" type="text" value="<%= criteria.getCategoria() %>" />
                </div>
                <div>
                    <label for="resolucion">Resolución</label>
                    <input id="resolucion" name="resolucion" type="text" value="<%= criteria.getResolucion() %>" />
                </div>
                <div>
                    <label for="fechaDesde">Fecha desde</label>
                    <input id="fechaDesde" name="fechaDesde" type="date" value="<%= criteria.getFechaDesde() == null ? "" : criteria.getFechaDesde() %>" />
                </div>
                <div>
                    <label for="fechaHasta">Fecha hasta</label>
                    <input id="fechaHasta" name="fechaHasta" type="date" value="<%= criteria.getFechaHasta() == null ? "" : criteria.getFechaHasta() %>" />
                </div>
                <div>
                    <label for="duracionMin">Duración mín (s)</label>
                    <input id="duracionMin" name="duracionMin" type="number" min="0" value="<%= criteria.getDuracionMin() == null ? "" : criteria.getDuracionMin() %>" />
                </div>
                <div>
                    <label for="duracionMax">Duración máx (s)</label>
                    <input id="duracionMax" name="duracionMax" type="number" min="0" value="<%= criteria.getDuracionMax() == null ? "" : criteria.getDuracionMax() %>" />
                </div>
            </div>
            <div class="searchActions">
                <a class="link" href="<%= request.getContextPath() %>/listadoVid">Limpiar filtros</a>
                <button class="button" type="submit">Buscar</button>
            </div>
        </form>
    </div>

    <div class="card">
        <table>
            <thead>
            <tr>
                <th>Identificador</th>
                <th>Título</th>
                <th>Descripción</th>
                <th>Categoría</th>
                <th>Resolución</th>
                <th>Formato</th>
                <th>Fecha creación</th>
                <th>Duración</th>
                <th>Reproducciones</th>
                <th>URL</th>
            </tr>
            </thead>
            <tbody>
            <%
                if (videos != null && !videos.isEmpty()) {
                    for (video item : videos) {
            %>
            <tr>
                <td><%= item.getIdentificador() %></td>
                <td><%= item.getTitulo() %></td>
                <td><%= item.getDescripcion() %></td>
                <td><%= item.getCategoria() %></td>
                <td><%= item.getResolucion() %></td>
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
                <td colspan="10">No hay videos registrados.</td>
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
