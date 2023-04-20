package ecs.entities;

import static org.junit.Assert.*;

import ecs.components.PositionComponent;
import ecs.components.animation.AnimationComponent;
import graphic.Animation;
import java.util.List;
import org.junit.Test;

public class AnimationEntityTest {

    @Test
    public void testNoComponentsMissing() {
        AnimationEntity entity = new AnimationEntity(new Animation(List.of("", ""), 1), null);
        assertTrue(
                "AnimationComponent is missing",
                entity.getComponent(AnimationComponent.class).isPresent());
        assertTrue(
                "PositionComponent is missing",
                entity.getComponent(PositionComponent.class).isPresent());
    }
}
