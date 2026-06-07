package backend;

public class PlayerSnapshot {

    private String name;
    private String[] cards;

    public PlayerSnapshot(String name, String[] cards) {
        this.name = name;
        this.cards = cards;
    }

    public String getName() {
        return name;
    }

    public String[] getCards() {
        return cards;
    }
    
    public int getScore() {

        Hand hand = new Hand();

        for (String cardCode : cards) {

            cardCode = cardCode.trim().toLowerCase();

            if (cardCode.isEmpty()
                    || cardCode.contains("hidden")) {
                continue;
            }

            if (cardCode.length() < 2) {
                continue;
            }

            String rankPart =
                    cardCode.substring(
                            0,
                            cardCode.length() - 1);

            char suitPart =
                    cardCode.charAt(
                            cardCode.length() - 1);

            int rank;

            switch (rankPart) {
                case "a": rank = 1; break;
                case "j": rank = 11; break;
                case "q": rank = 12; break;
                case "k": rank = 13; break;

                default:
                    rank = Integer.parseInt(rankPart);
            }

            int suit;

            switch (suitPart) {
                case 'd': suit = 0; break;
                case 'h': suit = 1; break;
                case 'c': suit = 2; break;
                case 's': suit = 3; break;
                default: suit = -1;
            }

            hand.addCard(
                    new Card(rank, suit)
            );
        }

        return hand.getScore();
    }
}