<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <title>Reproducció de Vídeo</title>
    <link href="https://vjs.zencdn.net/8.10.0/video-js.css" rel="stylesheet" />
    <style>
        * { box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #74ebd5, #ACB6E5); display: flex; justify-content: center; align-items: flex-start; min-height: 100vh; margin: 0; padding: 32px 16px; }
        .card { background: #fff; max-width: 760px; width: 100%; border-radius: 16px; box-shadow: 0 8px 30px rgba(0,0,0,0.15); overflow: hidden; }
        .topbar { display: flex; justify-content: space-between; align-items: center; padding: 14px 20px; border-bottom: 1px solid #eef2f7; }
        .link { color: #4d5b73; text-decoration: none; font-weight: 600; font-size: 14px; }
        .body { padding: 24px; }
        h2 { margin: 0 0 4px; color: #223; font-size: 24px; }
        .autor { color: #4d5b73; font-size: 15px; margin-bottom: 20px; }
        .meta-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 20px; }
        .meta-item label { display: block; font-size: 11px; text-transform: uppercase; color: #8a9ab5; font-weight: 700; margin-bottom: 3px; }
        .meta-item span { font-size: 14px; color: #223; font-weight: 500; }
        .descripcion { background: #f7f9fc; border-radius: 10px; padding: 14px; font-size: 14px; color: #445; margin-bottom: 20px; line-height: 1.6; }
        .reproduccions { font-size: 15px; color: #4d5b73; margin-top: 16px; }
        .reproduccions strong { color: #223; font-size: 20px; }
        .player-wrap { border-radius: 10px; overflow: hidden; background: #000; margin-bottom: 4px; }
        .success { background: #e6f9f0; color: #006400; border-radius: 8px; padding: 10px 14px; margin-bottom: 16px; font-weight: 500; font-size: 14px; }
        .error { background: #fdecea; color: #b00020; border-radius: 8px; padding: 10px 14px; margin-bottom: 16px; font-weight: 500; font-size: 14px; }
        .vjs-default-skin { width: 100% !important; height: 420px !important; }
    </style>
</head>
<body>
<%
    Map<String, String> video = (Map<String, String>) request.getAttribute("video");
    String id = request.getParameter("id") != null ? request.getParameter("id") : "";
    String mimeType = "video/mp4";
    if (video != null) {
        String fmt = video.getOrDefault("formato","").toLowerCase();
        if (fmt.contains("webm"))      mimeType = "video/webm";
        else if (fmt.contains("ogg")) mimeType = "video/ogg";
        else if (fmt.contains("avi")) mimeType = "video/x-msvideo";
    }
%>
<div class="card">
    <div class="topbar">
        <a class="link" href="<%= request.getContextPath() %>/busqueda">← Tornar a la cerca</a>
        <a class="link" href="<%= request.getContextPath() %>/logout">Tancar sessió</a>
    </div>
    <div class="body">
        <% if (request.getAttribute("error")   != null) { %><div class="error"><%= request.getAttribute("error") %></div><% } %>
        <% if (request.getAttribute("success") != null) { %><div class="success"><%= request.getAttribute("success") %></div><% } %>

        <% if (video != null) { %>
        <h2><%= video.getOrDefault("titulo","") %></h2>
        <p class="autor">Autor: <strong><%= video.getOrDefault("autor","") %></strong></p>

        <div class="meta-grid">
            <div class="meta-item"><label>Data de creació</label><span><%= video.getOrDefault("fechaCreacion","") %></span></div>
            <div class="meta-item"><label>Duració</label><span><%= video.getOrDefault("duracion","0") %> s</span></div>
            <div class="meta-item"><label>Format</label><span><%= video.getOrDefault("formato","") %></span></div>
            <div class="meta-item"><label>Resolució</label><span><%= video.getOrDefault("resolucion","") %></span></div>
            <div class="meta-item"><label>Categoria</label><span><%= video.getOrDefault("categoria","") %></span></div>
        </div>

        <div class="descripcion"><%= video.getOrDefault("descripcion","") %></div>

        <div class="player-wrap">
            <video id="video-player"
                   class="video-js vjs-default-skin vjs-big-play-centered"
                   controls preload="metadata"
                   data-video-id="<%= id %>"
                   data-ctx="<%= request.getContextPath() %>">
                <source src="<%= video.getOrDefault("url","") %>" type="<%= mimeType %>" />
                <p class="vjs-no-js">Per reproduir el vídeo cal activar JavaScript o usar un navegador compatible amb HTML5.</p>
            </video>
        </div>

        <div class="reproduccions">Reproduccions: <strong id="repro-count"><%= video.getOrDefault("reproducciones","0") %></strong></div>

        <% } else if (request.getAttribute("error") == null) { %>
        <p style="color:#4d5b73;">Vídeo no trobat.</p>
        <% } %>
    </div>
</div>

<script src="https://vjs.zencdn.net/8.10.0/video.min.js"></script>
<script>
    (function () {
        var playerEl = document.getElementById('video-player');
        if (!playerEl) return;

        var player = videojs('video-player', { fluid: false });
        var ctx = playerEl.dataset.ctx;
        var vid = playerEl.dataset.videoId;

        player.one('play', function () {
            fetch(ctx + '/reproduccion', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: 'id=' + encodeURIComponent(vid)
            })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                var el = document.getElementById('repro-count');
                if (el && data.reproducciones !== undefined) {
                    el.textContent = data.reproducciones;
                }
            })
            .catch(console.error);
        });
    })();
</script>
</body>
</html>
