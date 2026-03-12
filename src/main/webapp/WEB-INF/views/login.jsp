<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
    <style>
        /* Reset y estilo general */
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #74ebd5, #ACB6E5);
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }

        /* Tarjeta de login */
        .card {
            background: #fff;
            max-width: 400px;
            width: 100%;
            padding: 30px 25px;
            border-radius: 12px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.15);
            text-align: center;
        }

        .card h2 {
            margin-bottom: 20px;
            color: #333;
            font-size: 28px;
        }

        .field {
            margin-bottom: 16px;
            text-align: left;
            margin-right: 16px;
        }

        .field label {
            display: block;
            margin-bottom: 6px;
            font-weight: 600;
            color: #555;
        }

        .field input {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #ccc;
            border-radius: 8px;
            font-size: 14px;
            transition: border 0.3s, box-shadow 0.3s;
        }

        .field input:focus {
            border-color: #74ebd5;
            box-shadow: 0 0 5px rgba(116,235,213,0.5);
            outline: none;
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

        .actions {
            margin-top: 20px;
        }

        .actions button {
            width: 100%;
            padding: 12px;
            font-size: 16px;
            border: none;
            border-radius: 8px;
            background: #74ebd5;
            color: #fff;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.3s;
        }

        .actions button:hover {
            background: #4ac1b8;
        }

        p {
            margin-top: 20px;
            font-size: 14px;
            color: #555;
        }

        p a {
            color: #74ebd5;
            text-decoration: none;
            font-weight: 600;
            transition: color 0.3s;
        }

        p a:hover {
            color: #4ac1b8;
        }

        @media (max-width: 480px) {
            .card {
                padding: 25px 15px;
            }

            .card h2 {
                font-size: 24px;
            }
        }
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