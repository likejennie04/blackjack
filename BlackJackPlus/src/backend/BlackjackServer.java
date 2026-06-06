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
            System.out.println("👉 Tell your friends to connect to IP: " + getSystemIP() + " on port 8888\n");

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (BindException e) {
            System.err.println("\n⚠️ [SERVER ERROR]: Port 8888 is already bound!");
        } catch (IOException e) {
            System.err.println("General I/O Exception starting the server: " + e.getMessage());
        }
    }

    static class RemotePlayerData {
        public final Player player;
        public int avatarId = 0;
        
        public RemotePlayerData() {
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
                this.data = new RemotePlayerData();
                
                table.put(out, data);

                String input;
                while ((input = in.readLine()) != null) {
                    
                    if (input.startsWith("NAME_REGISTER:")) {
                        String name = input.substring(14).trim();
                        data.player.setName(name);
                        broadcast(name + " has joined the table.");
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
                }
            } catch (IOException e) {
                System.out.println("Player disconnected mapping sequence details.");
            } finally {
                if (out != null) {
                    table.remove(out);
                    turnOrderList.remove(out);
                }
                sendRosterSync();
                if (isGameActive && !turnOrderList.isEmpty()) {
                    checkAndAdvanceTurnOnDisconnect();
                }
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }

        private void handleStartGame() {
            if (table.isEmpty()) {
                out.println("[Arena]: Error: No players available to start!");
                return;
            }
            
            isGameActive = true;
            sharedDeck.shuffle();
            dealerHand.clear();
            turnOrderList.clear();
            currentTurnIndex = 0;

            Card d1 = sharedDeck.dealCard();
            dealerHand.add(d1);
            dealerHand.add(sharedDeck.dealCard());
            
            broadcast("DEALER_INFO: " + d1 + ", [Hidden Card]");

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
                
                turnOrderList.add(writer);
            }
            
            sendRosterSync();
            
            // 🎯 【核心修复点】开局时给第一个玩家精准发送解锁令牌
            if (!turnOrderList.isEmpty()) {
                PrintWriter firstClient = turnOrderList.get(0);
                RemotePlayerData firstPlayer = table.get(firstClient);
                
                // 广播当前是谁的回合（用于高亮顶部头像边框）
                broadcast("CURRENT_TURN:" + firstPlayer.player.getName());
                
                // 遍历所有连接，给第一名玩家发送解锁，其他人发送锁定
                for (PrintWriter writer : table.keySet()) {
                    if (writer == firstClient) {
                        writer.println("[Arena]: UNLOCK_ACTIONS_FOR_CLIENT");
                    } else {
                        writer.println("[Arena]: LOCK_ACTIONS_FOR_CLIENT");
                    }
                }
            }
            System.out.println("Game started safely with synchronized atomic tokens.");
        }

        private void handleHitAction() {
            if (isNotCurrentTurn()) {
                out.println("[Arena]: It is not your turn!");
                return;
            }

            Player p = data.player;
            p.addCard(sharedDeck.dealCard());
            
            if (p.isFiveCardCharlie()) {
                broadcast("PLAYER_CARDS: " + p.toString());
                broadcast("🌟 " + p.getName() + " achieved a 5-Card Charlie! Automatic Turn Pass.");
                advanceTurn();
            } 
            else if (p.isBusted()) {
                broadcast("PLAYER_CARDS: " + p.toString());
                broadcast("💥 " + p.getName() + " Busted with hand: " + p.toString());
                advanceTurn(); 
            } 
            else {
                broadcast("PLAYER_CARDS: " + p.toString());
                broadcast(p.getName() + " hit a card.");
            }
        }

        private void handleStandAction() {
            if (isNotCurrentTurn()) {
                out.println("[Arena]: It is not your turn!");
                return;
            }

            broadcast(data.player.getName() + " stands with: " + data.player.getScore());
            advanceTurn(); 
        }

        private boolean isNotCurrentTurn() {
            return !isGameActive || 
                   turnOrderList.isEmpty() || 
                   currentTurnIndex >= turnOrderList.size() || 
                   turnOrderList.get(currentTurnIndex) != out;
        }
    }

    private static void advanceTurn() {
        currentTurnIndex++;
        if (currentTurnIndex < turnOrderList.size()) {
            PrintWriter nextClient = turnOrderList.get(currentTurnIndex);
            RemotePlayerData nextData = table.get(nextClient);
            if (nextData != null && nextData.player.getName() != null) {
                broadcast("CURRENT_TURN:" + nextData.player.getName());
                
                // 🎯 换牌机制精准定向控制：给当前操作者解锁，其余人锁死
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
            broadcast("Dealer is playing out their hand...");
            
            // 庄家回合时锁定所有玩家的按钮
            for (PrintWriter writer : table.keySet()) {
                writer.println("[Arena]: LOCK_ACTIONS_FOR_CLIENT");
            }

            StringBuilder sb = new StringBuilder("DEALER_INFO: ");
            for(Card card : dealerHand) {
                sb.append(card).append(", ");
            }
            broadcast(sb.toString().trim());
        }
    }

    private static void checkAndAdvanceTurnOnDisconnect() {
        if (currentTurnIndex >= turnOrderList.size()) {
            currentTurnIndex = 0; 
            if (turnOrderList.isEmpty()) {
                isGameActive = false;
                return;
            }
        }
        PrintWriter currentActiveClient = turnOrderList.get(currentTurnIndex);
        RemotePlayerData data = table.get(currentActiveClient);
        if (data != null) {
            broadcast("CURRENT_TURN:" + data.player.getName());
            
            // 🎯 断开连接时，也要同步定向解锁新轮到的玩家
            for (PrintWriter writer : table.keySet()) {
                if (writer == currentActiveClient) {
                    writer.println("[Arena]: UNLOCK_ACTIONS_FOR_CLIENT");
                } else {
                    writer.println("[Arena]: LOCK_ACTIONS_FOR_CLIENT");
                }
            }
        } else {
            advanceTurn();
        }
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
            if (!message.startsWith("[Arena]:")) {
                writer.println("[Arena]: " + message);
            } else {
                writer.println(message);
            }
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
                        if (!ip.startsWith("192.168.56.") && !ip.startsWith("192.168.99.")) {
                            return ip; 
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1"; 
    }
}