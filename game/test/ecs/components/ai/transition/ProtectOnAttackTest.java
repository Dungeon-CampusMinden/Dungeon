package ecs.components.ai.transition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ecs.components.HealthComponent;
import ecs.components.PlayableComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.RadiusWalk;
import ecs.damage.Damage;
import ecs.entities.Entity;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ProtectOnAttackTest {
    private Entity protector;
    private Entity protectedEntity;
    private Entity attacker;
    private List<Entity> entitiesToProtect;
    private HealthComponent entityHC;

    @Before
    public void setUpEntities() {
        // Get a protector
        protector = new Entity();

        // Get a victim and its HealthComponent
        protectedEntity = new Entity();
        entityHC = new HealthComponent(protectedEntity);
        protectedEntity.addComponent(entityHC);

        // Get an attacker
        attacker = new Entity();
        attacker.addComponent(new PlayableComponent(attacker));
    }

    /** Prepare a list of entities with a HealthComponent */
    @Before
    public void setUpVictimList() {
        entitiesToProtect = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Entity e = new Entity();
            e.addComponent(new HealthComponent(e));
            entitiesToProtect.add(e);
        }
    }

    /** Add one entity to transition and inflict damage */
    @Test
    public void testOneEntityAdded() {
        // given
        AIComponent attackerAI =
                new AIComponent(
                        protector,
                        new CollideAI(2f),
                        new RadiusWalk(2, 2),
                        new ProtectOnAttack(protectedEntity));

        protector.addComponent(attackerAI);

        // when
        entityHC.receiveHit(new Damage(1, null, attacker));

        // then
        assertTrue(attackerAI.getTransitionAI().isInFightMode(protector));
    }

    /** Add one entity to transition and inflict no damage */
    @Test
    public void testOneEntityAddedWithoutDamage() {
        // given
        AIComponent attackerAI =
                new AIComponent(
                        protector,
                        new CollideAI(2f),
                        new RadiusWalk(2, 2),
                        new ProtectOnAttack(protectedEntity));

        // when
        protector.addComponent(attackerAI);

        // then
        assertFalse(attackerAI.getTransitionAI().isInFightMode(protector));
    }

    /** Try to add a list of entities to the transition */
    @Test
    public void testAddListOfEntities() {
        // when
        AIComponent attackerAI =
                new AIComponent(
                        protector,
                        new CollideAI(2f),
                        new RadiusWalk(2, 2),
                        new ProtectOnAttack(entitiesToProtect));
    }

    /** Add a list of entities to the transition and inflict damage to all */
    @Test
    public void addDmgToAllEntities() {
        // given
        AIComponent attackerAI =
                new AIComponent(
                        protector,
                        new CollideAI(2f),
                        new RadiusWalk(2, 2),
                        new ProtectOnAttack(entitiesToProtect));

        // when
        for (Entity e : entitiesToProtect) {
            if (e.getComponent(HealthComponent.class).isPresent()) {
                HealthComponent hCp = (HealthComponent) e.getComponent(HealthComponent.class).get();
                hCp.receiveHit(new Damage(1, null, attacker));
            }
        }

        // then
        assertTrue(attackerAI.getTransitionAI().isInFightMode(protector));
    }

    /** Add an empty list of entities to the transition */
    @Test
    public void addEmptyListOfEntities() {
        List<Entity> emptyList = new ArrayList<>();
        // given
        AIComponent attackerAI =
                new AIComponent(
                        protector,
                        new CollideAI(2f),
                        new RadiusWalk(2, 2),
                        new ProtectOnAttack(emptyList));

        // then
        assertFalse(attackerAI.getTransitionAI().isInFightMode(protector));
    }
}
