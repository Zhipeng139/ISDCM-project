<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String jwt = (String) session.getAttribute("jwt");
    String jwe = (String) session.getAttribute("jwe");
    String jwsSecret = (String) session.getAttribute("jwsSecret");
    String jweKey = (String) session.getAttribute("jweKey");
    String jweJwk = (String) session.getAttribute("jweJwk");
    String user = (String) session.getAttribute("usuarioLogueado");
    if (jwt == null) jwt = "";
    if (jwe == null) jwe = "";
    if (jwsSecret == null) jwsSecret = "";
    if (jweKey == null) jweKey = "";
    if (jweJwk == null) jweJwk = "";
%>
<!DOCTYPE html>
<html>
<head>
    <title>Mi token JWT</title>
    <style>
        * { box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #74ebd5, #ACB6E5);
            min-height: 100vh; margin: 0; padding: 32px 16px;
            display: flex; justify-content: center; align-items: flex-start;
        }
        .card { background: #fff; max-width: 760px; width: 100%;
            border-radius: 16px; box-shadow: 0 8px 30px rgba(0,0,0,0.15); overflow: hidden; }
        .topbar { display: flex; justify-content: space-between;
            padding: 14px 20px; border-bottom: 1px solid #eef2f7; }
        .link { color: #4d5b73; text-decoration: none; font-weight: 600; font-size: 14px; }
        .body { padding: 24px; }
        h2 { margin: 0 0 4px; color: #223; }
        .lead { color: #4d5b73; font-size: 14px; margin: 0 0 16px; }
        .token {
            background: #0f1722; color: #d6e2f5;
            padding: 14px; border-radius: 10px;
            font-family: ui-monospace, Menlo, Consolas, monospace;
            font-size: 12.5px; word-break: break-all; line-height: 1.5;
            user-select: all;
        }
        .row { display: flex; gap: 10px; margin-top: 14px; flex-wrap: wrap; }
        .button { padding: 10px 14px; border: none; border-radius: 8px;
            color: #fff; font-weight: 600; cursor: pointer; font-size: 14px;
            text-decoration: none; display: inline-block; }
        .button.primary { background: #4caf82; }
        .button.alt { background: #5779d8; }
        .button.ghost { background: #8a9ab5; }
        .hint { color: #6b7a92; font-size: 12.5px; margin-top: 16px; line-height: 1.6; }
        .field-label { font-size: 11px; text-transform: uppercase; color: #8a9ab5;
            font-weight: 700; margin-bottom: 6px; }
        details { margin-top: 18px; }
        summary { cursor: pointer; color: #4d5b73; font-weight: 600; }
        pre { background: #f7f9fc; padding: 10px; border-radius: 8px;
              font-size: 12px; overflow: auto; }
    </style>
</head>
<body>
<div class="card">
    <div class="topbar">
        <a class="link" href="<%= request.getContextPath() %>/listadoVid">Ir al listado</a>
        <a class="link" href="<%= request.getContextPath() %>/logout">Cerrar sesión</a>
    </div>
    <div class="body">
        <h2>Token JWT generado</h2>
        <p class="lead">Sesión iniciada como <strong><%= user %></strong>. Este token está firmado con HS256 por el backend (jjwt 0.7.0).</p>

        <% if (jwt.isEmpty()) { %>
            <p>No hay token en la sesión. Vuelve a iniciar sesión.</p>
        <% } else { %>
            <div class="token" id="tok"><%= jwt %></div>
            <div class="row">
                <button type="button" class="button primary" onclick="copyTok()">Copiar token</button>
                <a class="button alt" href="https://jwt.io/#debugger-io?token=<%= java.net.URLEncoder.encode(jwt, java.nio.charset.StandardCharsets.UTF_8) %>" target="_blank" rel="noopener">Abrir en jwt.io</a>
                <a class="button ghost" href="<%= request.getContextPath() %>/listadoVid">Continuar</a>
            </div>
            <p class="hint">En <a href="https://jwt.io/" target="_blank" rel="noopener">jwt.io</a> verás cabecera (<code>HS256</code>), payload (sub, apiKey, iat, exp) y la verificación de firma. Para validar la firma necesitas pegar el secreto del servidor.</p>
            <% if (!jwe.isEmpty()) { %>
                <h2 style="margin-top:28px">Token JWE (nested JWT, jose4j)</h2>
                <p class="lead">El mismo JWS anterior, envuelto con <strong>A128KW + A128CBC-HS256</strong>. El payload está cifrado: jwt.io no puede leerlo sin la clave AES.</p>
                <div class="token" id="jwe"><%= jwe %></div>
                <div class="row">
                    <button type="button" class="button primary" onclick="copyEl('jwe')">Copiar JWE</button>
                </div>
                <p class="hint">Cabecera (descifrable en jwt.io): <code>{"alg":"A128KW","enc":"A128CBC-HS256","cty":"JWT"}</code>. El segmento "cty=JWT" indica que el contenido descifrado es un JWS.</p>
            <% } %>

            <h2 style="margin-top:28px">Secretos del servidor</h2>
            <p class="lead">Solo para desarrollo / pruebas. Sirven para validar firma en jwt.io y descifrar el JWE.</p>

            <div class="field-label">Secreto HS256 (verifica el JWS en jwt.io)</div>
            <div class="token" id="jwsSecret"><%= jwsSecret %></div>
            <div class="row"><button type="button" class="button primary" onclick="copyEl('jwsSecret')">Copiar secreto JWS</button></div>

            <div class="field-label" style="margin-top:14px">Clave AES-128 del JWE (base64url)</div>
            <div class="token" id="jweKey"><%= jweKey %></div>
            <div class="row"><button type="button" class="button primary" onclick="copyEl('jweKey')">Copiar clave JWE</button></div>

            <div class="field-label" style="margin-top:14px">Misma clave como JWK (pégalo en jwt.io para descifrar)</div>
            <div class="token" id="jweJwk"><%= jweJwk %></div>
            <div class="row"><button type="button" class="button primary" onclick="copyEl('jweJwk')">Copiar JWK</button></div>

            <details>
                <summary>Formato esperado del payload</summary>
                <pre>{
  "iss": "isdcm-backend",
  "sub": "&lt;username&gt;",
  "apiKey": "&lt;api-key&gt;",
  "iat": &lt;epoch&gt;,
  "exp": &lt;epoch + 3600&gt;
}</pre>
            </details>
        <% } %>
    </div>
</div>
<script>
    function copyTok() { copyEl('tok'); }
    function copyEl(id) {
        var t = document.getElementById(id).innerText;
        navigator.clipboard.writeText(t).then(function () {
            alert('Copiado.');
        });
    }
</script>
</body>
</html>
