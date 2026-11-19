package feature.leveleditor.ui;

import feature.entities.deco.Deco;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Categories of the decorations in the {@link DecoSelector}, by look and use. Decorations in no
 * category are listed under "Other".
 */
final class DecoCategories {

  private static final List<Category> CATEGORIES =
      List.of(
          new Category(
              "Dungeon",
              List.of(
                  Deco.BookshelfLarge,
                  Deco.Chains0,
                  Deco.Chains1,
                  Deco.Chains2,
                  Deco.Chains3,
                  Deco.Chains4,
                  Deco.Chains5,
                  Deco.Chains6,
                  Deco.Chains7,
                  Deco.Chains8,
                  Deco.FloorBarsSquare,
                  Deco.FloorBarsRound,
                  Deco.FloorBarsSmall,
                  Deco.WindowBarred,
                  Deco.PushStone)),
          new Category(
              "Nature",
              List.of(
                  Deco.TreeBig,
                  Deco.TreeBigChainL,
                  Deco.TreeBigChainM,
                  Deco.TreeBigChainR,
                  Deco.TreeMedium,
                  Deco.TreeSmall,
                  Deco.StumpSmall,
                  Deco.LogBig,
                  Deco.Reeds,
                  Deco.Bush1,
                  Deco.Bush2,
                  Deco.Bush3,
                  Deco.Bush4)),
          new Category(
              "People & Animals",
              List.of(
                  Deco.BossAtDesk, Deco.Worker1, Deco.Worker2, Deco.Worker3, Deco.Cat, Deco.Dog)),
          new Category(
              "Office",
              List.of(
                  Deco.ChairRed,
                  Deco.ChairYellow,
                  Deco.ChairGreen,
                  Deco.ChairBlue,
                  Deco.ChairWhite,
                  Deco.ChairGrey,
                  Deco.TableShort,
                  Deco.TableLong,
                  Deco.Desk1,
                  Deco.Desk2,
                  Deco.CouchWhite,
                  Deco.CouchBlue,
                  Deco.CouchGreen,
                  Deco.CouchOrange,
                  Deco.BookShelf,
                  Deco.Furniture)),
          new Category(
              "Office Items",
              List.of(
                  Deco.PCThick,
                  Deco.PCFlat,
                  Deco.Printer,
                  Deco.CoffeeMachine,
                  Deco.Cup,
                  Deco.WaterDispenser,
                  Deco.SnackDispenser,
                  Deco.FolderRed,
                  Deco.FolderBlue,
                  Deco.FolderGreen,
                  Deco.SheetBlank,
                  Deco.SheetWritten1,
                  Deco.SheetWritten2,
                  Deco.PottedTree,
                  Deco.TrashCanGreen,
                  Deco.TrashCanRed,
                  Deco.TrashCanBlue)),
          new Category(
              "Office Walls",
              List.of(
                  Deco.WallWhite,
                  Deco.ThinWallHorizontal,
                  Deco.ThinWallVertical,
                  Deco.Window1,
                  Deco.Window2,
                  Deco.Elevator,
                  Deco.Sky,
                  Deco.Clock,
                  Deco.Board,
                  Deco.Stickies,
                  Deco.Painting1,
                  Deco.Painting2,
                  Deco.Painting3,
                  Deco.FlagIndia,
                  Deco.FlagUK,
                  Deco.FlagUSA)),
          new Category(
              "Retro Office",
              List.of(
                  Deco.Desk,
                  Deco.DeskWithPC1,
                  Deco.WritingTable,
                  Deco.StampingTable,
                  Deco.Chair,
                  Deco.Cabinet,
                  Deco.PCOn,
                  Deco.PCOff,
                  Deco.Printer1,
                  Deco.Printer2,
                  Deco.Phone,
                  Deco.CoffeeMaker,
                  Deco.WaterCooler,
                  Deco.Sink,
                  Deco.TrashBin,
                  Deco.Plant,
                  Deco.WallCorner,
                  Deco.WallVertical)),
          new Category(
              "Laboratory",
              List.of(
                  Deco.DigitalArchiveShelf,
                  Deco.LabDesk,
                  Deco.LabChair,
                  Deco.LabLockers,
                  Deco.LabLocker,
                  Deco.LabCryoBox,
                  Deco.LabCryoBoxOff,
                  Deco.LabDevice,
                  Deco.LabBioComputer,
                  Deco.LabSuperComputer,
                  Deco.LabMachine,
                  Deco.LabScannerMachine,
                  Deco.LabSmallMachine,
                  Deco.LabServer,
                  Deco.LabPillar,
                  Deco.LabPillarBroken,
                  Deco.LabBarrel,
                  Deco.LabRedBarrel,
                  Deco.LabTeleporter,
                  Deco.LabPedestal)),
          new Category(
              "Laboratory Walls",
              List.of(
                  Deco.LabScreenArrow,
                  Deco.LabScreenCross,
                  Deco.LabScreenQuestion,
                  Deco.LabScreenFace,
                  Deco.LabScreenSurprised,
                  Deco.LabWindow,
                  Deco.LabWindowSmall,
                  Deco.LabWhiteboard,
                  Deco.LabChartBoard,
                  Deco.LabNeon,
                  Deco.LabWallPipes,
                  Deco.LabElectricPanel,
                  Deco.LabWallDevice,
                  Deco.LabGlyphs)),
          new Category(
              "Soulweaver",
              List.of(
                  Deco.SoulweaverWorkbench,
                  Deco.SoulweaverTabletopVase,
                  Deco.SoulweaverTabletopTools,
                  Deco.SoulweaverVase,
                  Deco.SoulweaverCrate,
                  Deco.SoulweaverKettle,
                  Deco.SoulweaverWorkMarkerNorth,
                  Deco.SoulweaverWorkMarkerWest,
                  Deco.SoulweaverWorkMarkerEast,
                  Deco.SoulweaverPit,
                  Deco.SoulweaverSmallCrate,
                  Deco.SoulweaverPump,
                  Deco.SoulweaverPipe,
                  Deco.SoulweaverGateHeader,
                  Deco.SoulweaverRunePlinth,
                  Deco.SoulweaverAltarPlinth,
                  Deco.SoulweaverSourceBench,
                  Deco.SoulweaverSmallPlinth,
                  Deco.SoulweaverTablet,
                  Deco.SoulweaverDrainedVase,
                  Deco.SoulweaverNote,
                  Deco.SoulweaverSourcePlinth,
                  Deco.SoulweaverSourceCrystal,
                  Deco.SoulweaverStrengthStone,
                  Deco.SoulweaverSourceTools,
                  Deco.SoulweaverSourceKettle,
                  Deco.SoulweaverCauldron,
                  Deco.SoulweaverSealBook,
                  Deco.SoulweaverSealGem,
                  Deco.SoulweaverHeartPlinth,
                  Deco.SoulweaverWinchFrame)));

  private DecoCategories() {}

  /**
   * A named group of decorations.
   *
   * @param name category name
   * @param decos decorations in display order
   */
  record Category(String name, List<Deco> decos) {}

  /**
   * Sorts decorations into their categories.
   *
   * @param decos decorations to sort
   * @return the categories with any of the decorations, holding only these, in display order;
   *     decorations in no category last, under "Other"
   */
  static List<Category> of(Collection<Deco> decos) {
    Set<Deco> remaining = new LinkedHashSet<>(decos);
    List<Category> result = new ArrayList<>();
    for (Category category : CATEGORIES) {
      List<Deco> contained = new ArrayList<>();
      for (Deco deco : category.decos()) {
        if (remaining.remove(deco)) contained.add(deco);
      }
      if (!contained.isEmpty()) result.add(new Category(category.name(), contained));
    }
    if (!remaining.isEmpty()) result.add(new Category("Other", List.copyOf(remaining)));
    return result;
  }
}
