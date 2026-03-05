<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .card { max-width: 420px; padding: 20px; border: 1px solid #ccc; border-radius: 8px; }
        .field { margin-bottom: 12px; }
        .field label { display: block; margin-bottom: 6px; }
        .field input { width: 100%; padding: 8px; box-sizing: border-box; }
        .error { color: #b00020; margin-bottom: 12px; }
        .success { color: #006400; margin-bottom: 12px; }
        .actions { margin-top: 12px; }
    </style>
</head>
<body>
<div class="card">
    <h2>Iniciar sesión</h2>
    <% if (request.getAttribute("error") != null) { %>
    <div class="error"><%= request.getAttribute("error") %></div>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
    <div class="success"><%= request.getAttribute("success") %></div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/login">
        <div class="field">
            <label for="username">Usuario</label>
            <input id="username" name="username" type="text" value="${username}" />
        </div>
        <div class="field">
            <label for="password">Contraseña</label>
            <input id="password" name="password" type="password" />
        </div>
        <div class="actions">
            <button type="submit">Entrar</button>
        </div>
    </form>

    <p>¿No tienes cuenta? <a href="<%= request.getContextPath() %>/registroUsu">Registrarse</a></p>
</div>
</body>
</html>
