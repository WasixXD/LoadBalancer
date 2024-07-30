
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.InputStream;

import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class LoadBalancer {
    private final int n_servers = 3;
    private final int port = 8888;
    private ServerSocket lb;
    public AtomicInteger n_clients;
    private WebServer[] servers;

    public LoadBalancer() throws IOException {
        this.n_clients = new AtomicInteger(0);
        this.lb = new ServerSocket(this.port);
        this.servers = new WebServer[this.n_servers];
    }


    public void printInfo() {
        System.out.print("\033[H\033[5J");
        System.out.flush();
        System.out.println("- [@] Ouvindo em> localhost:" + this.port);
        System.out.println("- [@] Número de clientes>" + this.n_clients.get());
        System.out.println("- [@] Número de Threads>" + Thread.activeCount());
    }
    public void init() {
    
        while(true) { 
            try {
                // this.printInfo();
                Socket client = this.lb.accept();
                this.n_clients.incrementAndGet();
                System.out.println("\t - [!] Um novo cliente se conectou");
                new Thread(new ClientHandling(client, this.n_clients, this.servers[0])).start();
            } catch(IOException e) {
                e.printStackTrace();
            } 
        }
    }


    public void startServers() throws IOException {
        for(int i = 0; i < this.n_servers; i++) {
            int localports = 3000;
            this.servers[i] = new WebServer(localports + i);
            new Thread(this.servers[i]::start).start();
        }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("[@] - Iniciando Load Balancer");

        LoadBalancer l = new LoadBalancer();

        l.startServers();
        l.init();
    }


    static class ClientHandling implements Runnable {
        private Socket client;
        private AtomicInteger n;
        private WebServer proxy;
        public ClientHandling(Socket _socket, AtomicInteger _n, WebServer _proxy) {
            this.client = _socket;
            this.n = _n;
            this.proxy = _proxy;
        }
        public void run() {
            try (InputStream clientInput = this.client.getInputStream();
                BufferedReader cin = new BufferedReader(new InputStreamReader(clientInput));
                PrintWriter cout = new PrintWriter(this.client.getOutputStream(), true)) {

                try (Socket connection = new Socket(this.proxy.getHost(), this.proxy.port);
                    OutputStream out = connection.getOutputStream();
                    InputStream in = connection.getInputStream();
                    PrintWriter writer = new PrintWriter(out, true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

                    if(connection.isConnected()) {
                        System.out.println("Tudo certo");
                    }
                    String GET_REQUEST = "GET / HTTP/1.1\r\nHost:" + this.proxy.getHost() +"\r\n\r\n";

                   
                    writer.println(GET_REQUEST);
                    connection.shutdownOutput();
            
                    String responseLine;
                    while ((responseLine = reader.readLine()) != null) {
                        cout.println(responseLine);
                    }
                    cout.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    System.out.println("\t - [!] Um cliente se desconectou");
                    this.client.close();
                    this.n.decrementAndGet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
             
    }

}
