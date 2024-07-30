
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer extends Thread {
    private int port;
    public ServerSocket socket;
    private final ExecutorService clientHandlePool;
    public int connectedClients;

    public WebServer(int _port) throws IOException {
        this.port = _port;
        this.socket = new ServerSocket(this.port);
        this.connectedClients = 0;
        this.clientHandlePool = Executors.newFixedThreadPool(3);
        System.out.println("- [!] Servidor criado em localhost: " + this.port);
    }

    public int getPort() {
        return this.port;
    }

    public int getClients() {
        return this.connectedClients;
    }

    public void start() {
        while (true) {
            try {
                Socket client = this.socket.accept();
                clientHandlePool.submit(() -> handle_client(client));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handle_client(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Processar dados do cliente
                System.out.println("\t - [*] " + this.getPort() + ": " + inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
