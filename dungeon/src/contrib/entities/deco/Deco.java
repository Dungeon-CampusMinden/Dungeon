package contrib.entities.deco;

import core.utils.Rectangle;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public enum Deco {
  //  Tileset("spritesheets/TilesetProps.png", new
  // SpritesheetConfig().spriteWidth(176).spriteHeight(208)),
  Tileset2(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig().spriteWidth(512).spriteHeight(384))
          .scaleX(384 / 16f)),
  //  Tileset3("spritesheets/TilesetNatures.png", new
  // SpritesheetConfig().spriteWidth(512).spriteHeight(384)),
  //  Tileset4("spritesheets/TilesetProps02.png", new
  // SpritesheetConfig().spriteWidth(512).spriteHeight(384)),
  //  Tileset5("spritesheets/Items.png", new
  // SpritesheetConfig().spriteWidth(512).spriteHeight(384)),
  Tileset6(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig().rows(32).columns(32)).framesPerSprite(2)),

  BookshelfLarge(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(0, 16 * 16, 1, 1, 32, 32)).scaleX(2),
      Vector2.of(2, 1)),
  Chains0("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(17 * 16, 5 * 16)),
  Chains1("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(18 * 16, 5 * 16)),
  Chains2("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(19 * 16, 5 * 16)),
  Chains3("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(20 * 16, 5 * 16)),
  Chains4("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(16 * 16, 6 * 16)),
  Chains5("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(17 * 16, 6 * 16)),
  Chains6("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(18 * 16, 6 * 16)),
  Chains7("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(19 * 16, 6 * 16)),
  Chains8("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(20 * 16, 6 * 16)),

  FloorBarsSquare("spritesheets/FG_Cellar.png", new SpritesheetConfig(16 * 16, 4 * 16)),
  FloorBarsRound("spritesheets/FG_Cellar.png", new SpritesheetConfig(17 * 16, 4 * 16)),
  FloorBarsSmall("spritesheets/FG_Cellar.png", new SpritesheetConfig(18 * 16, 4 * 16)),

  WindowBarred("spritesheets/FG_Cellar.png", new SpritesheetConfig(16 * 16, 3 * 16)),

  TreeBig(
      "objects/nature/big_tree_tile.png",
      new AnimationConfig().scaleX(3).scaleY(0),
      new Rectangle(2.10f, 1.50f, 0.55f, 0.60f)),

  TreeBigChainL(
      "objects/nature/big_trees_sheet.png",
      new AnimationConfig(new SpritesheetConfig(0, 0, 1, 1, 40, 64)).scaleX((float) 40 / 16),
      new Rectangle(1.75f, 1.5f, 0.75f, 0.6f)),
  TreeBigChainM(
      "objects/nature/big_trees_sheet.png",
      new AnimationConfig(new SpritesheetConfig(40, 0, 1, 1, 32, 64)).scaleX(2),
      new Rectangle(2.00f, 1.50f, 0.50f, 0.60f)),
  TreeBigChainR(
      "objects/nature/big_trees_sheet.png",
      new AnimationConfig(new SpritesheetConfig(40 + 32, 0, 1, 1, 40, 64)).scaleX((float) 40 / 16),
      new Rectangle(2.20f, 1.50f, 0.00f, 0.60f)),

  TreeMedium(
      "objects/nature/medium_tree.png",
      new AnimationConfig().scaleX(2),
      new Rectangle(0.8f, 1.2f, 0.6f, 0.2f)),
  TreeSmall(
      "objects/nature/small_arbust.png",
      new AnimationConfig(),
      new Rectangle(0.5f, 0.8f, 0.25f, 0.2f)),
  StumpSmall(
      "objects/nature/stump.png", new AnimationConfig(), new Rectangle(0.50f, 0.65f, 0.30f, 0.30f)),
  Reeds("objects/nature/reeds.png", new AnimationConfig()),

  Bush1("objects/nature/bushes_sheet.png", new SpritesheetConfig(0, 0)),
  Bush2("objects/nature/bushes_sheet.png", new SpritesheetConfig(16, 0)),
  Bush3("objects/nature/bushes_sheet.png", new SpritesheetConfig(0, 16)),
  Bush4("objects/nature/bushes_sheet.png", new SpritesheetConfig(16, 16)),

  LogBig("objects/nature/big_log.png", new AnimationConfig().scaleX(2), new Rectangle(1.65f, 1.20f, 0.20f, 0.20f)),

//  SignSmall("spritesheets/TilesetProps.png", new SpritesheetConfig(0, 12*16)),
//  SignBig("spritesheets/TilesetProps.png", new SpritesheetConfig(5*16, 12*16).spriteWidth(32),
// Vector2.of(2, 0.5f)),
//  VaseFull("spritesheets/TilesetProps.png", new SpritesheetConfig(2*16, 7*16), Vector2.of(0.5f,
// 0.75f), Vector2.of(0.25f, 0)),
//  VaseEmpty("spritesheets/TilesetProps.png", new SpritesheetConfig(3*16, 7*16), Vector2.of(0.5f,
// 0.75f), Vector2.of(0.25f, 0)),

// Player
//  Bush("spritesheets/TilesetNatures.png", new SpritesheetConfig(2*16, 0), Vector2.of(1, 1)),
//  Logs("spritesheets/TilesetNatures.png", new SpritesheetConfig(16, 0), Vector2.of(1, 1)),
//  TreeStump("spritesheets/TilesetNatures.png", new SpritesheetConfig(0, 0), Vector2.of(1, 1)),
//  TreeTrunk("spritesheets/TilesetNatures.png", new SpritesheetConfig(7*16, 3*16).spriteWidth(32),
// Vector2.of(2, 0.8f)),
//  StonePillar0("spritesheets/TilesetNatures.png", new SpritesheetConfig(4*16,
// 16).spriteHeight(32), Vector2.of(1, 1)),
//  StonePillar1("spritesheets/TilesetNatures.png", new SpritesheetConfig(5*16,
// 16).spriteHeight(32), Vector2.of(1, 1)),
//  StonePillar2("spritesheets/TilesetNatures.png", new SpritesheetConfig(6*16,
// 16).spriteHeight(32), Vector2.of(1, 1)),
//  BigBush("spritesheets/TilesetNatures.png", new SpritesheetConfig(7*16, 0, 1, 1, 48, 48),
// Vector2.of(2.5f, 1.5f), Vector2.of(0.25f, 0.5f)),
//  StoneAltar("spritesheets/TilesetNatures.png", new SpritesheetConfig(0, 2*16).spriteWidth(32),
// Vector2.of(2, 0.75f)),

// Background
//  Rubble0("spritesheets/TilesetNatures.png", new SpritesheetConfig(0, 16)),
//  Rubble1("spritesheets/TilesetNatures.png", new SpritesheetConfig(16, 16)),
//  Rubble2("spritesheets/TilesetNatures.png", new SpritesheetConfig(2*16, 16)),
//  Rubble3("spritesheets/TilesetNatures.png", new SpritesheetConfig(3*16, 16)),
//  Campfire("spritesheets/TilesetNatures.png", new SpritesheetConfig(6*16, 0)),
//  Mushrooms0("spritesheets/TilesetNatures.png", new SpritesheetConfig(4*16, 0)),
//  Mushrooms1("spritesheets/TilesetNatures.png", new SpritesheetConfig(5*16, 0)),

//  ArchL("spritesheets/TilesetNatures.png", new SpritesheetConfig(2*16, 3*16).spriteHeight(32),
// Vector2.of(0.75f, 1.0f), Vector2.of(0.25f, 0)),
//  ArchC("spritesheets/TilesetNatures.png", new SpritesheetConfig(3*16, 3*16).spriteHeight(16),
// DepthLayer.AbovePlayer.depth()),
//  ArchR("spritesheets/TilesetNatures.png", new SpritesheetConfig(4*16, 3*16).spriteHeight(32),
// Vector2.of(0.75f, 1.0f)),
;

  private IPath path;
  private AnimationConfig config;
  private Rectangle defaultCollider = null;
  private int defaultDepth;

  Deco(String path, AnimationConfig config) {
    this.path = new SimpleIPath(path);
    this.config = config;
  }

  Deco(String path, SpritesheetConfig config) {
    this(path, config, (Rectangle) null);
  }

  Deco(String path, SpritesheetConfig config, int defaultDepth) {
    this(path, config, (Rectangle) null, defaultDepth);
  }

  /**
   * Creates a new Deco entry. Since a collider is provided, this assumes the collider is placed on
   * the players layer.
   */
  Deco(String path, SpritesheetConfig config, Vector2 defaultCollider) {
    this(path, config, new Rectangle(defaultCollider), DepthLayer.Player.depth());
  }

  Deco(String path, SpritesheetConfig config, Vector2 defaultCollider, int defaultDepth) {
    this(path, config, new Rectangle(defaultCollider), defaultDepth);
  }

  /**
   * Creates a new Deco entry. Since a collider is provided, this assumes the collider is placed on
   * the players layer.
   */
  Deco(String path, SpritesheetConfig config, Rectangle defaultCollider) {
    this(
        path,
        config,
        defaultCollider,
        defaultCollider == null ? DepthLayer.BackgroundDeco.depth() : DepthLayer.Player.depth());
  }

  Deco(String path, SpritesheetConfig config, Rectangle defaultCollider, int defaultDepth) {
    this(path, new AnimationConfig(config), defaultCollider, defaultDepth);
  }

  Deco(String path, AnimationConfig config, Vector2 defaultColliderSize) {
    this(path, config, new Rectangle(defaultColliderSize));
  }

  Deco(String path, AnimationConfig config, Rectangle defaultCollider) {
    this(
        path,
        config,
        defaultCollider,
        defaultCollider == null ? DepthLayer.BackgroundDeco.depth() : DepthLayer.Player.depth());
  }

  Deco(String path, AnimationConfig config, Rectangle defaultCollider, int defaultDepth) {
    this.path = new SimpleIPath(path);
    this.config = config;
    this.defaultCollider = defaultCollider;
    this.defaultDepth = defaultDepth;
  }

  public IPath path() {
    return path;
  }

  public AnimationConfig config() {
    return config;
  }

  public Rectangle defaultCollider() {
    return defaultCollider;
  }

  public int defaultDepth() {
    return defaultDepth;
  }
}
