package backend;
import java.net.*;
import java.io.*;
import java.util.*;

public class BlackjackServer {
    // Map to associate each player's output stream with their game state
    private static Map<PrintWriter, Player> table = new HashMap<>();
    private static Deck sharedDeck = new Deck();

    public static void main(String[] args) throws IOException {
        sharedDeck.shuffle();
        // Use port 8888 to avoid macOS system port conflicts (e.g., AirPlay on 5000)
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("Blackjack Arena started on port 8888...");

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket)).start();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private Player player;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.player = new Player();
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                // Register player in the shared game table
                synchronized (table) { table.put(out, player); }

                String input;
                // Main communication loop: reading commands from the client
                while ((input = in.readLine()) != null) {
                    
                    // --- Case 1: HIT command ---
                    if (input.equalsIgnoreCase("hit")) {
                        Card c = sharedDeck.dealCard();
                        player.addCard(c);
                        
                        int score = player.getScore();
                        // Check for "5-Card Charlie" (5 cards without busting)
                        if (score <= 21 && player.getHandStrings().length >= 5) {
                            broadcast("🏆 [5-Card Charlie] A player wins immediately with 5 cards: " + player.toString());
                            out.println("[Arena]: Congratulations! You achieved 5-Card Charlie!");
                        } 
                        // Check for "Bust"
                        else if (score > 21) {
                            broadcast("A player Busted with: " + player.toString());
                            out.println("[Arena]: You Busted! Game Over.");
                        } 
                        else {
                            broadcast("A player hit: " + c.toString() + " | Hand: " + player.toString());
                        }
                    } 
                    
                    // --- Case 2: STAND command ---
                    else if (input.equalsIgnoreCase("stand")) {
                        int score = player.getScore();
                        int count = player.getHandStrings().length;

                        if (score <= 21 && count >= 5) {
                            broadcast("🏆 [5-Card Charlie] A player stands and wins with 5 cards!");
                        } else {
                            broadcast("A player stands with: " + player.toString() + " (Score: " + score + ")");
                        }
                        out.println("[Arena]: You chose to Stand. Waiting for results...");
                    }
                }
            } catch (IOException e) {
                System.out.println("Player disconnected.");
            } finally {
                // Clean up: remove player from the table upon disconnection
                synchronized (table) { table.remove(out); }
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Helper method to send messages to all connected players in the arena.
     * This ensures everyone sees the game progress in real-time.
     */
    private static void broadcast(String message) {
        synchronized (table) {
            for (PrintWriter writer : table.keySet()) {
                writer.println("[Arena]: " + message);
            }
        }
    }
}