package rooms.systemRecovery.modules.computer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.utils.Point;
import feature.components.InventoryComponent;
import feature.components.ItemComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.items.SortProgramStickItem;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;

/** Regression tests for computer chip phases and one-time item transfer callbacks. */
class SystemRecoveryComputerFactoryTest {

  private Entity player;

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    player = new Entity("computer-test-player");
    player.add(new InventoryComponent(3));
    Game.add(player);
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
  }

  @Test
  void emptySearchChipCanOpenItsEditorOnlyDuringSearchProgramming() {
    assertTrue(
        ComputerProgramRules.canMount(
            ComputerProgramKind.SEARCH, SystemRecoveryLearningStep.SEARCH_PROGRAM));
    assertFalse(
        ComputerProgramRules.canMount(
            ComputerProgramKind.SEARCH, SystemRecoveryLearningStep.SEARCH_ROBOT_RUN));
    assertTrue(
        ComputerProgramRules.canSave(
            ComputerProgramKind.SEARCH, SystemRecoveryLearningStep.SEARCH_PROGRAM));
  }

  @Test
  void sortStickCanResumeAfterClosingEditorWithoutAdvancingOnMount() {
    assertTrue(
        ComputerProgramRules.canMount(
            ComputerProgramKind.SORT, SystemRecoveryLearningStep.BUBBLE_SORT_CONDITION));
    assertTrue(
        ComputerProgramRules.canSave(
            ComputerProgramKind.SORT, SystemRecoveryLearningStep.BUBBLE_SORT_CONDITION));
  }

  @Test
  void accessChipCanBeMountedOnlyAfterSearchRobotCompletes() {
    assertTrue(
        ComputerProgramRules.canMount(
            ComputerProgramKind.ACCESS, SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS));
    assertFalse(
        ComputerProgramRules.canMount(
            ComputerProgramKind.ACCESS, SystemRecoveryLearningStep.SEARCH_ROBOT_RUN));
    assertTrue(
        ComputerProgramRules.canSave(
            ComputerProgramKind.ACCESS, SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS));
  }

  @Test
  void confirmationRechecksInventoryAndRejectsAChipRemovedEarlier() {
    SortProgramStickItem stick = new SortProgramStickItem();
    InventoryComponent inventory = player.fetch(InventoryComponent.class).orElseThrow();
    assertTrue(inventory.add(stick));

    assertTrue(SystemRecoveryComputerFactory.removeMountedChip(player.id(), stick).isPresent());
    assertTrue(SystemRecoveryComputerFactory.removeMountedChip(player.id(), stick).isEmpty());
    assertEquals(0, inventory.count(SortProgramStickItem.class));
  }

  @Test
  void duplicateCloseCallbacksReturnOneChipOnly() {
    SortProgramStickItem stick = new SortProgramStickItem();
    ComputerChipSession session = new ComputerChipSession();

    assertTrue(
        session.resolve(() -> SystemRecoveryComputerFactory.addToInventory(player.id(), stick)));
    assertFalse(
        session.resolve(() -> SystemRecoveryComputerFactory.addToInventory(player.id(), stick)));
    assertEquals(
        1, player.fetch(InventoryComponent.class).orElseThrow().count(SortProgramStickItem.class));
  }

  @Test
  void saveFromAnotherTabCannotPassThePhaseRules() {
    assertFalse(
        ComputerProgramRules.canSave(
            ComputerProgramKind.SEARCH, SystemRecoveryLearningStep.BUBBLE_SORT_CONDITION));
    assertFalse(
        ComputerProgramRules.canSave(
            ComputerProgramKind.SORT, SystemRecoveryLearningStep.SEARCH_PROGRAM));
    assertFalse(
        ComputerProgramRules.canSave(
            ComputerProgramKind.NONE, SystemRecoveryLearningStep.BUBBLE_SORT_CONDITION));
  }

  @Test
  void fullInventoryDoesNotRemoveTheOriginalChipOrCreateAProgrammedOne() {
    Entity fullPlayer = new Entity("full-inventory-player");
    InventoryComponent inventory = new InventoryComponent(1);
    SearchProgramChipItem occupyingItem = new SearchProgramChipItem();
    assertTrue(inventory.add(occupyingItem));
    fullPlayer.add(inventory);
    fullPlayer.add(new PositionComponent(new Point(2, 3)));
    Game.add(fullPlayer);
    SortProgramStickItem insertedStick = new SortProgramStickItem();

    assertFalse(
        SystemRecoveryComputerFactory.addToInventory(
            fullPlayer.id(), new SortProgramStickItem(true)));
    assertTrue(inventory.hasItem(occupyingItem));
    assertEquals(1, inventory.count(SearchProgramChipItem.class));
    assertTrue(SystemRecoveryComputerFactory.returnInsertedChip(fullPlayer.id(), insertedStick));
    assertEquals(1, inventory.count(SearchProgramChipItem.class));
    assertEquals(
        1,
        Game.entities()
            .filter(entity -> entity.name().startsWith("worldItem_"))
            .filter(
                entity -> entity.fetch(ItemComponent.class).orElseThrow().item() == insertedStick)
            .count());
  }

  @Test
  void successfulSavePreventsCloseFromReturningTheEmptyChipAgain() {
    SortProgramStickItem emptyStick = new SortProgramStickItem();
    SortProgramStickItem programmedStick = new SortProgramStickItem(true);
    ComputerChipSession session = new ComputerChipSession();
    InventoryComponent inventory = player.fetch(InventoryComponent.class).orElseThrow();
    assertTrue(inventory.add(emptyStick));
    assertTrue(
        SystemRecoveryComputerFactory.removeMountedChip(player.id(), emptyStick).isPresent());

    assertTrue(
        session.resolve(
            () -> SystemRecoveryComputerFactory.addToInventory(player.id(), programmedStick)));
    assertFalse(
        session.resolve(
            () -> SystemRecoveryComputerFactory.returnInsertedChip(player.id(), emptyStick)));
    assertEquals(1, inventory.count(SortProgramStickItem.class));
    assertTrue(inventory.hasItem(programmedStick));
  }

  @Test
  void failedTransferCanBeRetriedWithoutMarkingTheSessionResolved() {
    ComputerChipSession session = new ComputerChipSession();

    assertFalse(session.resolve(() -> false));
    assertFalse(session.resolved());
    assertTrue(session.resolve(() -> true));
    assertTrue(session.resolved());
  }
}
