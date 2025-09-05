package contrib.systems;

import static org.junit.jupiter.api.Assertions.*;

import contrib.components.HealthComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.DirectionalState;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** WTF? . */
public class HealthSystemTest {
  private static final IPath ANIMATION_PATH = new SimpleIPath("textures/test_hero/test_hero.png");
  private DrawComponent animationComponent;

  /** WTF? . */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  /** WTF? . */
  @BeforeEach
  public void setup() {
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(ANIMATION_PATH);
    State stIdle = new DirectionalState("idle", animationMap);
    State stMove = new DirectionalState("move", animationMap, "run");
    State stHit = State.fromMap(animationMap, "hit");
    State stDead = new State("dead", animationMap.get("die"));
    StateMachine sm = new StateMachine(Arrays.asList(stIdle, stMove, stHit, stDead));
    sm.addTransition(stIdle, "move", stMove);
    sm.addTransition(stIdle, "hit", stHit);
    sm.addTransition(stIdle, "die", stDead);

    sm.addTransition(stMove, "idle", stIdle);
    sm.addTransition(stMove, "move", stMove);
    sm.addTransition(stMove, "hit", stHit);
    sm.addTransition(stMove, "die", stDead);

    sm.addTransition(stHit, "die", stDead);
    animationComponent = new DrawComponent(sm);
  }

  /** WTF? . */
  @Test
  public void updateEntityDies() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    Consumer<Entity> onDeath = entity1 -> Game.remove(entity1);
    HealthComponent component = new HealthComponent(1, onDeath);
    entity.add(animationComponent);
    entity.add(component);
    Game.add(entity);
    HealthSystem system = new HealthSystem();
    Game.add(system);
    component.currentHealthpoints(0);

    system.execute();
    assertEquals("dead", animationComponent.currentState().name);
    assertFalse(Game.levelEntities().anyMatch(e -> e == entity));
  }

  /** WTF? . */
  @Test
  public void updateEntityDiesGodMode() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
    HealthComponent component = new HealthComponent(1, onDeath);
    component.godMode(true);
    entity.add(animationComponent);
    entity.add(component);
    Game.add(entity);
    HealthSystem system = new HealthSystem();
    Game.add(system);
    component.currentHealthpoints(0);
    system.execute();
    assertNotEquals("dead", animationComponent.currentState().name);
    assertTrue(Game.levelEntities().anyMatch(e -> e == entity));
  }

  /** WTF? . */
  @Test
  public void updateEntityGetDamage() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
    HealthComponent component = new HealthComponent(10, onDeath);
    entity.add(animationComponent);
    entity.add(component);
    Game.add(entity);
    component.receiveHit(new Damage(5, DamageType.FIRE, null));
    component.receiveHit(new Damage(2, DamageType.FIRE, null));
    HealthSystem system = new HealthSystem();
    Game.add(system);

    system.execute();
    assertEquals(3, component.currentHealthpoints());
    assertEquals("hit", animationComponent.currentState().name);
  }

  /** WTF? . */
  @Test
  public void updateEntityGetNegativeDamage() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
    HealthComponent component = new HealthComponent(10, onDeath);
    entity.add(animationComponent);
    entity.add(component);
    Game.add(entity);
    component.currentHealthpoints(3);
    component.receiveHit(new Damage(-3, DamageType.FIRE, null));
    HealthSystem system = new HealthSystem();
    Game.add(system);
    system.execute();
    assertEquals(6, component.currentHealthpoints());
    assertNotEquals("hit", animationComponent.currentState().name);
  }

  /** WTF? . */
  @Test
  public void updateEntityGetZeroDamage() {
    Game.removeAllEntities();
    Entity entity = new Entity();
    Consumer<Entity> onDeath = Mockito.mock(Consumer.class);
    HealthComponent component = new HealthComponent(10, onDeath);
    entity.add(animationComponent);
    entity.add(component);
    Game.add(entity);
    component.receiveHit(new Damage(0, DamageType.FIRE, null));
    HealthSystem system = new HealthSystem();
    Game.add(system);
    system.execute();
    assertEquals(10, component.currentHealthpoints());
    assertNotEquals("hit", animationComponent.currentState().name);
  }

  /** WTF? . */
  @Test
  public void updateWithoutHealthComponent() {
    Game.removeAllEntities();
    HealthSystem system = new HealthSystem();
    Game.add(system);
    system.execute();
  }
}
