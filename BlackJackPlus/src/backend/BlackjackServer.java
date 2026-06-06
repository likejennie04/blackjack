package backend;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlackjackServer {
    private static final Map<PrintWriter, RemotePlayerData> table = new ConcurrentHashMap<>();
    private static final List<PrintWriter> turnOrderList = new CopyOnWriteArrayList<>();
    
    private static final Deck sharedDeck = new Deck();
    private static final List<Card> dealerHand = new ArrayList<>(); 
    
    private static int currentTurnIndex = 0;
    private static boolean isGameActive = false; 

    public static void main(String[] args) {
        sharedDeck.shuffle();
        
        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            System.out.println("Blackjack Arena started successfully!");
            System.out.println("👉 LAN IP for peers: " + getSystemIP() + " on port 8888\n");

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (BindException e) {
            System.err.println("\n⚠️ [SERVER ERROR]: Port 8888 bound.");
        } catch (IOException e) {
            System.err.println("General I/O Exception: " + e.getMessage());
        }
    }

    static class RemotePlayerData {
        public final Player player;
        public int avatarId = 0;
        public PrintWriter writer;
        
        public RemotePlayerData(PrintWriter writer) {
            this.writer = writer;
            this.player = new Player();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private RemotePlayerData data;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)
            ) {
                this.out = output;
                this.data = new RemotePlayerData(out);
                table.put(out, data);

                String input;
                while ((input = in.readLine()) != null) {
                    
                    if (input.startsWith("NAME_REGISTER:")) {
                        String name = input.substring(14).trim();
                        data.player.setName(name);
                        sendRosterSync();
                        continue;
                    }

                    if (input.startsWith("AVATAR_UPDATE:")) {
                        try {
                            data.avatarId = Integer.parseInt(input.substring(14).trim());
                            sendRosterSync(); 
                        } catch (NumberFormatException ignored) {}
                        continue;
                    }

                    if (input.equalsIgnoreCase("START_COMMAND")) {
                        handleStartGame();
                        continue;
                    }

                    if (input.equalsIgnoreCase("hit")) {
                        handleHitAction();
                        continue;
                    } 
                    
                    if (input.equalsIgnoreCase("stand")) {
                        handleStandAction();
                        continue;
                    }

                    if (input.equalsIgnoreCase("REQUEST_SNAPSHOT")) {
                        broadcastTableSnapshot();
                        continue;
                    }
                }
            } catch (IOException e) {
                System.out.println("Player left.");
            } finally {
                if (out != null) {
                    table.remove(out);
                    turnOrderList.remove(out);
                }
                sendRosterSync();
                if (isGameActive && !turnOrderList.isEmpty()) {
                    advanceTurn();
                }
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }

        private void handleStartGame() {
            if (table.isEmpty()) return;
            
            isGameActive = true;
            sharedDeck.shuffle();
            dealerHand.clear();
            turnOrderList.clear();
            currentTurnIndex = 0;

            dealerHand.add(sharedDeck.dealCard());
            dealerHand.add(sharedDeck.dealCard());

            for (Map.Entry<PrintWriter, RemotePlayerData> entry : table.entrySet()) {
                PrintWriter writer = entry.getKey();
                Player p = entry.getValue().player;
                p.reset(); 
                
                p.addCard(sharedDeck.dealCard());
                p.addCard(sharedDeck.dealCard());
                
               
                writer.println("[Arena]: GAME_STARTED");
                turnOrderList.add(writer);
            }
            
            
            broadcastTableSnapshot();
            sendRosterSync();
            
            if (!turnOrderList.isEmpty()) {
                PrintWriter firstClient = turnOrderList.get(0);
                RemotePlayerData firstPlayer = table.get(firstClient);
                broadcast("CURRENT_TURN:" + firstPlayer.player.getName());
                
                for (PrintWriter writer : table.keySet()) {
                    if (writer == firstClient) {
                        writer.println("[Arena]: UNLOCK_ACTIONS_FOR_CLIENT");
                    } else {
                        writer.println("[Arena]: LOCK_ACTIONS_FOR_CLIENT");
                    }
                }
            }
        }

        private void handleHitAction() {
            if (isNotCurrentTurn()) return;

            Player p = data.player;
            p.addCard(sharedDeck.dealCard());
            
         
            broadcastTableSnapshot();

            if (p.isFiveCardCharlie() || p.isBusted()) {
                advanceTurn(); 
            }
        }

        private void handleStandAction() {
            if (isNotCurrentTurn()) return;
            advanceTurn(); 
        }

        private boolean isNotCurrentTurn() {
            return !isGameActive || turnOrderList.isEmpty() || currentTurnIndex >= turnOrderList.size() || turnOrderList.get(currentTurnIndex) != out;
        }
    }

    private static void advanceTurn() {
        currentTurnIndex++;
        if (currentTurnIndex < turnOrderList.size()) {
            PrintWriter nextClient = turnOrderList.get(currentTurnIndex);
            RemotePlayerData nextData = table.get(nextClient);
            if (nextData != null && nextData.player.getName() != null) {
                broadcast("CURRENT_TURN:" + nextData.player.getName());
                for (PrintWriter writer : table.keySet()) {
                    if (writer == nextClient) {
                        writer.println("[Arena]: UNLOCK_ACTIONS_FOR_CLIENT");
                    } else {
                        writer.println("[Arena]: LOCK_ACTIONS_FOR_CLIENT");
                    }
                }
            } else {
                advanceTurn(); 
            }
        } else {
           
            isGameActive = false; 
            broadcast("CURRENT_TURN:Dealer");
            
            for (PrintWriter writer : table.keySet()) {
                writer.println("[Arena]: LOCK_ACTIONS_FOR_CLIENT");
            }

            int dealerScore = calculateDealerScore();
            while (dealerScore < 17) {
                dealerHand.add(sharedDeck.dealCard());
                dealerScore = calculateDealerScore();
            }

           
            broadcastTableSnapshot();
            
           
            evaluateFinalResults(dealerScore);
        }
    }

    
    private static void broadcastTableSnapshot() {
        StringBuilder sb = new StringBuilder("TABLE_SNAPSHOT:");
        
        // 1. Append Dealer Rows (If active game, hide first card)
        sb.append("Dealer (House)=");
        if (isGameActive && !dealerHand.isEmpty()) {
            sb.append("HIDDEN,");
            for (int i = 1; i < dealerHand.size(); i++) {
                sb.append(dealerHand.get(i)).append(",");
            }
        } else {
            for (Card c : dealerHand) sb.append(c).append(",");
        }
        sb.append(";");

        // 2. Append Active Remote Players Hand Assets
        for (RemotePlayerData pData : table.values()) {
            if (pData.player.getName() == null) continue;
            sb.append(pData.player.getName()).append("=");
            // Filter card strings directly without text metrics suffix
            String rawHand = pData.player.toString(); 
            if(rawHand.contains("(")) rawHand = rawHand.substring(0, rawHand.indexOf("(")).trim();
            sb.append(rawHand).append(";");
        }
        broadcast(sb.toString());
    }

    private static int calculateDealerScore() {
        int score = 0; int aces = 0;
        for (Card c : dealerHand) {
            String rank = c.toString().substring(0, c.toString().length() - 1).toUpperCase();
            if (rank.equals("A")) { aces++; score += 11; }
            else if (rank.equals("J") || rank.equals("Q") || rank.equals("K")) { score += 10; }
            else { try { score += Integer.parseInt(rank); } catch (Exception e) { score += 10; } }
        }
        while (score > 21 && aces > 0) { score -= 10; aces--; }
        return score;
    }

    private static void evaluateFinalResults(int dealerScore) {
        StringBuilder sb = new StringBuilder("SHOW_FINAL_SUMMARY: ");
        sb.append("■ HOUSE DEALER FINAL SCORE: ").append(dealerScore > 21 ? "Busted (Score 25)" : dealerScore).append(" | ");
        
        for (RemotePlayerData pData : table.values()) {
            Player p = pData.player;
            if (p.getName() == null) continue;
            
            int pScore = 0;
            String rawHand = p.toString();
            if (rawHand.contains("(")) {
                String scoreStr = rawHand.substring(rawHand.indexOf("(")+1, rawHand.indexOf(")")).trim();
                try { pScore = Integer.parseInt(scoreStr); } catch(Exception e) { pScore = 22; }
            } else {
                pScore = 18;
            }

            sb.append("• Player [").append(p.getName()).append("] Final Hand -> Status: ");
            if (pScore > 21) sb.append("LOSE ❌ (Busted)");
            else if (p.isFiveCardCharlie()) sb.append("WIN 🏆 (5-Card Charlie!)");
            else if (dealerScore > 21) sb.append("WIN 🏆 (House Busted)");
            else if (pScore > dealerScore) sb.append("WIN 🏆");
            else if (pScore < dealerScore) sb.append("LOSE ❌");
            else sb.append("PUSH 🤝 (Tie)");
            sb.append(" | ");
        }
        broadcast(sb.toString());
    }

    private static void sendRosterSync() {
        StringBuilder sb = new StringBuilder("ROSTER_UPDATE:");
        for (RemotePlayerData pData : table.values()) {
            String name = pData.player.getName();
            if (name == null || name.isEmpty()) name = "Anonymous";
            sb.append(name).append(",").append(pData.avatarId).append(";");
        }
        broadcast(sb.toString());
    }

    private static void broadcast(String message) {
        for (PrintWriter writer : table.keySet()) {
            writer.println(message.startsWith("[Arena]:") ? message : "[Arena]: " + message);
        }
    }
    
    public static String getSystemIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        if (!ip.startsWith("192.168.56.")) return ip;
                    }
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1"; 
    }
}