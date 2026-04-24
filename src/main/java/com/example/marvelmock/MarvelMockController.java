package com.example.marvelmock;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MarvelMockController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        // Parse query parameters
        String name = getQueryParam(exchange.getRequestURI(), "name");

        // Build response
        String characterName = name != null ? name : "3-D Man";
        String jsonResponse = String.format(
            "{\"code\":200,\"status\":\"Ok\",\"data\":{\"results\":[{\"id\":1011334,\"name\":\"%s\",\"description\":\"A mock character from Marvel API\",\"modified\":\"2014-04-29T14:18:17-0400\"}]}}",
            characterName
        );

        sendResponse(exchange, 200, jsonResponse);
    }

    public String getQueryParam(URI uri, String paramName) {
        String query = uri.getQuery();
        if (query == null) {
            return null;
        }

        Map<String, String> params = new HashMap<>();
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && !pair[1].isEmpty()) {
                params.put(pair[0], pair[1]);
            }
        }
        return params.get(paramName);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}

