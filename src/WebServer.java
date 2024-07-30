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

        this.server.createContext("/", new Handler(this.port));

        this.server.setExecutor(null);

    }

    public String getHost() {
        return this.server.getAddress().getHostString();
    }

    public void start() {
        this.server.start();
    }
    static class Handler implements HttpHandler {
        private int port;
        public Handler(int _port) {
            this.port = _port;
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Servidor: " + this.port + " - Recebeu algo");
            String response = "Ola, esse e o servidor [" + this.port + "]\n";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

   
}
