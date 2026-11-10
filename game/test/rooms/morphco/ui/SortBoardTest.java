package rooms.morphco.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.badlogic.gdx.math.Vector2;
import feature.canvas.CanvasArea;
import feature.canvas.CanvasOptions;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SortBoardTest {

  private int changes;
  private SortBoard board;

  @BeforeEach
  void setUp() {
    board = new SortBoard(new CanvasArea("test", 800, 600, new CanvasOptions()), () -> changes++);
    board.item("a", "a", 20);
    board.item("b", "b", 20);
    board.bin("crate", "Crate", 0, 100);
    board.bin("bowl", "Bowl", 1, 100);
  }

  @Test
  void fullBinReturnsItsOldestItemHome() {
    board.place("a", "bowl");
    board.place("b", "bowl");

    assertEquals(Map.of("b", "bowl"), board.placement());
  }

  @Test
  void clickOnChipThenOnBinPlacesTheItem() {
    board.clickChip("a");
    assertEquals("a", board.selected());

    board.clickBin("crate");

    assertEquals(Map.of("a", "crate"), board.placement());
    assertNull(board.selected());
    assertEquals(1, changes);
  }

  @Test
  void secondClickOnHomeChipDropsTheSelection() {
    board.clickChip("a");
    board.clickChip("a");
    board.clickBin("crate");

    assertNull(board.selected());
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
    assertNull(board.selected());

    board.place("a", "crate");
    board.clickSlot("a");
    board.clickBin("bowl");

    assertEquals(Map.of("a", "bowl"), board.placement());
  }

  @Test
  void placedChipReleasedOutsideOfAllBinsGoesHome() {
    board.place("a", "crate");

    board.dropOutside("a");

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

    assertEquals(Map.of("a", "crate"), board.placement());
    assertNull(board.selected());
  }

  @Test
  void flowWrapsBoxesThatDoNotFitIntoTheRow() {
    SortBoard.Flow flow = SortBoard.flow(List.of(100f, 100f, 100f), 250, 40);

    assertEquals(
        List.of(new Vector2(0, 0), new Vector2(108, 0), new Vector2(0, 48)), flow.offsets());
    assertEquals(88, flow.height());
  }

  @Test
  void flowGivesAnOverlongBoxARowOfItsOwn() {
    SortBoard.Flow flow = SortBoard.flow(List.of(300f, 50f), 250, 40);

    assertEquals(List.of(new Vector2(0, 0), new Vector2(0, 48)), flow.offsets());
    assertEquals(0, SortBoard.flow(List.of(), 250, 40).height());
  }
}
