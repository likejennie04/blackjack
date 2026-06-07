package gui;

import backend.PlayerSnapshot;
import backend.BlackjackClient;

import javax.swing.*;
import java.awt.*;

public class PlayerRowRenderer {

    public static JPanel createPlayerRow(
            PlayerSnapshot player,
            BlackjackClient client) {

        return createPlayerRow(
                player.getName(),
                player.getCards(),
                client);
    }

    public static JPanel createPlayerRow(
            String name,
            String[] cards,
            BlackjackClient client) {

        JPanel row = new JPanel(new BorderLayout());

        row.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        115));

        row.setBackground(
                Config.tableColor != null
                        ? Config.tableColor
                        : new Color(20, 50, 30));

        row.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(255,255,255,30),
                                1),
                        BorderFactory.createEmptyBorder(
                                10,15,10,15)));

        JPanel identityPanel =
                createIdentityPanel(
                        name,
                        cards,
                        client);

        JPanel handPanel =
                createHandPanel(cards);

        row.add(identityPanel, BorderLayout.WEST);
        row.add(handPanel, BorderLayout.CENTER);

        return row;
    }

    private static JPanel createIdentityPanel(
            String name,
            String[] cards,
            BlackjackClient client) {

        JPanel panel =
                new JPanel(new GridLayout(1,1));

        panel.setOpaque(false);

        panel.setPreferredSize(
                new Dimension(150,0));

        String displayName =
                buildDisplayName(name, client);

        int score =
                new PlayerSnapshot(name, cards)
                        .getScore();

        JLabel label =
                new JLabel(
                        displayName
                        + " ("
                        + score
                        + ")");

        label.setFont(
                new Font(
                        "Times New Roman",
                        Font.BOLD,
                        15));

        label.setForeground(Color.WHITE);

        panel.add(label);

        return panel;
    }

    private static JPanel createHandPanel(
            String[] cards) {

        JPanel panel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                12,
                                0));

        panel.setOpaque(false);

        for (String card : cards) {

            if (!card.trim().isEmpty()) {

                CardRenderer.displayCard(
                        panel,
                        card.trim());
            }
        }

        return panel;
    }

    private static String buildDisplayName(
            String name,
            BlackjackClient client) {

        String displayName =
                name.trim();

        String cleanName =
                displayName.toLowerCase();

        String localName =
                client.getPlayerName()
                        .trim()
                        .toLowerCase();

        if (cleanName.equals(localName)
                || (cleanName.equals("1")
                && localName.equals("anonymous"))) {

            if (!displayName.contains("(You)")) {
                displayName += " (You)";
            }
        }

        return displayName;
    }
}