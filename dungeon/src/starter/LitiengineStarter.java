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
import core.components.DrawComponent;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.loader.DungeonLoader;
import core.game.render.depth.DepthLayerColorGradeEffect;
import core.game.render.depth.DepthLayerEffectPipeline;
import core.platform.litiengine.render.effects.*;
import core.game.render.level.LevelColorGradeEffect;
import core.game.render.level.LevelEffectPipeline;
import core.game.render.scene.SceneColorGradeEffect;
import core.game.render.scene.SceneEffectPipeline;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.path.SimpleIPath;
import java.awt.Color;
import core.utils.Rectangle;
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
        installSceneColorGradeDemo();
        installLevelColorGradeDemo();
        installDepthLayerColorGradeDemo();

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

  /**
   * Registers a visible regional scene-pass color-grade demo for the LITIENGINE starter.
   *
   * <p>The effect is intentionally limited to a world-space region near the verification setup so it
   * can be distinguished clearly from the separate global scene-pass, level-pass and depth-layer-pass
   * demos. Inside the region, the scene is noticeably desaturated and slightly brightened. Outside
   * the region, the effect fades out smoothly across the configured transition band.
   */
  private static void installSceneColorGradeDemo() {
    SceneEffectPipeline.effects().remove("starter_scene_color_grade_demo");
    SceneEffectPipeline.effects().add(
      "starter_scene_color_grade_demo",
      new SceneColorGradeEffect(-1.0f, 0.72f, 1.08f)
        .region(new Rectangle(1f, 5f, 10f, 4f))
        .transitionSize(2.0f),
      100);
  }

  /**
   * Registers a visible regional level-pass color-grade demo for the LITIENGINE starter.
   *
   * <p>This demo intentionally affects only a lower world-space region of the rendered level tiles,
   * not entities on top of them. The region is kept separate from the upper regional scene-pass demo
   * so both pass types can be distinguished more clearly during verification.
   */
  private static void installLevelColorGradeDemo() {
    LevelEffectPipeline.effects().remove("starter_level_color_grade_demo");
    LevelEffectPipeline.effects().add(
      "starter_level_color_grade_demo",
      new LevelColorGradeEffect(-1.0f, 0.58f, 0.82f)
        .region(new Rectangle(3f, 0f, 7f, 4f))
        .transitionSize(2.0f),
      100);
  }

  /**
   * Registers a visible regional depth-layer color-grade demo for the LITIENGINE starter.
   *
   * <p>This demo targets only the dedicated verification chest on the foreground-deco depth layer.
   * The effect is limited to a small world-space region around that chest so the regional depth-pass
   * semantics can be verified independently of the broader scene-pass and level-pass demos.
   */
  private static void installDepthLayerColorGradeDemo() {
    int demoDepth = DepthLayer.ForegroundDeco.depth();

    DepthLayerEffectPipeline.effects(demoDepth).remove("starter_depth_color_grade_demo");
    DepthLayerEffectPipeline.effects(demoDepth).add(
      "starter_depth_color_grade_demo",
      new DepthLayerColorGradeEffect(-1.0f, 0.35f, 1.28f)
        .region(new Rectangle(7f, 5f, 3f, 3f))
        .transitionSize(1.5f),
      100);
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

    Entity depthLayerDemoChest =
      MiscFactory.newChest(
        createChestTestItems(),
        startPosition.translate(Vector2.of(8f, 6f)));
    installDepthLayerDemoDepth(depthLayerDemoChest);
    Game.add(depthLayerDemoChest);

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
   * <p>After introducing overlay padding support, the demo explicitly disables padding again so the
   * verification chest shows the shine strictly inside its own visual bounds. The remaining values
   * are intentionally tuned for a very small sprite:
   *
   * <ul>
   *   <li>few slices,
   *   <li>very large gaps,
   *   <li>high movement speed.
   * </ul>
   */
  private static void installShineDemoEffect(Entity entity) {
    LitiengineSpriteEffects effects = new LitiengineSpriteEffects();
    effects.add(
      "demo_shine_gold_slices",
      new LitiengineShineEffect()
        .sliceCount(2)
        .gapSize(0.88f)
        .rotationSpeed(1.80f)
        .shineColor(new Color(255, 250, 210, 255))
        .padding(0),
      100);

    entity.add(new LitiengineSpriteEffectsComponent(effects));
  }

  /**
   * Moves the given demo entity onto the dedicated depth layer that is used for depth-pass
   * verification in the LITIENGINE starter.
   */
  private static void installDepthLayerDemoDepth(Entity entity) {
    entity.fetch(DrawComponent.class)
      .ifPresent(drawComponent -> drawComponent.depth(DepthLayer.ForegroundDeco.depth()));
  }

  private static Set<contrib.item.Item> createChestTestItems() {
    return Set.of(new ItemPotionHealth(), new ItemPotionWater(), new ItemWoodenArrow(5));
  }
}
