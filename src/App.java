import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class App {
    private static WebServer[] servers;
    private static final int total_servers = 3;
    public static void main(String[] args) throws Exception {
    
      System.out.println("[@] Criando os servidores"); 
      servers = new WebServer[total_servers];

      for(int i = 0; i < total_servers; i++) {
        int port = 2000 + i;
        servers[i] = new WebServer(port);
        servers[i].start();
      }
        
    }
}
