package contrib.systems;

import static org.junit.Assert.*;

import contrib.components.HealthComponent;
import contrib.utils.components.draw.AdditionalAnimations;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class HealthSystemTest {
  private static final IPath ANIMATION_PATH = new SimpleIPath("textures/test_hero");

  @After
  public void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  @Ignore
  @Test
  public void updateEntityDies() throws IOException {
    Game.removeAllEntities();
    Entity entity = new Entity();
    Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
    DrawComponent ac = new DrawComponent(ANIMATION_PATH);
    HealthComponent component = new HealthComponent(1, onDeath);
    entity.add(ac);
    entity.add(component);
    Game.add(entity);
    HealthSystem system = new HealthSystem();
    Game.add(system);
    component.currentHealthpoints(0);

    system.execute();
    assertTrue(ac.isAnimationQueued(AdditionalAnimations.DIE));
    assertFalse(Game.entityStream().anyMatch(e -> e == entity));
  }

  @Test
  public void updateEntityDiesGodMode() throws IOException {
    Game.removeAllEntities();
    Entity entity = new Entity();
    Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
    DrawComponent ac = new DrawComponent(ANIMATION_PATH);
    HealthComponent component = new HealthComponent(1, onDeath);
    component.godMode(true);
    entity.add(ac);
    entity.add(component);
    Game.add(entity);
    HealthSystem system = new HealthSystem();
    Game.add(system);
    component.currentHealthpoints(0);
    system.execute();
    assertFalse(
        "Entity can not die, so dont show die animation",
        ac.isCurrentAnimation(AdditionalAnimations.DIE));
    assertTrue("Entity should still be in game.", Game.entityStream().anyMatch(e -> e == entity));
  }

  @Ignore
  @Test
  public void updateEntityGetDamage() throws IOException {
    Game.removeAllEntities();
    Entity entity = new Entity();
    Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
    DrawComponent ac = new DrawComponent(ANIMATION_PATH);
    HealthComponent component = new HealthComponent(10, onDeath);
    entity.add(ac);
    entity.add(component);
    Game.add(entity);
    component.receiveHit(new Damage(5, DamageType.FIRE, null));
    component.receiveHit(new Damage(2, DamageType.FIRE, null));
    HealthSystem system = new HealthSystem();
    Game.add(system);

    system.execute();
    assertEquals(3, component.currentHealthpoints());
    assertTrue(ac.isAnimationQueued(AdditionalAnimations.HIT));
  }

  @Test
  public void updateEntityGetNegativeDamage() throws IOException {
    Game.removeAllEntities();
    Entity entity = new Entity();
    Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
    DrawComponent ac = new DrawComponent(ANIMATION_PATH);
    HealthComponent component = new HealthComponent(10, onDeath);
    entity.add(ac);
    entity.add(component);
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
    entity.add(ac);
    entity.add(component);
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
}
