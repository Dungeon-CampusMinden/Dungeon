package contrib.components;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import java.util.function.Consumer;
import org.junit.Test;
import org.mockito.Mockito;

/** Tests for the {@link HealthComponent}. */
public class HealthComponentTest {

  /** WTF? . */
  @Test
  public void receiveHit() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    HealthComponent hc = new HealthComponent();
    entity.add(hc);
    Damage fdmg = new Damage(3, DamageType.FIRE, null);
    Damage fdmg2 = new Damage(5, DamageType.FIRE, null);
    Damage mdmg = new Damage(-1, DamageType.MAGIC, null);
    Damage pdmg = new Damage(4, DamageType.PHYSICAL, null);
    hc.receiveHit(fdmg);
    hc.receiveHit(fdmg2);
    hc.receiveHit(mdmg);
    hc.receiveHit(pdmg);
    assertEquals(fdmg.damageAmount() + fdmg2.damageAmount(), hc.calculateDamageOf(DamageType.FIRE));
    assertEquals(mdmg.damageAmount(), hc.calculateDamageOf(DamageType.MAGIC));
    assertEquals(pdmg.damageAmount(), hc.calculateDamageOf(DamageType.PHYSICAL));
  }

  /** WTF? . */
  @Test
  public void testDamageCause() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    Entity damager = new Entity();
    Entity damager2 = new Entity();
    HealthComponent hc = new HealthComponent();
    entity.add(hc);
    Damage dmg = new Damage(3, DamageType.FIRE, damager);
    Damage dmg2 = new Damage(5, DamageType.FIRE, damager2);
    hc.receiveHit(dmg);
    hc.receiveHit(dmg2);
    assertEquals(damager2, hc.lastDamageCause().get());
    hc.receiveHit(dmg);
    assertEquals(damager, hc.lastDamageCause().get());
  }

  /** WTF? . */
  @Test
  public void setMaximalHealthPointsLowerThanCurrent() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    HealthComponent hc = new HealthComponent(10, null);
    entity.add(hc);
    assertEquals(10, hc.maximalHealthpoints());
    assertEquals(10, hc.currentHealthpoints());
    hc.maximalHealthpoints(8);
    assertEquals(8, hc.maximalHealthpoints());
    assertEquals(8, hc.currentHealthpoints());
  }

  /** WTF? . */
  @Test
  public void setMaximalHealthPointsHigherThanCurrent() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    HealthComponent hc = new HealthComponent(10, null);
    entity.add(hc);
    assertEquals(10, hc.maximalHealthpoints());
    assertEquals(10, hc.currentHealthpoints());
    hc.maximalHealthpoints(12);
    assertEquals(12, hc.maximalHealthpoints());
    assertEquals(10, hc.currentHealthpoints());
  }

  /** WTF? . */
  @Test
  public void setCurrentHealthPointsHigherThanMaximum() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    HealthComponent hc = new HealthComponent(10, null);
    entity.add(hc);
    hc.currentHealthpoints(12);
    assertEquals(10, hc.currentHealthpoints());
  }

  /** WTF? . */
  @Test
  public void setCurrentHealthPointsLowerThanMaximum() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    HealthComponent hc = new HealthComponent(10, null);
    entity.add(hc);
    hc.currentHealthpoints(8);
    assertEquals(8, hc.currentHealthpoints());
  }

  /** WTF? . */
  @Test
  public void triggerOnDeath() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    Consumer<Entity> onDeathFunction = Mockito.mock(Consumer.class);
    HealthComponent hc = new HealthComponent(10, onDeathFunction);
    entity.add(hc);
    hc.triggerOnDeath(entity);
    Mockito.verify(onDeathFunction, times(1)).accept(entity);
  }
}
