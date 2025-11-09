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


  /** PixelOfficeAssets */
  ChairRed(
      "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(6, 41, 1, 1, 11, 22)),
      new Rectangle(1.15f, 1.15f, -0.15f, 0.00f)),
  ChairYellow(
      "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(19, 41, 1, 1, 11, 22)),
      new Rectangle(1.15f, 1.15f, -0.15f, 0.00f)),
  ChairGreen(
      "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(32, 41, 1, 1, 11, 22)),
      new Rectangle(1.15f, 1.15f, -0.15f, 0.00f)),
  ChairBlue(
      "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(45, 41, 1, 1, 11, 22)),
      new Rectangle(1.15f, 1.15f, -0.15f, 0.00f)),
  ChairWhite(
      "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(58, 41, 1, 1, 11, 22)),
      new Rectangle(1.15f, 1.15f, -0.15f, 0.00f)),
  ChairGrey(
      "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(71, 41, 1, 1, 11, 22)),
      new Rectangle(1.15f, 1.15f, -0.15f, 0.00f)),
  TableShort(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(85, 47, 1, 1, 26, 16)),
    new Rectangle(1.90f, 1.30f, -0.10f, 0.00f)),
  TableLong(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(115, 47, 1, 1, 40, 16)),
    new Rectangle(4.25f, 1.30f, -0.10f, 0.00f)),
  CouchWhite(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(119, 65, 1, 1, 33, 16)),
    new Rectangle(3.05f, 1.10f, -0.15f, 0.00f)),
  CouchBlue(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(120, 83, 1, 1, 33, 16)),
    new Rectangle(3.05f, 1.10f, -0.15f, 0.00f)),
  CouchGreen(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(120, 102, 1, 1, 33, 16)),
    new Rectangle(3.05f, 1.10f, -0.15f, 0.00f)),
  CouchOrange(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(120, 121, 1, 1, 33, 16)),
    new Rectangle(3.05f, 1.10f, -0.15f, 0.00f)),
  PottedTree(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(170, 65, 1, 1, 14, 19)),
    new Rectangle(1.10f, 0.85f, 0.00f, 0.00f)),
  TrashCanGreen(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(116, 143, 1, 1, 9, 14)),
    new Rectangle(1.00f, 1.00f, -0.15f, 0.00f)),
  TrashCanRed(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(126, 143, 1, 1, 9, 14)),
    new Rectangle(1.00f, 1.00f, -0.15f, 0.00f)),
  TrashCanBlue(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(136, 143, 1, 1, 9, 14)),
    new Rectangle(1.00f, 1.00f, -0.15f, 0.00f)),
  WaterDispenser(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(147, 140, 1, 1, 9, 17)),
    new Rectangle(1.00f, 1.00f, -0.15f, 0.00f)),
  SnackDispenser(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(159, 123, 1, 1, 24, 34)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  BookShelf(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(184, 126, 1, 1, 24, 31)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  FolderRed(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(211, 119, 1, 1, 11, 8)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  FolderBlue(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(211, 129, 1, 1, 11, 8)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  FolderGreen(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(211, 140, 1, 1, 11, 8)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  SheetBlank(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(211, 151, 1, 1, 10, 6)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Clock(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(159, 108, 1, 1, 19, 6)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  SheetWritten1(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(183, 107, 1, 1, 6, 8)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  SheetWritten2(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(192, 107, 1, 1, 6, 9)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Printer(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(200, 107, 1, 1, 9, 9)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  PCThick("spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(233, 106, 1, 1, 15, 19)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  PCFlat(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(233, 128, 1, 1, 13, 23)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  CoffeeMachine(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(159, 91, 1, 1, 7, 11)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Cup(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(169, 95, 1, 1, 8, 7)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  FlagIndia(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(179, 94, 1, 1, 12, 9)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  FlagUK(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(193, 94, 1, 1, 12, 9)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  FlagUSA(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(207, 94, 1, 1, 12, 9)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Painting1(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(222, 94, 1, 1, 6, 8)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Painting2(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(230, 94, 1, 1, 6, 8)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Painting3(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(239, 94, 1, 1, 11, 8)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Board(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(234, 81, 1, 1, 17, 11)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Stickies(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(217, 107, 1, 1, 6, 8)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  ThinWallHorizontal(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(171, 44, 1, 1, 79, 17)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  ThinWallVertical(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(207, 63, 1, 1, 4, 27)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Furniture(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(3, 68, 1, 1, 73, 24)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  WallWhite(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(84, 70, 1, 1, 26, 20)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Desk1(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(188, 63, 1, 1, 17, 19)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Desk2(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(213, 63, 1, 1, 17, 19)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Window1(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(59, 96, 1, 1, 26, 21)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Window2(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(88, 96, 1, 1, 26, 21)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Elevator(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(98, 120, 1, 1, 16, 31)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Cat(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(65, 129, 1, 1, 16, 14)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Dog(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(59, 147, 1, 1, 24, 11)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Sky(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(0, 0, 1, 1, 256, 38)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Tom(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(2, 105, 1, 1, 15, 23)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Joe(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(19, 104, 1, 1, 19, 24)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Bill(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(40, 107, 1, 1, 13, 21)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Julia(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(3, 132, 1, 1, 17, 23)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),
  Laura(
    "spritesheets/PixelOfficeAssets.png",
    normScale(new SpritesheetConfig(22, 132, 1, 1, 17, 23)),
    new Rectangle(2.20f, 2.00f, -0.15f, 0.00f)),

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



  // Warum ist in AnimationConfig scaleX = 1 aber scaleY = 0? Das sorgt für eine asymmetrische Skalierung bzw. Verzerrung.
  private static AnimationConfig normScale(SpritesheetConfig sc) {
    float TILE_SIZE = 16f;
    float REZISE_FACTOR = 1.3f;

    AnimationConfig cfg = new AnimationConfig(sc);
    if (sc.spriteHeight() > sc.spriteWidth()) {
      cfg.scaleX(sc.spriteWidth() * REZISE_FACTOR / TILE_SIZE);
    } else if (sc.spriteWidth() > sc.spriteHeight()) {
      cfg.scaleY(sc.spriteHeight() * REZISE_FACTOR / TILE_SIZE);
    } else {
      cfg.scaleX(sc.spriteWidth() * REZISE_FACTOR / TILE_SIZE);
    }
    return cfg;
  }


}
