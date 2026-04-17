<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Error</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f8f9fd;
            margin: 0;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        .card {
            max-width: 560px;
            width: 100%;
            background: #fff;
            border-radius: 12px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.12);
            padding: 24px;
        }
        h2 {
            margin-top: 0;
            color: #b00020;
        }
        .message {
            color: #334;
            line-height: 1.5;
            margin-bottom: 16px;
        }
        .link {
            color: #4ac1b8;
            text-decoration: none;
            font-weight: 600;
        }
    </style>
</head>
<body>
<div class="card">
    <h2>Se produjo un error</h2>
    <div class="message"><%= request.getAttribute("errorMessage") != null ? request.getAttribute("errorMessage") : "Inténtalo nuevamente." %></div>
    <a class="link" href="<%= request.getContextPath() %>/listadoVid">Volver al listado</a>
</div>
</body>
</html>
