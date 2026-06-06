package gui;

import javax.swing.*;
import backend.Computer;
import backend.Player;
import backend.House;
import backend.Card; 
import java.util.ArrayList;

public class FinalBoardPrint {

    /**
     * 🎯 离线单机版原有方法（100% 完全保留，一字不改，确保你离线模式完美运行）
     */
    public static String showSummary(JFrame parentFrame, House dealer, 
                                     ArrayList<Player> playerList, 
                                     ArrayList<Computer> aiList, 
                                     String gameMode,
                                     GameWindow gameWindow) { 
        
        int dealerScore = dealer.getScore();
        StringBuilder resultsBanner = new StringBuilder(); 
        StringBuilder dialogMessage = new StringBuilder();
        
        dialogMessage.append("=== THE END ===\n\n");
        dialogMessage.append(String.format("Dealer (House) Score: %d %s\n", 
            dealerScore, (dealerScore > 21 ? "BUSTED (lose)" : "")));
        dialogMessage.append("--------------------------------------------------\n");

        for (int i = 0; i < playerList.size(); i++) {
            Player p = playerList.get(i); 
            int score = p.getScore(); 
            String outcome = "";

            resultsBanner.append("P").append(i + 1).append(": "); 
            if (score > 21) {
                resultsBanner.append("Bust");
                outcome = " BUSTED (Lose)";
            } else if (p.getHandStrings().length == 5 && score <= 21) {
                resultsBanner.append("5-Card Win");
                outcome = " WIN (5-Card Charlie!)";
            } else if (dealerScore > 21) {
                resultsBanner.append("Win");
                outcome = " WIN (Dealer Busted)";
            } else if (score > dealerScore) {
                resultsBanner.append("Win");
                outcome = " WIN (Higher Score)";
            } else if (score < dealerScore) {
                resultsBanner.append("Lose");
                outcome = " LOSE";
            } else {
                resultsBanner.append("Draw");
                outcome = " DRAW (Push)";
            } 
            
            if (i < playerList.size() - 1) resultsBanner.append(" | ");
            dialogMessage.append(String.format(" Player %d:  Score: %d  ->  %s\n", (i + 1), score, outcome));
        }

        if (gameMode.equals("COMPUTER")) {
            dialogMessage.append("--------------------------------------------------\n");
            for (int i = 0; i < aiList.size(); i++) {
                Computer ai = aiList.get(i);
                int score = ai.getScore();
                String outcome = "";

                if (score > 21) outcome = " BUSTED";
                else if (ai.getHandStrings().length == 5 && score <= 21) outcome = " WIN (5-Card Charlie!)";
                else if (dealerScore > 21 || score > dealerScore) outcome = " WIN";
                else if (score < dealerScore) outcome = " LOSE";
                else outcome = " DRAW";

                dialogMessage.append(String.format(" AI Bot %d:  Score: %d  ->  %s\n", (i + 2), score, outcome));
            }
        }
        SoundManager.playSummaryMusic();

        Object[] options = {"Restart", "Return"};
        
        int choice = JOptionPane.showOptionDialog(
            parentFrame, 
            dialogMessage.toString(), 
            "Game Results", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.INFORMATION_MESSAGE, 
            null, 
            options,
            options[0]
        );

        if (choice == JOptionPane.YES_OPTION) { 
            SoundManager.buttonOne();
            System.out.println("User chose to continue playing.");
            SoundManager.switchDefaultMusic(); 
            gameWindow.restartMatch(); 
            
        } else if (choice == JOptionPane.NO_OPTION || choice == JOptionPane.CLOSED_OPTION) { 
            System.out.println("Exiting application.");
            SoundManager.switchDefaultMusic();
            new BlackjackStartWindow(); 
            parentFrame.dispose(); 
        }

        return resultsBanner.toString();
    }

    /**
     * 🎯 【联机版独创重载点】
     * 专门负责在网络联机对局结束时，承接 OnlineGameConnector 发来的数据。
     */
    public static void showOnlineSummary(JFrame parentFrame, String snapshotData, 
                                         backend.BlackjackClient client, OnlineGameConnector connector) {
        
        House dealer = new House();
        ArrayList<Player> playerList = new ArrayList<>();
        
        // 拆包并反向填充卡牌实体
        String[] rows = snapshotData.split(";");
        for (String row : rows) {
            if (row.trim().isEmpty()) continue;
            String[] tokens = row.split("=");
            if (tokens.length < 2) continue;
            
            String name = tokens[0].trim();
            String[] cards = tokens[1].split(",");
            
            if (name.contains("Dealer")) {
                for (String c : cards) {
                    if(!c.trim().isEmpty() && !c.equalsIgnoreCase("HIDDEN")) {
                        // 🎯 修复点：调用解析器转换成 int 重新塞入对应的构造方法
                        Card targetCard = parseCardString(c.trim());
                        if(targetCard != null) dealer.addCard(targetCard); 
                    }
                }
            } else {
                Player mockPlayer = new Player();
                mockPlayer.setName(name);
                for (String c : cards) {
                    if(!c.trim().isEmpty()) {
                        Card targetCard = parseCardString(c.trim());
                        if(targetCard != null) mockPlayer.addCard(targetCard);
                    }
                }
                playerList.add(mockPlayer);
            }
        }

        SoundManager.playSummaryMusic();
        
        // 完全使用你离线单机版一模一样的排版划线设计
        StringBuilder dialogMessage = new StringBuilder();
        int dealerScore = dealer.getScore();
        dialogMessage.append("=== THE END (ONLINE) ===\n\n");
        dialogMessage.append(String.format("Dealer (House) Score: %d %s\n", 
            dealerScore, (dealerScore > 21 ? "BUSTED (lose)" : "")));
        dialogMessage.append("--------------------------------------------------\n");

        for (int i = 0; i < playerList.size(); i++) {
            Player p = playerList.get(i);
            int score = p.getScore();
            String outcome = "";
            if (score > 21) outcome = " BUSTED (Lose)";
            else if (p.getHandStrings().length == 5 && score <= 21) outcome = " WIN (5-Card Charlie!)";
            else if (dealerScore > 21) outcome = " WIN (Dealer Busted)";
            else if (score > dealerScore) outcome = " WIN (Higher Score)";
            else if (score < dealerScore) outcome = " LOSE";
            else outcome = " DRAW (Push)";
            
            dialogMessage.append(String.format(" Player %d [%s]:  Score: %d  ->  %s\n", (i + 1), p.getName(), score, outcome));
        }

        // 按钮选择拦截处理器：支持 Restart 与 Return
        Object[] options = {"Restart", "Return"};
        int choice = JOptionPane.showOptionDialog(
            parentFrame, 
            dialogMessage.toString(), 
            "Game Results", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.INFORMATION_MESSAGE, 
            null, 
            options,
            options[0]
        );

        if (choice == JOptionPane.YES_OPTION) { 
            SoundManager.buttonOne();
            SoundManager.switchDefaultMusic(); 
            client.sendMove("START_COMMAND"); 
        } else { 
            System.out.println("Exiting online match.");
            SoundManager.switchDefaultMusic();
            client.disconnect();
            parentFrame.dispose(); 
            new BlackjackStartWindow(); 
        }
    }

    /**
     * 🎯 【网络资产翻译器】
     * 将网络传来的 "2h", "10d", "As" 代码翻译成符合原设计规范的 int(rank) 和 int(suit) 并组装成实体
     */
    private static Card parseCardString(String cardCode) {
        try {
            cardCode = cardCode.trim().toLowerCase();
            if (cardCode.isEmpty() || cardCode.contains("hidden")) return null;

            String rankPart = cardCode.substring(0, cardCode.length() - 1);
            char suitPart = cardCode.charAt(cardCode.length() - 1);

           
            int rank;
            switch (rankPart) {
                case "a":  rank = 0; break;
                case "j":  rank = 10; break;
                case "q":  rank = 11; break;
                case "k":  rank = 12; break;
                default:
                    rank = Integer.parseInt(rankPart) - 1; 
                    break;
            }

            
            int suit;
            switch (suitPart) {
                case 's': suit = 0; break; // Spades
                case 'h': suit = 1; break; // Hearts
                case 'c': suit = 2; break; // Clubs
                case 'd': suit = 3; break; // Diamonds
                default:  suit = 0; break;
            }

            return new Card(rank, suit); 
        } catch (Exception e) {
            System.err.println("Failed to parse remote token: " + cardCode);
            return new Card(0, 0); 
        }
    }
}