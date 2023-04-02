package ecs.components;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import graphic.Animation;
import org.junit.Test;
import org.mockito.Mockito;
import starter.Game;

public class HealthComponentTest {

    @Test
    public void receiveHit() {
        Game.getEntities().clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        Damage fdmg = new Damage(3, DamageType.FIRE, null);
        Damage fdmg2 = new Damage(5, DamageType.FIRE, null);
        Damage mdmg = new Damage(-1, DamageType.MAGIC, null);
        Damage pdmg = new Damage(4, DamageType.PHYSICAL, null);
        hc.receiveHit(fdmg);
        hc.receiveHit(fdmg2);
        hc.receiveHit(mdmg);
        hc.receiveHit(pdmg);
        assertEquals(fdmg.damageAmount() + fdmg2.damageAmount(), hc.getDamage(DamageType.FIRE));
        assertEquals(mdmg.damageAmount(), hc.getDamage(DamageType.MAGIC));
        assertEquals(pdmg.damageAmount(), hc.getDamage(DamageType.PHYSICAL));
    }

    @Test
    public void testDamageCause() {
        Game.getEntities().clear();
        Entity entity = new Entity();
        Entity damager = new Entity();
        Entity damager2 = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        Damage dmg = new Damage(3, DamageType.FIRE, damager);
        Damage dmg2 = new Damage(5, DamageType.FIRE, damager2);
        hc.receiveHit(dmg);
        hc.receiveHit(dmg2);
        assertEquals(damager2, hc.getLastDamageCause().get());
        hc.receiveHit(dmg);
        assertEquals(damager, hc.getLastDamageCause().get());
    }

    @Test
    public void setMaximalHealthPointsLowerThanCurrent() {
        Game.getEntities().clear();
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
        Game.getEntities().clear();
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
        Game.getEntities().clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity, 10, null, null, null);
        hc.setCurrentHealthpoints(12);
        assertEquals(10, hc.getCurrentHealthpoints());
    }

    @Test
    public void setCurrentHealthPointsLowerThanMaximum() {
        Game.getEntities().clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity, 10, null, null, null);
        hc.setCurrentHealthpoints(8);
        assertEquals(8, hc.getCurrentHealthpoints());
    }

    @Test
    public void triggerOnDeath() {
        Game.getEntities().clear();
        Entity entity = new Entity();
        IOnDeathFunction onDeathFunction = Mockito.mock(IOnDeathFunction.class);
        HealthComponent hc = new HealthComponent(entity, 10, onDeathFunction, null, null);
        hc.triggerOnDeath();
        Mockito.verify(onDeathFunction, times(1)).onDeath(entity);
    }

    @Test
    public void setDieAnimation() {
        Game.getEntities().clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        Animation animation = Mockito.mock(Animation.class);
        hc.setDieAnimation(animation);
        assertEquals(animation, hc.getDieAnimation());
    }

    @Test
    public void setGetHitAnimation() {
        Game.getEntities().clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        Animation animation = Mockito.mock(Animation.class);
        hc.setGetHitAnimation(animation);
        assertEquals(animation, hc.getGetHitAnimation());
    }

    @Test
    public void setOnDeathFunction() {
        Game.getEntities().clear();
        Entity entity = new Entity();
        HealthComponent hc = new HealthComponent(entity);
        IOnDeathFunction function = Mockito.mock(IOnDeathFunction.class);
        hc.setOnDeath(function);
        hc.triggerOnDeath();
        Mockito.verify(function, times(1)).onDeath(entity);
    }
}
