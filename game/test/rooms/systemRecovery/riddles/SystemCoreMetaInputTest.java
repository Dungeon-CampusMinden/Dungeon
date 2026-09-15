package rooms.systemRecovery.riddles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Tests the payload validation used by the final system-core input mask. */
public class SystemCoreMetaInputTest {

  private static final int[] SORTED_ENERGY = {8, 17, 23, 31, 42};

  /** The mask payload parses and matches all three authoritative results. */
  @Test
  public void acceptsCompleteMetaInput() {
    assertTrue(
        SystemCoreMetaInput.parse("8,17,23,31,42|3|3")
            .orElseThrow()
            .matches(Arrays.stream(SORTED_ENERGY).boxed().toList(), 3, 3));
  }

  /** A partial or malformed mask payload cannot satisfy the final state. */
  @Test
  public void rejectsPartialMetaInput() {
    assertFalse(SystemCoreMetaInput.parse("8,17,23,31|3|3").isPresent());
    assertFalse(
        SystemCoreMetaInput.parse("8,17,23,31,42|3|3")
            .orElseThrow()
            .matches(Arrays.stream(SORTED_ENERGY).boxed().toList(), 4, 3));
  }
}
