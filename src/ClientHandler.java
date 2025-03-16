import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private ArrayList<String> bannedWords;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public ClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = bufferedReader.readLine();
            this.bannedWords = Server.config.getBannedWords();

            while (isUsernameTaken(this.username)) {
                sendMessage("Username already taken. Please enter a different username:");
                this.username = bufferedReader.readLine();
            }
            addClient();
            sendWelcomeMessage();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()){
            try{
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient != null) {
                    processMessage(messageFromClient);
                } else {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public synchronized void addClient(){
        clientHandlers.add(this);
        broadcastSystemMessage("SERVER: "+ username+" has entered the chat!");
        sendClientList();
    }


    public synchronized void removeClient(){
        if (clientHandlers.contains(this)) {
            clientHandlers.remove(this);
            broadcastSystemMessage("SERVER: " + username + " has left the chat");
            sendClientList();
        }
    }

    public void processMessage(String message) throws IOException {
        if (message.startsWith("-msg ")) {
            String content = message.substring(5);
            int colonIndex = content.indexOf(':');
            if (colonIndex != -1) {
                String recipientsPart = content.substring(0, colonIndex).trim();
                String msgToSend = content.substring(colonIndex + 1).trim();
                String[] recipients = recipientsPart.split(",");
                broadcastMessage(msgToSend, recipients, false);
            } else {
                sendMessage("Invalid command format. Use: -msg user1,user2:Your message");
            }
        } else if (message.startsWith("-exclude ")) {
            String content = message.substring(9);
            int colonIndex = content.indexOf(':');
            if (colonIndex != -1) {
                String excludedUsersPart = content.substring(0, colonIndex).trim();
                String msgToSend = content.substring(colonIndex + 1).trim();
                String[] excludedUsers = excludedUsersPart.split(",");
                broadcastMessage(msgToSend, excludedUsers, true);
            } else {
                sendMessage("Invalid command format. Use: -exclude user1,user2:Your message");
            }
        } else if (message.equals("-banned")) {
            sendMessage("Banned phrases:");
            for (String phrase : bannedWords) {
                sendMessage("- " + phrase);
            }
        } else if (message.equals("-quit")) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        } else {
            broadcastMessage(message, null, false);
        }
    }


    public void broadcastMessage(String messageToSend, String[] users, boolean exclude){
        String bannedWord = getBannedWord(messageToSend);
        if (bannedWord != null) {
            try {
                sendMessage("Message was not sent because it contains a banned word: '" + bannedWord + "'");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }


        for (ClientHandler clientHandler : clientHandlers){
            try{
                if (!clientHandler.username.equals(username)){
                    boolean shouldSend = true;

                    if (users != null) {
                        boolean isRecipient = Arrays.asList(users).contains(clientHandler.username);
                        if (exclude) {
                            shouldSend = !isRecipient;
                        } else {
                            shouldSend = isRecipient;
                        }
                    }

                    if (shouldSend) {
                        clientHandler.sendMessage(username + ": " + messageToSend);
                    }
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public String getBannedWord(String message) {
        for (String bannedWord : bannedWords) {
            if (message.toLowerCase().contains(bannedWord.toLowerCase())) {
                return bannedWord;
            }
        }
        return null;
    }

    public void sendClientList() {
        StringBuilder users = new StringBuilder("Connected users: ");
        for (ClientHandler clientHandler : clientHandlers) {
            users.append(clientHandler.username).append(", ");
        }
        if (users.length() > 17) {
            String userList = users.substring(0, users.length() - 2);
            broadcastSystemMessage(userList);
        }
    }

    public void broadcastSystemMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers){
            try {
                clientHandler.sendMessage(message);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void sendMessage(String message) throws IOException {
        bufferedWriter.write(message);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

public void sendWelcomeMessage() throws IOException {
    sendMessage("Welcome to the chat, " + username + "!");
    sendMessage("Currently connected users:");

    for (ClientHandler clientHandler : clientHandlers) {
        sendMessage("- " + clientHandler.username);
    }

    sendMessage("#########################################################");
    sendMessage("Instructions:");
    sendMessage("Type your message and press Enter to send to everyone.");
    sendMessage("To send to specific users: -msg user1,user2:Your message");
    sendMessage("To exclude users: -exclude user1,user2:Your message");
    sendMessage("To get the list of banned phrases: -banned");
    sendMessage("To quit: -quit");
    sendMessage("#########################################################");

}

    public boolean isUsernameTaken(String username) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClient();
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
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}





