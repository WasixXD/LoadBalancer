
public class App {
    private static final int total_servers = 3;
    private static WebServer[] servers;

    public static void main(String[] args) throws Exception {

        System.out.println("[@] Criando os servidores");
        servers = new WebServer[total_servers];

        for (int i = 0; i < servers.length; i++) {
            int port = 2000 + i;
            servers[i] = new WebServer(port);
            new Thread(servers[i]::start).start();
        }

    }
}
