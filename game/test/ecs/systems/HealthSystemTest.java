package ecs.systems;

import static org.junit.Assert.*;

import controller.SystemController;
import ecs.components.AnimationComponent;
import ecs.components.HealthComponent;
import ecs.components.IOnDeathFunction;
import ecs.components.MissingComponentException;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import graphic.Animation;
import org.junit.Test;
import org.mockito.Mockito;
import starter.Game;

public class HealthSystemTest {

    @Test
    public void updateEntityDies() {
        Game.entities.clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        IOnDeathFunction onDeath = Mockito.mock(IOnDeathFunction.class);
        Animation dieAnimation = Mockito.mock(Animation.class);
        AnimationComponent ac = new AnimationComponent(entity);
        HealthComponent component = new HealthComponent(entity, 1, onDeath, null, dieAnimation);
        HealthSystem system = new HealthSystem();
        component.setCurrentHealthpoints(0);
        system.update();
        assertEquals(dieAnimation, ac.getCurrentAnimation());
        assertTrue(Game.entitiesToRemove.contains(entity));
    }

    @Test
    public void updateEntityGetDamage() {
        Game.entities.clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        IOnDeathFunction onDeath = Mockito.mock(IOnDeathFunction.class);
        Animation hitAnimation = Mockito.mock(Animation.class);
        AnimationComponent ac = new AnimationComponent(entity);
        HealthComponent component = new HealthComponent(entity, 10, onDeath, hitAnimation, null);
        component.receiveHit(new Damage(5, DamageType.FIRE));
        component.receiveHit(new Damage(2, DamageType.FIRE));
        HealthSystem system = new HealthSystem();
        system.update();
        assertEquals(3, component.getCurrentHealthpoints());
        assertEquals(hitAnimation, ac.getCurrentAnimation());
    }

    @Test
    public void updateEntityGetNegativeDamage() {
        Game.entities.clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        IOnDeathFunction onDeath = Mockito.mock(IOnDeathFunction.class);
        Animation hitAnimation = Mockito.mock(Animation.class);
        AnimationComponent ac = new AnimationComponent(entity);
        HealthComponent component = new HealthComponent(entity, 10, onDeath, hitAnimation, null);
        component.setCurrentHealthpoints(3);
        component.receiveHit(new Damage(-3, DamageType.FIRE));
        HealthSystem system = new HealthSystem();
        system.update();
        assertEquals(6, component.getCurrentHealthpoints());
        assertNotEquals(hitAnimation, ac.getCurrentAnimation());
    }

    @Test
    public void updateEntityGetZeroDamage() {
        Game.entities.clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        IOnDeathFunction onDeath = Mockito.mock(IOnDeathFunction.class);
        Animation hitAnimation = Mockito.mock(Animation.class);
        AnimationComponent ac = new AnimationComponent(entity);
        HealthComponent component = new HealthComponent(entity, 10, onDeath, hitAnimation, null);
        component.receiveHit(new Damage(0, DamageType.FIRE));
        HealthSystem system = new HealthSystem();
        system.update();
        assertEquals(10, component.getCurrentHealthpoints());
        assertNotEquals(hitAnimation, ac.getCurrentAnimation());
    }

    @Test
    public void updateWithoutHealthComponent() {
        Game.entities.clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        HealthSystem system = new HealthSystem();
        system.update();
    }

    @Test
    public void updateWithoutAnimationComponent() {
        Game.entities.clear();
        Game.systems = new SystemController();
        Entity entity = new Entity();
        HealthComponent component = new HealthComponent(entity);
        HealthSystem system = new HealthSystem();
        assertThrows(MissingComponentException.class, () -> system.update());
    }
}
