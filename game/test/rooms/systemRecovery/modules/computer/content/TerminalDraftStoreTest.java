package rooms.systemRecovery.modules.computer.content;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TerminalDraftStoreTest {

  @AfterEach
  void clearDraft() {
    TerminalDraftStore.clear();
  }

  @Test
  void draftSurvivesDialogRecreationUntilTheCurrentRunIsReset() {
    TerminalDraftStore.code("String[] module = new String[5];");

    assertEquals("String[] module = new String[5];", TerminalDraftStore.code());
    assertEquals("String[] module = new String[5];", TerminalDraftStore.code());

    TerminalDraftStore.clear();
    assertEquals("", TerminalDraftStore.code());
  }

  @Test
  void nullDraftIsNormalizedToEmptyText() {
    TerminalDraftStore.code(null);

    assertEquals("", TerminalDraftStore.code());
  }
}
