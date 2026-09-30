package rooms.systemRecovery;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests propagation of System Recovery debug mode to the hosted server process. */
class SystemRecoveryDebugModeTest {

  @AfterEach
  void resetDebugMode() {
    SystemRecovery.configureDebugMode();
  }

  @Test
  void hostedServerReceivesDebugFlagWhenClientStartsInDebugMode() {
    SystemRecovery.configureDebugMode("--debug");

    org.junit.jupiter.api.Assertions.assertTrue(SystemRecovery.debugMode());
    org.junit.jupiter.api.Assertions.assertFalse(SystemRecovery.levelEditorMode());

    assertArrayEquals(
        new String[] {"--server", "--new-system-recovery", "--debug"},
        SystemRecovery.hostedServerArguments());
  }

  @Test
  void hostedServerReceivesDebugFlagWhenLevelEditorStarts() {
    SystemRecovery.configureDebugMode("--leveleditor");

    org.junit.jupiter.api.Assertions.assertTrue(SystemRecovery.debugMode());
    org.junit.jupiter.api.Assertions.assertTrue(SystemRecovery.levelEditorMode());

    assertArrayEquals(
        new String[] {"--server", "--new-system-recovery", "--debug"},
        SystemRecovery.hostedServerArguments());
  }

  @Test
  void normalHostedServerDoesNotReceiveDebugFlag() {
    SystemRecovery.configureDebugMode();

    org.junit.jupiter.api.Assertions.assertFalse(SystemRecovery.debugMode());
    org.junit.jupiter.api.Assertions.assertFalse(SystemRecovery.levelEditorMode());

    assertArrayEquals(
        new String[] {"--server", "--new-system-recovery"}, SystemRecovery.hostedServerArguments());
  }
}
