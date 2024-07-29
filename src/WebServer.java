
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer extends Thread {
    private int port;
    private ServerSocket socket;
    public int connectedClients;
    public WebServer(int _port) throws IOException {
        this.port = _port;
        this.socket = new ServerSocket(this.port);
        this.connectedClients = 0;
        System.out.println("- [!] Servidor criado em http://localhost:" + this.port);
    } 

    public int getPort() {
        return this.port;
    }

    public void run() {
        while(true) {
            try(Socket clientSocket = this.socket.accept()) {
                this.connectedClients++;
                System.out.println("- [*] " + this.port + ": novo cliente!");
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

}
