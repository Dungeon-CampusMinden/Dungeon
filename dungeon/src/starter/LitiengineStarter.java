package starter;

import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.configuration.KeyboardConfig;
import contrib.crafting.Crafting;
import contrib.crafting.CraftingIngredient;
import contrib.crafting.Recipe;
import contrib.entities.EntityFactory;
import contrib.entities.MiscFactory;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.item.concreteItem.ItemResourceBerry;
import contrib.systems.EventScheduler;
import core.Entity;
import core.Game;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.platform.Platform;
import core.utils.InputManager;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

/**
 * Entry point for the LITIENGINE-based dungeon game.
 *
 * <p>This starter initializes and starts the game by configuring levels, loading settings,
 * setting up the game window, and launching the LITIENGINE host loop.
 *
 * <p>This version also installs a small manual crafting test setup:
 *
 * <ul>
 *   <li>the hero starts with guaranteed crafting items,
 *   <li>a crafting cauldron is spawned next to the start tile,
 *   <li>a guaranteed test recipe is registered,
 *   <li>a starter-only fallback opens crafting via {@code E} if the normal interaction path
 *       does not react.
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
    Game.frameRate(30);
    Game.windowTitle("LITIENGINE Dungeon");

    // Use borderless fullscreen on startup for the LITIENGINE test client.
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
        LitienginePlatformBootstrap.installDebugger();
      });

    Game.userOnLevelLoad(
      firstLoad -> {
        if (!firstLoad) {
          return;
        }

        Game.startTile()
          .map(tile -> tile.position().translate(Vector2.of(1f, 0f)))
          .ifPresent(position -> Game.add(MiscFactory.newCraftingCauldron(position)));
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
}
