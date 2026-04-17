<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Registro</title>
    <style>
        /* Reset */
        * {
            box-sizing: border-box;
        }

        /* Layout */
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #74ebd5, #ACB6E5);
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }

        /* Card */
        .card {
            background: #fff;
            max-width: 500px;
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

        /* Form grid */
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

        .field input {
            width: 100%;
            padding: 10px 15px;
            border: 1px solid #ccc;
            border-radius: 10px;
            font-size: 14px;
            transition: border 0.3s, box-shadow 0.3s;
        }

        .field input:focus {
            border-color: #74ebd5;
            box-shadow: 0 0 5px rgba(116,235,213,0.5);
            outline: none;
        }

        /* Messages */
        .error {
            color: #b00020;
            margin-bottom: 12px;
            font-weight: 500;
        }

        /* Submit button */
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

        /* Footer link */
        p {
            margin-top: 20px;
            font-size: 14px;
            color: #555;
            text-align: center;
        }

        p a {
            color: #74ebd5;
            text-decoration: none;
            font-weight: 600;
        }

        p a:hover {
            color: #4ac1b8;
        }

        /* Mobile */
        @media (max-width: 480px) {
            .grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
<div class="card">
    <h2>Crear cuenta</h2>

    <% if (request.getAttribute("error") != null) { %>
    <div class="error"><%= request.getAttribute("error") %></div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/registroUsu">
        <div class="grid">
            <div class="field full">
                <label for="username">Usuario</label>
                <input id="username" name="username" type="text" value="${username}" />
            </div>
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
            <div class="field full">
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
