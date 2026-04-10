package starter;

import contrib.components.InventoryComponent;
import contrib.configuration.KeyboardConfig;
import contrib.crafting.Crafting;
import contrib.crafting.CraftingIngredient;
import contrib.crafting.Recipe;
import contrib.entities.EntityFactory;
import contrib.entities.MiscFactory;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.item.concreteItem.ItemResourceBerry;
import contrib.item.concreteItem.ItemWoodenArrow;
import contrib.modules.levelHide.LevelHideFactory;
import core.Entity;
import core.Game;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.loader.DungeonLoader;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Set;

/**
 * Entry point for the LITIENGINE-based dungeon game.
 *
 * <p>This starter initializes and starts the game by configuring levels, loading settings,
 * setting up the game window, and launching the LITIENGINE host loop.
 *
 * <p>This version also installs a small manual crafting and level-hide verification setup:
 *
 * <ul>
 *   <li>the hero starts with guaranteed crafting items,
 *   <li>a crafting cauldron is spawned next to the start tile,
 *   <li>a chest is spawned a few tiles to the right,
 *   <li>a level-hide demo region is spawned around the chest area,
 *   <li>a guaranteed test recipe is registered.
 * </ul>
 */
public final class LitiengineStarter {

  private LitiengineStarter() {}

  static void main(String[] args) {
    DungeonLoader.addLevel(Tuple.of("playground", DungeonLevel.class));

    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Game.disableAudio(false);
    Game.frameRate(60);
    Game.windowTitle("LITIENGINE Dungeon");

    // Start the LITIENGINE client in a normal 1600x900 window.
    PreRunConfiguration.windowWidth(1600);
    PreRunConfiguration.windowHeight(900);
    PreRunConfiguration.fullScreen(false);

    Game.userOnSetup(
      () -> {
        installCraftingTestRecipe();

        Entity hero = EntityFactory.newHero();
        addCraftingTestItems(hero);
        Game.add(hero);

        LitienginePlatformBootstrap.installHudSystems();
        LitienginePlatformBootstrap.installGameplayExtensions();
        LitienginePlatformBootstrap.installDebugger();
      });

    Game.userOnLevelLoad(
      firstLoad -> {
        if (!firstLoad) {
          return;
        }

        Game.startTile().map(Tile::position).ifPresent(LitiengineStarter::spawnVerificationFixtures);
      });

    LitienginePlatformBootstrap.init();
    Game.initialize();
    GameLoop.run(args);
  }

  /**
   * Registers a guaranteed local test recipe for the LITIENGINE crafting overlay.
   *
   * <p>Recipe: Water Potion + Berry -> Weak Health Potion
   */
  private static void installCraftingTestRecipe() {
    Crafting.addRecipe(
      new Recipe(
        false,
        new CraftingIngredient[] {new ItemPotionWater(), new ItemResourceBerry()},
        new ItemPotionHealth[] {new ItemPotionHealth()}));
  }

  /** Adds a few guaranteed crafting test items to the hero inventory. */
  private static void addCraftingTestItems(Entity hero) {
    hero.fetch(InventoryComponent.class)
      .ifPresent(
        inventory -> {
          inventory.add(new ItemPotionWater());
          inventory.add(new ItemPotionWater());
          inventory.add(new ItemResourceBerry());
          inventory.add(new ItemResourceBerry());
        });
  }

  /**
   * Spawns a small deterministic verification scene near the start tile.
   *
   * <p>The level-hide region is placed a few tiles to the right of the start position so that:
   *
   * <ul>
   *   <li>the player does not start inside the hidden region,
   *   <li>the darkened area is immediately visible on screen,
   *   <li>walking a few tiles to the right triggers reveal/hide clearly.
   * </ul>
   *
   * @param startPosition world position of the level start
   */
  private static void spawnVerificationFixtures(Point startPosition) {
    Game.add(MiscFactory.newCraftingCauldron(startPosition.translate(Vector2.of(1f, 0f))));
    Game.add(MiscFactory.newChest(createChestTestItems(), startPosition.translate(Vector2.of(5f, 0f))));

    Game.add(
      LevelHideFactory.createLevelHide(
        startPosition.translate(Vector2.of(4f, 0f)),
        4f,
        3f,
        1.5f));
  }

  /** Creates a small deterministic test loot set for the starter chest. */
  private static Set<contrib.item.Item> createChestTestItems() {
    return Set.of(new ItemPotionHealth(), new ItemPotionWater(), new ItemWoodenArrow(5));
  }
}
