package org.example.isdcmproject.controller;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = {"/videos", "/videos/*"})
public class AuthSessionFilter implements Filter {
    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        Object usuarioLogueado = session == null ? null : session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?auth=required");
            return;
        }
        chain.doFilter(request, response);
    }
}
