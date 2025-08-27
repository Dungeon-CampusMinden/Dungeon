package contrib.utils.components.skill;

import static org.junit.jupiter.api.Assertions.*;

import core.Entity;
import core.Game;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** WTF? . */
public class SkillTest {

  private static int value = 0;
  private final int baseCoolDownInMilliSeconds = 2000;
  private final Consumer<Entity> skillFunction = entity -> value++;
  private Entity entity;
  private OldSkill skill;

  /** WTF? . */
  @BeforeEach
  public void setup() {
    entity = new Entity();
    skill = new OldSkill(skillFunction, baseCoolDownInMilliSeconds);
  }

  /** WTF? . */
  @AfterEach
  public void cleanup() {
    value = 0;
    Game.removeAllEntities();
  }

  /** WTF? . */
  @Test
  public void execute() {
    assertTrue(skill.canBeUsedAgain());
    skill.execute(entity);
    assertEquals(1, value);
    assertFalse(skill.canBeUsedAgain());
  }

  /** WTF? . */
  @Test
  public void executeWhenCoolDownActive() {
    skill.execute(entity);
    assertFalse(skill.canBeUsedAgain());
    skill.execute(entity);
    assertEquals(1, value);
  }

  /** WTF? . */
  @Test
  public void executeWhenCoolDownExpired() throws InterruptedException {
    final long baseCoolDown = 1;
    skill = new OldSkill(skillFunction, baseCoolDown);
    skill.execute(entity);
    assertEquals(1, value);
    Thread.sleep(5);
    assertTrue(skill.canBeUsedAgain());
    skill.execute(entity);
    assertEquals(2, value);
  }
}
