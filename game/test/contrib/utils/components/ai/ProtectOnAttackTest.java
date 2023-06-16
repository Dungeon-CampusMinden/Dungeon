package contrib.utils.components.ai;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.ProtectOnAttack;
import contrib.utils.components.health.Damage;

import core.Entity;
import core.components.PlayerComponent;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ProtectOnAttackTest {
    private Entity protector;
    private Entity protectedEntity;
    private Entity attacker;
    private List<Entity> entitiesToProtect;
    private HealthComponent entityHC;

    @Before
    public void setup() {
        // Get a protector
        protector = new Entity();

        // Get a victim and its HealthComponent
        protectedEntity = new Entity();
        entityHC = new HealthComponent(protectedEntity);

        // Get an attacker
        attacker = new Entity();
        new PlayerComponent(attacker);

        // Prepare a list of entities with a HealthComponent
        entitiesToProtect = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Entity e = new Entity();
            new HealthComponent(e);
            entitiesToProtect.add(e);
        }
    }

    /** Add one entity to transition and inflict damage */
    @Test
    public void oneEntityAdded() {
        // given
        AIComponent attackerAI =
                new AIComponent(
                        protector,
                        new CollideAI(2f),
                        new RadiusWalk(2, 2),
                        new ProtectOnAttack(protectedEntity));

        // when
        entityHC.receiveHit(new Damage(1, null, attacker));

        // then
        assertTrue(attackerAI.transitionAI().apply(protector));
    }

    /** Add one entity to transition and inflict no damage */
    @Test
    public void oneEntityAddedWithoutDamage() {
        // given
        AIComponent attackerAI =
                new AIComponent(
                        protector,
                        new CollideAI(2f),
                        new RadiusWalk(2, 2),
                        new ProtectOnAttack(protectedEntity));

        // when

        // then
        assertFalse(attackerAI.transitionAI().apply(protector));
    }

    /** Add a list of entities to the transition and inflict damage to all */
    @Test
    public void dmgToAllEntities() {
        // given
        AIComponent attackerAI =
                new AIComponent(
                        protector,
                        new CollideAI(2f),
                        new RadiusWalk(2, 2),
                        new ProtectOnAttack(entitiesToProtect));

        // when
        for (Entity e : entitiesToProtect) {
            if (e.fetch(HealthComponent.class).isPresent()) {
                HealthComponent hCp = (HealthComponent) e.fetch(HealthComponent.class).get();
                hCp.receiveHit(new Damage(1, null, attacker));
            }
        }

        // then
        assertTrue(attackerAI.transitionAI().apply(protector));
    }

    /** Add an empty list of entities to the transition */
    @Test
    public void emptyListOfEntities() {
        List<Entity> emptyList = new ArrayList<>();
        // given
        AIComponent attackerAI =
                new AIComponent(
                        protector,
                        new CollideAI(2f),
                        new RadiusWalk(2, 2),
                        new ProtectOnAttack(emptyList));

        // then
        assertFalse(attackerAI.transitionAI().apply(protector));
    }
}
