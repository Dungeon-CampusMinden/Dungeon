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
  /** A decoration. */
  ChairRed(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(6, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  ChairYellow(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(19, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  ChairGreen(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(32, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  ChairBlue(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(45, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  ChairWhite(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(58, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  ChairGrey(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(71, 41, 1, 1, 11, 22)).scaleX(0.8f),
      new Rectangle(1.00f, 1.05f, -0.1f, 0.00f)),
  /** A decoration. */
  TableShort(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(85, 47, 1, 1, 26, 16)).scaleX(1.3f),
      new Rectangle(2.30f, 1.30f, -0.10f, 0.00f)),
  /** A decoration. */
  TableLong(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(115, 47, 1, 1, 40, 16)).scaleX(1.3f),
      new Rectangle(3.45f, 1.30f, -0.10f, 0.00f)),
  /** A decoration. */
  CouchWhite(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(119, 65, 1, 1, 33, 16)).scaleX(1.2f),
      new Rectangle(2.60f, 1.10f, -0.05f, 0.00f)),
  /** A decoration. */
  CouchBlue(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(120, 83, 1, 1, 33, 16)).scaleX(1.2f),
      new Rectangle(2.60f, 1.10f, -0.05f, 0.00f)),
  /** A decoration. */
  CouchGreen(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(120, 102, 1, 1, 33, 16)).scaleX(1.2f),
      new Rectangle(2.60f, 1.10f, -0.05f, 0.00f)),
  /** A decoration. */
  CouchOrange(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(120, 121, 1, 1, 33, 16)).scaleX(1.2f),
      new Rectangle(2.60f, 1.10f, -0.05f, 0.00f)),
  /** A decoration. */
  PottedTree(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(170, 65, 1, 1, 14, 19)).scaleX(1.2f),
      new Rectangle(1.20f, 0.85f, 0.00f, 0.00f)),
  /** A decoration. */
  TrashCanGreen(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(116, 143, 1, 1, 9, 14)).scaleX(0.7f),
      new Rectangle(0.90f, 0.85f, -0.10f, 0.00f)),
  /** A decoration. */
  TrashCanRed(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(126, 143, 1, 1, 9, 14)).scaleX(0.7f),
      new Rectangle(0.90f, 0.85f, -0.10f, 0.00f)),
  /** A decoration. */
  TrashCanBlue(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(136, 143, 1, 1, 9, 14)).scaleX(0.7f),
      new Rectangle(0.90f, 0.85f, -0.10f, 0.00f)),
  /** A decoration. */
  WaterDispenser(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(147, 140, 1, 1, 9, 17)).scaleX(0.7f),
      new Rectangle(0.90f, 0.85f, -0.10f, 0.00f)),
  /** A decoration. */
  SnackDispenser(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(159, 123, 1, 1, 24, 34)).scaleX(1.7f),
      new Rectangle(1.90f, 1.40f, -0.10f, 0.00f)),
  /** A decoration. */
  BookShelf(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(184, 126, 1, 1, 24, 31)).scaleX(1.7f),
      new Rectangle(1.90f, 1.40f, -0.10f, 0.00f)),
  /** A decoration. */
  FolderRed(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(211, 119, 1, 1, 11, 8)).scaleX(0.6f),
      new Rectangle(0.85f, 0.70f, 0.00f, 0.00f)),
  /** A decoration. */
  FolderBlue(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(211, 129, 1, 1, 11, 8)).scaleX(0.6f),
      new Rectangle(0.85f, 0.70f, 0.00f, 0.00f)),
  /** A decoration. */
  FolderGreen(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(211, 140, 1, 1, 11, 8)).scaleX(0.6f),
      new Rectangle(0.85f, 0.70f, 0.00f, 0.00f)),
  /** A decoration. */
  SheetBlank(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(211, 151, 1, 1, 10, 6)).scaleX(0.5f),
      new Rectangle(0.95f, 0.50f, -0.05f, 0.00f)),
  /** A decoration. */
  Clock(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(159, 108, 1, 1, 19, 6)).scaleX(0.5f),
      new Rectangle(1.60f, 0.50f, 0.00f, 0.00f)),
  /** A decoration. */
  SheetWritten1(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(183, 107, 1, 1, 6, 8)).scaleX(0.5f),
      new Rectangle(0.50f, 0.70f, 0.00f, 0.00f)),
  /** A decoration. */
  SheetWritten2(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(192, 107, 1, 1, 6, 9)).scaleX(0.5f),
      new Rectangle(0.50f, 0.75f, 0.00f, 0.00f)),
  /** A decoration. */
  Printer(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(200, 107, 1, 1, 9, 9)).scaleX(0.65f),
      new Rectangle(0.65f, 0.65f, 0.00f, 0.00f)),
  /** A decoration. */
  PCThick(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(233, 106, 1, 1, 15, 19)).scaleX(1.0f),
      new Rectangle(1.00f, 1.05f, 0.00f, 0.00f)),
  /** A decoration. */
  PCFlat(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(233, 128, 1, 1, 13, 23)).scaleX(1.0f),
      new Rectangle(1.00f, 1.45f, 0.00f, 0.00f)),
  /** A decoration. */
  CoffeeMachine(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(159, 91, 1, 1, 7, 11)).scaleX(0.55f),
      new Rectangle(0.55f, 0.65f, 0.00f, 0.00f)),
  /** A decoration. */
  Cup(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(169, 95, 1, 1, 8, 7)).scaleX(0.55f),
      new Rectangle(0.65f, 0.55f, 0.00f, 0.00f)),
  /** A decoration. */
  FlagIndia(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(179, 94, 1, 1, 12, 9)).scaleX(0.75f),
      new Rectangle(1.00f, 0.75f, 0.00f, 0.00f)),
  /** A decoration. */
  FlagUK(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(193, 94, 1, 1, 12, 9)).scaleX(0.75f),
      new Rectangle(1.00f, 0.75f, 0.00f, 0.00f)),
  /** A decoration. */
  FlagUSA(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(207, 94, 1, 1, 12, 9)).scaleX(0.75f),
      new Rectangle(1.00f, 0.75f, 0.00f, 0.00f)),
  /** A decoration. */
  Painting1(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(222, 94, 1, 1, 6, 8)).scaleX(0.5f),
      new Rectangle(0.50f, 0.65f, 0.00f, 0.00f)),
  /** A decoration. */
  Painting2(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(230, 94, 1, 1, 6, 8)).scaleX(0.5f),
      new Rectangle(0.50f, 0.65f, 0.00f, 0.00f)),
  /** A decoration. */
  Painting3(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(239, 94, 1, 1, 11, 8)).scaleX(0.5f),
      new Rectangle(0.70f, 0.50f, 0.00f, 0.00f)),
  /** A decoration. */
  Board(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(234, 81, 1, 1, 17, 11)).scaleX(0.9f),
      new Rectangle(1.40f, 0.90f, 0.00f, 0.00f)),
  /** A decoration. */
  Stickies(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(217, 107, 1, 1, 6, 8)).scaleX(0.5f),
      new Rectangle(0.50f, 0.65f, 0.00f, 0.00f)),
  /** A decoration. */
  ThinWallHorizontal(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(171, 44, 1, 1, 79, 17)).scaleX(1.3f),
      new Rectangle(6.05f, 0.50f, 0.00f, 0.00f)),
  /** A decoration. */
  ThinWallVertical(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(207, 63, 1, 1, 4, 27)).scaleX(0.37f),
      new Rectangle(0.40f, 1.45f, 0.00f, 0.00f)),
  /** A decoration. */
  Furniture(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(3, 68, 1, 1, 73, 24)).scaleX(2.15f)),
  /** A decoration. */
  WallWhite(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(84, 70, 1, 1, 26, 20)).scaleX(1.55f),
      new Rectangle(2.05f, 1.55f, 0.00f, 0.00f)),
  /** A decoration. */
  Desk1(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(188, 63, 1, 1, 17, 19)).scaleX(1.4f),
      new Rectangle(1.40f, 1.40f, 0.00f, 0.00f)),
  /** A decoration. */
  Desk2(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(213, 63, 1, 1, 17, 19)).scaleX(1.4f),
      new Rectangle(1.40f, 1.40f, 0.00f, 0.00f)),
  /** A decoration. */
  Window1(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(59, 96, 1, 1, 26, 21)).scaleX(1.55f),
      new Rectangle(1.95f, 1.55f, 0.00f, 0.00f)),
  /** A decoration. */
  Window2(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(88, 96, 1, 1, 26, 21)).scaleX(1.55f),
      new Rectangle(1.95f, 1.55f, 0.00f, 0.00f)),
  /** A decoration. */
  Elevator(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(98, 120, 1, 1, 16, 31)).scaleX(1.25f),
      new Rectangle(1.25f, 2.40f, 0.00f, 0.00f)),
  /** A decoration. */
  Cat(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(65, 129, 1, 1, 16, 14)).scaleX(1.1f),
      new Rectangle(0.95f, 0.65f, 0.15f, 0.00f)),
  /** A decoration. */
  Dog(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(59, 147, 1, 1, 24, 11)).scaleX(0.9f),
      new Rectangle(1.55f, 0.70f, 0.20f, 0.00f)),
  /** A decoration. */
  Sky(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(0, 0, 1, 1, 256, 38)).scaleX(2.7f)),
  /** A decoration. */
  Tom(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(2, 105, 1, 1, 15, 23)).scaleX(1.15f),
      new Rectangle(0.95f, 0.95f, 0.10f, 0.00f)),
  /** A decoration. */
  Joe(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(19, 104, 1, 1, 19, 24)).scaleX(1.15f),
      new Rectangle(0.95f, 0.95f, 0.10f, 0.00f)),
  /** A decoration. */
  Bill(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(40, 107, 1, 1, 13, 21)).scaleX(1.15f),
      new Rectangle(0.95f, 0.95f, 0.10f, 0.00f)),
  /** A decoration. */
  Sarah(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(3, 132, 1, 1, 17, 23)).scaleX(1.15f),
      new Rectangle(0.95f, 0.95f, 0.10f, 0.00f)),
  /** A decoration. */
  Laura(
      "spritesheets/PixelOfficeAssets.png",
      new AnimationConfig(new SpritesheetConfig(22, 132, 1, 1, 17, 23)).scaleX(1.15f),
      new Rectangle(0.95f, 0.95f, 0.10f, 0.00f)),

  /** Free Pixel Office Assets */
  /** A decoration. */
  BossAtDesk(
      "office/boss.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 51, 48)).scaleX(2.30f),
      new Rectangle(2.45f, 1.87f, 0.00f, 0.00f)),
  /** A decoration. */
  Worker1(
      "office/worker1.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 51, 48)).scaleX(2.30f),
      new Rectangle(1.80f, 1.70f, 0.00f, 0.00f)),
  /** A decoration. */
  Worker2(
      "office/worker2.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 51, 48)).scaleX(2.30f),
      new Rectangle(1.80f, 1.70f, 0.00f, 0.00f)),
  /** A decoration. */
  Worker3(
      "office/worker4.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 51, 48)).scaleX(2.30f),
      new Rectangle(1.80f, 1.70f, 0.00f, 0.00f)),
  /** A decoration. */
  Cabinet(
      "office/cabinet.png",
      new AnimationConfig(new SpritesheetConfig(18, 6, 1, 1, 25, 43)).scaleX(1.40f),
      new Rectangle(1.40f, 1.35f, 0.00f, 0.00f)),
  /** A decoration. */
  Chair(
      "office/chair.png",
      new AnimationConfig(new SpritesheetConfig(2, 0, 1, 1, 12, 16)).scaleX(0.55f),
      new Rectangle(0.55f, 0.45f, 0.00f, 0.00f)),
  /** A decoration. */
  CoffeeMaker(
      "office/coffee-maker.png",
      new AnimationConfig(new SpritesheetConfig(1, 8, 1, 1, 62, 43)).scaleX(2.35f),
      new Rectangle(3.35f, 1.20f, 0.00f, 0.00f)),
  /** A decoration. */
  Desk(
      "office/desk.png",
      new AnimationConfig(new SpritesheetConfig(13, 3, 1, 1, 38, 26)).scaleX(1.40f),
      new Rectangle(2.05f, 1.20f, 0.00f, 0.00f)),
  /** A decoration. */
  DeskWithPC1(
      "office/desk-with-pc.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 38, 35)).scaleX(1.95f),
      new Rectangle(2.15f, 1.30f, 0.00f, 0.00f)),
  /** A decoration. */
  DeskWithPC2(
      "office/Julia_PC.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 38, 41)).scaleX(2.05f),
      new Rectangle(2.05f, 1.55f, 0.00f, 0.00f)),
  /** A decoration. */
  WallCorner(
      "office/office-partitions-1.png",
      new AnimationConfig(new SpritesheetConfig(0, 4, 1, 1, 64, 60)).scaleX(3.20f),
      new Rectangle(3.40f, 2.20f, 0.00f, 0.00f)),
  /** A decoration. */
  WallVertical(
      "office/office-partitions-2.png",
      new AnimationConfig(new SpritesheetConfig(30, 4, 1, 1, 3, 60)).scaleX(0.16f),
      new Rectangle(0.15f, 2.15f, 0.00f, 0.00f)),
  /** A decoration. */
  PCOn(
      "office/PC1.png",
      new AnimationConfig(new SpritesheetConfig(4, 4, 1, 1, 25, 22)).scaleX(1.20f),
      new Rectangle(1.40f, 0.90f, 0.00f, 0.00f)),
  /** A decoration. */
  PCOff(
      "office/PC2.png",
      new AnimationConfig(new SpritesheetConfig(4, 4, 1, 1, 25, 22)).scaleX(1.20f),
      new Rectangle(1.40f, 0.90f, 0.00f, 0.00f)),
  /** A decoration. */
  Plant(
      "office/plant.png",
      new AnimationConfig(new SpritesheetConfig(9, 9, 1, 1, 11, 23)).scaleX(0.50f),
      new Rectangle(0.50f, 0.50f, 0.00f, 0.00f)),
  /** A decoration. */
  Printer1(
      "office/printer.png",
      new AnimationConfig(new SpritesheetConfig(2, 2, 1, 1, 29, 30)).scaleX(1.40f),
      new Rectangle(1.40f, 1.00f, 0.00f, 0.00f)),
  /** A decoration. */
  Printer2(
      "office/printer.png",
      new AnimationConfig(new SpritesheetConfig(34, 2, 1, 1, 29, 30)).scaleX(1.40f),
      new Rectangle(1.40f, 1.00f, 0.00f, 0.00f)),
  /** A decoration. */
  StampingTable(
      "office/stamping-table.png",
      new AnimationConfig(new SpritesheetConfig(5, 3, 1, 1, 46, 26)).scaleX(1.50f),
      new Rectangle(2.65f, 1.20f, 0.00f, 0.00f)),
  /** A decoration. */
  WritingTable(
      "office/writing-table.png",
      new AnimationConfig(new SpritesheetConfig(13, 13, 1, 1, 38, 36)).scaleX(2.10f),
      new Rectangle(2.25f, 1.80f, 0.00f, 0.00f)),
  /** A decoration. */
  TrashBin(
      "office/trash.png",
      new AnimationConfig(new SpritesheetConfig(3, 3, 1, 1, 9, 10)).scaleX(0.60f),
      new Rectangle(0.60f, 0.50f, 0.00f, 0.00f)),
  /** A decoration. */
  WaterCooler(
      "office/water-cooler.png",
      new AnimationConfig(new SpritesheetConfig(0, 4, 1, 1, 14, 27)).scaleX(0.70f),
      new Rectangle(0.70f, 0.70f, 0.00f, 0.00f)),
  /** A decoration. */
  Sink(
      "office/sink.png",
      new AnimationConfig(new SpritesheetConfig(15, 22, 1, 1, 34, 26)).scaleX(1.50f),
      new Rectangle(2.00f, 1.10f, 0.00f, 0.00f)),
  /** A decoration. */
  JuliaFront(
      "office/Julia.png",
      new AnimationConfig(new SpritesheetConfig(4, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaRight(
      "office/Julia.png",
      new AnimationConfig(new SpritesheetConfig(33, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaBack(
      "office/Julia.png",
      new AnimationConfig(new SpritesheetConfig(68, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaLeft(
      "office/Julia.png",
      new AnimationConfig(new SpritesheetConfig(103, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaDrinkingCoffee1(
      "office/Julia_Drinking_Coffee.png",
      new AnimationConfig(new SpritesheetConfig(4, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaDrinkingCoffee2(
      "office/Julia_Drinking_Coffee.png",
      new AnimationConfig(new SpritesheetConfig(36, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaDrinkingCoffee3(
      "office/Julia_Drinking_Coffee.png",
      new AnimationConfig(new SpritesheetConfig(68, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaIdle1(
      "office/Julia-Idle.png",
      new AnimationConfig(new SpritesheetConfig(4, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaIdle2(
      "office/Julia-Idle.png",
      new AnimationConfig(new SpritesheetConfig(36, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaIdle3(
      "office/Julia-Idle.png",
      new AnimationConfig(new SpritesheetConfig(68, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaIdle4(
      "office/Julia-Idle.png",
      new AnimationConfig(new SpritesheetConfig(100, 0, 1, 1, 24, 32)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkForward1(
      "office/Julia_walk_Foward.png",
      new AnimationConfig(new SpritesheetConfig(20, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkForward2(
      "office/Julia_walk_Foward.png",
      new AnimationConfig(new SpritesheetConfig(84, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkForward3(
      "office/Julia_walk_Foward.png",
      new AnimationConfig(new SpritesheetConfig(148, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkForward4(
      "office/Julia_walk_Foward.png",
      new AnimationConfig(new SpritesheetConfig(212, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkLeft1(
      "office/Julia_walk_Left.png",
      new AnimationConfig(new SpritesheetConfig(18, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkLeft2(
      "office/Julia_walk_Left.png",
      new AnimationConfig(new SpritesheetConfig(82, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkLeft3(
      "office/Julia_walk_Left.png",
      new AnimationConfig(new SpritesheetConfig(146, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkLeft4(
      "office/Julia_walk_Left.png",
      new AnimationConfig(new SpritesheetConfig(210, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkRight1(
      "office/Julia_walk_Rigth.png",
      new AnimationConfig(new SpritesheetConfig(17, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkRight2(
      "office/Julia_walk_Rigth.png",
      new AnimationConfig(new SpritesheetConfig(81, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkRight3(
      "office/Julia_walk_Rigth.png",
      new AnimationConfig(new SpritesheetConfig(145, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkRight4(
      "office/Julia_walk_Rigth.png",
      new AnimationConfig(new SpritesheetConfig(209, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkUp1(
      "office/Julia_walk_Up.png",
      new AnimationConfig(new SpritesheetConfig(20, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkUp2(
      "office/Julia_walk_Up.png",
      new AnimationConfig(new SpritesheetConfig(84, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkUp3(
      "office/Julia_walk_Up.png",
      new AnimationConfig(new SpritesheetConfig(148, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),
  /** A decoration. */
  JuliaWalkUp4(
      "office/Julia_walk_Up.png",
      new AnimationConfig(new SpritesheetConfig(212, 15, 1, 1, 24, 33)).scaleX(1.20f),
      new Rectangle(1.00f, 1.00f, 0.10f, 0.00f)),

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
