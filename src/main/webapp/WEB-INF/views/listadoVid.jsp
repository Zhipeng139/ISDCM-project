<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <title>Llistat de Vídeos</title>
    <style>
        * { box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f7f9fc; margin: 0; padding: 24px; }
        .container { max-width: 1200px; margin: 0 auto; }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
        .header h2 { margin: 0; color: #223; }
        .actions { display: flex; gap: 10px; align-items: center; }
        .button { display: inline-block; text-decoration: none; border: none; border-radius: 8px; background: #74ebd5; color: #fff; font-weight: 600; cursor: pointer; transition: background 0.3s; padding: 10px 14px; }
        .button:hover { background: #4ac1b8; }
        .link { color: #4d5b73; text-decoration: none; font-weight: 600; }
        .card { background: #fff; border-radius: 12px; box-shadow: 0 8px 20px rgba(0,0,0,0.08); overflow: auto; margin-top: 16px; }
        .searchCard { background: #fff; border-radius: 12px; box-shadow: 0 8px 20px rgba(0,0,0,0.08); padding: 20px; }
        .tabs { display: flex; gap: 8px; margin-bottom: 16px; }
        .tab { padding: 8px 18px; border-radius: 20px; border: 2px solid #d9e1ec; background: #fff; cursor: pointer; font-weight: 600; color: #4d5b73; transition: all 0.2s; }
        .tab.active { background: #74ebd5; border-color: #74ebd5; color: #fff; }
        .searchPanel { display: none; }
        .searchPanel.active { display: flex; gap: 10px; align-items: flex-end; flex-wrap: wrap; }
        .field { display: flex; flex-direction: column; }
        .field label { font-size: 12px; color: #4d5b73; margin-bottom: 6px; font-weight: 600; }
        .field input { border: 1px solid #d9e1ec; border-radius: 8px; padding: 8px 10px; font-size: 13px; min-width: 140px; }
        .searchActions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { text-align: left; padding: 12px; border-bottom: 1px solid #eef2f7; vertical-align: top; font-size: 14px; }
        th { background: #f0f5fb; font-size: 13px; color: #3a4963; }
        .muted { color: #75839a; font-size: 13px; }
        .error { color: #b00020; margin-bottom: 12px; font-weight: 500; }
        .success { color: #006400; margin-bottom: 12px; font-weight: 500; }
        .play-btn { background: #74ebd5; color: #fff; border: none; border-radius: 6px; padding: 5px 10px; cursor: pointer; font-weight: 600; text-decoration: none; font-size: 12px; white-space: nowrap; }
        .play-btn:hover { background: #4ac1b8; }
    </style>
</head>
<body>
<%
    List<Map<String, String>> videos = (List<Map<String, String>>) request.getAttribute("videos");
    String titolParam = request.getParameter("titulo") != null ? request.getParameter("titulo") : "";
    String autorParam = request.getParameter("autor")  != null ? request.getParameter("autor")  : "";
    String yearParam  = request.getParameter("year")   != null ? request.getParameter("year")   : "";
    String monthParam = request.getParameter("month")  != null ? request.getParameter("month")  : "";
    String dayParam   = request.getParameter("day")    != null ? request.getParameter("day")    : "";
    String activeTab  = !autorParam.isBlank() ? "autor" : (!yearParam.isBlank() ? "data" : "titol");
%>
<div class="container">
    <div class="header">
        <h2>Llistat de vídeos</h2>
        <div class="actions">
            <a class="button" href="<%= request.getContextPath() %>/busqueda">Cercar vídeo</a>
            <a class="button" href="<%= request.getContextPath() %>/registroVid">Nou vídeo</a>
            <a class="link"   href="<%= request.getContextPath() %>/logout">Tancar sessió</a>
        </div>
    </div>
    <p class="muted">Usuari: <strong><%= session.getAttribute("usuarioLogueado") %></strong></p>

    <% if (request.getAttribute("error") != null) { %><div class="error"><%= request.getAttribute("error") %></div><% } %>
    <% if (request.getAttribute("success") != null) { %><div class="success"><%= request.getAttribute("success") %></div><% } %>

    <div class="searchCard">
        <div class="tabs">
            <button class="tab <%= "titol".equals(activeTab) ? "active" : "" %>" onclick="showTab('titol')">Per títol</button>
            <button class="tab <%= "autor".equals(activeTab) ? "active" : "" %>" onclick="showTab('autor')">Per autor</button>
            <button class="tab <%= "data".equals(activeTab)  ? "active" : "" %>" onclick="showTab('data')">Per data</button>
        </div>
        <form method="get" action="<%= request.getContextPath() %>/listadoVid">
            <div id="panel-titol" class="searchPanel <%= "titol".equals(activeTab) ? "active" : "" %>">
                <div class="field"><label>Títol</label><input name="titulo" type="text" value="<%= titolParam %>" placeholder="ex: Java" /></div>
                <button class="button" type="submit">Cercar</button>
            </div>
        </form>
        <form method="get" action="<%= request.getContextPath() %>/listadoVid">
            <div id="panel-autor" class="searchPanel <%= "autor".equals(activeTab) ? "active" : "" %>">
                <div class="field"><label>Autor</label><input name="autor" type="text" value="<%= autorParam %>" placeholder="ex: Garcia" /></div>
                <button class="button" type="submit">Cercar</button>
            </div>
        </form>
        <form method="get" action="<%= request.getContextPath() %>/listadoVid">
            <div id="panel-data" class="searchPanel <%= "data".equals(activeTab) ? "active" : "" %>">
                <div class="field"><label>Any *</label><input name="year" type="number" value="<%= yearParam %>" placeholder="2024" style="width:90px;" /></div>
                <div class="field"><label>Mes</label><input name="month" type="number" min="1" max="12" value="<%= monthParam %>" placeholder="1-12" style="width:80px;" /></div>
                <div class="field"><label>Dia</label><input name="day" type="number" min="1" max="31" value="<%= dayParam %>" placeholder="1-31" style="width:80px;" /></div>
                <button class="button" type="submit">Cercar</button>
            </div>
        </form>
        <div class="searchActions">
            <a class="link" href="<%= request.getContextPath() %>/listadoVid">Netejar filtres</a>
        </div>
    </div>

    <div class="card">
        <table>
            <thead>
            <tr>
                <th>Títol</th><th>Autor</th><th>Categoria</th><th>Resolució</th>
                <th>Format</th><th>Data creació</th><th>Duració</th><th>Reproduccions</th><th></th>
            </tr>
            </thead>
            <tbody>
            <%
                if (videos != null && !videos.isEmpty()) {
                    for (Map<String, String> v : videos) {
            %>
            <tr>
                <td><strong><%= v.getOrDefault("titulo","") %></strong></td>
                <td><%= v.getOrDefault("autor","") %></td>
                <td><%= v.getOrDefault("categoria","") %></td>
                <td><%= v.getOrDefault("resolucion","") %></td>
                <td><%= v.getOrDefault("formato","") %></td>
                <td><%= v.getOrDefault("fechaCreacion","") %></td>
                <td><%= v.getOrDefault("duracion","0") %> s</td>
                <td><%= v.getOrDefault("reproducciones","0") %></td>
                <td><a class="play-btn" href="<%= request.getContextPath() %>/reproduccion?id=<%= v.getOrDefault("id","") %>">▶ Reproduir</a></td>
            </tr>
            <%  } } else if (videos != null) { %>
            <tr><td colspan="9" class="muted">No hi ha vídeos registrats.</td></tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
<script>
    function showTab(tab) {
        ['titol','autor','data'].forEach(t => document.getElementById('panel-'+t).classList.remove('active'));
        document.getElementById('panel-'+tab).classList.add('active');
        document.querySelectorAll('.tab').forEach((btn,i) => {
            btn.classList.toggle('active', ['titol','autor','data'][i] === tab);
        });
    }
</script>
</body>
</html>
