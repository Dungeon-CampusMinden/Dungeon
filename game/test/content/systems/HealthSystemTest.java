package content.systems;

import static org.junit.Assert.*;

import api.Entity;
import api.Game;
import api.components.DrawComponent;
import api.utils.component_utils.MissingComponentException;
import api.utils.component_utils.drawComponent.Animation;
import api.utils.component_utils.healthComponent.Damage;
import api.utils.component_utils.healthComponent.DamageType;
import api.utils.component_utils.healthComponent.IOnDeathFunction;
import api.utils.controller.SystemController;
import content.component.HealthComponent;
import content.component.StatsComponent;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;

public class HealthSystemTest {

    @Test
    public void updateEntityDies() {
        Game.getDelayedEntitySet().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        Game.getDelayedEntitySet().update();
        IOnDeathFunction onDeath = Mockito.mock(IOnDeathFunction.class);
        Animation dieAnimation = new Animation(List.of("FRAME1"), 1, false);
        DrawComponent ac = new DrawComponent(entity);
        HealthComponent component = new HealthComponent(entity, 1, onDeath, null, dieAnimation);
        HealthSystem system = new HealthSystem();
        component.setCurrentHealthpoints(0);
        system.update();
        assertEquals(dieAnimation, ac.getCurrentAnimation());
        Game.getDelayedEntitySet().update();
        assertFalse(Game.getEntities().contains(entity));
    }

    @Test
    public void updateEntityGetDamage() {
        Game.getDelayedEntitySet().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        Game.getDelayedEntitySet().update();
        IOnDeathFunction onDeath = Mockito.mock(IOnDeathFunction.class);
        Animation hitAnimation = Mockito.mock(Animation.class);
        DrawComponent ac = new DrawComponent(entity);
        HealthComponent component = new HealthComponent(entity, 10, onDeath, hitAnimation, null);
        component.receiveHit(new Damage(5, DamageType.FIRE, null));
        component.receiveHit(new Damage(2, DamageType.FIRE, null));
        HealthSystem system = new HealthSystem();
        system.update();
        assertEquals(3, component.getCurrentHealthpoints());
        assertEquals(hitAnimation, ac.getCurrentAnimation());
    }

    @Test
    public void updateEntityGetNegativeDamage() {
        Game.getDelayedEntitySet().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        Game.getDelayedEntitySet().update();
        IOnDeathFunction onDeath = Mockito.mock(IOnDeathFunction.class);
        Animation hitAnimation = Mockito.mock(Animation.class);
        DrawComponent ac = new DrawComponent(entity);
        HealthComponent component = new HealthComponent(entity, 10, onDeath, hitAnimation, null);
        component.setCurrentHealthpoints(3);
        component.receiveHit(new Damage(-3, DamageType.FIRE, null));
        HealthSystem system = new HealthSystem();
        system.update();
        assertEquals(6, component.getCurrentHealthpoints());
        assertNotEquals(hitAnimation, ac.getCurrentAnimation());
    }

    @Test
    public void updateEntityGetZeroDamage() {
        Game.getDelayedEntitySet().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        Game.getDelayedEntitySet().update();
        IOnDeathFunction onDeath = Mockito.mock(IOnDeathFunction.class);
        Animation hitAnimation = Mockito.mock(Animation.class);
        DrawComponent ac = new DrawComponent(entity);
        HealthComponent component = new HealthComponent(entity, 10, onDeath, hitAnimation, null);
        component.receiveHit(new Damage(0, DamageType.FIRE, null));
        HealthSystem system = new HealthSystem();
        system.update();
        assertEquals(10, component.getCurrentHealthpoints());
        assertNotEquals(hitAnimation, ac.getCurrentAnimation());
    }

    @Test
    public void updateWithoutHealthComponent() {
        Game.getDelayedEntitySet().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        Game.getDelayedEntitySet().update();
        HealthSystem system = new HealthSystem();
        system.update();
    }

    @Test
    public void updateWithoutAnimationComponent() {
        Game.getDelayedEntitySet().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        Game.getDelayedEntitySet().update();
        HealthComponent component = new HealthComponent(entity);
        HealthSystem system = new HealthSystem();
        assertThrows(MissingComponentException.class, () -> system.update());
    }

    @Test
    public void testDamageWithModifier() {
        Game.getDelayedEntitySet().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        Game.getDelayedEntitySet().update();
        new DrawComponent(entity);
        StatsComponent statsComponent = new StatsComponent(entity);
        statsComponent.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, 2);

        HealthComponent healthComponent = new HealthComponent(entity);
        healthComponent.setMaximalHealthpoints(100);
        healthComponent.setCurrentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        system.update();

        assertEquals(80, healthComponent.getCurrentHealthpoints()); // 100 - 10 * 2
    }

    @Test
    public void testDamageWithModifierNegative() {
        Game.getDelayedEntitySet().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        Game.getDelayedEntitySet().update();
        new DrawComponent(entity);
        StatsComponent statsComponent = new StatsComponent(entity);
        statsComponent.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, -2);

        HealthComponent healthComponent = new HealthComponent(entity);
        healthComponent.setMaximalHealthpoints(200);
        healthComponent.setCurrentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        system.update();

        assertEquals(120, healthComponent.getCurrentHealthpoints()); // 100 - 10 * -2
    }

    @Test
    public void testDamageWithModifierZero() {
        Game.getDelayedEntitySet().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        Game.getDelayedEntitySet().update();
        new DrawComponent(entity);
        StatsComponent statsComponent = new StatsComponent(entity);
        statsComponent.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, 0);

        HealthComponent healthComponent = new HealthComponent(entity);
        healthComponent.setMaximalHealthpoints(200);
        healthComponent.setCurrentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        system.update();

        assertEquals(100, healthComponent.getCurrentHealthpoints()); // 100 - 10 * 0
    }

    @Test
    public void testDamageWithModifierHuge() {
        Game.getDelayedEntitySet().clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        Game.getDelayedEntitySet().update();
        new DrawComponent(entity);
        StatsComponent statsComponent = new StatsComponent(entity);
        statsComponent.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, 100);

        HealthComponent healthComponent = new HealthComponent(entity);
        healthComponent.setMaximalHealthpoints(200);
        healthComponent.setCurrentHealthpoints(100);
        healthComponent.receiveHit(new Damage(10, DamageType.PHYSICAL, null));

        HealthSystem system = new HealthSystem();
        system.update();

        assertTrue(
                "Entity should have 0 ore less health points.",
                healthComponent.getCurrentHealthpoints() <= 0); // 100 - 10 * 100
    }
}
