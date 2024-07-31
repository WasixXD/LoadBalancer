import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.net.InetSocketAddress;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.util.HashMap;

public class WebServer extends Thread {
    private HttpServer server;
    public short port;
    private final byte clientPerTime = 3;

    private HashMap<Long, Boolean> cache;

    public WebServer(short _port) throws IOException {
        this.port = _port;
        this.server = HttpServer.create(new InetSocketAddress(this.port), 0);

        this.server.createContext("/", new Hello(this.port));

        this.cache = new HashMap<Long, Boolean>();
        this.server.createContext("/prime/", new ComputePrime(this.port, this.cache));

        this.server.setExecutor(Executors.newFixedThreadPool(this.clientPerTime));

    }

    public String getHost() {
        return this.server.getAddress().getHostString();
    }

    public void start() {
        System.out.println("\t[%] Servidor iniciando em localhost:" + this.port);
        this.server.start();
    }


    static class Hello implements HttpHandler {
        private int port;

        public Hello(int _port ) {
            this.port = _port;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("\t\t[%] Servidor: " + this.port + " - Recebeu algo");
            String response = "Ola, esse e o servidor [" + this.port + "]\n";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class ComputePrime implements HttpHandler {
        private int port;
        private HashMap<Long, Boolean> cache;
        public ComputePrime(int _port, HashMap<Long, Boolean> _cache) {
            this.port = _port;
            this.cache = _cache;
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
                return false;
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
                
                
                boolean answer;
                // cacheando as respostas
                if(this.cache.get(primeToCompute) != null) {
                    answer = this.cache.get(primeToCompute);
                } else {
                    System.out.println("\t\t[%] Servidor: " + this.port + " - vai computar: " + primeLiteral);
                    answer = isPrime(primeToCompute);
                    this.cache.put(primeToCompute, answer);
                }
                response = String.format("["+ this.port +"]: " + "O numero %d -> %s\n", primeToCompute, answer ? "primo" : "nao primo");
                exchange.sendResponseHeaders(200, response.length());
            
                os.write(response.getBytes());
            }

            os.close();
        }

    }

}