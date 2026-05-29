package backend;
import java.util.ArrayList;

public class Hand {
    protected ArrayList<Card> handCards = new ArrayList<>();
    
    public synchronized void addCard(Card c) { 
        handCards.add(c); 
    }
    
    public synchronized int getScore() {
        int total = 0;
        int aces = 0;
        for (Card c : handCards) {
            if (c.rank == 1) {
                aces++;
                total += 11;
            } else if (c.rank >= 10) {
                total += 10;
            } else {
                total += c.rank;
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }
    
    @Override
    public synchronized String toString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < handCards.size(); i++) {
            res.append(handCards.get(i).toString());
            if (i < handCards.size() - 1) res.append(", ");
        }
        return res.toString() + " (" + getScore() + ")";
    }
    public synchronized String[] getHandStrings() {
        String[] strings = new String[handCards.size()];
        for (int i = 0; i < handCards.size(); i++) {
            strings[i] = handCards.get(i).toString();
        }
        return strings;
    }
}