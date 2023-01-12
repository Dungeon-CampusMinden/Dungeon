package ecs.components;

import ecs.entities.Entity;

public class PlayableComponent extends Component {

    public static String name = "PlayableComponent";
    private boolean playable;

    public PlayableComponent(Entity entity) {
        super(entity);
        playable = true;
    }

    public boolean isPlayable() {
        return playable;
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
    }
}
