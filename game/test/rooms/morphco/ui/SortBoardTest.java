package rooms.morphco.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import feature.canvas.CanvasArea;
import feature.canvas.CanvasDragContext;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasOptions;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SortBoardTest {

  private final CanvasArea area = new CanvasArea("test", 800, 600, new CanvasOptions());
  private int changes;
  private SortBoard board;
  private SortBoard.Slot ghostOfA;
  private SortBoard.Bin crate;
  private SortBoard.Bin bowl;

  @BeforeEach
  void setUp() {
    board = new SortBoard(area, () -> changes++);
    ghostOfA = board.item("a", "a", 20);
    board.item("b", "b", 20);
    crate = board.bin("crate", "Crate", 0, 100);
    bowl = board.bin("bowl", "Bowl", 1, 100);
    crate.size(200, 100);
    bowl.size(200, 100);
  }

  private boolean drop(CanvasNode dragged, SortBoard.Bin bin, float localX, float localY) {
    return bin.onNodeDropped(
        new CanvasDragContext(area, dragged, List.of(dragged), 0, 0, 0, 0, localX, localY));
  }

  @Test
  void fullBinSendsItsItemHome() {
    board.place("a", "bowl");
    board.place("b", "bowl");

    assertEquals(Map.of("b", "bowl"), board.placement());
  }

  @Test
  void clickOnChipThenOnBinPlacesTheItem() {
    board.clickChip("a");
    board.clickBin("crate");
    board.clickBin("bowl");

    assertEquals(Map.of("a", "crate"), board.placement());
    assertEquals(1, changes);
  }

  @Test
  void secondClickOnHomeChipDropsTheSelection() {
    board.clickChip("a");
    board.clickChip("a");
    board.clickBin("crate");

    assertEquals(Map.of(), board.placement());
  }

  @Test
  void clickOnPlacedChipPutsItBackUnlessAnotherItemIsSelected() {
    board.place("a", "crate");
    board.clickChip("b");

    board.clickChip("a");
    assertEquals(Map.of("a", "crate", "b", "crate"), board.placement());

    board.clickChip("a");
    assertEquals(Map.of("b", "crate"), board.placement());
  }

  @Test
  void clickOnGhostSelectsThePlacedItemForAnotherBin() {
    board.clickSlot("b");
    board.clickBin("crate");
    assertEquals(Map.of(), board.placement());

    board.place("a", "crate");
    board.clickSlot("a");
    board.clickBin("bowl");

    assertEquals(Map.of("a", "bowl"), board.placement());
  }

  @Test
  void clickOnGhostThenOnTheOwnChipPutsTheItemBack() {
    board.place("a", "crate");

    board.clickSlot("a");
    board.clickChip("a");

    assertEquals(Map.of(), board.placement());
  }

  @Test
  void ghostDroppedOnAnotherBinMovesTheItem() {
    board.place("a", "crate");

    assertTrue(drop(ghostOfA, bowl, 50, 50));

    assertEquals(Map.of("a", "bowl"), board.placement());
  }

  @Test
  void chipDroppedOnItsOwnBinChangesNothing() {
    board.place("a", "crate");

    assertTrue(drop(area.nodeById("a").orElseThrow(), crate, 50, 50));

    assertEquals(Map.of("a", "crate"), board.placement());
    assertEquals(1, changes);
  }

  @Test
  void chipReleasedBesideTheBinIsNotTaken() {
    assertFalse(drop(area.nodeById("a").orElseThrow(), crate, 250, 50));

    assertEquals(Map.of(), board.placement());
  }

  @Test
  void placedChipReleasedOutsideOfAllBinsGoesHome() {
    board.place("a", "crate");

    area.nodeById("a").orElseThrow().onDrop(0, 0);

    assertEquals(Map.of(), board.placement());
    assertEquals(2, changes);
  }

  @Test
  void removedItemsLoseTheirPlacementAndSelection() {
    board.item("w0#0", "un", 20);
    board.item("w0#1", "frei", 20);
    board.place("w0#0", "crate");
    board.place("a", "crate");
    board.clickChip("w0#1");

    board.removeItems("w0#");
    board.place("w0#0", "crate");
    board.clickBin("bowl");

    assertEquals(Map.of("a", "crate"), board.placement());
  }

  @Test
  void flowWrapsChipsThatDoNotFitIntoTheRow() {
    SortBoard.Flow flow = SortBoard.flow(List.of(100f, 100f, 100f), 250);

    float row = SortBoard.CHIP_HEIGHT + SortBoard.GAP;
    assertEquals(
        List.of(new Vector2(0, 0), new Vector2(108, 0), new Vector2(0, row)), flow.offsets());
    assertEquals(row + SortBoard.CHIP_HEIGHT, flow.height());
  }

  @Test
  void flowGivesAnOverlongChipARowOfItsOwn() {
    SortBoard.Flow flow = SortBoard.flow(List.of(300f, 50f), 250);

    assertEquals(
        List.of(new Vector2(0, 0), new Vector2(0, SortBoard.CHIP_HEIGHT + SortBoard.GAP)),
        flow.offsets());
    assertEquals(0, SortBoard.flow(List.of(), 250).height());
  }
}
