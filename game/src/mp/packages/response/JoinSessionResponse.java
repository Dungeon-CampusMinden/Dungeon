package mp.packages.response;

import level.elements.ILevel;

import java.util.HashMap;

public class JoinSessionResponse {

    private ILevel level;
    private HashMap playerPositions;

    public JoinSessionResponse(ILevel level, HashMap playerPositions) {
        this.level = level;
        this.playerPositions = playerPositions;
    }

    public ILevel getLevel() {
        return level;
    }

    public HashMap getPlayerPositions() {
        return this.playerPositions;
    }
}
