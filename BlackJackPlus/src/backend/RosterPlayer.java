package backend;

public class RosterPlayer {

    private String name;
    private int avatarId;

    public RosterPlayer(String name, int avatarId) {
        this.name = name;
        this.avatarId = avatarId;
    }

    public String getName() {
        return name;
    }

    public int getAvatarId() {
        return avatarId;
    }
}