package rooms.systemRecovery.modules.computer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class UsbProgramDraftTest {

  @Test
  void encodesAndRestoresMultipleIndependentBlankValues() {
    List<String> values = List.of("map[row].length", "map[row][column] == 1", "roboter.collect()");

    String encoded = UsbProgramDraft.encode(values);

    assertEquals(values, UsbProgramDraft.decode(encoded, values.size()));
    assertTrue(UsbProgramDraft.isValid(encoded, values.size()));
  }

  @Test
  void newAndPartiallyFilledDraftsKeepEmptyBlanks() {
    assertEquals(List.of(""), UsbProgramDraft.decode("", 1));
    assertEquals(
        List.of("bound", "", ""),
        UsbProgramDraft.decode(UsbProgramDraft.encode(List.of("bound", "", "")), 3));
  }

  @Test
  void rejectsWrongFieldCountAndMultilineValues() {
    assertFalse(UsbProgramDraft.isValid("only-one", 3));
    assertFalse(UsbProgramDraft.isValid(UsbProgramDraft.encode(List.of("first", "second")) + "\nthird", 2));
    assertThrows(
        IllegalArgumentException.class,
        () -> UsbProgramDraft.encode(List.of("first\nsecond")));
  }
}
