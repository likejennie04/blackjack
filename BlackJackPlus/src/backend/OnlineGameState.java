package backend;

import java.util.ArrayList;
import java.util.List;

public class OnlineGameState {

    private String latestSnapshotRaw;
    private String rosterData;
    private String currentTurnPlayer;
    private List<PlayerSnapshot> playerSnapshots; 
    private List<RosterPlayer> rosterPlayers;

    public OnlineGameState() {
        latestSnapshotRaw = "";
        rosterData = "";
        currentTurnPlayer = "Waiting...";
        playerSnapshots = new ArrayList<>(); 
        rosterPlayers = new ArrayList<>();
    }
    
    public List<RosterPlayer> getRosterPlayers() {
    	return rosterPlayers; 
    }
    
   
    public void setPlayerSnapshots(List<PlayerSnapshot> playerSnapshots) {
        this.playerSnapshots = playerSnapshots;
    }

    public String getLatestSnapshotRaw() {
        return latestSnapshotRaw;
    }

    public void setLatestSnapshotRaw(String latestSnapshotRaw) {
        this.latestSnapshotRaw = latestSnapshotRaw;
    }

    public String getRosterData() {
        return rosterData;
    }

    public void setRosterData(String rosterData) {
        this.rosterData = rosterData;
    }

    public String getCurrentTurnPlayer() {
        return currentTurnPlayer;
    }

    public void setCurrentTurnPlayer(String currentTurnPlayer) {
        this.currentTurnPlayer = currentTurnPlayer;
    }
    
    
    public List<PlayerSnapshot> getPlayerSnapshots() {
        return playerSnapshots;
    }
   
    public void updateSnapshot(String rawSnapshot) {

        latestSnapshotRaw = rawSnapshot;

        playerSnapshots.clear();

        if (rawSnapshot == null || rawSnapshot.isEmpty()) {
            return;
        }

        String[] rows = rawSnapshot.split(";");

        for (String row : rows) {

            if (row.trim().isEmpty()) {
                continue;
            }

            String[] tokens = row.split("=");

            if (tokens.length < 2) {
                continue;
            }

            String name = tokens[0].trim();

            String[] cards =
                    tokens[1]
                            .split(",");

            PlayerSnapshot snapshot =
                    new PlayerSnapshot(name, cards);

            playerSnapshots.add(snapshot);
        }
    }
    
    public void updateRoster(String rosterData) {

        this.rosterData = rosterData;

        rosterPlayers.clear();

        if (rosterData == null || rosterData.isEmpty()) {
            return;
        }

        String[] participants = rosterData.split(";");

        for (String part : participants) {

            if (part.trim().isEmpty()) {
                continue;
            }

            String[] tokens = part.split(",");

            if (tokens.length < 2) {
                continue;
            }

            String name = tokens[0].trim();

            int avatarId = 0;

            try {
                avatarId = Integer.parseInt(tokens[1].trim());
            }
            catch (Exception ignored) {}

            rosterPlayers.add(
                new RosterPlayer(name, avatarId)
            );
        }
    }
}
