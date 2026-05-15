<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Cifrado de archivos</title>
    <style>
        * { box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #74ebd5, #ACB6E5);
            min-height: 100vh;
            margin: 0;
            padding: 32px 16px;
            display: flex;
            justify-content: center;
            align-items: flex-start;
        }
        .card {
            background: #fff;
            max-width: 720px;
            width: 100%;
            border-radius: 16px;
            box-shadow: 0 8px 30px rgba(0,0,0,0.15);
            overflow: hidden;
        }
        .topbar {
            display: flex;
            justify-content: space-between;
            padding: 14px 20px;
            border-bottom: 1px solid #eef2f7;
        }
        .link { color: #4d5b73; text-decoration: none; font-weight: 600; font-size: 14px; }
        .body { padding: 24px; }
        h2 { margin: 0 0 4px; color: #223; }
        .lead { color: #4d5b73; margin: 0 0 20px; font-size: 14px; }
        .panels { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
        .panel {
            border: 1px solid #eef2f7;
            border-radius: 12px;
            padding: 18px;
            background: #f7f9fc;
        }
        .panel h3 { margin: 0 0 12px; color: #223; font-size: 17px; }
        .field { margin-bottom: 12px; }
        .field label {
            display: block;
            font-size: 12px;
            text-transform: uppercase;
            color: #8a9ab5;
            font-weight: 700;
            margin-bottom: 6px;
        }
        .field input[type=file],
        .field input[type=password] {
            width: 100%;
            padding: 9px 10px;
            border: 1px solid #d6deea;
            border-radius: 8px;
            background: #fff;
            font-size: 14px;
        }
        .button {
            display: inline-block;
            width: 100%;
            padding: 10px 14px;
            border: none;
            border-radius: 8px;
            color: #fff;
            font-weight: 600;
            cursor: pointer;
            font-size: 14px;
        }
        .button.enc { background: #4caf82; }
        .button.dec { background: #5779d8; }
        .error {
            background: #fdecea;
            color: #b00020;
            border-radius: 8px;
            padding: 10px 14px;
            margin-bottom: 16px;
            font-weight: 500;
            font-size: 14px;
        }
        .hint {
            color: #6b7a92;
            font-size: 12px;
            margin-top: 8px;
            line-height: 1.5;
        }
        @media (max-width: 640px) {
            .panels { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<div class="card">
    <div class="topbar">
        <a class="link" href="<%= request.getContextPath() %>/busqueda">← Volver</a>
        <a class="link" href="<%= request.getContextPath() %>/logout">Cerrar sesión</a>
    </div>
    <div class="body">
        <h2>Cifrado / descifrado de archivos</h2>
        <p class="lead">AES-256-GCM con clave derivada por PBKDF2-HMAC-SHA256 (200 000 iteraciones).</p>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <div class="panels">
            <form class="panel" method="post" action="<%= request.getContextPath() %>/cifrado" enctype="multipart/form-data">
                <h3>Cifrar</h3>
                <input type="hidden" name="action" value="encrypt" />
                <div class="field">
                    <label>Archivo (vídeo, imagen, audio, PDF…)</label>
                    <input type="file" name="file" required />
                </div>
                <div class="field">
                    <label>Contraseña</label>
                    <input type="password" name="passphrase" required minlength="4" />
                </div>
                <button type="submit" class="button enc">Cifrar y descargar .enc</button>
                <p class="hint">El archivo cifrado incluye una cabecera con sal e IV aleatorios.</p>
            </form>

            <form class="panel" method="post" action="<%= request.getContextPath() %>/cifrado" enctype="multipart/form-data">
                <h3>Descifrar</h3>
                <input type="hidden" name="action" value="decrypt" />
                <div class="field">
                    <label>Archivo .enc</label>
                    <input type="file" name="file" required accept=".enc" />
                </div>
                <div class="field">
                    <label>Contraseña</label>
                    <input type="password" name="passphrase" required minlength="4" />
                </div>
                <button type="submit" class="button dec">Descifrar y descargar</button>
                <p class="hint">Si la contraseña no es correcta, el descifrado se rechaza por la etiqueta GCM.</p>
            </form>
        </div>
    </div>
</div>
</body>
</html>
