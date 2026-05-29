import java.util.Random;
import java.util.Scanner;
import java.util.ArrayList;

public class Blackjack {
    public static void main(String[] args) {
        // Fallback checks if program arguments are missing via terminal testing
        int gameSeed = (args.length > 0) ? Integer.parseInt(args[0]) : 42;
        int numParticipants = (args.length > 1) ? Integer.parseInt(args[1]) : 3;

        Deck cardDeck = new Deck();
        cardDeck.shuffle(gameSeed); // Fixed: Passed seed configuration into our shuffle engine

        Player user = new Player();
        ArrayList<Computer> aiList = new ArrayList<>();
        java.util.Random aiRand = new java.util.Random(gameSeed);

        // FIX 1: Spawn AI elements directly using the constructor they need!
        for (int i = 0; i < numParticipants - 1; i++) {
            aiList.add(new Computer(aiRand, cardDeck, i + 2));
        }
        House dealer = new House();

        ArrayList<Hand> playOrder = new ArrayList<>();
        playOrder.add(user);
        for (Computer c : aiList) playOrder.add(c);
        playOrder.add(dealer);

        // Initial Card Dealing Cycle (2 Cards each)
        for (int round = 0; round < 2; round++) {
            for (Hand h : playOrder) {
                h.addCard(cardDeck.dealCard());
            }
        }

        // Show opening deal states
        System.out.println("House: " + dealer.getInitialView());
        System.out.println("Player1: " + user.toString());
        for (int i = 0; i < aiList.size(); i++) {
            System.out.println("Player" + (i + 2) + ": " + aiList.get(i).toString());
        }

        // Check Natural BlackJack scenario
        if (dealer.getScore() == 21) {
            System.out.println();
            displayFinal(dealer, user, aiList);
            return;
        }

        // Human Turn Processing
        Scanner sc = new Scanner(System.in);
        user.runTurn(sc, cardDeck);

        // FIX 2: Spin up multi-threading workers directly utilizing the existing, dealt-to AI instances!
        Thread[] aiThreads = new Thread[aiList.size()];
        for (int i = 0; i < aiList.size(); i++) {
            aiThreads[i] = new Thread(aiList.get(i)); 
            aiThreads[i].start();
        }
        
        // Wait for AI actions to terminate execution loops
        for (Thread t : aiThreads) {
            try {
                t.join(); 
            } catch (InterruptedException e) {
                System.out.println("Thread execution interrupted");
            }
        }
        
        // House / Dealer Phase
        dealer.runTurn(cardDeck);
        displayFinal(dealer, user, aiList);
        sc.close();
    }

    private static void displayFinal(House h, Player p1, ArrayList<Computer> comps) {
        System.out.println("\n--- Game Results ---");
        System.out.println("House: " + h.toString());
        judgeResult("Player1", p1, h);
        for (int i = 0; i < comps.size(); i++) {
            judgeResult("Player" + (i + 2), comps.get(i), h);
        }
    }

    private static void judgeResult(String name, Hand p, House h) {
        int ps = p.getScore();
        int hs = h.getScore();
        String tag;
        if (ps > 21) tag = "[Lose]";
        else if (hs > 21 || ps > hs) tag = "[Win]";
        else if (ps < hs) tag = "[Lose]";
        else tag = "[Draw]";
        System.out.print(tag + " " + name + ": " + p.toString());
        if (ps > 21) System.out.print(" - Bust!");
        System.out.println();
    }
}

class Card {
    int rank;
    int suit;
    
    public Card(int theValue, int theSuit) {
        this.rank = theValue;
        this.suit = theSuit;
    }
    
    @Override
    public String toString() {
        String[] ranks = {"", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] suits = {"c", "h", "d", "s"}; // Clubs, Hearts, Diamonds, Spades
        return ranks[rank] + suits[suit];
    }
}

class Deck {
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
    
    // FIX 3: Added overload allowing a custom integer pseudo-random generation seed input
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
        if (cardsUsed >= cards.length) throw new IllegalStateException("The deck is empty!");
        return cards[cardsUsed++];
    }
}

class Hand {
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

    // EXPOSED CONTEXT ACCESSOR: Returns names of cards in an array (Perfect wrapper helper for your GUI panels)
    public synchronized String[] getHandStrings() {
        String[] strings = new String[handCards.size()];
        for (int i = 0; i < handCards.size(); i++) {
            strings[i] = handCards.get(i).toString();
        }
        return strings;
    }
}

class Computer extends Hand implements Runnable {
    private java.util.Random r;
    private Deck d;
    private int id;

    // FIX 4: Removed empty constructor that was inducing error logs in Eclipse compiler tracking
    public Computer(java.util.Random r, Deck d, int id) {
        this.r = r;
        this.d = d;
        this.id = id;
    }
    
    @Override
    public void run() {
        runTurn(r, d, id);
    }
    
    public void runTurn(java.util.Random r, Deck d, int id) {
        System.out.println("\n--- Player" + id + " turn ---");
        while (getScore() < 21) {
            System.out.println("Player" + id + ": " + this.toString());
            int s = getScore();
            boolean hit = (s < 14) || (s <= 17 && r.nextInt(2) == 1);
            if (hit) {
                System.out.println("Hit");
                addCard(d.dealCard());
                if (getScore() > 21) {
                    System.out.println("Player" + id + ": " + this.toString() + " - Bust!");
                    break;
                }
            } else {
                System.out.println("Stand\nPlayer" + id + ": " + this.toString());
                break;
            }
        }
    }
}

class Player extends Hand {
    public void runTurn(Scanner s, Deck d) {
        System.out.println("\n--- Player1 turn ---");
        while (getScore() < 21) {
            System.out.println("Player1: " + this.toString());
            System.out.print("Type 'Hit' or 'Stand': ");
            String input = s.next();
            if (input.equalsIgnoreCase("Hit")) {
                System.out.println("Hit");
                addCard(d.dealCard());
                if (getScore() > 21) {
                    System.out.println("Player1: " + this.toString() + " - Bust!");
                    break;
                }
            } else {
                System.out.println("Stand\nPlayer1: " + this.toString());
                break;
            }
        }
    }
}

class House extends Hand {
    public void runTurn(Deck d) {
        System.out.println("\n--- House turn ---");
        while (getScore() <= 16) {
            System.out.println("House: " + this.toString() + "\nHit");
            addCard(d.dealCard());
            if (getScore() > 21) {
                System.out.println("House: " + this.toString() + " - Bust!");
                return;
            }
        }
        System.out.println("House: " + this.toString() + "\nStand\nHouse: " + this.toString());
    }
    
    public String getInitialView() {
        if (handCards.size() < 2) return "";
        return "HIDDEN, " + handCards.get(1).toString();
    }
}