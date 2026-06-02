package backend;

import java.net.*;
import java.io.*;
import java.util.*;

public class BlackjackServer {
    private static Map<PrintWriter, Player> table = new HashMap<>();
    private static Deck sharedDeck = new Deck();
    // To store Dealer's hand state
    private static List<Card> dealerHand = new ArrayList<>(); 

    public static void main(String[] args) throws IOException {
        sharedDeck.shuffle();
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
                
                synchronized (table) { table.put(out, player); }

                String input;
                while ((input = in.readLine()) != null) {
                    
                    if (input.startsWith("NAME_REGISTER:")) {
                        String name = input.substring(14);
                        player.setName(name);
                        System.out.println("Player registered: " + name);
                        broadcast(name + " has joined the table.");
                        continue;
                    }

                    // --- Updated Case 1: START_COMMAND with Dealer Logic ---
                    if (input.equalsIgnoreCase("START_COMMAND")) {
                        synchronized (table) {
                            if (table.size() < 2) {
                                out.println("[Arena]: Error: At least 2 players are required to start!");
                                continue;
                            }
                            
                            sharedDeck.shuffle();
                            dealerHand.clear();

                            // 1. Dealer deals to themselves first
                            Card d1 = sharedDeck.dealCard();
                            Card d2 = sharedDeck.dealCard();
                            dealerHand.add(d1);
                            dealerHand.add(d2);
                            
                            // Broadcast dealer info (Hide the second card according to rules)
                            broadcast("DEALER_INFO: " + d1 + ", [Hidden Card]");

                            // 2. Deal to each player in the table
                            for (Map.Entry<PrintWriter, Player> entry : table.entrySet()) {
                                PrintWriter writer = entry.getKey();
                                Player p = entry.getValue();
                                p.reset(); 
                                
                                Card c1 = sharedDeck.dealCard();
                                Card c2 = sharedDeck.dealCard();
                                p.addCard(c1);
                                p.addCard(c2);
                                
                                writer.println("[Arena]: GAME_STARTED");
                                // Specific tag for client to identify their own cards
                                writer.println("[Arena]: PLAYER_CARDS: " + c1 + ", " + c2);
                            }
                        }
                        System.out.println("Game started. Dealer and Players cards dealt.");
                        continue;
                    }

                    if (input.equalsIgnoreCase("hit")) {
                        Card c = sharedDeck.dealCard();
                        player.addCard(c);
                        int score = player.getScore();
                        
                        if (score > 21) {
                            broadcast("💥 " + player.getName() + " Busted with: " + player.toString());
                            out.println("[Arena]: You Busted! Game Over.");
                        } else {
                            // Update only the current player's hand display
                            out.println("[Arena]: PLAYER_CARDS: " + player.toString());
                            broadcast(player.getName() + " hit a card.");
                        }
                    } 
                    
                    else if (input.equalsIgnoreCase("stand")) {
                        broadcast(player.getName() + " stands with: " + player.getScore());
                        out.println("[Arena]: Waiting for dealer's turn...");
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