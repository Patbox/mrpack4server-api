package eu.pb4.mrpackserverapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import eu.pb4.mrpackserverapi.handlers.AssetsHandle;
import eu.pb4.mrpackserverapi.handlers.DownloadHandle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;


public class Main {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static void main(String[] args) throws Throwable {
        var configPath = Paths.get("./config.json");
        var config = new Config();

        if (Files.exists(configPath)) {
            Logger.info("Config found! Loading it now!", config.address, config.port);
            config = GSON.fromJson(Files.readString(configPath), Config.class);
        }

        Files.writeString(configPath, GSON.toJson(config));
        Logger.info("Locating jars...");

        //var jars = new HashMap<>();
        var jarPath = Paths.get("./jars");

        if (!Files.exists(jarPath)) {
            Files.createDirectories(jarPath);
            Logger.warn("No jars found! Quitting...");
            return;
        }

        var map = new HashMap<String, byte[]>();

        Config finalConfig = config;
        Files.list(jarPath).filter(x -> x.toString().endsWith(".jar")).forEach(path -> {
            var parts = path.getFileName().toString().split("-", 3);
            int jvm;
            if (parts.length < 3) {
                jvm = finalConfig.defaultJvm;
            } else {
                jvm = Integer.parseInt(parts[2].substring("jvm".length(), parts[2].length() - ".jar".length()));
            }

            byte[] file;

            try {
                file = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Logger.info("Found %s (with java target %s)!", path.getFileName(), jvm);

            map.put("java" + jvm, file);
            map.put("jvm" + jvm, file);
            map.put("java-" + jvm, file);
            map.put("jvm-" + jvm, file);
            map.put("" + jvm, file);
        });

        map.put("", map.get("jvm8"));

        Logger.info("Starting a server at %s:%s!", config.address, config.port);
        var server = HttpServer.create(new InetSocketAddress(config.address, config.port), config.backlog);

        server.createContext("/download", new DownloadHandle(map));
        //server.createContext("/assets", new AssetsHandle());
        //server.createContext("/download", new DownloadHandle(map));
        server.start();
    }
}