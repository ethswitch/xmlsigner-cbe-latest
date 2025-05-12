package org.ips.xml.signer.xmlsigner.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

import java.util.stream.Collectors;

@Component
public class RequestLoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        if ("POST".equalsIgnoreCase(request.getMethod())
                && request.getContentType() != null
                && request.getContentType().contains("application/x-www-form-urlencoded")) {

            String body = req.getReader().lines().collect(Collectors.joining());
            String masked = body.replaceAll("(?i)(password=)[^&]*", "$1******");
            System.out.println("Masked POST body: " + masked);
        }

        chain.doFilter(req, res);
    }
}
