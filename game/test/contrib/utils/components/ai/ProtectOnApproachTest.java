package contrib.utils.components.ai;

import static org.junit.Assert.assertTrue;

import contrib.components.AIComponent;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.ProtectOnApproach;
import contrib.utils.components.ai.transition.RangeTransition;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ProtectOnApproachTest {
    private Entity entity;
    private AIComponent entityAI;
    private Entity protectedEntity;
    private Entity hero;
    private final Point pointOfProtect = new Point(0, 0);

    @Before
    public void setup() {

        // Protected Entity
        protectedEntity = new Entity();

        // Add AI Component
        AIComponent protectedAI =
                new AIComponent(
                        protectedEntity,
                        new CollideAI(0.2f),
                        new RadiusWalk(0, 50),
                        new RangeTransition(2));

        // Add Position Component
        new PositionComponent(protectedEntity, pointOfProtect);

        // Protecting Entity
        entity = new Entity();

        // Add AI Component
        entityAI =
                new AIComponent(
                        entity,
                        new CollideAI(0.2f),
                        new RadiusWalk(0, 50),
                        new ProtectOnApproach(2f, protectedEntity));

        // Add Position Component
        new PositionComponent(entity, new Point(0f, 0f));

        // Hero
        hero = Game.getHero().orElse(new Entity());
    }

    // Ignore because no solution to create hero during tests at the moment
    @Test
    @Ignore
    public void heroInRange() {
        // when
        hero.removeComponent(PositionComponent.class);
        new PositionComponent(hero, pointOfProtect);

        // then
        assertTrue(entityAI.getTransitionAI().apply(entity));
    }
}
