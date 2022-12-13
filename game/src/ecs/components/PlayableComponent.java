package ecs.components;

import ecs.entitys.Entity;
import mydungeon.ECS;

public class PlayableComponent implements Component {

    private boolean playable;

    public PlayableComponent(Entity entity) {
        ECS.playableComponentMap.put(entity, this);
        playable = true;
    }

    public boolean isPlayable() {
        return playable;
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
    }
}
