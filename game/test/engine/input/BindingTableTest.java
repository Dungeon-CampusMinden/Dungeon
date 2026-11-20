package engine.input;

import static engine.input.Binding.key;
import static engine.input.Binding.mouse;
import static engine.input.Binding.pad;
import static engine.input.Binding.wheel;
import static engine.input.Binding.wheelStep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import engine.input.Binding.Pad;
import engine.input.Binding.PadAxisId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests defaults, overrides, conflicts and persistence of {@link BindingTable}. */
class BindingTableTest {
  private static final ActionId JUMP = new ActionId("test", "jump", key(Keys.SPACE));
  private static final ActionId DUCK = new ActionId("test", "duck", key(Keys.S));
  private static final ActionId MENU = new ActionId("test", "menu", key(Keys.SPACE));
  private static final ContextSpec GAME = ContextSpec.of("game", ContextLayer.BASE, JUMP, DUCK);

  @TempDir Path dir;

  private BindingTable table() {
    BindingTable table = new BindingTable();
    table.register(GAME);
    return table;
  }

  @Test
  void factoriesAndStringsDescribeTheSameBindings() {
    assertEquals(Binding.parse("key:E"), key(Keys.E));
    assertEquals("key:UP", key(Keys.UP).format());
    assertEquals(Binding.parse("key:F3+A"), key(Keys.F3, Keys.A));
    assertEquals(Binding.parse("key:Ctrl+C"), key(Keys.CONTROL_LEFT, Keys.C));
    assertEquals(Binding.parse("key:F3+SHIFT_LEFT"), key(Keys.F3, Keys.SHIFT_LEFT));
    assertEquals(Binding.parse("mouse:RIGHT"), mouse(Buttons.RIGHT));
    assertEquals(Binding.parse("mouse:WHEEL"), wheel());
    assertEquals(Binding.parse("pad:A"), pad(Pad.A));
    assertEquals(Binding.parse("pad:LSTICK_UP"), pad(PadAxisId.LSTICK_UP));
    assertEquals(key(Keys.A), key(Keys.F3, Keys.A).trigger());
  }

  @Test
  void bindingsSurviveTheirReadableForm() {
    for (Binding binding :
        List.of(
            key(Keys.E),
            key(Keys.CONTROL_LEFT, Keys.C),
            key(Keys.F3, Keys.SHIFT_LEFT),
            mouse(Buttons.RIGHT),
            wheel(),
            wheelStep(true),
            new Binding.MouseButton(Buttons.LEFT, Set.of(Keys.SHIFT_LEFT)),
            pad(Pad.A),
            pad(PadAxisId.RT))) {
      assertEquals(binding, Binding.parse(binding.format()));
    }
  }

  @Test
  void onlyTheNameDecidesEquality() {
    assertEquals(JUMP, ActionId.of("test:jump"));
    assertEquals(JUMP.hashCode(), ActionId.of("test:jump").hashCode());
  }

  @Test
  void writtenIdFindsTheDefaultsOfARegisteredAction() {
    assertEquals(List.of(key(Keys.SPACE)), table().bindings(ActionId.of("test:jump")));
  }

  @Test
  void playerOverrideWinsAndResetRestoresDefault() {
    BindingTable table = table();
    table.load(dir.resolve("input_bindings.json"));

    table.rebind(JUMP, List.of(key(Keys.J)));
    assertEquals(List.of(key(Keys.J)), table.bindings(JUMP));

    table.reset(JUMP);
    assertEquals(List.of(key(Keys.SPACE)), table.bindings(JUMP));
  }

  @Test
  void conflictInsideOneContextIsRejected() {
    BindingTable table = table();

    assertTrue(table.rebind(DUCK, List.of(key(Keys.SPACE))).isPresent());
    assertEquals(List.of(key(Keys.S)), table.bindings(DUCK));
  }

  @Test
  void resetIsRejectedWhileAnotherActionHoldsTheDefault() {
    BindingTable table = table();
    table.rebind(JUMP, List.of(key(Keys.J)));
    table.rebind(DUCK, List.of(key(Keys.SPACE)));

    assertTrue(table.reset(JUMP).isPresent());
    assertEquals(List.of(key(Keys.J)), table.bindings(JUMP));
  }

  @Test
  void conflictAmongDefaultsBlocksNeitherRebindingNorTheWayBack() {
    BindingTable table = table();
    table.register(ContextSpec.of("hud", ContextLayer.DEBUG, MENU));

    assertTrue(table.rebind(JUMP, List.of(key(Keys.SPACE), key(Keys.J))).isEmpty());
    assertTrue(table.rebind(JUMP, List.of(key(Keys.J))).isEmpty());
    assertTrue(table.reset(JUMP).isEmpty());
  }

  @Test
  void modifierOnItsOwnConflictsWithACombinationUsingIt() {
    BindingTable table = table();
    table.rebind(JUMP, List.of(key(Keys.SHIFT_RIGHT)));

    assertTrue(table.rebind(DUCK, List.of(key(Keys.SHIFT_LEFT, Keys.A))).isPresent());
    assertTrue(table.rebind(DUCK, List.of(key(Keys.CONTROL_LEFT, Keys.A))).isEmpty());
  }

  @Test
  void resetAllRestoresDefaultsAndKeepsUnknownActions() throws IOException {
    Path file = dir.resolve("input_bindings.json");
    Files.writeString(file, "{\"otherroom:lens\": [\"key:L\"], \"test:jump\": [\"key:J\"]}");
    BindingTable table = table();
    table.load(file);

    table.resetAll();

    assertEquals(List.of(key(Keys.SPACE)), table.bindings(JUMP));
    assertTrue(Files.readString(file).contains("otherroom:lens"));
    assertFalse(Files.readString(file).contains("test:jump"));
  }

  @Test
  void inputAWidgetHandlesItselfCannotDriveAnotherAction() {
    BindingTable table = new BindingTable();
    table.register(GAME.handles(JUMP, mouse(Buttons.RIGHT)));

    assertTrue(table.rebind(DUCK, List.of(mouse(Buttons.RIGHT))).isPresent());
    assertTrue(table.rebind(JUMP, List.of(mouse(Buttons.RIGHT))).isEmpty());
  }

  @Test
  void nonBlockingContextsConflictTogether() {
    BindingTable table = table();
    table.register(ContextSpec.of("hud", ContextLayer.DEBUG, MENU));

    assertEquals(1, table.conflicts().size());
  }

  @Test
  void blockingContextMayCoverLowerKeys() {
    BindingTable table = table();
    table.register(ContextSpec.of("dialog", ContextLayer.UI, MENU).blocking());

    assertEquals(List.of(), table.conflicts());
  }

  @Test
  void comboDoesNotConflictWithItsPlainKey() {
    BindingTable table = new BindingTable();
    table.register(
        ContextSpec.of(
            "game",
            ContextLayer.BASE,
            new ActionId("test", "plain", key(Keys.A)),
            new ActionId("test", "combo", key(Keys.F3, Keys.A))));

    assertEquals(List.of(), table.conflicts());
  }

  @Test
  void closingActionsMayShareABinding() {
    ActionId close = new ActionId("test", "close", key(Keys.ESCAPE));
    ActionId menu = new ActionId("test", "menu", key(Keys.ESCAPE), pad(Pad.START));
    BindingTable table = new BindingTable();
    table.register(ContextSpec.of("menu", ContextLayer.UI).closedBy(close, menu).blocking());

    assertEquals(List.of(), table.conflicts());
  }

  @Test
  void aPadOnlyActionIgnoresItsKeysInConflicts() {
    ActionId close = new ActionId("test", "close", key(Keys.ESCAPE), pad(Pad.B));
    ActionId pause = new ActionId("test", "pause", key(Keys.ESCAPE), pad(Pad.START));
    BindingTable table = new BindingTable();
    table.register(ContextSpec.of("dialog", ContextLayer.UI, close).padOnly(pause).blocking());

    assertEquals(List.of(), table.conflicts());
  }

  @Test
  void emptyListMeansUnboundAndMissingEntryMeansDefault() throws IOException {
    Path file = dir.resolve("input_bindings.json");
    Files.writeString(file, "{\"test:jump\": []}");

    BindingTable table = table();
    table.load(file);

    assertEquals(List.of(), table.bindings(JUMP));
    assertEquals(List.of(key(Keys.S)), table.bindings(DUCK));
  }

  @Test
  void savedOverridesSurviveRestartAndKeepUnknownActions() throws IOException {
    Path file = dir.resolve("input_bindings.json");
    Files.writeString(file, "{\"otherroom:lens\": [\"key:L\"]}");

    BindingTable first = table();
    first.load(file);
    first.rebind(JUMP, List.of(key(Keys.J)));

    BindingTable second = table();
    second.load(file);
    assertEquals(List.of(key(Keys.J)), second.bindings(JUMP));
    assertTrue(Files.readString(file).contains("otherroom:lens"));
    assertFalse(Files.exists(dir.resolve("input_bindings.json.tmp")));
  }

  @Test
  void invalidFileIsMovedAsideAndDefaultsApply() throws IOException {
    Path file = dir.resolve("input_bindings.json");
    Files.writeString(file, "{\"test:jump\": [\"key:NOPE\"]}");

    BindingTable table = table();
    table.load(file);

    assertEquals(List.of(key(Keys.SPACE)), table.bindings(JUMP));
    assertFalse(Files.exists(file));
    assertTrue(Files.exists(dir.resolve("input_bindings.json.invalid")));
  }

  @Test
  void actionRegisteredAfterLoadUsesStoredOverride() throws IOException {
    Path file = dir.resolve("input_bindings.json");
    Files.writeString(file, "{\"room:lens\": [\"key:L\"]}");
    BindingTable table = new BindingTable();
    table.load(file);

    ActionId lens = new ActionId("room", "lens", key(Keys.V));
    table.register(ContextSpec.of("room", ContextLayer.ROOM, lens));

    assertEquals(List.of(key(Keys.L)), table.bindings(lens));
    assertEquals(1, table.bound(key(Keys.L)).size());
  }
}
