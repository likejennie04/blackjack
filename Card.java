public class Card {
    int rank;
    int suit;
    
    public Card(int theValue, int theSuit) {
        this.rank = theValue;
        this.suit = theSuit;
    }
    
    @Override
    public String toString() {
        String[] ranks = {"", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] suits = {"c", "h", "d", "s"};
        return ranks[rank] + suits[suit];
    }
}