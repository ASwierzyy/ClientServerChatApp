import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ServerSocket serverSocket;
    static ServerConfig config = new ServerConfig("src/serverConfig.txt");
    private ExecutorService executorService;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void startServer(){
        System.out.println(config.getServerName() + " has started on port: "+ config.getPort());
        try {
            while (!serverSocket.isClosed()){
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client is connected");
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                executorService.submit(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeServerSocket();
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
        }
    }

    public void closeServerSocket(){
        try {
            if (serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(config.getPort());
            Server server = new Server(serverSocket);
            server.startServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}