package feature.interaction.combinationlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContextKeys;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the wheels of a combination lock, its buttons and dialed codes on the server and its
 * synchronization.
 */
class CombinationLockTest {

  private final CombinationLockComponent component = new CombinationLockComponent(2);
  private final Entity lock = new Entity("lock");
  private final Entity user = new Entity("user");

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    lock.add(component);
    Game.add(lock);
    Game.add(user);
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
  }

  @Test
  void wheelsRollOverBetweenNineAndZero() {
    component.turn(-1);
    assertEquals("90", component.digits());
    component.turn(1);
    assertEquals("00", component.digits());
  }

  @Test
  void selectionWrapsAroundTheWheels() {
    component.select(-1);
    assertEquals(1, component.selected());
    component.select(1);
    assertEquals(0, component.selected());
  }

  @Test
  void buttonsTurnAndSelectWheelsUntilTheUserTriesToOpen() {
    assertFalse(CombinationLock.press(lock, user, CombinationLock.DOWN));
    assertFalse(CombinationLock.press(lock, user, CombinationLock.RIGHT));
    assertFalse(CombinationLock.press(lock, user, CombinationLock.UP));
    assertFalse(CombinationLock.press(lock, user, "kick"));
    assertEquals("91", component.digits());
    assertTrue(CombinationLock.press(lock, user, CombinationLock.OPEN));
  }

  @Test
  void openClosesTheDialogAndHandsOverTheDigits() {
    AtomicReference<String> opened = new AtomicReference<>();
    UIComponent ui =
        CombinationLock.show(lock, user, "Schloss", "Welche Zahl?", "", true, opened::set);
    press(ui, CombinationLock.UP);
    assertNull(opened.get());
    press(ui, CombinationLock.OPEN);
    assertEquals("10", opened.get());
    assertTrue(Game.entities(Set.of(UIComponent.class)).findAny().isEmpty());
  }

  @Test
  void dialTurnsOneWheelAfterTheOtherAndThenTriesToOpen() {
    List<Long> delays = new ArrayList<>();
    List<Runnable> steps = new ArrayList<>();
    AtomicReference<String> opened = new AtomicReference<>();
    boolean dialing =
        CombinationLock.dial(
            lock,
            user,
            "83",
            () -> opened.set(component.digits()),
            (step, delay) -> {
              steps.add(step);
              delays.add(delay);
            });

    assertTrue(dialing);
    // The first wheel turns at once, the second and the try to open follow.
    assertEquals("80", component.digits());
    assertEquals(List.of(800L, 2000L), delays);
    steps.get(0).run();
    assertEquals("83", component.digits());
    assertEquals(1, component.selected());
    steps.get(1).run();
    assertEquals("83", opened.get());
  }

  @Test
  void codeOfTheWrongLengthDialsNothing() {
    assertFalse(CombinationLock.dial(lock, user, "834", () -> {}, (step, delay) -> step.run()));
    assertEquals("00", component.digits());
  }

  @Test
  void clientReceivesTheWheels() {
    component.digits("83");
    component.selected(1);
    Entity copy = new Entity("copy");
    CombinationLockSync.apply(copy, CombinationLockSync.metadata(lock));
    CombinationLockComponent synced = copy.fetch(CombinationLockComponent.class).orElseThrow();
    assertEquals("83", synced.digits());
    assertEquals(1, synced.selected());
  }

  @Test
  void invalidStateLeavesTheClientUnchanged() {
    Map<String, String> metadata = new HashMap<>(CombinationLockSync.metadata(lock));
    metadata.put(CombinationLockSync.SELECTED_KEY, "2");
    Entity copy = new Entity("copy");
    CombinationLockSync.apply(copy, metadata);
    assertFalse(copy.isPresent(CombinationLockComponent.class));
  }

  private static void press(UIComponent ui, String action) {
    ui.callbacks()
        .get(DialogContextKeys.ON_CONFIRM)
        .accept(new DialogResponseMessage.StringValue(action));
  }
}
