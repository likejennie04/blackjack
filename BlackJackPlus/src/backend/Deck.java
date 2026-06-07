package backend;
public class Deck {
    private Card[] cards = new Card[52];
    private int cardsUsed = 0;
    
    public Deck() {
        int pos = 0;
        for (int s = 0; s < 4; s++) { 
            for (int v = 1; v <= 13; v++) { 
                cards[pos++] = new Card(v, s);
            }
        }
    }
    
    public void shuffle(int seed) {
        java.util.Random random = new java.util.Random(seed);
        for (int i = cards.length - 1; i > 0; i--) {
            int rand = random.nextInt(i + 1);
            Card temp = cards[i];
            cards[i] = cards[rand];
            cards[rand] = temp;
        }
        cardsUsed = 0;
    }
    
    public void shuffle() {
        shuffle((int) System.currentTimeMillis());
    }
    
    
    public synchronized Card dealCard() {
        if (cardsUsed >= cards.length) {
            System.out.println("[Deck]: Reshuffling the deck...");
            shuffle();
        }
        return cards[cardsUsed++];
    }
}