package rooms.systemRecovery.util.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Tests the registered System Recovery terminal puzzle sequence. */
public class TerminalInterpreterSetupTest {

  private String source;

  /** Resets and registers the preview puzzle sequence before each test. */
  @BeforeEach
  public void setup() {
    TerminalInterpreter.instance().reset();
    TerminalInterpreterSetup.setupPreviewStates();
    source = "";
  }

  /** The complete System Recovery terminal sequence is registered in gameplay order. */
  @Test
  public void completeTerminalPuzzleSequenceIsSupported() {
    submit(
        """
        int[] energie = new int[5];
        """);
    submit(
        """
        energie[0] = 40;
        energie[1] = 10;
        energie[2] = 80;
        energie[3] = 30;
        energie[4] = 60;
        """);
    submit(
        """
        String[] m = new String[5];
        """);
    submit(
        """
        m[3] = "SSD";
        m[4] = "NETWORK";
        m[0] = "CPU";
        m[1] = "RAM";
        m[2] = "GPU";
        """);
    submit(
        """
        m[2] = null;
        """);
    submit(
        """
        m.length;
        """);
    submit(
        """
        int count = 0;
        for (String currentModule : m) {
            if (currentModule != null) {
                count += 1;
            }
        }
        """);
    submit(
        """
        int[] pakete = new int[]{15, 40, 20, 60, 30};
        """);
    submit(
        """
        for (int index = 0; index < pakete.length; ++index) {
            roboter.collect();
        }
        """);
    submit(
        """
        int[] energie = new int[]{20, 50, 80};
        String[] module = new String[]{"CPU", "GPU", "RAM"};
        boolean[] aktiv = new boolean[]{true, false, true};
        """);
    submit(
        """
        int[][] storage = new int[3][4];
        """);
    submit(
        """
        storage[0][2] = 1;
        storage[1][3] = 2;
        storage[2][1] = 3;
        """);
    String searchProgram =
        """
        for (int row = 0; row < map.length; ++row) {
            for (int column = 0; column < map[row].length; ++column) {
                if (map[row][column] == 1) {
                    roboter.collect();
                }
            }
        }
        """;
    assertTrue(TerminalInterpreterSetup.matchesSearchRobotProgram(searchProgram));
    assertEquals(12, TerminalInterpreter.instance().currentState());
    TerminalInterpreter.instance().synchronizeState(TerminalInterpreterSetup.CENTRAL_SORT_STATE);
    submit(
        """
        for (int outer = 0; outer <= values.length - 2; ++outer) {
            for (int inner = 0; inner <= values.length - 2 - outer; ++inner) {
                if (values[inner] > values[inner + 1]) {
                    int tmp = values[inner];
                    values[inner] = values[inner + 1];
                    values[inner + 1] = tmp;
                }
            }
        }
        """);
    submit(
        """
        int count = 0;
        for (String currentModule : modules) {
            if (currentModule != null) {
                count = count + 1;
            }
        }
        """);
    submit(
        """
        for (int row = 0; row < map.length; ++row) {
            for (int column = 0; column < map[row].length; ++column) {
                roboter.collect();
            }
        }
        """);
  }

  /** Riddle 2 accepts a flexible array name but rejects mixing it with another name. */
  @Test
  public void riddleTwoRequiresConsistentCapturedArrayName() {
    submit(
        """
        int[] energie = new int[5];
        """);
    submit(
        """
        energie[0] = 40;
        energie[1] = 10;
        energie[2] = 80;
        energie[3] = 30;
        energie[4] = 60;
        """);
    submit(
        """
        String[] m = new String[5];
        """);

    source +=
        """
        module[0] = "CPU";
        module[1] = "RAM";
        module[2] = "GPU";
        module[3] = "SSD";
        module[4] = "NETWORK";
        """;

    assertFalse(TerminalInterpreter.instance().interpret(source));
  }

  /** The battery search requires collect() inside the matching condition. */
  @Test
  public void riddleNineRejectsBatterySearchWithoutCollectCall() {
    advanceToRiddleNine();

    String searchProgram =
        """
        for (int row = 0; row < map.length; row++) {
            for (int column = 0; column < map[row].length; column++) {
                if (map[row][column] == 1) {
                    // Batterie gefunden
                }
            }
        }
        """;

    assertFalse(TerminalInterpreterSetup.matchesSearchRobotProgram(searchProgram));
    assertFalse(TerminalInterpreter.instance().interpret(searchProgram));
  }

  /** The prepared one-column search is rejected until the missing inner loop is added. */
  @Test
  public void riddleNineRequiresTheMissingInnerLoop() {
    advanceToRiddleNine();

    String searchProgram =
        """
        for (int i = 0; i < map.length; i++) {
            int j = 0;
            if (map[i][j] == 1) {
                roboter.collect();
            }
        }
        """;

    assertFalse(TerminalInterpreterSetup.matchesSearchRobotProgram(searchProgram));
    assertFalse(TerminalInterpreter.instance().interpret(searchProgram));
  }

  /** The inner loop can replace the prepared column initialization and complete the search. */
  @Test
  public void riddleNineAcceptsTheInnerLoopAddedToThePreparedCode() {
    advanceToRiddleNine();

    String searchProgram =
        """
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 1) {
                    roboter.collect();
                }
            }
        }
        """;

    assertTrue(TerminalInterpreterSetup.matchesSearchRobotProgram(searchProgram));
    assertFalse(TerminalInterpreter.instance().interpret(searchProgram));
    assertEquals(12, TerminalInterpreter.instance().currentState());
  }

  /** Riddle 8 accepts a flexible storage array name but rejects mixing it with another name. */
  @Test
  public void riddleEightRequiresConsistentCapturedStorageArrayName() {
    advanceToRiddleEight();
    submit(
        """
        int[][] storage = new int[3][4];
        """);

    source +=
        """
        lager[0][2] = 1;
        lager[1][3] = 2;
        lager[2][1] = 3;
        """;

    assertFalse(TerminalInterpreter.instance().interpret(source));
  }

  /** Riddle 10 accepts equivalent bubble-sort bounds and flexible array and temp names. */
  @Test
  public void riddleTenAcceptsFlexibleBubbleSortNamesAndEquivalentBounds() {
    advanceToRiddleTen();

    submit(
        """
        for (int pass = 0; pass <= daten.length - 2; pass++) {
            for (int position = 0; position <= daten.length - 2 - pass; position++) {
                if (daten[position] > daten[position + 1]) {
                    int swap = daten[position];
                    daten[position] = daten[position + 1];
                    daten[position + 1] = swap;
                }
            }
        }
        """);
  }

  /** Riddle 10 rejects non-equivalent bubble-sort outer bounds. */
  @Test
  public void riddleTenRejectsWrongBubbleSortOuterBound() {
    advanceToRiddleTen();

    source +=
        """
        for (int pass = 0; pass <= daten.length - 1; pass++) {
            for (int position = 0; position < daten.length - 1 - pass; position++) {
                if (daten[position] > daten[position + 1]) {
                    int swap = daten[position];
                    daten[position] = daten[position + 1];
                    daten[position + 1] = swap;
                }
            }
        }
        """;

    assertFalse(TerminalInterpreter.instance().interpret(source));
  }

  /** Riddle 10 rejects non-equivalent bubble-sort comparison operators. */
  @Test
  public void riddleTenRejectsWrongBubbleSortComparisonOperator() {
    advanceToRiddleTen();

    source +=
        """
        for (int pass = 0; pass >= daten.length - 1; pass++) {
            for (int position = 0; position < daten.length - 1 - pass; position++) {
                if (daten[position] > daten[position + 1]) {
                    int swap = daten[position];
                    daten[position] = daten[position + 1];
                    daten[position + 1] = swap;
                }
            }
        }
        """;

    assertFalse(TerminalInterpreter.instance().interpret(source));
  }

  /** Supported Java array declaration variants are accepted by the registered riddles. */
  @Test
  public void registeredRiddlesAcceptJavaArrayBracketVariants() {
    submit(
        """
        int energie[] = new int[5];
        """);
    submit(
        """
        energie[0] = 40;
        energie[1] = 10;
        energie[2] = 80;
        energie[3] = 30;
        energie[4] = 60;
        """);
    submit(
        """
        String module[] = new String[5];
        """);
  }

  /** The standalone search-chip requirement accepts equivalent loop bounds and increments. */
  @Test
  public void searchChipAcceptsEquivalentLoopIncrementAndBoundsVariants() {
    advanceToRiddleNine();

    String searchProgram =
        """
        for (int row = 0; row <= map.length - 1; row += 1) {
            for (int column = 0; column <= map[row].length - 1; column = column + 1) {
                if (map[row][column] == 1) {
                    roboter.collect();
                }
            }
        }
        """;

    assertTrue(TerminalInterpreterSetup.matchesSearchRobotProgram(searchProgram));
    assertFalse(TerminalInterpreter.instance().interpret(searchProgram));
    assertEquals(12, TerminalInterpreter.instance().currentState());
  }

  /**
   * The search-chip requirement validates its source without advancing the shared terminal state.
   */
  @Test
  public void searchChipValidationDoesNotAdvanceTheSharedTerminalState() {
    advanceToRiddleNine();
    String searchProgram =
        """
        for (int row = 0; row < map.length; row++) {
            for (int column = 0; column < map[row].length; column++) {
                if (map[row][column] == 1) {
                    roboter.collect();
                }
            }
        }
        """;

    assertTrue(
        TerminalInterpreterSetup.matchesSearchRobotProgram(searchProgram),
        "The search chip must use the standalone nested-loop requirement");
    assertEquals(12, TerminalInterpreter.instance().currentState());
  }

  /** Extra statements with a wrong captured variable name are rejected. */
  @Test
  public void registeredRiddlesRejectExtraStatementsWithWrongCapturedArrayName() {
    submit(
        """
        int[] energie = new int[5];
        """);
    submit(
        """
        energie[0] = 40;
        energie[1] = 10;
        energie[2] = 80;
        energie[3] = 30;
        energie[4] = 60;
        """);
    submit(
        """
        String[] m = new String[5];
        """);

    source +=
        """
        m[0] = "CPU";
        m[1] = "RAM";
        m[2] = "GPU";
        m[3] = "SSD";
        m[4] = "NETWORK";
        other[4] = "NETWORK";
        """;

    assertFalse(TerminalInterpreter.instance().interpret(source));
  }

  /** Debug examples do not advance by themselves and pass through the normal interpreter path. */
  @Test
  public void debugSourcesAreValidWithoutSkippingTerminalStates() {
    TerminalInterpreter interpreter = TerminalInterpreter.instance();

    for (int state = 0; state <= 11; state++) {
      assertEquals(state, interpreter.currentState());
      String debugSource = TerminalInterpreterSetup.debugSourceForState(state).orElseThrow();
      assertEquals(state, interpreter.currentState(), "requesting source must not advance state");
      assertTrue(interpreter.interpret(debugSource), "invalid debug source for state " + state);
    }

    assertFalse(TerminalInterpreterSetup.debugSourceForState(12).isPresent());
    interpreter.synchronizeState(TerminalInterpreterSetup.CENTRAL_SORT_STATE);

    for (int state = 13; state <= 15; state++) {
      assertEquals(state, interpreter.currentState());
      String debugSource = TerminalInterpreterSetup.debugSourceForState(state).orElseThrow();
      assertEquals(state, interpreter.currentState(), "requesting source must not advance state");
      assertTrue(interpreter.interpret(debugSource), "invalid debug source for state " + state);
    }

    assertFalse(TerminalInterpreterSetup.debugSourceForState(16).isPresent());
    assertFalse(TerminalInterpreterSetup.debugSourceForState(Integer.MAX_VALUE).isPresent());
  }

  /** The debug Solve programs are the same programs accepted by the two chip validators. */
  @Test
  public void debugChipSourcesAreValid() {
    assertTrue(
        TerminalInterpreterSetup.matchesSearchRobotProgram(
            TerminalInterpreterSetup.searchRobotDebugSource()));

    TerminalInterpreter.instance().synchronizeState(TerminalInterpreterSetup.CENTRAL_SORT_STATE);
    assertTrue(
        TerminalInterpreter.instance().interpret(TerminalInterpreterSetup.bubbleSortDebugSource()));
  }

  private void submit(String addition) {
    source += addition;
    assertTrue(TerminalInterpreter.instance().interpret(source), addition);
  }

  private void advanceToRiddleNine() {
    submit(
        """
        int[] energie = new int[5];
        """);
    submit(
        """
        energie[0] = 40;
        energie[1] = 10;
        energie[2] = 80;
        energie[3] = 30;
        energie[4] = 60;
        """);
    submit(
        """
        String[] module = new String[5];
        """);
    submit(
        """
        module[0] = "CPU";
        module[1] = "RAM";
        module[2] = "GPU";
        module[3] = "SSD";
        module[4] = "NETWORK";
        """);
    submit(
        """
        module[2] = null;
        """);
    submit(
        """
        module.length;
        """);
    submit(
        """
        int count = 0;
        for (String m : module) {
            if (m != null) {
                count++;
            }
        }
        """);
    submit(
        """
        int[] pakete = {15, 40, 20, 60, 30};
        """);
    submit(
        """
        for (int index = 0; index < pakete.length; ++index) {
            roboter.collect();
        }
        """);
    submit(
        """
        int[] energie = {20, 50, 80};
        String[] module = {"CPU", "GPU", "RAM"};
        boolean[] aktiv = {true, false, true};
        """);
    submit(
        """
        int[][] lager = new int[3][4];
        """);
    submit(
        """
        lager[0][2] = 1;
        lager[1][3] = 2;
        lager[2][1] = 3;
        """);
  }

  private void advanceToRiddleEight() {
    submit(
        """
        int[] energie = new int[5];
        """);
    submit(
        """
        energie[0] = 40;
        energie[1] = 10;
        energie[2] = 80;
        energie[3] = 30;
        energie[4] = 60;
        """);
    submit(
        """
        String[] module = new String[5];
        """);
    submit(
        """
        module[0] = "CPU";
        module[1] = "RAM";
        module[2] = "GPU";
        module[3] = "SSD";
        module[4] = "NETWORK";
        """);
    submit(
        """
        module[2] = null;
        """);
    submit(
        """
        module.length;
        """);
    submit(
        """
        int count = 0;
        for (String m : module) {
            if (m != null) {
                count++;
            }
        }
        """);
    submit(
        """
        int[] pakete = {15, 40, 20, 60, 30};
        """);
    submit(
        """
        for (int index = 0; index < pakete.length; ++index) {
            roboter.collect();
        }
        """);
    submit(
        """
        int[] energie = {20, 50, 80};
        String[] module = {"CPU", "GPU", "RAM"};
        boolean[] aktiv = {true, false, true};
        """);
  }

  private void advanceToRiddleTen() {
    advanceToRiddleEight();
    submit(
        """
        int[][] lager = new int[3][4];
        """);
    submit(
        """
        lager[0][2] = 1;
        lager[1][3] = 2;
        lager[2][1] = 3;
        """);
    String searchProgram =
        """
        for (int row = 0; row < map.length; ++row) {
            for (int column = 0; column < map[row].length; ++column) {
                if (map[row][column] == 1) {
                    roboter.collect();
                }
            }
        }
        """;
    assertTrue(TerminalInterpreterSetup.matchesSearchRobotProgram(searchProgram));
    TerminalInterpreter.instance().synchronizeState(TerminalInterpreterSetup.CENTRAL_SORT_STATE);
  }
}
