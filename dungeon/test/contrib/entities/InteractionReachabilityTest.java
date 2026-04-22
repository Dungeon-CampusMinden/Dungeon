package contrib.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogType;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import contrib.systems.PositionSyncSystem;
import core.utils.Point;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Regression tests for interactable reachability in the LITIENGINE setup. */
class InteractionReachabilityTest {

  @BeforeEach
  void setUp() {
    Game.add(new LevelSystem());
    Game.add(new PositionSyncSystem());
    Game.currentLevel(createFloorLevel(20, 5));
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
    Game.currentLevel(null);
    Game.removeAllSystems();
  }

  @Test
  void cauldronCanBeInteractedWithFromDiagonalNeighbor() {
    Entity hero = heroAt(new Point(7, 2));
    Game.add(MiscFactory.newCraftingCauldron(new Point(8, 1)));

    HeroController.interact(hero, new Point(8, 1));

    assertDialogType(hero, DialogType.DefaultTypes.CRAFTING_GUI);
  }

  @Test
  void cauldronCannotBeInteractedWithFromTwoTilesAway() {
    Entity hero = heroAt(new Point(6, 1));
    Game.add(MiscFactory.newCraftingCauldron(new Point(8, 1)));

    HeroController.interact(hero, new Point(8, 1));

    assertTrue(hero.fetch(UIComponent.class).isEmpty());
  }

  @Test
  void chestCanBeInteractedWithFromAdjacentNeighbor() {
    Entity hero = heroAt(new Point(11, 1));
    Game.add(MiscFactory.newChest(Set.of(), new Point(12, 1)));

    HeroController.interact(hero, new Point(12, 1));

    assertDialogType(hero, DialogType.DefaultTypes.DUAL_INVENTORY);
  }

  @Test
  void chestCannotBeInteractedWithFromTwoTilesAway() {
    Entity hero = heroAt(new Point(10, 1));
    Game.add(MiscFactory.newChest(Set.of(), new Point(12, 1)));

    HeroController.interact(hero, new Point(12, 1));

    assertTrue(hero.fetch(UIComponent.class).isEmpty());
  }

  private static Entity heroAt(Point position) {
    Entity hero = EntityFactory.newHero();
    hero.fetch(PositionComponent.class).orElseThrow().position(position);
    Game.add(hero);
    return hero;
  }

  private static void assertDialogType(Entity hero, DialogType expectedType) {
    DialogType actualType =
        hero.fetch(UIComponent.class).orElseThrow().dialogContext().dialogType();
    assertEquals(expectedType, actualType);
  }

  private static DungeonLevel createFloorLevel(int width, int height) {
    LevelElement[][] layout = new LevelElement[height][width];
    for (LevelElement[] row : layout) {
      Arrays.fill(row, LevelElement.FLOOR);
    }
    return new DungeonLevel(layout, DesignLabel.DEFAULT);
  }
}
