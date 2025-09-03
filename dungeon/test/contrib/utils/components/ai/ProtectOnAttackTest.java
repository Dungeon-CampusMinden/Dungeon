package contrib.utils.components.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.systems.AISystem;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.ProtectOnAttack;
import contrib.utils.components.health.Damage;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** WTF? . */
public class ProtectOnAttackTest {

  private AISystem system;
  private Entity protector;

  private Entity protectedEntity;
  private Entity attacker;
  private List<Entity> entitiesToProtect;
  private HealthComponent entityHC;
  private int updateCounter;

  /** WTF? . */
  @BeforeEach
  public void setup() {
    // Get a protector
    protector = new Entity();
    Game.add(protector);

    // Get a victim and its HealthComponent
    protectedEntity = new Entity();
    Game.add(protectedEntity);
    entityHC = new HealthComponent();
    protectedEntity.add(entityHC);

    // Get an attacker
    attacker = new Entity();
    attacker.add(new PlayerComponent(true));

    // Prepare a list of entities with a HealthComponent
    entitiesToProtect = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      Entity e = new Entity();
      e.add(new HealthComponent());
      entitiesToProtect.add(e);
      Game.add(e);
    }
    updateCounter = 0;
    system = new AISystem();
  }

  /** WTF? . */
  @AfterEach
  public void cleanup() {
    Game.removeAllSystems();
    Game.removeAllEntities();
  }

  /** Add one entity to transition and inflict damage. */
  @Test
  public void oneEntityAdded() {
    // given
    AIComponent attackerAI =
        new AIComponent(
            entity -> {
              updateCounter++;
            },
            new RadiusWalk(2, 2),
            new ProtectOnAttack(protectedEntity));
    protector.add(attackerAI);
    // when
    entityHC.receiveHit(new Damage(1, null, attacker));

    // then
    system.execute();
    assertEquals(1, updateCounter);
  }

  /** Add one entity to transition and inflict no damage. */
  @Test
  public void oneEntityAddedWithoutDamage() {
    // given
    AIComponent attackerAI =
        new AIComponent(
            entity -> {
              updateCounter++;
            },
            new RadiusWalk(2, 2),
            new ProtectOnAttack(protectedEntity));
    protector.add(attackerAI);

    // then
    system.execute();
    assertEquals(0, updateCounter);
  }

  /** Add a list of entities to the transition and inflict damage to all. */
  @Test
  public void dmgToAllEntities() {
    // given
    AIComponent attackerAI =
        new AIComponent(
            entity -> {
              updateCounter++;
            },
            new RadiusWalk(2, 2),
            new ProtectOnAttack(entitiesToProtect));
    protector.add(attackerAI);
    // when
    for (Entity e : entitiesToProtect) {
      if (e.isPresent(HealthComponent.class)) {
        HealthComponent hCp = e.fetch(HealthComponent.class).get();
        hCp.receiveHit(new Damage(1, null, attacker));
      }
    }

    // then
    system.execute();
    assertEquals(1, updateCounter);
  }

  /** Add an empty list of entities to the transition. */
  @Test
  public void emptyListOfEntities() {
    List<Entity> emptyList = new ArrayList<>();
    // given
    AIComponent attackerAI =
        new AIComponent(
            entity -> {
              updateCounter++;
            },
            new RadiusWalk(2, 2),
            new ProtectOnAttack(protectedEntity));

    protector.add(attackerAI);
    // then
    system.execute();
    assertEquals(0, updateCounter);
  }
}
