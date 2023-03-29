package mp.packages.request;

import tools.Point;

public class UpdateOwnPositionRequest{
    private int playerId;
    private Point position;

    public UpdateOwnPositionRequest(int playerId, Point position){
        this.playerId = playerId;
        this.position = position;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Point getPosition() {
        return position;
    }
}
