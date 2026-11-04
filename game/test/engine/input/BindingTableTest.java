package engine.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests defaults, overrides, conflicts and persistence of {@link BindingTable}. */
class BindingTableTest {
  private static final ActionId JUMP = new ActionId("test", "jump");
  private static final ActionId DUCK = new ActionId("test", "duck");
  private static final ActionId MENU = new ActionId("test", "menu");

  @TempDir Path dir;

  private BindingTable table() {
    BindingTable table = new BindingTable();
    table.define(JUMP, "key:SPACE");
    table.define(DUCK, "key:S");
    table.define(MENU, "key:SPACE");
    return table;
  }

  @Test
  void parsesAndFormatsEveryBindingKind() {
    for (String text :
        List.of(
            "key:E",
            "key:Ctrl+C",
            "key:F3+A",
            "key:F3+SHIFT_LEFT",
            "mouse:RIGHT",
            "mouse:WHEEL",
            "pad:A",
            "pad:LSTICK",
            "pad:LSTICK_UP",
            "pad:RT")) {
      assertEquals(text, Binding.parse(text).format());
    }
    assertEquals(new Binding.Key(Input.Keys.A), Binding.parse("key:F3+A").trigger());
  }

  @Test
  void playerOverrideWinsAndResetRestoresDefault() {
    BindingTable table = table();
    table.load(dir.resolve("input_bindings.json"));

    table.rebind(JUMP, List.of(Binding.parse("key:J")));
    assertEquals(List.of(Binding.parse("key:J")), table.bindings(JUMP));

    table.reset(JUMP);
    assertEquals(List.of(Binding.parse("key:SPACE")), table.bindings(JUMP));
  }

  @Test
  void conflictInsideOneContextIsRejected() {
    BindingTable table = table();
    table.context(ContextSpec.of("game", ContextLayer.BASE, true, JUMP, DUCK));

    assertTrue(table.rebind(DUCK, List.of(Binding.parse("key:SPACE"))).isPresent());
    assertEquals(List.of(Binding.parse("key:S")), table.bindings(DUCK));
  }

  @Test
  void nonBlockingContextsConflictTogether() {
    BindingTable table = table();
    table.context(ContextSpec.of("game", ContextLayer.BASE, false, JUMP));
    table.context(ContextSpec.of("hud", ContextLayer.DEBUG, false, MENU));

    assertEquals(1, table.conflicts().size());
  }

  @Test
  void blockingContextMayCoverLowerKeys() {
    BindingTable table = table();
    table.context(ContextSpec.of("game", ContextLayer.BASE, false, JUMP));
    table.context(ContextSpec.of("dialog", ContextLayer.UI, true, MENU));

    assertEquals(List.of(), table.conflicts());
  }

  @Test
  void comboDoesNotConflictWithItsPlainKey() {
    BindingTable table = new BindingTable();
    table.define(JUMP, "key:A");
    table.define(DUCK, "key:F3+A");
    table.context(ContextSpec.of("game", ContextLayer.BASE, false, JUMP, DUCK));

    assertEquals(List.of(), table.conflicts());
  }

  @Test
  void keyboardOnlyActionIgnoresItsPadBindingInConflicts() {
    BindingTable table = new BindingTable();
    table.define(JUMP, "pad:Y");
    table.define(DUCK, "pad:Y");
    table.context(
        ContextSpec.of("inventory", ContextLayer.UI, true, JUMP, DUCK).keyboardOnly(JUMP));

    assertEquals(List.of(), table.conflicts());
  }

  @Test
  void emptyListMeansUnboundAndMissingEntryMeansDefault() throws IOException {
    Path file = dir.resolve("input_bindings.json");
    Files.writeString(file, "{\"test:jump\": []}");

    BindingTable table = table();
    table.load(file);

    assertEquals(List.of(), table.bindings(JUMP));
    assertEquals(List.of(Binding.parse("key:S")), table.bindings(DUCK));
  }

  @Test
  void savedOverridesSurviveRestartAndKeepUnknownActions() throws IOException {
    Path file = dir.resolve("input_bindings.json");
    Files.writeString(file, "{\"otherroom:lens\": [\"key:L\"]}");

    BindingTable first = table();
    first.load(file);
    first.rebind(JUMP, List.of(Binding.parse("key:J")));

    BindingTable second = table();
    second.load(file);
    assertEquals(List.of(Binding.parse("key:J")), second.bindings(JUMP));
    assertTrue(Files.readString(file).contains("otherroom:lens"));
    assertFalse(Files.exists(dir.resolve("input_bindings.json.tmp")));
  }

  @Test
  void invalidFileIsMovedAsideAndDefaultsApply() throws IOException {
    Path file = dir.resolve("input_bindings.json");
    Files.writeString(file, "{\"test:jump\": [\"key:NOPE\"]}");

    BindingTable table = table();
    table.load(file);

    assertEquals(List.of(Binding.parse("key:SPACE")), table.bindings(JUMP));
    assertFalse(Files.exists(file));
    assertTrue(Files.exists(dir.resolve("input_bindings.json.invalid")));
  }

  @Test
  void bindingOfRoomDefinedAfterLoadUsesStoredOverride() throws IOException {
    Path file = dir.resolve("input_bindings.json");
    Files.writeString(file, "{\"room:lens\": [\"key:L\"]}");
    BindingTable table = new BindingTable();
    table.load(file);

    ActionId lens = new ActionId("room", "lens");
    table.define(lens, "key:V");

    assertEquals(List.of(Binding.parse("key:L")), table.bindings(lens));
    assertEquals(1, table.bound(new Binding.Key(Input.Keys.L)).size());
  }
}
