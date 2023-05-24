package ecs.components.ai.transition;

import ecs.components.PositionComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.RadiusWalk;
import ecs.entities.Entity;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import starter.Game;
import tools.Point;

import static org.junit.Assert.assertTrue;

public class ProtectOnApproachTest {
    private Entity entity;
    private AIComponent entityAI;
    private Entity protectedEntity;
    private Entity hero;
    private final Point pointOfProtect = new Point(0, 0);

    @Before
    public void setUpEntityToProtect() {
        protectedEntity = new Entity();

        // Add AI Component
        AIComponent protectedAI =
            new AIComponent(
                protectedEntity,
                new CollideAI(0.2f),
                new RadiusWalk(0, 50),
                new RangeTransition(2));

        protectedEntity.addComponent(protectedAI);

        // Add Position Component
        protectedEntity.addComponent(new PositionComponent(protectedEntity, pointOfProtect));
    }

    @Before
    public void setUpEntityThatProtects() {
        entity = new Entity();

        // Add AI Component
        entityAI =
            new AIComponent(
                entity,
                new CollideAI(0.2f),
                new RadiusWalk(0, 50),
                new ProtectOnApproach(2f, protectedEntity));
        entity.addComponent(entityAI);

        // Add Position Component
        entity.addComponent(new PositionComponent(entity, new Point(0f, 0f)));
    }

    @Before
    public void setUpHero() {
        hero = Game.getHero().orElse(new Entity());
    }


    //Ignore because no solution to create hero during tests at the moment
    @Test
    @Ignore
    public void testHeroInRange() {
        // when
        hero.removeComponent(PositionComponent.class);
        hero.addComponent(new PositionComponent(hero, pointOfProtect));

        // then
        assertTrue(entityAI.getTransitionAI().isInFightMode(entity));
    }
}
