public class House extends Hand {
    public String getInitialView() {
        if (handCards.size() < 2) return "";
        return "HIDDEN, " + handCards.get(1).toString();
    }
}