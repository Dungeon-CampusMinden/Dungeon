package ecs.components.ai.transition;

import static org.junit.Assert.*;

import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.entities.Entity;
import org.junit.Test;

public class SelfDefendTransitionTest {

    /**
     * tests if the isInFight method returns false when the current HealthPoints of an entity are
     * equal to its max HealthPoints
     */
    @Test
    public void isInFightModeHealtpointsAreMax() {
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        hc.setMaximalHealthpoints(10);
        hc.setCurrentHealthpoints(10);
        ITransition defend = new SelfDefendTransition();

        assertFalse(defend.isInFightMode(entity));
    }
    /**
     * tests if the isInFight method returns true when the current HealthPoints of an entity are
     * lower than its max HealthPoints
     */
    @Test
    public void isInFightModeHealthpointsAreLowerThenMax() {
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        hc.setMaximalHealthpoints(10);
        hc.setCurrentHealthpoints(10);
        ITransition defend = new SelfDefendTransition();
        assertFalse(defend.isInFightMode(entity));
        hc.setCurrentHealthpoints(9);
        assertTrue(defend.isInFightMode(entity));
    }

    /**
     * checks the thrown Exception when the required HealthComponent is missing in the provided
     * Entity
     */
    @Test
    public void isInFightModeHealthComponentMissing() {
        Entity entity = new Entity();
        ITransition defend = new SelfDefendTransition();
        MissingComponentException exception =
                assertThrows(MissingComponentException.class, () -> defend.isInFightMode(entity));
        assertTrue(exception.getMessage().contains(HealthComponent.class.getName()));
        assertTrue(exception.getMessage().contains(SelfDefendTransition.class.getName()));
    }
}
