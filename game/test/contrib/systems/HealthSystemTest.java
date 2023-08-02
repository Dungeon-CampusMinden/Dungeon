package contrib.systems;

import static org.junit.Assert.*;

import contrib.components.HealthComponent;
import contrib.components.StatsComponent;
import contrib.utils.components.draw.AdditionalAnimations;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;

import core.Entity;
import core.Game;
import core.components.DrawComponent;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.function.Consumer;

public class HealthSystemTest {
    private static final String ANIMATION_PATH = "../test/textures/test_hero";

    @After
    public void cleanup() {
        Game.removeAllEntities();
        Game.currentLevel(null);
        Game.removeAllSystems();
    }

    @Test
    public void updateEntityDies() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
        DrawComponent ac = new DrawComponent(ANIMATION_PATH);
        HealthComponent component = new HealthComponent(1, onDeath);
        entity.addComponent(ac);
        entity.addComponent(component);
        Game.add(entity);
        HealthSystem system = new HealthSystem();
        Game.add(system);
        component.currentHealthpoints(0);

        system.execute();
        assertTrue(ac.isCurrentAnimation(AdditionalAnimations.DIE));
        assertFalse(Game.entityStream().anyMatch(e -> e == entity));
    }

    @Test
    public void updateEntityGetDamage() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
        DrawComponent ac = new DrawComponent(ANIMATION_PATH);
        HealthComponent component = new HealthComponent(10, onDeath);
        entity.addComponent(ac);
        entity.addComponent(component);
        Game.add(entity);
        component.receiveHit(new Damage(5, DamageType.FIRE, null));
        component.receiveHit(new Damage(2, DamageType.FIRE, null));
        HealthSystem system = new HealthSystem();
        Game.add(system);

        system.execute();
        assertEquals(3, component.currentHealthpoints());
        assertTrue(ac.isCurrentAnimation(AdditionalAnimations.HIT));
    }

    @Test
    public void updateEntityGetNegativeDamage() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
        DrawComponent ac = new DrawComponent(ANIMATION_PATH);
        HealthComponent component = new HealthComponent(10, onDeath);
        entity.addComponent(ac);
        entity.addComponent(component);
        Game.add(entity);
        component.currentHealthpoints(3);
        component.receiveHit(new Damage(-3, DamageType.FIRE, null));
        HealthSystem system = new HealthSystem();
        Game.add(system);
        system.execute();
        assertEquals(6, component.currentHealthpoints());
        assertFalse(ac.isCurrentAnimation(AdditionalAnimations.HIT));
    }

    @Test
    public void updateEntityGetZeroDamage() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
        DrawComponent ac = new DrawComponent(ANIMATION_PATH);
        HealthComponent component = new HealthComponent(10, onDeath);
        entity.addComponent(ac);
        entity.addComponent(component);
        Game.add(entity);
        component.receiveHit(new Damage(0, DamageType.FIRE, null));
        HealthSystem system = new HealthSystem();
        Game.add(system);
        system.execute();
        assertEquals(10, component.currentHealthpoints());
        assertFalse(ac.isCurrentAnimation(AdditionalAnimations.HIT));
    }

    @Test
    public void updateWithoutHealthComponent() {
        Game.removeAllEntities();
        HealthSystem system = new HealthSystem();
        Game.add(system);
        system.execute();
    }

    @Test
    public void testDamageWithModifier() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        entity.addComponent(new DrawComponent(ANIMATION_PATH));
        StatsComponent statsComponent = new StatsComponent();
        entity.addComponent(statsComponent);
        statsComponent.multiplier(DamageType.PHYSICAL, 2);

        HealthComponent healthComponent = new HealthComponent();
        entity.addComponent(healthComponent);
        healthComponent.maximalHealthpoints(100);
        healthComponent.currentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        Game.add(system);
        Game.add(entity);

        system.execute();

        assertEquals(80, healthComponent.currentHealthpoints()); // 100 - 10 * 2
    }

    @Test
    public void testDamageWithModifierNegative() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        entity.addComponent(new DrawComponent(ANIMATION_PATH));
        StatsComponent statsComponent = new StatsComponent();
        entity.addComponent(statsComponent);
        statsComponent.multiplier(DamageType.PHYSICAL, -2);

        HealthComponent healthComponent = new HealthComponent();
        entity.addComponent(healthComponent);
        healthComponent.maximalHealthpoints(200);
        healthComponent.currentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        Game.add(system);
        Game.add(entity);

        system.execute();

        assertEquals(120, healthComponent.currentHealthpoints()); // 100 - 10 * -2
    }

    @Test
    public void testDamageWithModifierZero() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        entity.addComponent(new DrawComponent(ANIMATION_PATH));
        StatsComponent statsComponent = new StatsComponent();
        entity.addComponent(statsComponent);
        statsComponent.multiplier(DamageType.PHYSICAL, 0);

        HealthComponent healthComponent = new HealthComponent();
        entity.addComponent(healthComponent);
        healthComponent.maximalHealthpoints(200);
        healthComponent.currentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        Game.add(system);
        Game.add(entity);

        system.execute();

        assertEquals(100, healthComponent.currentHealthpoints()); // 100 - 10 * 0
    }

    @Test
    public void testDamageWithModifierHuge() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        entity.addComponent(new DrawComponent(ANIMATION_PATH));
        StatsComponent statsComponent = new StatsComponent();
        entity.addComponent(statsComponent);
        statsComponent.multiplier(DamageType.PHYSICAL, 100);

        HealthComponent healthComponent = new HealthComponent();
        entity.addComponent(healthComponent);
        healthComponent.maximalHealthpoints(200);
        healthComponent.currentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        Game.add(system);
        Game.add(entity);

        system.execute();

        assertTrue(
                "Entity should have 0 ore less health points.",
                healthComponent.currentHealthpoints() <= 0); // 100 - 10 * 100
    }
}
