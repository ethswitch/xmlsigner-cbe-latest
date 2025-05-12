package org.ips.xml.signer.xmlsigner.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class RestClientConfig {

    private static final List<String> SENSITIVE_KEYS = Arrays.asList(
            "password", "grant_type", "apiKey", "secret", "authorization"
    );

    @Value("${rest.client.connect-timeout:5}")
    private int connectTimeoutSeconds;

    @Value("${rest.client.read-timeout:60}")
    private int readTimeoutSeconds;

    @Bean

    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Create a buffered request factory
        ClientHttpRequestFactory factory =
                new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());

        return builder
                .requestFactory(() -> factory) // Use lambda for Supplier
                .setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .setReadTimeout(Duration.ofSeconds(readTimeoutSeconds))
                .additionalInterceptors(
                        new SanitizingInterceptor(),
                        new LoggingInterceptor()
                )
                .errorHandler(new RestTemplateErrorHandler())
                .build();
    }


    private static class SanitizingInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            // Sanitize request before execution
            byte[] cleanBody = sanitizeRequestBody(body).getBytes(StandardCharsets.UTF_8);
            return execution.execute(request, cleanBody);
        }

        private String sanitizeRequestBody(byte[] rawBody) {
            String body = new String(rawBody, StandardCharsets.UTF_8);
            // Handle both JSON and form-urlencoded
            for (String key : SENSITIVE_KEYS) {
                // JSON: "password":"value"
                body = body.replaceAll(
                        "(?i)(\"?" + key + "\"?[:=]\\s?[\"]?)([^\"]+)([\"]?)",
                        "$1*****$3"
                );
                // Form: password=value
                body = body.replaceAll(
                        "(?i)(" + key + "=)([^&]+)",
                        "$1*****"
                );
            }
            return body;
        }
    }

    private static class LoggingInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            if (log.isDebugEnabled()) {
                log.debug("Request: {} {}", request.getMethod(), request.getURI());
                log.debug("Headers: {}", sanitizeHeaders(request.getHeaders()));
            }

            ClientHttpResponse response = execution.execute(request, body);

            if (log.isDebugEnabled()) {
                log.debug("Response: {} {}", response.getStatusCode(), response.getStatusText());
                log.debug("Response Headers: {}", response.getHeaders());
            }
            return response;
        }

        private HttpHeaders sanitizeHeaders(HttpHeaders headers) {
            HttpHeaders cleanHeaders = new HttpHeaders();
            headers.forEach((key, values) -> {
                if (SENSITIVE_KEYS.contains(key.toLowerCase())) {
                    cleanHeaders.add(key, "*****");
                } else {
                    cleanHeaders.addAll(key, values);
                }
            });
            return cleanHeaders;
        }
    }

    public static class RestTemplateErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().isError();
        }

        @Override
        public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
            String errorMessage = String.format("Request to %s %s failed with %d %s",
                    method, url,
                    response.getStatusCode().value(),
                    response.getStatusText());

            String errorBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            log.error("{} - Response Body: {}", errorMessage, errorBody);
            throw new HttpClientErrorException(response.getStatusCode(), errorMessage);
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            this.handleError(null, null, response);
        }
    }
}