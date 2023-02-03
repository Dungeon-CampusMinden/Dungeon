package ecs.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import graphic.Animation;
import mydungeon.ECS;
import org.junit.Test;
import org.mockito.Mockito;

public class HealthComponentTest {

    @Test
    public void getHit() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        Damage dmg = new Damage(3, DamageType.FIRE);
        hc.getHit(dmg);
        assertTrue(hc.getDamageList().contains(dmg));
    }

    @Test
    public void setMaximalHitPointsLowerThanCurrent() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity, 10, null, null, null);
        assertEquals(10, hc.getMaximalHitPoints());
        assertEquals(10, hc.getCurrentHitPoints());
        hc.setMaximalHitPoints(8);
        assertEquals(8, hc.getMaximalHitPoints());
        assertEquals(8, hc.getCurrentHitPoints());
    }

    @Test
    public void setMaximalHitPointsHigherThanCurrent() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity, 10, null, null, null);
        assertEquals(10, hc.getMaximalHitPoints());
        assertEquals(10, hc.getCurrentHitPoints());
        hc.setMaximalHitPoints(12);
        assertEquals(12, hc.getMaximalHitPoints());
        assertEquals(10, hc.getCurrentHitPoints());
    }

    @Test
    public void setCurrentHitPointsHigherThanMaxmimum() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity, 10, null, null, null);
        hc.setCurrentHitPoints(12);
        assertEquals(10, hc.getCurrentHitPoints());
    }

    @Test
    public void setCurrentHitPointsLowerThanMaxmimum() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity, 10, null, null, null);
        hc.setCurrentHitPoints(8);
        assertEquals(8, hc.getCurrentHitPoints());
    }

    @Test
    public void triggerOnDeath() {
        ECS.entities.clear();
        Entity entity = new Entity();
        IOnDeathFunction onDeathFunction = Mockito.mock(IOnDeathFunction.class);
        HealthComponent hc = new HealthComponent(entity, 10, onDeathFunction, null, null);
        hc.triggerOnDeath();
        Mockito.verify(onDeathFunction, times(1)).onDeath(entity);
    }

    @Test
    public void setDieAnimation() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        Animation animation = Mockito.mock(Animation.class);
        hc.setDieAnimation(animation);
        assertEquals(animation, hc.getDieAnimation());
    }

    @Test
    public void setGetHitAnimation() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        Animation animation = Mockito.mock(Animation.class);
        hc.setGetHitAnimation(animation);
        assertEquals(animation, hc.getGetHitAnimation());
    }

    @Test
    public void setOnDeathFunction() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        IOnDeathFunction function = Mockito.mock(IOnDeathFunction.class);
        hc.setOnDeath(function);
        hc.triggerOnDeath();
        Mockito.verify(function, times(1)).onDeath(entity);
    }
}
