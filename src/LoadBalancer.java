
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Arrays;
import java.util.HashMap;


public class LoadBalancer {
    private final byte n_servers = 3;
    private final short port = 8888;
    private ServerSocket lb;
    public AtomicInteger n_clients;

    private WebServer[] servers;
    private HashMap<Byte, AtomicInteger> alives;
    private Queue<Socket> clients;


    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[41m";

    public LoadBalancer() throws IOException {
        this.n_clients = new AtomicInteger(0);
        this.lb = new ServerSocket(this.port);
        this.servers = new WebServer[this.n_servers];
        this.clients = new LinkedList<>();
        this.alives = new HashMap<Byte, AtomicInteger>();
    }


    public byte getLeastConnection() {
        int min = Integer.MAX_VALUE;
        byte i = 0;

        for(Byte key : alives.keySet()) {
            AtomicInteger value = alives.get(key);
            if(value.get() < min) {
                min = value.get();
                i = key;
            }
        }
        return i;
    }
    

   
    public void init() {

        while (true) {
            try {
                // Aceito a conexão
                Socket client = this.lb.accept();
                this.n_clients.incrementAndGet();

                // Adiciono na fila
                this.clients.add(client);

                // Least Connection algorithm
                byte server_idx = this.getLeastConnection();
                WebServer finalServer = this.servers[server_idx];
                AtomicInteger connCount = this.alives.get(server_idx);

                // Client é tratado
                System.out.println("\t - [!] Um novo cliente se conectou");
                System.out.println(ANSI_RED + "[@] - Clientes simultâneo: " + this.n_clients + ANSI_RESET);
                new Thread(new ClientHandling(this.clients.poll(), this.n_clients, finalServer, connCount)).start();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
    }

    public void startServers() throws IOException {
        short localports = 3000;
        for (short i = 0; i < this.n_servers; i++) {
            this.servers[i] = new WebServer((short)(localports + i));
            this.alives.put((byte)i, new AtomicInteger(0));
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
        private AtomicInteger connectionCount;
        private WebServer proxy;

        public ClientHandling(Socket _socket, AtomicInteger _n, WebServer _proxy, AtomicInteger _connectionAcount) {
            this.client = _socket;
            this.n = _n;
            this.proxy = _proxy;
            this.connectionCount = _connectionAcount;
        }

        public void run() {
            this.connectionCount.incrementAndGet();
            try (InputStream clientInput = this.client.getInputStream();
                    BufferedReader cin = new BufferedReader(new InputStreamReader(clientInput));
                    PrintWriter cout = new PrintWriter(this.client.getOutputStream(), true)) {

                try (Socket connection = new Socket(this.proxy.getHost(), this.proxy.port);
                        OutputStream out = connection.getOutputStream();
                        InputStream in = connection.getInputStream();
                        PrintWriter writer = new PrintWriter(out, true);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

                    String path = cin.readLine().split(" ")[1];

                    String GET_REQUEST = "GET " + path + " HTTP/1.1\r\nHost: " + this.proxy.getHost() + "\r\n\r\n";

                    // System.out.println(GET_REQUEST);
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
                    System.out.println("\t[!] Um cliente se desconectou");
                    this.client.close();
                    this.n.decrementAndGet();
                    this.connectionCount.decrementAndGet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}