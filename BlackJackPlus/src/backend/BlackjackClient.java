package backend;

import java.net.*;
import java.io.*;

public class BlackjackClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverIp;
    private int port;
    private int avatarId = 0; 
    private String playerName = "Anonymous";

    public BlackjackClient(String ip) {
        this.serverIp = ip;
        this.port = 8888;
        connectToServer();
    }

    public void setPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.playerName = name.trim();
            // If already connected, immediately sync name update to server
            if (out != null) {
                sendMove("NAME_REGISTER:" + this.playerName);
            }
        }
    }

    public String getPlayerName() {
        return this.playerName;
    }

    private void connectToServer() {
        try {
            this.socket = new Socket(serverIp, port);
            System.out.println("Connected to the Blackjack Arena at " + serverIp);

            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Auto-register initial identity tags upon established handshake connection
            sendMove("NAME_REGISTER:" + this.playerName);
            sendMove("AVATAR_UPDATE:" + this.avatarId);

        } catch (IOException e) {
            System.err.println("Could not connect to server at " + serverIp + ":" + port);
            throw new RuntimeException("Connection failed", e); 
        }
    }

    public void setAvatarId(int id) {
        this.avatarId = id;
        // If already connected, immediately sync avatar update to server
        if (out != null) {
            sendMove("AVATAR_UPDATE:" + this.avatarId);
        }
    }

    public int getAvatarId() {
        return this.avatarId;
    }

    public void sendMove(String move) {
        if (out != null) {
            out.println(move);
        }
    }

    public BufferedReader getInputStream() {
        return this.in;
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            BlackjackClient client = new BlackjackClient("localhost");
            client.setPlayerName("ConsoleTester");
            client.setAvatarId(2);
            
            Thread listenerThread = new Thread(() -> {
                try {
                    BufferedReader in = client.getInputStream();
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println("\n" + response);
                        System.out.print("Your move (hit/stand/exit or START_COMMAND): "); 
                    }
                } catch (IOException e) {
                    System.out.println("\n[System]: Connection to server lost.");
                }
            });
            listenerThread.start();

            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            System.out.print("Your move (hit/stand/exit or START_COMMAND): ");
            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.equalsIgnoreCase("exit") || userInput.equalsIgnoreCase("quit")) {
                    break;
                }
                client.sendMove(userInput);
            }
            client.disconnect();
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Console client failed to start.");
        }
    }
}