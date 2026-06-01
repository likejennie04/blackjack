package gui;

import javax.swing.*;
import backend.Computer;
import backend.Player;
import backend.House;
import java.util.ArrayList;

public class FinalBoardPrint {
    public static String showSummary(JFrame parentFrame, House dealer, 
                                     ArrayList<Player> playerList, 
                                     ArrayList<Computer> aiList, 
                                     String gameMode) {
        
        int dealerScore = dealer.getScore();
        StringBuilder resultsBanner = new StringBuilder(); 
        StringBuilder dialogMessage = new StringBuilder();
        
        dialogMessage.append("🏆 === GAME OVER SUMMARY === 🏆\n\n");
        dialogMessage.append(String.format("🏠 Dealer (House) Score: %d %s\n", 
            dealerScore, (dealerScore > 21 ? "[BUSTED! 💥]" : "")));
        dialogMessage.append("--------------------------------------------------\n");

        // --- Calculate and append user outcomes ---
        for (int i = 0; i < playerList.size(); i++) {
            Player p = playerList.get(i); 
            int score = p.getScore(); 
            String outcome = "";

            resultsBanner.append("P").append(i + 1).append(": "); 
            if (score > 21) {
                resultsBanner.append("Bust");
                outcome = "❌ BUSTED (Lose)";
            } else if (p.getHandStrings().length == 5 && score <= 21) {
                resultsBanner.append("5-Card Win");
                outcome = "👑 WIN (5-Card Charlie!)";
            } else if (dealerScore > 21) {
                resultsBanner.append("Win");
                outcome = "🎉 WIN (Dealer Busted)";
            } else if (score > dealerScore) {
                resultsBanner.append("Win");
                outcome = "🎉 WIN (Higher Score)";
            } else if (score < dealerScore) {
                resultsBanner.append("Lose");
                outcome = "😭 LOSE";
            } else {
                resultsBanner.append("Draw");
                outcome = "🤝 DRAW (Push)";
            } 
            
            if (i < playerList.size() - 1) resultsBanner.append(" | ");
            dialogMessage.append(String.format("👤 Player %d:  Score: %d  ->  %s\n", (i + 1), score, outcome));
        }

        // --- Calculate and append AI outcomes ---
        if (gameMode.equals("COMPUTER")) {
            dialogMessage.append("--------------------------------------------------\n");
            for (int i = 0; i < aiList.size(); i++) {
                Computer ai = aiList.get(i);
                int score = ai.getScore();
                String outcome = "";

                if (score > 21) outcome = "❌ BUSTED";
                else if (ai.getHandStrings().length == 5 && score <= 21) outcome = "👑 WIN (5-Card Charlie!)";
                else if (dealerScore > 21 || score > dealerScore) outcome = "🎉 WIN";
                else if (score < dealerScore) outcome = "😭 LOSE";
                else outcome = "🤝 DRAW";

                dialogMessage.append(String.format("🤖 AI Bot %d:  Score: %d  ->  %s\n", (i + 2), score, outcome));
            }
        }
 
        JOptionPane.showMessageDialog(
            parentFrame, 
            dialogMessage.toString(), 
            "Game Results", 
            JOptionPane.INFORMATION_MESSAGE
        );
        return resultsBanner.toString();
    }
}