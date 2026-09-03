package rooms.systemRecovery.util.interpreter;

import engine.utils.Point;
import java.util.regex.Pattern;
import rooms.systemRecovery.modules.interpreter.CodeLine;
import rooms.systemRecovery.modules.interpreter.TerminalCodeRequirement;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Sets up the interpreter states used by the System Recovery terminal puzzle. */
public final class TerminalInterpreterSetup {

  private static final CodeLine INITIALISE_ENERGIE_ARRAY =
      new CodeLine(
          Pattern.compile("int\\s*\\[\\s*]\\s*energie\\s*=\\s*new\\s+int\\s*\\[\\s*5\\s*]"),
          Pattern.compile("int\\s+energie\\s*\\[\\s*]\\s*=\\s*new\\s+int\\s*\\[\\s*5\\s*]"));
  private static final CodeLine SET_ENERGIE_0 = energyAssignmentCodeLine(0, 20);
  private static final CodeLine SET_ENERGIE_1 = energyAssignmentCodeLine(1, 80);
  private static final CodeLine SET_ENERGIE_3 = energyAssignmentCodeLine(3, 50);
  private static final CodeLine SET_ENERGIE_4 = energyAssignmentCodeLine(4, 70);
  private static final CodeLine[] ENERGY_ASSIGNMENTS = {
    SET_ENERGIE_0, SET_ENERGIE_1, SET_ENERGIE_3, SET_ENERGIE_4
  };

  private TerminalInterpreterSetup() {}

  /** Sets up no-op callbacks so the client can preview interpretation feedback. */
  public static void setupPreviewStates() {
    setupStateOneEnergyArrayInitialization(null);
    setupStateTwoEnergyAssignments(null);
  }

  /**
   * Registers both code requirements and their room-side reactions.
   *
   * @param terminalPoint point used to place the energy crates
   */
  public static void setupRoomStates(Point terminalPoint) {
    setupStateOneEnergyArrayInitialization(
        () -> InterpretationCallbacks.spawnEnergieCrates(terminalPoint));
    setupStateTwoEnergyAssignments(InterpretationCallbacks::markEnergyCratesCorrect);
  }

  private static void setupStateOneEnergyArrayInitialization(Runnable onSuccess) {
    TerminalInterpreter.instance()
        .register(
            0,
            new TerminalCodeRequirement(
                new CodeLine[] {INITIALISE_ENERGIE_ARRAY}, onSuccess, null));
  }

  private static void setupStateTwoEnergyAssignments(Runnable onSuccess) {
    TerminalInterpreter.instance()
        .register(1, new TerminalCodeRequirement(ENERGY_ASSIGNMENTS, onSuccess, null));
  }

  private static CodeLine energyAssignmentCodeLine(int index, int value) {
    String regex = "energie\\s*\\[\\s*" + index + "\\s*]\\s*=\\s*" + value;
    return new CodeLine(Pattern.compile(regex));
  }
}
