package ecs.components.mp;

import ecs.components.Component;
import ecs.entities.Entity;

public class MultiplayerComponent extends Component {

    private int playerId;

    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public MultiplayerComponent(Entity entity, int playerId) {
        super(entity);
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId){
        this.playerId = playerId;
    }
}
