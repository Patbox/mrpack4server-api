package eu.pb4.mrpackserverapi.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class AssetsHandle implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (var input = AssetsHandle.class.getResourceAsStream(exchange.getRequestURI().getPath())) {
            if (input == null) {
                DownloadHandle.handleError(exchange, "File not found!");
                return;
            }

            var bytes = input.readAllBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }
    }
}
