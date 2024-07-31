import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.net.InetSocketAddress;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class WebServer extends Thread {
    private HttpServer server;
    public int port;

    public WebServer(int _port) throws IOException {
        this.port = _port;
        this.server = HttpServer.create(new InetSocketAddress(this.port), 0);

        this.server.createContext("/", new Hello(this.port));
        this.server.createContext("/prime/", new ComputePrime(this.port));

        this.server.setExecutor(null);

    }

    public String getHost() {
        return this.server.getAddress().getHostString();
    }

    public void start() {
        this.server.start();
    }

    static class Hello implements HttpHandler {
        private int port;

        public Hello(int _port) {
            this.port = _port;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("\t - [!] Servidor: " + this.port + " - Recebeu algo");
            String response = "Ola, esse e o servidor [" + this.port + "]\n";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class ComputePrime implements HttpHandler {
        private int port;

        public ComputePrime(int _port) {
            this.port = _port;
        }

        public static boolean isLong(String literal) {
            if (literal.isEmpty() || literal == null) {
                return false;
            }

            try {
                Long.valueOf(literal);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public static boolean isPrime(long n) {
            if (n < 2)
                return true;
            for (int i = 2; i * i <= n; i++) {
                if (n % i == 0)
                    return false;
            }

            return true;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String primeLiteral = exchange.getRequestURI().getPath().split("/")[2];
            String response = "Erro";
            OutputStream os = exchange.getResponseBody();
            if (!isLong(primeLiteral)) {
                response = String.format("Erro: Nao foi possivel computar %s", primeLiteral);
                exchange.sendResponseHeaders(400, response.length());
                os.write(response.getBytes());
            } else {
                long primeToCompute = Long.valueOf(primeLiteral);

                System.out.println("Servidor: " + this.port + " - vai computar: " + primeLiteral);
                boolean answer = isPrime(primeToCompute);
                response = String.format("O numero %d -> %s", primeToCompute, answer ? "primo" : "nao primo");
                exchange.sendResponseHeaders(200, response.length());
                os.write(response.getBytes());
            }

            os.close();
        }

    }

}
