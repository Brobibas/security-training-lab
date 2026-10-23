import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class PhishingLab {

    public static void main(String[] args) throws IOException {

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8021")
        );

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port), 0
        );

        server.createContext("/", PhishingLab::showMessage);

        server.start();

        System.out.println("Lab server is running!");
        System.out.println("Open: http://localhost:" + port);
    }

    private static void showMessage(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.getResponseHeaders().set("Allow", "GET");
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>My Lab</title>

                    <style>
                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            font-family: Arial, sans-serif;
                            background: #f0f2f5;
                        }

                        .box {
                            background: white;
                            padding: 40px;
                            margin: 20px;
                            border-radius: 15px;
                            box-shadow: 0 4px 15px #cccccc;
                            text-align: center;
                        }

                        h1 {
                            color: #1877f2;
                            font-size: 32px;
                        }

                        p {
                            color: #333333;
                            font-size: 18px;
                        }
                    </style>
                </head>

                <body>
                    <div class="box">
                        <h1>Nanali biki moa</h1>
                        <p>Welcome to my lab!</p>
                    </div>
                </body>
                </html>
                """;

        sendResponse(exchange, 200, html);
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String html) throws IOException {

        byte[] response = html.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "text/html; charset=UTF-8"
        );

        exchange.getResponseHeaders().set(
                "X-Content-Type-Options", "nosniff"
        );

        exchange.sendResponseHeaders(statusCode, response.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
