package gui;

import backend.RosterPlayer;
import backend.BlackjackClient;

import javax.swing.*;
import java.awt.*;

public class RosterRenderer {

    public static JPanel createDealerBlock(
            String currentTurnPlayer) {

        JPanel dealerBlock = new JPanel(new BorderLayout());
        dealerBlock.setOpaque(false);

        JLabel dealerAvatar = new JLabel();

        if (AvatarManager.getAllAvatars() != null
                && !AvatarManager.getAllAvatars().isEmpty()) {

            dealerAvatar.setIcon(
                    AvatarManager.getAllAvatars().get(0));
        }

        dealerAvatar.setHorizontalAlignment(JLabel.CENTER);

        JLabel dealerName =
                new JLabel("Dealer", JLabel.CENTER);

        dealerName.setFont(
                new Font("Times New Roman",
                        Font.BOLD,
                        12));

        if ("Dealer".equalsIgnoreCase(currentTurnPlayer)) {

            dealerName.setForeground(Color.YELLOW);

            dealerBlock.setBorder(
                    BorderFactory.createLineBorder(
                            new Color(255,215,0),
                            2,
                            true));
        }
        else {

            dealerName.setForeground(Color.RED);

            dealerBlock.setBorder(
                    BorderFactory.createEmptyBorder(
                            2,2,2,2));
        }

        dealerBlock.add(dealerAvatar, BorderLayout.CENTER);
        dealerBlock.add(dealerName, BorderLayout.SOUTH);

        return dealerBlock;
    }

    public static JPanel createPlayerBlock(
            RosterPlayer player,
            String currentTurnPlayer,
            BlackjackClient client) {

        JPanel pBlock = new JPanel(new BorderLayout());
        pBlock.setOpaque(false);

        JLabel avatarLabel = new JLabel();

        int avatarId = player.getAvatarId();

        if (AvatarManager.getAllAvatars() != null
                && !AvatarManager.getAllAvatars().isEmpty()) {

            int safeId =
                    (avatarId >= 0
                    && avatarId < AvatarManager.getAllAvatars().size())
                    ? avatarId
                    : 0;

            avatarLabel.setIcon(
                    AvatarManager.getAllAvatars().get(safeId));
        }

        avatarLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel nameLabel =
                new JLabel(player.getName(), JLabel.CENTER);

        nameLabel.setFont(
                new Font("Times New Roman",
                        Font.PLAIN,
                        12));

        String cleanPlayer =
                player.getName().trim().toLowerCase();

        String cleanLocal =
                client.getPlayerName().trim().toLowerCase();

        if (cleanPlayer.equals(cleanLocal)) {
            nameLabel.setText(player.getName() + " (You)");
            nameLabel.setForeground(Color.GREEN);
        }
        else {
            nameLabel.setForeground(Color.WHITE);
        }

        if (player.getName()
                .equalsIgnoreCase(currentTurnPlayer)) {

            pBlock.setBorder(
                    BorderFactory.createLineBorder(
                            new Color(255,215,0),
                            2,
                            true));
        }
        else {

            pBlock.setBorder(
                    BorderFactory.createEmptyBorder(
                            2,2,2,2));
        }

        pBlock.add(avatarLabel, BorderLayout.CENTER);
        pBlock.add(nameLabel, BorderLayout.SOUTH);

        return pBlock;
    }
}