package ecs.systems;

import static org.junit.Assert.*;

import ecs.components.AnimationComponent;
import ecs.components.HealthComponent;
import ecs.components.IOnDeathFunction;
import ecs.components.MissingComponentException;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import graphic.Animation;
import mydungeon.ECS;
import org.junit.Test;
import org.mockito.Mockito;

public class HealthSystemTest {

    @Test
    public void updateEntityDies() {
        ECS.entities.clear();
        ECS.systems = new SystemController();
        Entity entity = new Entity();
        IOnDeathFunction onDeath = Mockito.mock(IOnDeathFunction.class);
        Animation dieAnimation = Mockito.mock(Animation.class);
        AnimationComponent ac = new AnimationComponent(entity);
        HealthComponent component = new HealthComponent(entity, 1, onDeath, null, dieAnimation);
        HealthSystem system = new HealthSystem();
        component.setCurrentHealthpoints(0);
        system.update();
        assertEquals(dieAnimation, ac.getCurrentAnimation());
        assertTrue(ECS.entitiesToRemove.contains(entity));
    }

    @Test
    public void updateEntityGetDamage() {
        ECS.entities.clear();
        ECS.systems = new SystemController();
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
        ECS.entities.clear();
        ECS.systems = new SystemController();
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
        ECS.entities.clear();
        ECS.systems = new SystemController();
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
        ECS.entities.clear();
        ECS.systems = new SystemController();
        Entity entity = new Entity();
        HealthSystem system = new HealthSystem();
        system.update();
    }

    @Test
    public void updateWithoutAnimationComponent() {
        ECS.entities.clear();
        ECS.systems = new SystemController();
        Entity entity = new Entity();
        HealthComponent component = new HealthComponent(entity);
        HealthSystem system = new HealthSystem();
        assertThrows(MissingComponentException.class, () -> system.update());
    }
}
