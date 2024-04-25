package contrib.utils.components.skill;

import static org.junit.Assert.*;

import core.Entity;
import core.Game;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** WTF? . */
public class SkillTest {

  private static int value = 0;
  private final int baseCoolDownInMilliSeconds = 2000;
  private Entity entity;
  private Skill skill;
  private final Consumer<Entity> skillFunction = entity -> value++;

  /** WTF? . */
  @Before
  public void setup() {
    entity = new Entity();
    skill = new Skill(skillFunction, baseCoolDownInMilliSeconds);
  }

  /** WTF? . */
  @After
  public void cleanup() {
    value = 0;
    Game.removeAllEntities();
  }

  /** WTF? . */
  @Test
  public void execute() {
    assertTrue("Skill should be executable", skill.canBeUsedAgain());
    skill.execute(entity);
    assertEquals("Skill should have been executed once", 1, value);
    assertFalse("Skill is in cool down and can not be executable", skill.canBeUsedAgain());
  }

  /** WTF? . */
  @Test
  public void executeWhenCoolDownActive() {
    skill.execute(entity);
    assertFalse("Skill should not be executable", skill.canBeUsedAgain());
    skill.execute(entity);
    assertEquals("Skill should have been executed once", 1, value);
  }

  /** WTF? . */
  @Test
  public void executeWhenCoolDownExpired() throws InterruptedException {
    final long baseCoolDown = 1;
    skill = new Skill(skillFunction, baseCoolDown);
    skill.execute(entity);
    assertEquals("Skill should have been executed once", 1, value);
    Thread.sleep(5);
    assertTrue("Skill should be usable again", skill.canBeUsedAgain());
    skill.execute(entity);
    assertEquals("Skill should have been executed twice", 2, value);
  }
}
