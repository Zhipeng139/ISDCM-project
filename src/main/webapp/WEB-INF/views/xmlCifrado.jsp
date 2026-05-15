<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Cifrado de XML (xmlsec)</title>
    <style>
        * { box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #74ebd5, #ACB6E5);
            min-height: 100vh; margin: 0; padding: 32px 16px;
            display: flex; justify-content: center; align-items: flex-start;
        }
        .card {
            background: #fff; max-width: 920px; width: 100%;
            border-radius: 16px; box-shadow: 0 8px 30px rgba(0,0,0,0.15);
            overflow: hidden;
        }
        .topbar {
            display: flex; justify-content: space-between;
            padding: 14px 20px; border-bottom: 1px solid #eef2f7;
        }
        .link { color: #4d5b73; text-decoration: none; font-weight: 600; font-size: 14px; }
        .body { padding: 24px; }
        h2 { margin: 0 0 4px; color: #223; }
        .lead { color: #4d5b73; margin: 0 0 20px; font-size: 14px; }
        .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
        .panel { border: 1px solid #eef2f7; border-radius: 12px; padding: 18px; background: #f7f9fc; }
        .panel h3 { margin: 0 0 12px; color: #223; font-size: 16px; }
        .field { margin-bottom: 12px; }
        .field label { display: block; font-size: 12px; text-transform: uppercase; color: #8a9ab5; font-weight: 700; margin-bottom: 6px; }
        .field input[type=file], .field input[type=password], .field input[type=text], .field select {
            width: 100%; padding: 9px 10px; border: 1px solid #d6deea;
            border-radius: 8px; background: #fff; font-size: 14px;
        }
        .actions { display: flex; flex-wrap: wrap; gap: 8px; }
        .button {
            display: inline-block; padding: 9px 12px; border: none; border-radius: 8px;
            color: #fff; font-weight: 600; cursor: pointer; font-size: 13px;
        }
        .button.enc { background: #4caf82; }
        .button.encDoc { background: #2c8f63; }
        .button.dec { background: #5779d8; }
        .button.ghost { background: #8a9ab5; }
        .error, .info {
            border-radius: 8px; padding: 10px 14px; margin-bottom: 16px;
            font-weight: 500; font-size: 14px;
        }
        .error { background: #fdecea; color: #b00020; }
        .info  { background: #e6f9f0; color: #006400; }
        pre.result {
            background: #0f1722; color: #d6e2f5; padding: 14px;
            border-radius: 10px; overflow: auto; max-height: 420px;
            font-size: 12.5px; line-height: 1.45; white-space: pre-wrap; word-break: break-word;
        }
        .hint { color: #6b7a92; font-size: 12px; margin-top: 8px; line-height: 1.5; }
        @media (max-width: 720px) { .grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body>
<div class="card">
    <div class="topbar">
        <a class="link" href="<%= request.getContextPath() %>/busqueda">← Volver</a>
        <a class="link" href="<%= request.getContextPath() %>/cifrado">Cifrado binario</a>
        <a class="link" href="<%= request.getContextPath() %>/logout">Cerrar sesión</a>
    </div>
    <div class="body">
        <h2>Cifrado de XML — Apache xmlsec (Santuario)</h2>
        <p class="lead">Cifra todo el documento o un elemento concreto (p. ej. <code>&lt;metadata&gt;</code>) usando <strong>AES-128 XML Encryption</strong>. La clave AES se deriva de la contraseña con SHA-256.</p>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <div class="grid">
            <form class="panel" method="post" action="<%= request.getContextPath() %>/xmlcifrado" enctype="multipart/form-data">
                <h3>Cifrar</h3>
                <div class="field">
                    <label>Origen del XML</label>
                    <select name="source">
                        <option value="upload">Subir archivo</option>
                        <option value="sample">Usar didlFilm1.xml (ejemplo Racó)</option>
                    </select>
                </div>
                <div class="field">
                    <label>Archivo XML (si subes uno)</label>
                    <input type="file" name="file" accept=".xml,application/xml" />
                </div>
                <div class="field">
                    <label>Elemento a cifrar (nombre local)</label>
                    <input type="text" name="element" value="metadata" placeholder="metadata" />
                </div>
                <div class="field">
                    <label>Contraseña</label>
                    <input type="password" name="passphrase" required minlength="4" />
                </div>
                <div class="actions">
                    <button type="submit" name="action" value="encryptElement" class="button enc">Cifrar elemento</button>
                    <button type="submit" name="action" value="encryptDocument" class="button encDoc">Cifrar documento</button>
                    <button type="submit" name="action" value="encryptElement" class="button ghost" formtarget="_self"
                            onclick="document.getElementById('inline-enc').value='1'">Ver en línea</button>
                    <input type="hidden" name="inline" id="inline-enc" value="" />
                </div>
                <p class="hint">"Cifrar elemento" sustituye el contenido del elemento por un <code>&lt;EncryptedData&gt;</code> (xmlenc).</p>
                <p class="hint"><a class="link" href="<%= request.getContextPath() %>/xmlcifrado?mode=preview" target="_blank">Ver didlFilm1.xml original</a></p>
            </form>

            <form class="panel" method="post" action="<%= request.getContextPath() %>/xmlcifrado" enctype="multipart/form-data">
                <h3>Descifrar</h3>
                <input type="hidden" name="source" value="upload" />
                <div class="field">
                    <label>Archivo XML cifrado</label>
                    <input type="file" name="file" accept=".xml,application/xml" required />
                </div>
                <div class="field">
                    <label>Contraseña</label>
                    <input type="password" name="passphrase" required minlength="4" />
                </div>
                <div class="actions">
                    <button type="submit" name="action" value="decrypt" class="button dec">Descifrar y descargar</button>
                    <button type="submit" name="action" value="decrypt" class="button ghost"
                            onclick="document.getElementById('inline-dec').value='1'">Ver en línea</button>
                    <input type="hidden" name="inline" id="inline-dec" value="" />
                </div>
                <p class="hint">Localiza todos los <code>&lt;EncryptedData&gt;</code> y los restituye al contenido original.</p>
            </form>
        </div>

        <% if (request.getAttribute("resultado") != null) { %>
            <div class="info">Resultado generado: <strong><%= request.getAttribute("resultadoNombre") %></strong></div>
            <pre class="result"><%= ((String) request.getAttribute("resultado"))
                    .replace("&","&amp;").replace("<","&lt;").replace(">","&gt;") %></pre>
        <% } %>
    </div>
</div>
</body>
</html>
