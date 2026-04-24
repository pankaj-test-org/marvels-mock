package com.example.marvelmock;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class MarvelMockApplication {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Register character endpoint
        server.createContext("/v1/public/characters", new MarvelMockController());

        // Health check endpoint
        server.createContext("/health", exchange -> {
            String response = "{\"status\":\"UP\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        });

        server.setExecutor(null);
        server.start();

        System.out.println("Marvel Mock API started on port " + PORT);
        System.out.println("Available endpoints:");
        System.out.println("  - GET http://localhost:" + PORT + "/v1/public/characters");
        System.out.println("  - GET http://localhost:" + PORT + "/health");
    }
}

