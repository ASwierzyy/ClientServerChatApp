import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ServerConfig {

    int port;
    String serverName;
    ArrayList<String> bannedWords;

    public ServerConfig(String configFile) {
        bannedWords = new ArrayList<>();
        loadConfig(configFile);
    }

    void loadConfig(String configFile){
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {

            String line;
            while ((line = br.readLine()) != null){
                if(line.startsWith("port=")){
                    port = Integer.parseInt(line.substring(5));
                } else if (line.startsWith("serverName=")) {
                    serverName = line.substring(11);
                } else if (line.startsWith("bannedWord=")) {
                    bannedWords.add(line.substring(11));
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        return port;
    }

    public String getServerName() {
        return serverName;
    }

    public ArrayList<String> getBannedWords() {
        return bannedWords;
    }
}
