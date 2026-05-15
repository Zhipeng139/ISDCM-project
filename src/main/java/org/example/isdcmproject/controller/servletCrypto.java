package org.example.isdcmproject.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.example.isdcmproject.crypto.CryptoUtil;

import javax.crypto.AEADBadTagException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "servletCrypto", urlPatterns = "/cifrado")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 512L * 1024 * 1024,
        maxRequestSize = 520L * 1024 * 1024
)
public class servletCrypto extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(servletCrypto.class.getName());
    private static final String ENC_SUFFIX = ".enc";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/cifrado.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        String passphrase = req.getParameter("passphrase");
        Part filePart = req.getPart("file");

        if (action == null || passphrase == null || passphrase.isEmpty()) {
            forwardWithError(req, resp, "Indica la acción y la contraseña.");
            return;
        }
        if (filePart == null || filePart.getSize() == 0) {
            forwardWithError(req, resp, "Selecciona un archivo.");
            return;
        }

        String original = sanitizeFilename(filePart.getSubmittedFileName());
        boolean encrypt = "encrypt".equals(action);
        String outName = encrypt ? original + ENC_SUFFIX
                : (original.endsWith(ENC_SUFFIX) ? original.substring(0, original.length() - ENC_SUFFIX.length()) : "descifrado_" + original);

        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + outName + "\"");

        char[] pass = passphrase.toCharArray();
        try (InputStream in = filePart.getInputStream();
             OutputStream out = resp.getOutputStream()) {
            if (encrypt) CryptoUtil.encrypt(in, out, pass);
            else         CryptoUtil.decrypt(in, out, pass);
        } catch (IOException e) {
            Throwable cause = e.getCause();
            if (cause instanceof AEADBadTagException) {
                LOGGER.log(Level.WARNING, "Descifrado fallido: tag GCM no coincide");
                if (!resp.isCommitted()) {
                    resp.reset();
                    forwardWithError(req, resp, "Contraseña incorrecta o archivo manipulado.");
                }
                return;
            }
            LOGGER.log(Level.SEVERE, "Error en cifrado/descifrado", e);
            if (!resp.isCommitted()) {
                resp.reset();
                forwardWithError(req, resp, "Error procesando el archivo: " + e.getMessage());
            }
        } finally {
            java.util.Arrays.fill(pass, '\0');
        }
    }

    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String message) throws ServletException, IOException {
        req.setAttribute("error", message);
        req.getRequestDispatcher("/WEB-INF/views/cifrado.jsp").forward(req, resp);
    }

    private String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) return "archivo";
        String name = raw.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        return name.replaceAll("[\\r\\n\"]", "_");
    }
}
