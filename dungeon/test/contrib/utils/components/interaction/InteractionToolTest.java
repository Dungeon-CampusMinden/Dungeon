package contrib.utils.components.interaction;

import static org.junit.jupiter.api.Assertions.*;

import contrib.components.InteractionComponent;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import org.junit.jupiter.api.Test;
import testingUtils.SimpleCounter;

/** WTF? . */
public class InteractionToolTest {

  private static ILevel prepareLevel() {
    LevelElement[][] layout = new LevelElement[5][5];
    for (int y = 0; y < 5; y++) {
      for (int x = 0; x < 5; x++) {
        layout[y][x] = LevelElement.FLOOR;
      }
    }
    return new DungeonLevel(layout, DesignLabel.DEFAULT);
  }

  /**
   * Helper Method which prepares a mock, of the Hero which can´t be created using the constructor.
   *
   * @param havingPositionComponent if the Hero should have the PositionComponent
   * @return the Mocked Hero
   */
  private static Entity testHero(boolean havingPositionComponent) {
    Entity hero = new Entity();
    hero.add(new PlayerComponent(true));
    if (havingPositionComponent) hero.add(new PositionComponent(new Point(0, 0)));
    return hero;
  }

  /** Cleanup to reset static Attributes from Game used by the InteractionTool. */
  private static void cleanup() {
    Game.removeAllEntities();
    Game.currentLevel(null);
  }

  /** Tests the functionality when the Hero does not have the PositionComponent. */
  @Test
  public void interactWithClosestInteractableHeroMissingPositionComponent() {
    cleanup();
    Game.add(testHero(false));
    Game.currentLevel(prepareLevel());

    MissingComponentException e =
        assertThrows(
            MissingComponentException.class,
            () -> InteractionTool.interactWithClosestInteractable(Game.hero().get()));
    assertTrue(e.getMessage().contains(PositionComponent.class.getName()));
    cleanup();
  }

  /** Tests the functionality when there are no Entities in the Game. */
  @Test
  public void interactWithClosestInteractableNoEntities() {
    cleanup();
    Game.add(testHero(true));
    Game.currentLevel(prepareLevel());
    InteractionTool.interactWithClosestInteractable(Game.hero().get());
    cleanup();
  }

  /**
   * Tests the functionality when there are no Entities with the interactionComponent in the Game.
   */
  @Test
  public void interactWithClosestInteractableNoInteractable() {
    cleanup();
    Game.add(testHero(true));
    Game.currentLevel(prepareLevel());
    Game.add(Game.hero().get());
    InteractionTool.interactWithClosestInteractable(Game.hero().get());
    cleanup();
  }

  /**
   * Tests the functionality when there is exactly one Entity in the Game with the
   * InteractionComponent and not in Radius.
   */
  @Test
  public void interactWithClosestInteractableOneInteractableOutOfRange() {
    cleanup();
    Game.add(testHero(true));
    Game.currentLevel(prepareLevel());

    Entity e = new Entity();
    e.add(new PositionComponent(new Point(10, 10)));

    SimpleCounter sc_e = new SimpleCounter();
    e.add(new InteractionComponent(5f, false, (x, who) -> sc_e.inc()));

    InteractionTool.interactWithClosestInteractable(Game.hero().get());
    assertEquals(0, sc_e.getCount());

    cleanup();
  }

  /*
   * Tests the functionality when there is exactly one Entity in the Game with the
   * InteractionComponent and n range.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the game loop, this is testcase
   * cant be tested.
   */
  /* @Test
  public void interactWithClosestInteractableOneInteractableInRange() {
      cleanup();
      Game.setHero(testHero(true));
      Game.currentLevel = prepareLevel();

      Entity e = new Entity();
      new PositionComponent(e, new Point(3, 0));

      SimpleCounter sc_e = new SimpleCounter();
      new InteractionComponent(e, 5f, false, (x) -> sc_e.inc());

      InteractionTool.interactWithClosestInteractable(Game.getHero().get());
      assertEquals("One interaction should happen", 1, sc_e.getCount());

      cleanup();
  }*/

  /*
   * Test if the interactable is missing the PositionComponent.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the game loop, this is testcase
   * cant be tested.
   */
  /* @Test
  public void interactWithClosestInteractableOneInteractableInRangeMissingPosition() {
      cleanup();
      Game.setHero(testHero(true));
      Game.currentLevel = prepareLevel();

      Entity e = new Entity();

      SimpleCounter sc_e = new SimpleCounter();
      new InteractionComponent(e, 5f, false, (x) -> sc_e.inc());

      MissingComponentException exception =
              assertThrows(
                      MissingComponentException.class,
                      () ->
                              InteractionTool.interactWithClosestInteractable(
                                      Game.getHero().get()));
      assertTrue(
              "Errormessage should contain information where the Exception was thrown.",
              exception.getMessage().contains(InteractionTool.class.getName()));
      assertTrue(
              "Errormessage should contain information about which class did miss the Component.",
              exception.getMessage().contains(e.getClass().getName()));
      assertTrue(
              "Errormessage should contain information about which Component is missing.",
              exception.getMessage().contains(PositionComponent.class.getName()));

      assertEquals("No interaction should happen", 0, sc_e.getCount());

      cleanup();
  }*/

  /*
   * Test if the interaction happens with the closest entity closer Entity is first in
   * `Game.entities`.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the game loop, this is testcase
   * cant be tested.
   */
  /*@Test
  public void interactWithClosestInteractableClosestEntityFirst() {
      cleanup();
      Game.setHero(testHero(true));
      Game.currentLevel = prepareLevel();
      // distance 2
      Entity eClose = new Entity();
      new PositionComponent(eClose, new Point(2, 0));

      SimpleCounter sc_eClose = new SimpleCounter();
      new InteractionComponent(eClose, 5f, false, (x) -> sc_eClose.inc());

      // distance 3
      Entity eFar = new Entity();
      new PositionComponent(eFar, new Point(3, 0));

      SimpleCounter sc_eFar = new SimpleCounter();
      new InteractionComponent(eFar, 5f, false, (x) -> sc_eFar.inc());

      InteractionTool.interactWithClosestInteractable(Game.getHero().get());
      assertEquals("One interaction should happen", 1, sc_eClose.getCount());
      assertEquals("No interaction should happen", 0, sc_eFar.getCount());

      cleanup();
  }*/

  /*
   * Test if the interaction happens with the closest entity closer Entity is last in
   * `Game.entities`.
   *
   * <p>Since we cant update the {@link Game#entities} from outside the game loop, this is testcase
   * cant be tested.
   */
  /* @Test
  public void interactWithClosestInteractableClosestEntityLast() {
      cleanup();
      Game.setHero(testHero(true));
      Game.currentLevel = prepareLevel();

      // distance 3
      Entity eFar = new Entity();
      PositionComponent pc2 = new PositionComponent(eFar, new Point(3, 0));

      SimpleCounter sc_eFar = new SimpleCounter();
      new InteractionComponent(eFar, 5f, false, (x) -> sc_eFar.inc());

      // distance 2
      Entity eClose = new Entity();
      PositionComponent pc = new PositionComponent(eClose, new Point(2, 0));

      SimpleCounter sc_eClose = new SimpleCounter();
      new InteractionComponent(eClose, 5f, false, (x) -> sc_eClose.inc());

      InteractionTool.interactWithClosestInteractable(Game.getHero().get());
      assertEquals("One interaction should happen", 1, sc_eClose.getCount());
      assertEquals("No interaction should happen", 0, sc_eFar.getCount());

      cleanup();
  }*/
}
