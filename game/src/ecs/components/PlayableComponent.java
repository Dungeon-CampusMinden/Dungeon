package ecs.components;

import ecs.entities.Entity;

/** Marks an entity as player */
public class PlayableComponent extends Component {

    private boolean playable;

    /** {@inheritDoc} */
    public PlayableComponent(Entity entity) {
        super(entity, PlayableComponent.class);
        playable = true;
    }

    /**
     * @return the playable state
     */
    public boolean isPlayable() {
        return playable;
    }

    /**
     * @param playable set the playabale state
     */
    public void setPlayable(boolean playable) {
        this.playable = playable;
    }
}
