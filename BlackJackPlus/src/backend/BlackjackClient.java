package backend;

import java.net.*;
import java.io.*;

public class BlackjackClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverIp;
    private int port;

    // CONSTRUCTOR: This is what your GUI windows are looking for!
    public BlackjackClient(String ip) {
        this.serverIp = ip;
        this.port = 8888;
        connectToServer();
    }

    private void connectToServer() {
        try {
            this.socket = new Socket(serverIp, port);
            System.out.println("Connected to the Blackjack Arena at " + serverIp);

            // Set up input and output streams for network communication
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException e) {
            System.err.println("Could not connect to server at " + serverIp + ":" + port);
            // Re-throw exception so the GUI window knows the connection failed
            throw new RuntimeException("Connection failed", e); 
        }
    }

    // Method to send moves (hit/stand) from your GUI buttons to the server
    public void sendMove(String move) {
        if (out != null) {
            out.println(move);
        }
    }

    // Method to let your OnlineGameWindow get the input stream to listen for server cards/scores
    public BufferedReader getInputStream() {
        return this.in;
    }

    // Gracefully close the connection when a player leaves
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Keeping your original main method here just in case you still want to run it via console
    public static void main(String[] args) {
        try {
            BlackjackClient client = new BlackjackClient("localhost");
            
            // Console listener loop fallback
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