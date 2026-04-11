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
import core.platform.litiengine.render.effects.*;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.SimpleIPath;

import java.awt.*;
import java.io.IOException;
import java.util.Set;

/**
 * Entry point for the LITIENGINE-based dungeon game.
 *
 * <p>This starter initializes and starts the game by configuring levels, loading settings,
 * setting up the game window, and launching the LITIENGINE host loop.
 *
 * <p>This version also installs a small manual crafting, level-hide and sprite-effect
 * verification setup.
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

    Entity hueRemapDemoChest =
      MiscFactory.newChest(
        createChestTestItems(),
        startPosition.translate(Vector2.of(2f, 6f)));
    installHueRemapDemoEffect(hueRemapDemoChest);
    Game.add(hueRemapDemoChest);

    Entity colorGradeDemoChest =
      MiscFactory.newChest(
        createChestTestItems(),
        startPosition.translate(Vector2.of(4f, 6f)));
    installColorGradeDemoEffect(colorGradeDemoChest);
    Game.add(colorGradeDemoChest);

    Entity shineDemoChest =
      MiscFactory.newChest(
        createChestTestItems(),
        startPosition.translate(Vector2.of(6f, 6f)));
    installShineDemoEffect(shineDemoChest);
    Game.add(shineDemoChest);

    Game.add(
      MiscFactory.newChest(
        createChestTestItems(),
        startPosition.translate(Vector2.of(5f, 0f))));

    Game.add(
      LevelHideFactory.createLevelHide(
        startPosition.translate(Vector2.of(4f, 0f)),
        4f,
        3f,
        1.5f));
  }

  /**
   * Adds a clearly visible hue-remap demo effect to a static verification chest.
   *
   * <p>The chosen values intentionally remap warm brown/orange chest tones into a cyan-blue range
   * with a fairly wide tolerance so the result is easy to verify by eye in the starter.
   */
  private static void installHueRemapDemoEffect(Entity entity) {
    LitiengineSpriteEffects effects = new LitiengineSpriteEffects();
    effects.add(
      "demo_hue_remap_warm_to_cyan",
      new LitiengineHueRemapEffect(0.08f, 0.56f, 0.18f),
      100);

    entity.add(new LitiengineSpriteEffectsComponent(effects));
  }

  /**
   * Adds a clearly visible color-grade demo effect to a static verification chest.
   *
   * <p>This demo intentionally keeps the original hue and instead changes saturation and brightness
   * strongly, so the effect is distinguishable from the separate hue-remap demo chest.
   */
  private static void installColorGradeDemoEffect(Entity entity) {
    LitiengineSpriteEffects effects = new LitiengineSpriteEffects();
    effects.add(
      "demo_color_grade_desaturate_brighten",
      new LitiengineColorGradeEffect(-1.0f, 0.20f, 1.35f),
      100);

    entity.add(new LitiengineSpriteEffectsComponent(effects));
  }

  /**
   * Adds a clearly visible animated shine demo effect to a static verification chest.
   *
   * <p>This demo keeps the original chest colors and adds rotating bright highlight slices so the
   * result is visually distinct from both the hue-remap and the color-grade demo entities.
   */
  private static void installShineDemoEffect(Entity entity) {
    LitiengineSpriteEffects effects = new LitiengineSpriteEffects();
    effects.add(
      "demo_shine_gold_slices",
      new LitiengineShineEffect(
        5,
        0.35f,
        0.35f,
        new Color(255, 245, 170, 210)),
      100);

    entity.add(new LitiengineSpriteEffectsComponent(effects));
  }

  private static Set<contrib.item.Item> createChestTestItems() {
    return Set.of(new ItemPotionHealth(), new ItemPotionWater(), new ItemWoodenArrow(5));
  }
}
