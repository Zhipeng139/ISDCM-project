<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Registro</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .card { max-width: 520px; padding: 20px; border: 1px solid #ccc; border-radius: 8px; }
        .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
        .field { margin-bottom: 12px; }
        .field label { display: block; margin-bottom: 6px; }
        .field input { width: 100%; padding: 8px; box-sizing: border-box; }
        .full { grid-column: 1 / -1; }
        .error { color: #b00020; margin-bottom: 12px; }
        .actions { margin-top: 12px; }
    </style>
</head>
<body>
<div class="card">
    <h2>Registro de usuario</h2>
    <% if (request.getAttribute("error") != null) { %>
    <div class="error"><%= request.getAttribute("error") %></div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/registroUsu">
        <div class="grid">
            <div class="field">
                <label for="nombre">Nombre</label>
                <input id="nombre" name="nombre" type="text" value="${nombre}" />
            </div>
            <div class="field">
                <label for="apellido">Apellido</label>
                <input id="apellido" name="apellido" type="text" value="${apellido}" />
            </div>
            <div class="field full">
                <label for="email">Correo electrónico</label>
                <input id="email" name="email" type="text" value="${email}" />
            </div>
            <div class="field">
                <label for="username">Usuario</label>
                <input id="username" name="username" type="text" value="${username}" />
            </div>
            <div class="field">
                <label for="password">Contraseña</label>
                <input id="password" name="password" type="password" />
            </div>
            <div class="field full">
                <label for="confirmPassword">Confirmar contraseña</label>
                <input id="confirmPassword" name="confirmPassword" type="password" />
            </div>
        </div>
        <div class="actions">
            <button type="submit">Crear cuenta</button>
        </div>
    </form>

    <p>¿Ya tienes cuenta? <a href="<%= request.getContextPath() %>/login">Iniciar sesión</a></p>
</div>
</body>
</html>
