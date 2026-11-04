package engine.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import engine.utils.Vector2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests routing, ownership, cancelling and combos of {@link InputResolver}. */
class InputResolverTest {
  private static final ActionId ATTACK = new ActionId("test", "attack");
  private static final ActionId OTHER = new ActionId("test", "other");
  private static final ActionId CLOSE = new ActionId("test", "close");
  private static final ActionId PAUSE = new ActionId("test", "pause");
  private static final ActionId HUD = new ActionId("test", "hud");
  private static final ActionId LIST = new ActionId("test", "list");
  private static final ActionId LEFT = new ActionId("test", "left");
  private static final ActionId COPY = new ActionId("test", "copy");
  private static final ActionId DOORS = new ActionId("test", "doors");
  private static final ActionId STICK = new ActionId("test", "stick");
  private static final ActionId ZOOM = new ActionId("test", "zoom");

  private static final ContextSpec GAME =
      ContextSpec.of(
          "game", ContextLayer.BASE, false, ATTACK, OTHER, PAUSE, LEFT, DOORS, STICK, ZOOM);
  private static final ContextSpec DEBUG =
      ContextSpec.of("debug", ContextLayer.DEBUG, false, HUD, LIST, COPY);
  private static final ContextSpec DIALOG = ContextSpec.of("dialog", ContextLayer.UI, true, CLOSE);

  private final Set<Integer> stageHandledKeys = new HashSet<>();
  private final List<String> stageEvents = new ArrayList<>();
  private BindingTable table;
  private InputResolver resolver;
  private InputContext game;

  @BeforeEach
  void setUp() {
    table = new BindingTable();
    table.define(ATTACK, "key:Q", "mouse:LEFT");
    table.define(OTHER, "key:TAB", "key:I");
    table.define(CLOSE, "key:ESCAPE");
    table.define(PAUSE, "key:ESCAPE");
    table.define(HUD, "key:F3");
    table.define(LIST, "key:F3+A");
    table.define(LEFT, "key:A");
    table.define(COPY, "key:Ctrl+C");
    table.define(DOORS, "key:C");
    table.define(STICK, "pad:LSTICK");
    table.define(ZOOM, "mouse:WHEEL");
    resolver = new InputResolver(table);
    resolver.stage(
        new InputAdapter() {
          @Override
          public boolean keyDown(int keycode) {
            stageEvents.add("down " + keycode);
            return stageHandledKeys.contains(keycode);
          }

          @Override
          public boolean keyUp(int keycode) {
            stageEvents.add("up " + keycode);
            return false;
          }
        });
    game = new InputContext(GAME);
    resolver.push(game);
    resolver.push(new InputContext(DEBUG));
  }

  private void tap(int key) {
    resolver.keyDown(key);
    resolver.keyUp(key);
  }

  @Test
  void pressGoesToTopmostContextThatTakesIt() {
    List<String> fired = new ArrayList<>();
    game.onPress(PAUSE, () -> fired.add("pause"));
    InputContext dialog = new InputContext(DIALOG).onPress(CLOSE, () -> fired.add("close"));
    resolver.push(dialog);

    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.advance(0);

    assertEquals(List.of("close"), fired);
  }

  @Test
  void eventIsNeverRoutedTwiceAfterContextChange() {
    List<String> fired = new ArrayList<>();
    game.onPress(PAUSE, () -> fired.add("pause"));
    InputContext dialog = new InputContext(DIALOG);
    dialog.onPress(
        CLOSE,
        () -> {
          fired.add("close");
          resolver.remove(dialog);
        });
    resolver.push(dialog);

    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.advance(0);
    resolver.keyUp(Input.Keys.ESCAPE);
    resolver.advance(0);
    resolver.advance(0);

    assertEquals(List.of("close"), fired);
  }

  @Test
  void pressThatOpensBlockingContextDoesNotReachIt() {
    List<String> fired = new ArrayList<>();
    InputContext dialog = new InputContext(DIALOG).onPress(CLOSE, () -> fired.add("close"));
    game.onPress(PAUSE, () -> resolver.push(dialog));

    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.advance(0);
    resolver.advance(0);

    assertEquals(List.of(), fired);
  }

  @Test
  void blockingContextHidesLowerActions() {
    resolver.push(new InputContext(DIALOG));

    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);

    assertFalse(resolver.pressed(ATTACK));
    assertEquals(List.of("down " + Input.Keys.Q), stageEvents);
  }

  @Test
  void textContextKeepsEscapeFromDialogAndOthersBelow() {
    List<String> fired = new ArrayList<>();
    resolver.push(new InputContext(DIALOG).onPress(CLOSE, () -> fired.add("close")));
    resolver.push(new InputContext(CoreActions.TEXT));

    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.advance(0);

    assertEquals(List.of(), fired);
    assertEquals(List.of("down " + Input.Keys.ESCAPE), stageEvents);
  }

  @Test
  void stageHandledEventCreatesNoAction() {
    stageHandledKeys.add(Input.Keys.Q);

    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);

    assertFalse(resolver.justPressed(ATTACK));
    assertFalse(resolver.pressed(ATTACK));
  }

  @Test
  void releaseAlwaysReachesStageAndOwner() {
    List<String> fired = new ArrayList<>();
    game.onRelease(ATTACK, () -> fired.add("released"));

    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);
    resolver.keyUp(Input.Keys.Q);
    resolver.advance(0);

    assertEquals(List.of("released"), fired);
    assertTrue(stageEvents.contains("up " + Input.Keys.Q));
    assertTrue(resolver.released(ATTACK));
    assertFalse(resolver.pressed(ATTACK));
  }

  @Test
  void quickTapReachesFrameAsPressThenRelease() {
    tap(Input.Keys.Q);

    resolver.advance(0);
    assertTrue(resolver.justPressed(ATTACK));
    assertTrue(resolver.pressed(ATTACK));
    assertFalse(resolver.released(ATTACK));

    resolver.advance(0);
    assertFalse(resolver.pressed(ATTACK));
    assertTrue(resolver.released(ATTACK));
  }

  @Test
  void severalPressesInOneFrameStaySeparate() {
    tap(Input.Keys.Q);
    tap(Input.Keys.Q);

    int presses = 0;
    for (int frame = 0; frame < 6; frame++) {
      resolver.advance(0);
      if (resolver.justPressed(ATTACK)) presses++;
    }

    assertEquals(2, presses);
  }

  @Test
  void bindingsOfOneActionAggregate() {
    resolver.keyDown(Input.Keys.TAB);
    resolver.advance(0);
    assertTrue(resolver.justPressed(OTHER));

    resolver.keyDown(Input.Keys.I);
    resolver.advance(0);
    assertFalse(resolver.justPressed(OTHER));

    resolver.keyUp(Input.Keys.TAB);
    resolver.advance(0);
    assertFalse(resolver.released(OTHER));
    assertTrue(resolver.pressed(OTHER));

    resolver.keyUp(Input.Keys.I);
    resolver.advance(0);
    assertTrue(resolver.released(OTHER));
  }

  @Test
  void bindingAndVirtualSourceAggregate() {
    resolver.keyDown(Input.Keys.Q);
    resolver.press(ATTACK, "touch", 3);
    resolver.advance(0);
    assertTrue(resolver.justPressed(ATTACK));

    resolver.keyUp(Input.Keys.Q);
    resolver.advance(0);
    assertTrue(resolver.pressed(ATTACK));

    resolver.release("touch", 3);
    resolver.advance(0);
    assertTrue(resolver.released(ATTACK));
  }

  @Test
  void ownershipAndCancelAreTrackedPerPointer() {
    resolver.press(ATTACK, "touch", 1);
    resolver.advance(0);
    resolver.push(new InputContext(DIALOG));
    resolver.press(OTHER, "touch", 2);
    resolver.advance(0);

    assertFalse(resolver.pressed(ATTACK), "pointer 1 is cancelled by the dialog");
    assertFalse(
        resolver.pressed(OTHER), "pointer 2 belongs to the dialog, which has no such action");

    resolver.release("touch", 1);
    resolver.advance(0);
    assertFalse(resolver.released(ATTACK), "a cancelled pointer fires no release");
  }

  @Test
  void blockingContextCancelsWithoutReleaseAndStaysLocked() {
    List<String> fired = new ArrayList<>();
    game.onRelease(ATTACK, () -> fired.add("released"));
    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);
    assertTrue(resolver.pressed(ATTACK));

    InputContext dialog = new InputContext(DIALOG);
    resolver.push(dialog);
    resolver.advance(0);
    assertFalse(resolver.pressed(ATTACK));

    resolver.remove(dialog);
    resolver.advance(0);
    assertFalse(resolver.pressed(ATTACK), "closing the dialog must not re-arm the held key");

    resolver.keyUp(Input.Keys.Q);
    resolver.advance(0);
    assertEquals(List.of(), fired);

    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);
    assertTrue(resolver.pressed(ATTACK), "a real new press works again");
  }

  @Test
  void focusLossCancelsAndKeyDownAfterMissedReleaseStartsNewPress() {
    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);

    resolver.cancelAll();
    resolver.advance(0);
    assertFalse(resolver.pressed(ATTACK));

    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);
    assertTrue(resolver.justPressed(ATTACK));
  }

  @Test
  void removingOwnerCancelsOnlyItsPresses() {
    InputContext dialog = new InputContext(DIALOG);
    resolver.push(dialog);
    resolver.keyDown(Input.Keys.ESCAPE);
    resolver.advance(0);
    assertTrue(resolver.pressed(CLOSE));

    resolver.remove(dialog);
    resolver.advance(0);

    assertFalse(resolver.pressed(CLOSE));
    assertFalse(resolver.released(CLOSE));
  }

  @Test
  void comboWinsOverPlainKey() {
    resolver.keyDown(Input.Keys.F3);
    resolver.keyDown(Input.Keys.A);
    resolver.advance(0);

    assertTrue(resolver.justPressed(LIST));
    assertFalse(resolver.pressed(LEFT));
  }

  @Test
  void latePrefixStartsNoCombo() {
    resolver.keyDown(Input.Keys.C);
    resolver.keyDown(Input.Keys.CONTROL_LEFT);
    resolver.advance(0);

    assertTrue(resolver.justPressed(DOORS));
    assertFalse(resolver.justPressed(COPY));
  }

  @Test
  void comboModifierWorksOnEitherSide() {
    resolver.keyDown(Input.Keys.CONTROL_RIGHT);
    resolver.keyDown(Input.Keys.C);
    resolver.advance(0);

    assertTrue(resolver.justPressed(COPY));
    assertFalse(resolver.justPressed(DOORS));
  }

  @Test
  void keyUsedByComboDoesNotBecomeOwnActionAfterPrefixRelease() {
    resolver.keyDown(Input.Keys.F3);
    resolver.keyDown(Input.Keys.A);
    resolver.keyUp(Input.Keys.F3);
    resolver.advance(0);
    resolver.advance(0);
    assertFalse(resolver.pressed(LEFT));

    resolver.keyUp(Input.Keys.A);
    resolver.keyDown(Input.Keys.A);
    resolver.advance(0);
    resolver.advance(0);
    assertTrue(resolver.pressed(LEFT), "only the next press is the plain key");
  }

  @Test
  void keyThatIsActionAndPrefixFiresOnRelease() {
    resolver.keyDown(Input.Keys.F3);
    resolver.advance(0);
    assertFalse(resolver.justPressed(HUD));

    resolver.keyUp(Input.Keys.F3);
    resolver.advance(0);
    assertTrue(resolver.justPressed(HUD));
  }

  @Test
  void prefixKeyStaysSilentAfterCombo() {
    resolver.keyDown(Input.Keys.F3);
    tap(Input.Keys.A);
    resolver.keyUp(Input.Keys.F3);

    boolean fired = false;
    for (int frame = 0; frame < 4; frame++) {
      resolver.advance(0);
      fired |= resolver.justPressed(HUD);
    }

    assertFalse(fired);
  }

  @Test
  void cancelledPrefixKeyStaysSilent() {
    resolver.keyDown(Input.Keys.F3);
    resolver.cancelAll();
    resolver.keyUp(Input.Keys.F3);

    resolver.advance(0);

    assertFalse(resolver.justPressed(HUD));
  }

  @Test
  void stickKeepsAnalogValueAndIsLimitedToLengthOne() {
    resolver.padAxis(new Binding.PadAxis(Binding.PadAxisId.LSTICK), 0.3f, 0.4f);
    Vector2 half = resolver.vector(STICK);
    assertEquals(0.5, half.length(), 1e-4);

    resolver.padAxis(new Binding.PadAxis(Binding.PadAxisId.LSTICK), 3f, 4f);
    assertEquals(1.0, resolver.vector(STICK).length(), 1e-4);
  }

  @Test
  void cancelledStickStaysLockedUntilNeutral() {
    Binding stick = new Binding.PadAxis(Binding.PadAxisId.LSTICK);
    resolver.padAxis(stick, 1f, 0f);
    InputContext dialog = new InputContext(DIALOG);
    resolver.push(dialog);
    resolver.remove(dialog);

    resolver.padAxis(stick, 1f, 0f);
    assertTrue(resolver.vector(STICK).isZero());

    resolver.padAxis(stick, 0f, 0f);
    resolver.padAxis(stick, 1f, 0f);
    assertEquals(1.0, resolver.vector(STICK).length(), 1e-4);
  }

  @Test
  void wheelIsCollectedPerFrame() {
    resolver.scrolled(0, 1);
    resolver.scrolled(0, 2);

    resolver.advance(0);
    assertEquals(3f, resolver.axis(ZOOM));

    resolver.advance(0);
    assertEquals(0f, resolver.axis(ZOOM));
  }

  @Test
  void rebindingCancelsHeldInputAndNewKeyWorks() {
    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);

    resolver.rebind(ATTACK, List.of(Binding.parse("key:F")));
    resolver.advance(0);
    assertFalse(resolver.pressed(ATTACK));

    resolver.keyDown(Input.Keys.F);
    resolver.advance(0);
    assertTrue(resolver.pressed(ATTACK));
  }

  @Test
  void repeatingActionReportsNewPressesWhileHeld() {
    resolver.repeating(ATTACK);
    resolver.keyDown(Input.Keys.Q);
    resolver.advance(0);
    assertTrue(resolver.justPressed(ATTACK));

    resolver.advance(0.1f);
    assertFalse(resolver.justPressed(ATTACK));

    resolver.advance(0.4f);
    assertTrue(resolver.justPressed(ATTACK));
  }
}
