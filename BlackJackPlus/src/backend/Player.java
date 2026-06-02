package backend;
public class Player extends Hand {
    public void reset() {
        handCards.clear();
    }
    private String name = "Unknown Player";

public void setName(String name) {
    this.name = name;
}

public String getName() {
    return this.name;
}
}