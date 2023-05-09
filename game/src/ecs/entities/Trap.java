package ecs.entities;

import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import java.util.List;
import tools.Point;
import graphic.Animation;

/**
 * A Trap is an Entity with a PositionComponent and an AnimationComponent.
 * It can be triggered
 */
public abstract class Trap extends Entity{    
    /**
     * @param frame_time_idle
     * @param frame_time_triggered
     * @param Idleanimationpictures
     * @param Triggeredanimationpictures
     * @param isRepeatable
     * @param position
     */
    public Trap(int frame_time_idle,
                int frame_time_triggered,
                List<String> DEFAULT_IDLE_ANIMATION_FRAMES,
                List<String> DEFAULT_TRIGGERED_ANIMATION_FRAMES,
                Boolean isRepeatable,
                Point position) {

        new PositionComponent(this, position);
        AnimationComponent ac =
                new AnimationComponent(
                        this,
                        new Animation(DEFAULT_IDLE_ANIMATION_FRAMES, frame_time_idle, isRepeatable),
                        new Animation(DEFAULT_TRIGGERED_ANIMATION_FRAMES, frame_time_triggered, isRepeatable));
    }

    /**
     * The method that is called when the trap is triggered
     * 
     * @param entity
     */
    private void onTrigger(Entity entity) {
        
    }


}
