package backend;
import java.net.*;
import java.io.*;

public class BlackjackClient {
    public static void main(String[] args) {
        // Use port 8888 to match our customized ServerSocket
        String serverIP = "localhost";
        int port = 8888;

        try {
            Socket socket = new Socket(serverIP, port);
            System.out.println("Connected to the Blackjack Arena!");

            // BACKGROUND THREAD: Listening for broadcast messages from the server
            // This ensures we can receive updates while waiting for user input
            Thread listenerThread = new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

            // MAIN THREAD: Handling user input from the console
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            
            String userInput;
            System.out.print("Your move (hit/stand/exit): ");
            while ((userInput = stdIn.readLine()) != null) {
                // Allow user to quit the game gracefully
                if (userInput.equalsIgnoreCase("exit") || userInput.equalsIgnoreCase("quit")) {
                    System.out.println("Exiting arena...");
                    break;
                }
                out.println(userInput);
            }
            
            // Step 5: Close connection properly [cite: 52]
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Could not connect to server at " + serverIP + ":" + port);
        }
    }
}