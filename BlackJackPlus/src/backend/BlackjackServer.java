package backend;
import java.net.*;
import java.io.*;
import java.util.*;

public class BlackjackServer {
    private static Map<PrintWriter, Player> table = new HashMap<>();
    private static Deck sharedDeck = new Deck();
    private static final int MAX_PLAYERS = 4;

    public static void main(String[] args) throws IOException {
        sharedDeck.shuffle();
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("Blackjack Arena started on port 8888...");

        while (true) {
            Socket socket = serverSocket.accept();
            
            // Connection limit logic
            synchronized (table) {
                if (table.size() >= MAX_PLAYERS) {
                    PrintWriter tempOut = new PrintWriter(socket.getOutputStream(), true);
                    tempOut.println("[Arena]: ERROR_FULL");
                    socket.close();
                    continue;
                }
            }
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
                
                synchronized (table) { 
                    table.put(out, player); 
                    System.out.println("Player joined. Total: " + table.size());
                }

                String input;
                while ((input = in.readLine()) != null) {
                    
                    // --- Case 0: START_COMMAND (New Logic for Host) ---
                    if (input.equalsIgnoreCase("START_COMMAND")) {
                        synchronized (table) {
                            // Deal 2 initial cards to EVERYONE at the table
                            for (Map.Entry<PrintWriter, Player> entry : table.entrySet()) {
                                PrintWriter writer = entry.getKey();
                                Player p = entry.getValue();
                                
                                // Clear hand if it's a new round
                                p.reset();
                                
                                Card c1 = sharedDeck.dealCard();
                                Card c2 = sharedDeck.dealCard();
                                p.addCard(c1);
                                p.addCard(c2);
                                
                                // Notify each client to update their UI
                                writer.println("[Arena]: GAME_STARTED");
                                writer.println("[Arena]: Initial Cards: " + c1 + ", " + c2);
                                writer.println("[Arena]: Your current hand: " + p.toString());
                            }
                            System.out.println("Host started the game. Cards dealt to all.");
                        }
                    }
                    
                    // --- Case 1: HIT command ---
                    else if (input.equalsIgnoreCase("hit")) {
                        Card c = sharedDeck.dealCard();
                        player.addCard(c);
                        int score = player.getScore();
                        
                        if (score <= 21 && player.getHandStrings().length >= 5) {
                            broadcast("🏆 [5-Card Charlie] A player wins with 5 cards: " + player.toString());
                            out.println("[Arena]: Congratulations! You achieved 5-Card Charlie!");
                        } else if (score > 21) {
                            broadcast("A player Busted with: " + player.toString());
                            out.println("[Arena]: You Busted! Game Over.");
                        } else {
                            broadcast("A player hit: " + c.toString() + " | Hand: " + player.toString());
                        }
                    } 
                    
                    // --- Case 2: STAND command ---
                    else if (input.equalsIgnoreCase("stand")) {
                        int score = player.getScore();
                        broadcast("A player stands with: " + player.toString() + " (Score: " + score + ")");
                        out.println("[Arena]: You chose to Stand. Waiting for results...");
                    }
                }
            } catch (IOException e) {
                System.out.println("Player disconnected.");
            } finally {
                synchronized (table) { table.remove(out); }
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    private static void broadcast(String message) {
        synchronized (table) {
            for (PrintWriter writer : table.keySet()) {
                writer.println("[Arena]: " + message);
            }
        }
    }
}