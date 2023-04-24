package mp.packages.response;

import level.elements.ILevel;

import java.util.HashMap;

public class JoinSessionResponse {

    private ILevel level;
    private Integer playerId;
    private HashMap playerPositions;

    public JoinSessionResponse(ILevel level, Integer playerId, HashMap playerPositions) {
        this.level = level;
        this.playerId = playerId;
        this.playerPositions = playerPositions;
    }

    public ILevel getLevel() {
        return level;
    }

    public HashMap getPlayerPositions() {
        return this.playerPositions;
    }

    public Integer getPlayerId() { return this.playerId; }
}
