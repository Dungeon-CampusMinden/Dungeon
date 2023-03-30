package mp.packages.response;

import mp.packages.request.UpdateOwnPositionRequest;
import tools.Point;

import java.util.HashMap;

public class UpdateAllPositionsResponse {
    private HashMap<Integer, Point> playerPositions = new HashMap<Integer, Point>();

    public UpdateAllPositionsResponse(HashMap playerPositions){
        this.playerPositions = playerPositions;
    }

    public HashMap<Integer, Point> getPlayerPositions() {
        return playerPositions;
    }
}
