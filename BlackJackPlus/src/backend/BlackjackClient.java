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

    public BlackjackClient(String ip) {
        this.serverIp = ip;
        this.port = 8888;
        connectToServer();
    }

    private void connectToServer() {
        try {
            this.socket = new Socket(serverIp, port);
            System.out.println("Connected to the Blackjack Arena at " + serverIp);

            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException e) {
            System.err.println("Could not connect to server at " + serverIp + ":" + port);
            throw new RuntimeException("Connection failed", e); 
        }
    }

    public void setAvatarId(int id) {
        this.avatarId = id;
        
    }

    // 新增：获取头像 ID
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
            
            Thread listenerThread = new Thread(() -> {
                try {
                    BufferedReader in = client.getInputStream();
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println("\n" + response);
                        System.out.print("Your move (hit/stand/exit): "); 
                    }
                } catch (IOException e) {
                    System.out.println("\n[System]: Connection to server lost.");
                }
            });
            listenerThread.start();

            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            System.out.print("Your move (hit/stand/exit): ");
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
} //lucan shit