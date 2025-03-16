import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    ExecutorService executorService;

    public Client(Socket socket, String username){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            this.executorService = Executors.newVirtualThreadPerTaskExecutor();

            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);

        }
    }


    public void sendMessage() {
        try {
            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                if (messageToSend.equals("-quit")) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    public void listenForMessage(){
        executorService.submit(() -> {
            String msgFromGroupChat;

            while (socket.isConnected()){
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    if (msgFromGroupChat != null) {
                        System.out.println(msgFromGroupChat);
                    } else {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        });
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try {
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();
            }
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
            System.exit(0);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {

        Scanner ip = new Scanner(System.in);
        System.out.println("Enter IP adress: ");
        String ipAddress = ip.nextLine();

        Scanner port = new Scanner(System.in);
        System.out.println("Enter Port number: ");
        String portNum = port.nextLine();

        Scanner name = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = name.nextLine();

    try {
        Socket socket = new Socket(ipAddress, Integer.parseInt(portNum));
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    } catch (IOException e){
        System.out.println("Could not connect to the server at " + ipAddress + ":" + portNum);
        System.exit(0);
    }


    }


}
