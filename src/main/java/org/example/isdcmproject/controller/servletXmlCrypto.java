package org.example.isdcmproject.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.example.isdcmproject.crypto.XmlCryptoUtil;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "servletXmlCrypto", urlPatterns = "/xmlcifrado")
@MultipartConfig(
        fileSizeThreshold = 256 * 1024,
        maxFileSize = 16L * 1024 * 1024,
        maxRequestSize = 20L * 1024 * 1024
)
public class servletXmlCrypto extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(servletXmlCrypto.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("preview".equals(req.getParameter("mode"))) {
            try (InputStream is = getServletContext().getResourceAsStream("/WEB-INF/samples/didlFilm1.xml")) {
                if (is == null) { resp.sendError(404); return; }
                resp.setContentType("application/xml; charset=UTF-8");
                is.transferTo(resp.getOutputStream());
            }
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/xmlCifrado.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = param(req, "action");
        String passphrase = req.getParameter("passphrase");
        String element = param(req, "element");
        if (element.isEmpty()) element = "metadata";
        String source = param(req, "source");

        if (passphrase == null || passphrase.isEmpty()) {
            forwardError(req, resp, "Indica la contraseña.");
            return;
        }
        if (action.isEmpty()) {
            forwardError(req, resp, "Selecciona una acción.");
            return;
        }

        byte[] xmlBytes;
        String filename;
        try {
            if ("sample".equals(source)) {
                try (InputStream is = getServletContext().getResourceAsStream("/WEB-INF/samples/didlFilm1.xml")) {
                    if (is == null) throw new IOException("Sample XML no disponible.");
                    xmlBytes = is.readAllBytes();
                }
                filename = "didlFilm1";
            } else {
                Part part = req.getPart("file");
                if (part == null || part.getSize() == 0) {
                    forwardError(req, resp, "Selecciona un archivo XML o usa el ejemplo didlFilm1.xml.");
                    return;
                }
                try (InputStream is = part.getInputStream()) {
                    xmlBytes = is.readAllBytes();
                }
                String submitted = part.getSubmittedFileName();
                filename = stripExtension(submitted == null || submitted.isBlank() ? "documento.xml" : submitted);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error leyendo XML", e);
            forwardError(req, resp, "No se pudo leer el archivo: " + e.getMessage());
            return;
        }

        try (InputStream in = new java.io.ByteArrayInputStream(xmlBytes)) {
            Document doc = XmlCryptoUtil.parse(in);
            String outName;
            switch (action) {
                case "encryptElement" -> {
                    XmlCryptoUtil.encryptElement(doc, element, passphrase);
                    outName = filename + "-enc-" + element + ".xml";
                }
                case "encryptDocument" -> {
                    XmlCryptoUtil.encryptDocument(doc, passphrase);
                    outName = filename + "-enc-doc.xml";
                }
                case "decrypt" -> {
                    XmlCryptoUtil.decrypt(doc, passphrase);
                    outName = filename + "-dec.xml";
                }
                default -> { forwardError(req, resp, "Acción no soportada: " + action); return; }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XmlCryptoUtil.write(doc, baos);
            byte[] out = baos.toByteArray();

            if ("1".equals(req.getParameter("inline"))) {
                req.setAttribute("resultado", new String(out, StandardCharsets.UTF_8));
                req.setAttribute("resultadoNombre", outName);
                req.getRequestDispatcher("/WEB-INF/views/xmlCifrado.jsp").forward(req, resp);
            } else {
                resp.setContentType("application/xml; charset=UTF-8");
                resp.setHeader("Content-Disposition", "attachment; filename=\"" + outName + "\"");
                resp.getOutputStream().write(out);
            }
        } catch (IllegalArgumentException e) {
            forwardError(req, resp, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en XML crypto", e);
            forwardError(req, resp, "Error procesando XML: " + e.getMessage());
        }
    }

    private void forwardError(HttpServletRequest req, HttpServletResponse resp, String msg) throws ServletException, IOException {
        req.setAttribute("error", msg);
        req.getRequestDispatcher("/WEB-INF/views/xmlCifrado.jsp").forward(req, resp);
    }

    private String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    private String stripExtension(String name) {
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) name = name.substring(slash + 1);
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
