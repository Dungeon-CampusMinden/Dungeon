package starter;

import contrib.components.InventoryComponent;
import contrib.components.ManaComponent;
import contrib.components.SkillComponent;
import contrib.configuration.KeyboardConfig;
import contrib.crafting.Crafting;
import contrib.crafting.CraftingIngredient;
import contrib.crafting.Recipe;
import contrib.entities.EntityFactory;
import contrib.entities.MiscFactory;
import contrib.entities.ShowImageFactory;
import contrib.entities.SignFactory;
import contrib.entities.WorldItemBuilder;
import contrib.hud.showimage.ShowImageText;
import contrib.item.Item;
import contrib.item.concreteItem.ItemKey;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.item.concreteItem.ItemResourceBerry;
import contrib.item.concreteItem.ItemWoodenArrow;
import contrib.modules.levelHide.LevelHideFactory;
import contrib.systems.ManaRestoreSystem;
import contrib.systems.ProjectileSystem;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.game.ECSManagement;
import core.game.PreRunConfiguration;
import core.game.render.depth.DepthLayerColorGradeEffect;
import core.game.render.depth.DepthLayerEffectPipeline;
import core.game.render.level.LevelColorGradeEffect;
import core.game.render.level.LevelEffectPipeline;
import core.game.render.scene.SceneColorGradeEffect;
import core.game.render.scene.SceneEffectPipeline;
import core.game.render.sprite.effects.HueRemapSpriteEffect;
import core.game.render.sprite.effects.ShineSpriteEffect;
import core.game.render.sprite.effects.SpriteColorGradeEffect;
import core.game.render.sprite.effects.SpriteEffectRegistry;
import core.game.render.sprite.effects.SpriteEffectsComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.loader.DungeonLoader;
import core.utils.Point;
import core.utils.Rectangle;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.path.SimpleIPath;
import java.awt.Color;
import java.io.IOException;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Entry point for the LITIENGINE-based dungeon game.
 *
 * <p>This starter initializes and starts the game by configuring levels, loading settings,
 * setting up the game window, and launching the client host loop.
 *
 * <p>In addition to a basic playable setup, this starter installs a deterministic manual
 * verification sandbox near the start tile. The sandbox is meant to validate that visible
 * old functionality is still present after the port:
 *
 * <ul>
 *   <li>inventory interaction
 *   <li>crafting
 *   <li>dialogs and show-image overlays
 *   <li>locked chest flow
 *   <li>sprite effects
 *   <li>scene/level/depth-layer effects
 *   <li>level-hide behavior
 *   <li>debug/editor test entry points
 * </ul>
 */
public final class BasicStarter {

  private static final String STARTER_SCENE_COLOR_GRADE_DEMO_ID = "starter_scene_color_grade_demo";
  private static final String STARTER_LEVEL_COLOR_GRADE_DEMO_ID = "starter_level_color_grade_demo";
  private static final String STARTER_DEPTH_COLOR_GRADE_DEMO_ID = "starter_depth_color_grade_demo";

  private BasicStarter() {}

  public static void main(String[] args) {
    DungeonLoader.addLevel(Tuple.of("playground", DungeonLevel.class));

    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Game.disableAudio(false);
    Game.frameRate(60);
    Game.windowTitle("LITIENGINE Dungeon Verification");

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
        addStarterInventoryItems(hero);
        prepareHeroManaVerification(hero);
        prepareHeroFireballVerification(hero);
        Game.add(hero);
      });

    Game.userOnLevelLoad(
      firstLoad -> {
        if (!firstLoad) {
          return;
        }

        Game.startTile().map(Tile::position).ifPresent(BasicStarter::spawnVerificationFixtures);
      });

    Game.addClientStartupTask(BasicStarter::ensureManaRestoreSystem);
    Game.addClientStartupTask(Crafting::loadRecipes);
    Game.run();
  }

  private static void ensureManaRestoreSystem() {
    if (!Game.systems().containsKey(ManaRestoreSystem.class)) {
      Game.add(new ManaRestoreSystem());
    }
  }

  private static void prepareHeroManaVerification(Entity hero) {
    hero.fetch(ManaComponent.class)
      .ifPresent(
        mana -> {
          float visibleStartMana = Math.max(10f, mana.maxAmount() * 0.20f);
          mana.currentAmount(visibleStartMana);
        });
  }

  private static void ensureBasicStarterGameplaySystems() {
    registerIfAbsent(ProjectileSystem.class, ProjectileSystem::new);
  }

  private static <T extends core.System> void registerIfAbsent(
    Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }

  private static void prepareHeroFireballVerification(Entity hero) {
    hero.fetch(SkillComponent.class)
      .ifPresent(
        skillComponent -> {
          int guard = skillComponent.getSkills().size();
          while (guard-- > 0
            && skillComponent.activeSkill().filter(FireballSkill.class::isInstance).isEmpty()) {
            skillComponent.nextSkill();
          }
        });
  }

  /**
   * Registers a guaranteed local test recipe for the crafting verification area.
   *
   * <p>Recipe: Water Potion + Berry -> Health Potion
   */
  private static void installCraftingTestRecipe() {
    Crafting.addRecipe(
      new Recipe(
        false,
        new CraftingIngredient[] {new ItemPotionWater(), new ItemResourceBerry()},
        new ItemPotionHealth[] {new ItemPotionHealth()}));
  }

  /**
   * Registers a visible regional scene-pass color-grade demo.
   *
   * <p>The effect is limited to an upper world-space region so it can be distinguished clearly
   * from the separate level-pass and depth-layer-pass demos.
   */
  private static void installSceneColorGradeDemo() {
    SceneEffectPipeline.effects().remove(STARTER_SCENE_COLOR_GRADE_DEMO_ID);
    SceneEffectPipeline.effects().add(
      STARTER_SCENE_COLOR_GRADE_DEMO_ID,
      new SceneColorGradeEffect(-1.0f, 0.72f, 1.08f)
        .region(new Rectangle(1f, 5f, 10f, 4f))
        .transitionSize(2.0f),
      100);
  }

  /**
   * Registers a visible regional level-pass color-grade demo.
   *
   * <p>This affects only lower level tiles and not the entities on top of them.
   */
  private static void installLevelColorGradeDemo() {
    LevelEffectPipeline.effects().remove(STARTER_LEVEL_COLOR_GRADE_DEMO_ID);
    LevelEffectPipeline.effects().add(
      STARTER_LEVEL_COLOR_GRADE_DEMO_ID,
      new LevelColorGradeEffect(-1.0f, 0.58f, 0.82f)
        .region(new Rectangle(3f, 0f, 7f, 4f))
        .transitionSize(2.0f),
      100);
  }

  /**
   * Registers a visible regional depth-layer color-grade demo.
   *
   * <p>This targets only the dedicated verification chest on the foreground-deco layer.
   */
  private static void installDepthLayerColorGradeDemo() {
    int demoDepth = DepthLayer.ForegroundDeco.depth();

    DepthLayerEffectPipeline.effects(demoDepth).remove(STARTER_DEPTH_COLOR_GRADE_DEMO_ID);
    DepthLayerEffectPipeline.effects(demoDepth).add(
      STARTER_DEPTH_COLOR_GRADE_DEMO_ID,
      new DepthLayerColorGradeEffect(-1.0f, 0.35f, 1.28f)
        .region(new Rectangle(7f, 5f, 3f, 3f))
        .transitionSize(1.5f),
      100);
  }

  /** Adds deterministic starter items to the hero inventory. */
  private static void addStarterInventoryItems(Entity hero) {
    hero.fetch(InventoryComponent.class)
      .ifPresent(
        inventory -> {
          inventory.add(new ItemPotionWater());
          inventory.add(new ItemPotionWater());
          inventory.add(new ItemPotionHealth());
          inventory.add(new ItemResourceBerry());
          inventory.add(new ItemResourceBerry());
          inventory.add(new ItemWoodenArrow(8));
          inventory.add(new ItemKey());
        });
  }

  /**
   * Spawns the full manual verification sandbox near the level start.
   *
   * @param startPosition world position of the level start
   */
  private static void spawnVerificationFixtures(Point startPosition) {
    spawnHubZone(startPosition);
    spawnInventoryZone(startPosition.translate(Vector2.of(3f, 0f)));
    spawnCraftingZone(startPosition.translate(Vector2.of(8f, 0f)));
    spawnDialogZone(startPosition.translate(Vector2.of(13f, 0f)));
    spawnLockedChestZone(startPosition.translate(Vector2.of(18f, 0f)));
    spawnRenderAndEffectsZone(startPosition.translate(Vector2.of(2f, 6f)));
    spawnLevelHideZone(startPosition.translate(Vector2.of(11f, 5f)));
    spawnDebugAndEditorHintZone(startPosition.translate(Vector2.of(18f, 6f)));
  }

  /** Spawn area with general instructions. */
  private static void spawnHubZone(Point origin) {
    addSign(
      "Verification Hub",
      """
      This BasicStarter is a manual verification sandbox.
      Test at least:
      - movement
      - interaction
      - inventory
      - crafting
      - dialogs
      - show-image
      - render effects
      - level hide
      - debug hotkeys
      - level editor
      """,
      origin.translate(Vector2.of(0f, 1.5f)));

    addSign(
      "Core Controls",
      """
      I = inventory
      E = interact / use item
      Q = use active skill
      , / . = switch skill
      ESC = close UI
      F3 = debug HUD
      F4 = level editor
      """,
      origin.translate(Vector2.of(0f, 3.5f)));

    Game.add(
      WorldItemBuilder.buildWorldItem(
        new ItemPotionHealth(), origin.translate(Vector2.of(1f, 0.5f))));
  }

  /** Inventory verification area with multiple chest variants and ground items. */
  private static void spawnInventoryZone(Point origin) {
    addSign(
      "Inventory Zone",
      """
      Verify:
      - open chest
      - move items between inventories
      - quick transfer
      - stack behavior
      - dropping world items
      - using player items
      """,
      origin.translate(Vector2.of(0f, 2f)));

    Game.add(MiscFactory.newChest(createChestTestItems(), origin.translate(Vector2.of(0f, 0f))));
    Game.add(MiscFactory.newChest(createLargeChestTestItems(), origin.translate(Vector2.of(2f, 0f))));

    Game.add(
      WorldItemBuilder.buildWorldItem(
        new ItemPotionWater(), origin.translate(Vector2.of(1f, 1.5f))));
    Game.add(
      WorldItemBuilder.buildWorldItem(
        new ItemResourceBerry(), origin.translate(Vector2.of(2f, 1.5f))));
    Game.add(
      WorldItemBuilder.buildWorldItem(
        new ItemWoodenArrow(8), origin.translate(Vector2.of(3f, 1.5f))));
  }

  /** Crafting verification area. */
  private static void spawnCraftingZone(Point origin) {
    addSign(
      "Crafting Zone",
      """
      Verify:
      - open crafting cauldron
      - move Water Potion + Berry into crafting
      - result preview appears
      - craft returns Health Potion
      - cancel returns ingredients
      - close dialog also returns ingredients
      """,
      origin.translate(Vector2.of(0f, 2f)));

    Game.add(MiscFactory.newCraftingCauldron(origin.translate(Vector2.of(0f, 0f))));
    Game.add(MiscFactory.newChest(createCraftingSupportItems(), origin.translate(Vector2.of(2f, 0f))));
  }

  /** Dialog and show-image verification area. */
  private static void spawnDialogZone(Point origin) {
    addSign(
      "Dialog Zone",
      """
      Verify:
      - reading sign dialogs
      - closing dialogs with ESC
      - image overlay opens
      - image overlay closes correctly
      - image text is visible
      """,
      origin.translate(Vector2.of(0f, 2f)));

    Game.add(
      SignFactory.createSign(
        "The sign dialog is working if you can read this text and close it again.",
        "Simple Sign",
        origin.translate(Vector2.of(0f, 0f)),
        (sign, who) -> {}));

    Game.add(
      ShowImageFactory.createShowImage(
        origin.translate(Vector2.of(2f, 0f)),
        "items/book/red_book.png",
        "items/book/red_book.png",
        (trigger, overlay) -> {},
        0.80f,
        1.5f,
        ShowImageText.ofRgb(
          "ShowImage overlay verification", 1.0f, 0, 0, 0)));

    Game.add(
      ShowImageFactory.createShowImage(
        origin.translate(Vector2.of(4f, 0f)),
        "items/book/spell_book.png",
        "items/book/spell_book.png",
        (trigger, overlay) -> {},
        0.95f,
        1.5f,
        ShowImageText.ofRgb(
          "Second image for reopen / close testing", 0.9f, 20, 20, 20)));
  }

  /** Locked chest verification area. */
  private static void spawnLockedChestZone(Point origin) {
    addSign(
      "Locked Chest Zone",
      """
      Verify:
      - interaction with locked chest
      - missing / present key behavior
      - confirm dialog
      - chest opens after consuming key
      """,
      origin.translate(Vector2.of(0f, 2f)));

    Game.add(
      MiscFactory.newLockedChest(
        createChestTestItems(), origin.translate(Vector2.of(0f, 0f)), ItemKey.class));

    Game.add(
      WorldItemBuilder.buildWorldItem(
        new ItemKey(), origin.translate(Vector2.of(2f, 0.5f))));
  }

  /** Render/effect verification area with effect-specific chests. */
  private static void spawnRenderAndEffectsZone(Point origin) {
    addSign(
      "Render & Effects Zone",
      """
      Verify visually:
      - hue remap
      - sprite color grading
      - animated shine
      - dedicated depth-layer effect
      - regional scene/level/depth effects
      """,
      origin.translate(Vector2.of(0f, 3f)));

    Entity hueRemapDemoChest =
      MiscFactory.newChest(createChestTestItems(), origin.translate(Vector2.of(0f, 0f)));
    installHueRemapDemoEffect(hueRemapDemoChest);
    Game.add(hueRemapDemoChest);

    Entity colorGradeDemoChest =
      MiscFactory.newChest(createChestTestItems(), origin.translate(Vector2.of(2f, 0f)));
    installColorGradeDemoEffect(colorGradeDemoChest);
    Game.add(colorGradeDemoChest);

    Entity shineDemoChest =
      MiscFactory.newChest(createChestTestItems(), origin.translate(Vector2.of(4f, 0f)));
    installShineDemoEffect(shineDemoChest);
    Game.add(shineDemoChest);

    Entity depthLayerDemoChest =
      MiscFactory.newChest(createChestTestItems(), origin.translate(Vector2.of(6f, 0f)));
    installDepthLayerDemoDepth(depthLayerDemoChest);
    Game.add(depthLayerDemoChest);
  }

  /** LevelHide verification area. */
  private static void spawnLevelHideZone(Point origin) {
    addSign(
      "Level Hide Zone",
      """
      Verify:
      - region is darkened before entering
      - walking into the area reveals it
      - leaving the area hides it again
      """,
      origin.translate(Vector2.of(0f, 3f)));

    Game.add(LevelHideFactory.createLevelHide(origin.translate(Vector2.of(0f, 0f)), 4f, 3f, 1.5f));
  }

  /** Debug/editor guidance zone. */
  private static void spawnDebugAndEditorHintZone(Point origin) {
    addSign(
      "Debug Hotkeys",
      """
      Verify with hotkeys:
      - F3 toggle debug HUD
      - X spawn monster at cursor
      - K / L zoom in / out
      - J / H / G / O teleport variants
      - C open doors
      """,
      origin.translate(Vector2.of(0f, 2f)));

    addSign(
      "Editor Hotkeys",
      """
      Verify with F4:
      - activate level editor
      - switch modes 1..7
      - tile / deco / point / bounds / shift / start tiles / save
      - clipboard export in save mode
      """,
      origin.translate(Vector2.of(0f, 4.5f)));
  }

  /** Visible hue-remap demo chest effect. */
  private static void installHueRemapDemoEffect(Entity entity) {
    SpriteEffectRegistry effects = new SpriteEffectRegistry();
    effects.add(
      "demo_hue_remap_warm_to_cyan",
      new HueRemapSpriteEffect(0.08f, 0.56f, 0.18f),
      100);
    entity.add(new SpriteEffectsComponent(effects));
  }

  /** Visible sprite color-grade demo chest effect. */
  private static void installColorGradeDemoEffect(Entity entity) {
    SpriteEffectRegistry effects = new SpriteEffectRegistry();
    effects.add(
      "demo_color_grade_desaturate_brighten",
      new SpriteColorGradeEffect(-1.0f, 0.20f, 1.35f),
      100);
    entity.add(new SpriteEffectsComponent(effects));
  }

  /** Visible animated shine demo chest effect. */
  private static void installShineDemoEffect(Entity entity) {
    SpriteEffectRegistry effects = new SpriteEffectRegistry();
    effects.add(
      "demo_shine_overlay",
      new ShineSpriteEffect()
        .padding(0)
        .sliceCount(2)
        .gapSize(0.72f)
        .rotationSpeed(0.90f)
        .shineColor(new Color(255, 255, 128, 255)),
      100);
    entity.add(new SpriteEffectsComponent(effects));
  }

  /** Puts one chest explicitly onto the depth layer that is targeted by the depth effect demo. */
  private static void installDepthLayerDemoDepth(Entity entity) {
    entity.fetch(DrawComponent.class)
      .ifPresent(dc -> dc.depth(DepthLayer.ForegroundDeco.depth()));
  }

  /** Creates a small set of mixed chest items. */
  private static Set<Item> createChestTestItems() {
    return Set.of(
      new ItemPotionWater(),
      new ItemPotionHealth(),
      new ItemResourceBerry(),
      new ItemWoodenArrow(8));
  }

  /** Creates a slightly larger set for transfer and inventory layout checks. */
  private static Set<Item> createLargeChestTestItems() {
    return Set.of(
      new ItemPotionWater(),
      new ItemPotionHealth(),
      new ItemResourceBerry(),
      new ItemWoodenArrow(8),
      new ItemWoodenArrow(16),
      new ItemKey());
  }

  /** Support items for crafting verification. */
  private static Set<Item> createCraftingSupportItems() {
    return Set.of(
      new ItemPotionWater(),
      new ItemPotionWater(),
      new ItemResourceBerry(),
      new ItemResourceBerry(),
      new ItemPotionHealth());
  }

  /** Adds a simple sign to the level. */
  private static void addSign(String title, String text, Point position) {
    Game.add(SignFactory.createSign(text, title, position, (sign, who) -> {}));
  }
}
