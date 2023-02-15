package ecs.components;

import static org.junit.Assert.assertEquals;
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
    public void receiveHit() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        Damage fdmg = new Damage(3, DamageType.FIRE);
        Damage fdmg2 = new Damage(5, DamageType.FIRE);
        Damage mdmg = new Damage(-1, DamageType.MAGIC);
        Damage pdmg = new Damage(4, DamageType.PHYSICAL);
        hc.receiveHit(fdmg);
        hc.receiveHit(fdmg2);
        hc.receiveHit(mdmg);
        hc.receiveHit(pdmg);
        assertEquals(fdmg.damageAmount() + fdmg2.damageAmount(), hc.getDamage(DamageType.FIRE));
        assertEquals(mdmg.damageAmount(), hc.getDamage(DamageType.MAGIC));
        assertEquals(pdmg.damageAmount(), hc.getDamage(DamageType.PHYSICAL));
    }

    @Test
    public void setMaximalHealthPointsLowerThanCurrent() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity, 10, null, null, null);
        assertEquals(10, hc.getMaximalHealthpoints());
        assertEquals(10, hc.getCurrentHealthpoints());
        hc.setMaximalHealthpoints(8);
        assertEquals(8, hc.getMaximalHealthpoints());
        assertEquals(8, hc.getCurrentHealthpoints());
    }

    @Test
    public void setMaximalHealthPointsHigherThanCurrent() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity, 10, null, null, null);
        assertEquals(10, hc.getMaximalHealthpoints());
        assertEquals(10, hc.getCurrentHealthpoints());
        hc.setMaximalHealthpoints(12);
        assertEquals(12, hc.getMaximalHealthpoints());
        assertEquals(10, hc.getCurrentHealthpoints());
    }

    @Test
    public void setCurrentHealthPointsHigherThanMaximum() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity, 10, null, null, null);
        hc.setCurrentHealthpoints(12);
        assertEquals(10, hc.getCurrentHealthpoints());
    }

    @Test
    public void setCurrentHealthPointsLowerThanMaximum() {
        ECS.entities.clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity, 10, null, null, null);
        hc.setCurrentHealthpoints(8);
        assertEquals(8, hc.getCurrentHealthpoints());
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
