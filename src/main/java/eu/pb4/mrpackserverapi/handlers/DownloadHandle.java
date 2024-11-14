package eu.pb4.mrpackserverapi.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.StringTokenizer;

public class DownloadHandle implements HttpHandler {
    private final HashMap<String,byte[]> files;

    public DownloadHandle(HashMap<String,byte[]> files) {
        this.files = files;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Server", "mrpackserver-api");
        var path = exchange.getRequestURI().getPath();
        var reader = new StringTokenizer(path, "/");
        reader.nextToken();

        if (!reader.hasMoreTokens()) {
            handleError(exchange, "Modpack id and version definition is missing!");
            return;
        }

        var modpack = reader.nextToken();

        if (!reader.hasMoreTokens()) {
            handleError(exchange, "Modpack version definition is missing!");
            return;
        }

        var version = reader.nextToken();

        if (!reader.hasMoreTokens()) {
            handleError(exchange, "Java version is missing!");
            return;
        }

        var java = reader.nextToken();

        var base = this.files.get(java);

        if (base == null) {
            handleError(exchange, "Unsupported java version!");
            return;
        }

        if (!reader.hasMoreTokens()) {
            handleError(exchange, "Filename is missing!");
            return;
        }

        var filename = reader.nextToken();

        if (!filename.endsWith(".jar")) {
            handleError(exchange, "File name is not a jar!");
        }
        exchange.getResponseHeaders().add("Content-Type", "application/java-archive");

        var jsonData = ("{\"project_id\":\"" + modpack + "\", \"version_id\": \"" + version + "\"}").getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, jsonData.length + base.length);
        exchange.getResponseBody().write(jsonData);
        exchange.getResponseBody().write(base);
        exchange.close();
    }

    public static void handleError(HttpExchange exchange, String reason) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text");
        var bytes = reason.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(400, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
