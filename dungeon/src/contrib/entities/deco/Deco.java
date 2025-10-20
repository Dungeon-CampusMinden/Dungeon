package contrib.entities.deco;

import core.utils.Rectangle;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * An enumeration of predefined decorative objects (Decos) that can be placed in the game world.
 *
 * <p>Each {@code Deco} entry defines its own sprite or spritesheet path, animation configuration,
 * and optional default collider and depth layer. Decorations are typically static or animated world
 * props such as trees, bushes, furniture, or architectural elements.
 *
 * <p>The configuration data provided by this enum is used by the {@link
 * contrib.entities.deco.DecoFactory} to instantiate corresponding decorative entities.
 */
public enum Deco {
  /** A decoration. */
  Tileset1(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig().spriteWidth(512).spriteHeight(384))
          .scaleX(384 / 16f)),
  /** A decoration. */
  Tileset2(
      "spritesheets/FG_Cellar.png",
      new AnimationConfig(new SpritesheetConfig().spriteWidth(512).spriteHeight(512))),

  /** A decoration. */
  BookshelfLarge(
      "spritesheets/FD_Dungeon_Free.png",
      new AnimationConfig(new SpritesheetConfig(0, 16 * 16, 1, 1, 32, 32)).scaleX(2),
      Vector2.of(2, 1)),
  /** A decoration. */
  Chains0("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(17 * 16, 5 * 16)),
  /** A decoration. */
  Chains1("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(18 * 16, 5 * 16)),
  /** A decoration. */
  Chains2("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(19 * 16, 5 * 16)),
  /** A decoration. */
  Chains3("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(20 * 16, 5 * 16)),
  /** A decoration. */
  Chains4("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(16 * 16, 6 * 16)),
  /** A decoration. */
  Chains5("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(17 * 16, 6 * 16)),
  /** A decoration. */
  Chains6("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(18 * 16, 6 * 16)),
  /** A decoration. */
  Chains7("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(19 * 16, 6 * 16)),
  /** A decoration. */
  Chains8("spritesheets/FD_Dungeon_Free.png", new SpritesheetConfig(20 * 16, 6 * 16)),

  /** A decoration. */
  FloorBarsSquare("spritesheets/FG_Cellar.png", new SpritesheetConfig(16 * 16, 4 * 16)),
  /** A decoration. */
  FloorBarsRound("spritesheets/FG_Cellar.png", new SpritesheetConfig(17 * 16, 4 * 16)),
  /** A decoration. */
  FloorBarsSmall("spritesheets/FG_Cellar.png", new SpritesheetConfig(18 * 16, 4 * 16)),

  /** A decoration. */
  WindowBarred("spritesheets/FG_Cellar.png", new SpritesheetConfig(16 * 16, 3 * 16)),

  /** A decoration. */
  TreeBig(
      "objects/nature/big_tree_tile.png",
      new AnimationConfig().scaleX(3).scaleY(0),
      new Rectangle(2.10f, 1.50f, 0.55f, 0.60f)),

  /** A decoration. */
  TreeBigChainL(
      "objects/nature/big_trees_sheet.png",
      new AnimationConfig(new SpritesheetConfig(0, 0, 1, 1, 40, 64)).scaleX((float) 40 / 16),
      new Rectangle(1.75f, 1.5f, 0.75f, 0.6f)),
  /** A decoration. */
  TreeBigChainM(
      "objects/nature/big_trees_sheet.png",
      new AnimationConfig(new SpritesheetConfig(40, 0, 1, 1, 32, 64)).scaleX(2),
      new Rectangle(2.00f, 1.50f, 0.00f, 0.60f)),
  /** A decoration. */
  TreeBigChainR(
      "objects/nature/big_trees_sheet.png",
      new AnimationConfig(new SpritesheetConfig(40 + 32, 0, 1, 1, 40, 64)).scaleX((float) 40 / 16),
      new Rectangle(2.20f, 1.50f, 0.00f, 0.60f)),

  /** A decoration. */
  TreeMedium(
      "objects/nature/medium_tree.png",
      new AnimationConfig().scaleX(2),
      new Rectangle(0.8f, 1.2f, 0.6f, 0.2f)),
  /** A decoration. */
  TreeSmall(
      "objects/nature/small_arbust.png",
      new AnimationConfig(),
      new Rectangle(0.5f, 0.8f, 0.25f, 0.2f)),
  /** A decoration. */
  StumpSmall(
      "objects/nature/stump.png", new AnimationConfig(), new Rectangle(0.50f, 0.65f, 0.30f, 0.30f)),
  /** A decoration. */
  Reeds("objects/nature/reeds.png", new AnimationConfig()),

  /** A decoration. */
  Bush1("objects/nature/bushes_sheet.png", new SpritesheetConfig(0, 0)),
  /** A decoration. */
  Bush2("objects/nature/bushes_sheet.png", new SpritesheetConfig(16, 0)),
  /** A decoration. */
  Bush3("objects/nature/bushes_sheet.png", new SpritesheetConfig(0, 16)),
  /** A decoration. */
  Bush4("objects/nature/bushes_sheet.png", new SpritesheetConfig(16, 16)),

  /** A decoration. */
  LogBig(
      "objects/nature/big_log.png",
      new AnimationConfig().scaleX(2),
      new Rectangle(1.65f, 1.20f, 0.20f, 0.20f)),
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

  Deco(String path, SpritesheetConfig config, Vector2 defaultCollider) {
    this(path, config, new Rectangle(defaultCollider), DepthLayer.Player.depth());
  }

  Deco(String path, SpritesheetConfig config, Vector2 defaultCollider, int defaultDepth) {
    this(path, config, new Rectangle(defaultCollider), defaultDepth);
  }

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

  /**
   * Returns the {@link IPath} representing the file path to the sprite or spritesheet used by this
   * decoration.
   *
   * @return the {@link IPath} to the decoration’s sprite or spritesheet
   */
  public IPath path() {
    return path;
  }

  /**
   * Returns the {@link AnimationConfig} associated with this decoration.
   *
   * <p>This configuration defines how the sprite or spritesheet is rendered and scaled.
   *
   * @return the {@link AnimationConfig} of this decoration
   */
  public AnimationConfig config() {
    return config;
  }

  /**
   * Returns the default collider for this decoration, if any.
   *
   * <p>The collider defines the solid area occupied by the object and is used for player or
   * environment collision. If the decoration is purely visual, this may return {@code null}.
   *
   * @return the {@link Rectangle} representing the default collider, or {@code null} if none exists
   */
  public Rectangle defaultCollider() {
    return defaultCollider;
  }

  /**
   * Returns the default rendering depth for this decoration.
   *
   * <p>The depth determines the draw order relative to other entities (e.g., background, player,
   * foreground).
   *
   * @return the default rendering depth value
   */
  public int defaultDepth() {
    return defaultDepth;
  }
}
