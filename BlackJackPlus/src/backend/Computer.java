package backend;
import java.util.Random;

/**
 * Computer class represents an AI player.
 * It implements Runnable to allow multiple AI players to think concurrently.
 */
public class Computer extends Hand implements Runnable {
    private Random r;
    private Deck d;
    private int id;

    public Computer(Random r, Deck d, int id) {
        this.r = r;
        this.d = d;
        this.id = id;
    }
    
    @Override
    public void run() {
        // AI Decision Loop
        // The AI will continue to hit as long as its score is under 21
        while (getScore() < 21) {
            // STRATEGY: Stop hitting immediately if 5-Card Charlie is achieved
            if (this.handCards.size() >= 5) {
                System.out.println("AI Player " + id + " achieved 5-Card Charlie and stands.");
                break;
            }

            int currentScore = getScore();
            // AI Logic: Always hit if score < 14, or 50% chance to hit if score is between 14 and 17
            boolean shouldHit = (currentScore < 14) || (currentScore <= 17 && r.nextInt(2) == 1);
            
            if (shouldHit) {
                addCard(d.dealCard());
                // Check if AI busted after hitting
                if (getScore() > 21) {
                    break;
                }
            } else {
                // AI chooses to stand
                break;
            }
        }
    }
}