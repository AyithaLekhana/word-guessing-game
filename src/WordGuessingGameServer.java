import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WordGuessingGameServer {

    private static final Map<String, WordGuessingGame> SESSIONS = new ConcurrentHashMap<>();
    private static final int PORT = 8080;
    private static final String STATIC_DIR = "public";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/new", new NewGameHandler());
        server.createContext("/api/guess", new GuessHandler());
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Word Guessing Game server running at http://localhost:" + PORT);
    }

    static class NewGameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String sessionId = UUID.randomUUID().toString();
            WordGuessingGame game = new WordGuessingGame();
            SESSIONS.put(sessionId, game);

            String json = buildStateJson(sessionId, game, null);
            sendJson(exchange, 200, json);
        }
    }

    static class GuessHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(exchange);
            String sessionId = extractJsonField(body, "sessionId");
            String letterStr = extractJsonField(body, "letter");

            WordGuessingGame game = sessionId == null ? null : SESSIONS.get(sessionId);
            if (game == null) {
                sendJson(exchange, 400, "{\"error\":\"Invalid or missing sessionId. Start a new game.\"}");
                return;
            }
            if (letterStr == null || letterStr.isEmpty()) {
                sendJson(exchange, 400, "{\"error\":\"Missing letter.\"}");
                return;
            }

            char letter = letterStr.trim().toLowerCase().charAt(0);
            WordGuessingGame.GuessResult result = game.guessLetter(letter);

            String json = buildStateJson(sessionId, game, result);
            sendJson(exchange, 200, json);
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }

            File file = new File(STATIC_DIR, requestPath).getCanonicalFile();
            File staticRoot = new File(STATIC_DIR).getCanonicalFile();

            if (!file.getPath().startsWith(staticRoot.getPath()) || !file.isFile()) {
                String notFound = "404 Not Found";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes());
                }
                return;
            }

            String contentType = guessContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String guessContentType(String fileName) {
            if (fileName.endsWith(".html")) return "text/html";
            if (fileName.endsWith(".css")) return "text/css";
            if (fileName.endsWith(".js")) return "application/javascript";
            return "application/octet-stream";
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        Scanner scanner = new Scanner(exchange.getRequestBody(), "UTF-8").useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private static String extractJsonField(String json, String field) {
        if (json == null) return null;
        String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static String buildStateJson(String sessionId, WordGuessingGame game, WordGuessingGame.GuessResult result) {
        StringBuilder wrong = new StringBuilder();
        for (char c : game.getWrongLetters()) {
            if (wrong.length() > 0) wrong.append(",");
            wrong.append("\"").append(c).append("\"");
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"sessionId\":\"").append(sessionId).append("\",");
        json.append("\"clue\":\"").append(escapeJson(game.getClue())).append("\",");
        json.append("\"masked\":\"").append(game.getMaskedWord()).append("\",");
        json.append("\"wrongLetters\":[").append(wrong).append("],");
        json.append("\"remainingAttempts\":").append(game.getRemainingAttempts()).append(",");
        json.append("\"maxAttempts\":").append(WordGuessingGame.MAX_ATTEMPTS).append(",");
        json.append("\"gameOver\":").append(game.isGameOver()).append(",");
        json.append("\"won\":").append(game.isWon()).append(",");

        if (game.isGameOver()) {
            json.append("\"secretWord\":\"").append(game.getSecretWord()).append("\",");
        } else {
            json.append("\"secretWord\":null,");
        }

        if (result != null) {
            json.append("\"guessValid\":").append(result.valid).append(",");
            json.append("\"guessCorrect\":").append(result.correct).append(",");
            json.append("\"message\":\"").append(escapeJson(result.message)).append("\"");
        } else {
            json.append("\"guessValid\":true,");
            json.append("\"guessCorrect\":false,");
            json.append("\"message\":\"New game started. Good luck!\"");
        }

        json.append("}");
        return json.toString();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}