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

import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.function.Consumer;
import java.io.IOException;

public class HealthSystemTest {
    private static final String ANIMATION_PATH = "character/knight";

    @Test
    public void updateEntityDies() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
        DrawComponent ac = new DrawComponent(entity, ANIMATION_PATH);
        HealthComponent component = new HealthComponent(entity, 1, onDeath);
        HealthSystem system = new HealthSystem();
        component.setCurrentHealthpoints(0);
        system.showEntity(entity);

        system.execute();
        assertTrue(ac.isCurrentAnimation(AdditionalAnimations.DIE));
        assertFalse(Game.getEntitiesStream().anyMatch(e -> e == entity));
    }

    @Test
    public void updateEntityGetDamage() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
        DrawComponent ac = new DrawComponent(entity, ANIMATION_PATH);
        HealthComponent component = new HealthComponent(entity, 10, onDeath);
        component.receiveHit(new Damage(5, DamageType.FIRE, null));
        component.receiveHit(new Damage(2, DamageType.FIRE, null));
        HealthSystem system = new HealthSystem();
        system.showEntity(entity);

        system.execute();
        assertEquals(3, component.getCurrentHealthpoints());
        assertTrue(ac.isCurrentAnimation(AdditionalAnimations.HIT));
    }

    @Test
    public void updateEntityGetNegativeDamage() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
        DrawComponent ac = new DrawComponent(entity, ANIMATION_PATH);
        HealthComponent component = new HealthComponent(entity, 10, onDeath);
        component.setCurrentHealthpoints(3);
        component.receiveHit(new Damage(-3, DamageType.FIRE, null));
        HealthSystem system = new HealthSystem();
        system.showEntity(entity);
        system.execute();
        assertEquals(6, component.getCurrentHealthpoints());
        assertFalse(ac.isCurrentAnimation(AdditionalAnimations.HIT));
    }

    @Test
    public void updateEntityGetZeroDamage() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
        DrawComponent ac = new DrawComponent(entity, ANIMATION_PATH);
        HealthComponent component = new HealthComponent(entity, 10, onDeath);
        component.receiveHit(new Damage(0, DamageType.FIRE, null));
        HealthSystem system = new HealthSystem();
        system.showEntity(entity);
        system.execute();
        assertEquals(10, component.getCurrentHealthpoints());
        assertFalse(ac.isCurrentAnimation(AdditionalAnimations.HIT));
    }

    @Test
    public void updateWithoutHealthComponent() {
        Game.removeAllEntities();
        HealthSystem system = new HealthSystem();
        system.execute();
    }

    @Test
    public void testDamageWithModifier() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        new DrawComponent(entity, ANIMATION_PATH);
        StatsComponent statsComponent = new StatsComponent(entity);
        statsComponent.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, 2);

        HealthComponent healthComponent = new HealthComponent(entity);
        healthComponent.setMaximalHealthpoints(100);
        healthComponent.setCurrentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        system.showEntity(entity);

        system.execute();

        assertEquals(80, healthComponent.getCurrentHealthpoints()); // 100 - 10 * 2
    }

    @Test
    public void testDamageWithModifierNegative() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        new DrawComponent(entity, ANIMATION_PATH);
        StatsComponent statsComponent = new StatsComponent(entity);
        statsComponent.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, -2);

        HealthComponent healthComponent = new HealthComponent(entity);
        healthComponent.setMaximalHealthpoints(200);
        healthComponent.setCurrentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        system.showEntity(entity);

        system.execute();

        assertEquals(120, healthComponent.getCurrentHealthpoints()); // 100 - 10 * -2
    }

    @Test
    public void testDamageWithModifierZero() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        new DrawComponent(entity, ANIMATION_PATH);
        StatsComponent statsComponent = new StatsComponent(entity);
        statsComponent.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, 0);

        HealthComponent healthComponent = new HealthComponent(entity);
        healthComponent.setMaximalHealthpoints(200);
        healthComponent.setCurrentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        system.showEntity(entity);

        system.execute();

        assertEquals(100, healthComponent.getCurrentHealthpoints()); // 100 - 10 * 0
    }

    @Test
    public void testDamageWithModifierHuge() throws IOException {
        Game.removeAllEntities();
        Entity entity = new Entity();
        new DrawComponent(entity, ANIMATION_PATH);
        StatsComponent statsComponent = new StatsComponent(entity);
        statsComponent.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, 100);

        HealthComponent healthComponent = new HealthComponent(entity);
        healthComponent.setMaximalHealthpoints(200);
        healthComponent.setCurrentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        system.showEntity(entity);

        system.execute();

        assertTrue(
                "Entity should have 0 ore less health points.",
                healthComponent.getCurrentHealthpoints() <= 0); // 100 - 10 * 100
    }
}
