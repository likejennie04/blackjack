package backend;

import java.net.*;
import java.io.*;
import java.util.*;

public class BlackjackServer {
    // Tracks player connection handlers, gameplay state, and chosen avatars together
    private static Map<PrintWriter, RemotePlayerData> table = new HashMap<>();
    private static Deck sharedDeck = new Deck();
    private static List<Card> dealerHand = new ArrayList<>(); 

    // Dynamic sequencing components tracking chronological turn order list states
    private static List<PrintWriter> turnOrderList = new ArrayList<>();
    private static int currentTurnIndex = 0;

    public static void main(String[] args) throws IOException {
        sharedDeck.shuffle();
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("Blackjack Arena started on port 8888...");

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket)).start();
        }
    }

    // Composite metadata wrapper to link individual networking parameters safely
    static class RemotePlayerData {
        public Player player;
        public int avatarId = 0;
        
        public RemotePlayerData() {
            this.player = new Player();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private RemotePlayerData data;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.data = new RemotePlayerData();
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                synchronized (table) { table.put(out, data); }

                String input;
                while ((input = in.readLine()) != null) {
                    
                    if (input.startsWith("NAME_REGISTER:")) {
                        String name = input.substring(14).trim();
                        data.player.setName(name);
                        System.out.println("Player registered: " + name);
                        broadcast(name + " has joined the table.");
                        sendRosterSync(); // Synchronizes graphical client lineup panels
                        continue;
                    }

                    if (input.startsWith("AVATAR_UPDATE:")) {
                        try {
                            int id = Integer.parseInt(input.substring(14).trim());
                            data.avatarId = id;
                            sendRosterSync(); // Synchronizes graphical client lineup panels
                        } catch (Exception e) {}
                        continue;
                    }

                    if (input.equalsIgnoreCase("START_COMMAND")) {
                        synchronized (table) {
                            if (table.isEmpty()) {
                                out.println("[Arena]: Error: No players available to start!");
                                continue;
                            }
                            
                            sharedDeck.shuffle();
                            dealerHand.clear();
                            turnOrderList.clear();
                            currentTurnIndex = 0;

                            // 1. Deal cards to House Dealer
                            Card d1 = sharedDeck.dealCard();
                            Card d2 = sharedDeck.dealCard();
                            dealerHand.add(d1);
                            dealerHand.add(d2);
                            
                            broadcast("DEALER_INFO: " + d1 + ", [Hidden Card]");

                            // 2. Refresh active hands and provision fresh card sets to all connections
                            for (Map.Entry<PrintWriter, RemotePlayerData> entry : table.entrySet()) {
                                PrintWriter writer = entry.getKey();
                                Player p = entry.getValue().player;
                                p.reset(); 
                                
                                Card c1 = sharedDeck.dealCard();
                                Card c2 = sharedDeck.dealCard();
                                p.addCard(c1);
                                p.addCard(c2);
                                
                                writer.println("[Arena]: GAME_STARTED");
                                writer.println("[Arena]: PLAYER_CARDS: " + c1 + ", " + c2);
                                
                                // Insert connection into sequence list to manage order
                                turnOrderList.add(writer);
                            }
                            
                            // Send layout synchronizer block updates before changing turn values
                            sendRosterSync();
                            
                            // Broadcast name of initial user up to choose an action
                            if (!turnOrderList.isEmpty()) {
                                RemotePlayerData firstPlayer = table.get(turnOrderList.get(0));
                                broadcast("CURRENT_TURN:" + firstPlayer.player.getName());
                            }
                        }
                        System.out.println("Game started. Sequence order routing initialized.");
                        continue;
                    }

                    if (input.equalsIgnoreCase("hit")) {
                        // Turn Protection Guard: Restricts actions if it's not this client's turn
                        if (turnOrderList.isEmpty() || turnOrderList.size() <= currentTurnIndex || turnOrderList.get(currentTurnIndex) != out) {
                            out.println("[Arena]: It is not your turn!");
                            continue;
                        }

                        Player p = data.player;
                        p.addCard(sharedDeck.dealCard());
                        int score = p.getScore();
                        
                        if (score > 21) {
                            broadcast("💥 " + p.getName() + " Busted with: " + p.toString());
                            out.println("[Arena]: You Busted! Game Over.");
                            advanceTurn(); // Shift turn order automatically following a bust
                        } else {
                            out.println("[Arena]: PLAYER_CARDS: " + p.toString());
                            broadcast(p.getName() + " hit a card.");
                        }
                    } 
                    else if (input.equalsIgnoreCase("stand")) {
                        // Turn Protection Guard: Restricts actions if it's not this client's turn
                        if (turnOrderList.isEmpty() || turnOrderList.size() <= currentTurnIndex || turnOrderList.get(currentTurnIndex) != out) {
                            out.println("[Arena]: It is not your turn!");
                            continue;
                        }

                        broadcast(data.player.getName() + " stands with: " + data.player.getScore());
                        advanceTurn(); // Shift turn order down to next participant
                    }
                }
            } catch (IOException e) {
                System.out.println("Player disconnected.");
            } finally {
                synchronized (table) { table.remove(out); }
                sendRosterSync();
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    // Sequentially moves the active round handle down to the next participant index
    private static void advanceTurn() {
        currentTurnIndex++;
        if (currentTurnIndex < turnOrderList.size()) {
            PrintWriter nextClient = turnOrderList.get(currentTurnIndex);
            RemotePlayerData nextData = table.get(nextClient);
            if (nextData != null && nextData.player.getName() != null) {
                broadcast("CURRENT_TURN:" + nextData.player.getName());
            } else {
                advanceTurn(); // Handle empty tracks gracefully by skipping ahead
            }
        } else {
            // Hand ultimate game turn execution priority over to House elements
            broadcast("CURRENT_TURN:Dealer (House)");
            broadcast("Dealer is playing out their hand...");
        }
    }

    // Packages active user collections into structural transmission streams
    private static void sendRosterSync() {
        StringBuilder sb = new StringBuilder("ROSTER_UPDATE:");
        synchronized (table) {
            for (RemotePlayerData pData : table.values()) {
                String name = pData.player.getName();
                if (name == null || name.isEmpty()) name = "Anonymous";
                sb.append(name).append(",").append(pData.avatarId).append(";");
            }
        }
        broadcast(sb.toString());
    }

    private static void broadcast(String message) {
        synchronized (table) {
            for (PrintWriter writer : table.keySet()) {
                writer.println("[Arena]: " + message);
            }
        }
    }
}