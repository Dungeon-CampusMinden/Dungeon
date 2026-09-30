package feature.hud.dialogs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import feature.input.configuration.KeyboardConfig;
import org.junit.jupiter.api.Test;

/** Verifies the keys used to advance sequenced dialogs. */
class DialogAdvanceInputTest {

  @Test
  void escapeAdvancesLikeTheConfiguredInteractionKey() {
    assertTrue(DialogAdvanceInput.isAdvanceKey(Input.Keys.ESCAPE));
    assertTrue(DialogAdvanceInput.isAdvanceKey(KeyboardConfig.INTERACT_WORLD.value()));
  }

  @Test
  void unrelatedKeysDoNotAdvanceTheDialog() {
    assertFalse(DialogAdvanceInput.isAdvanceKey(Input.Keys.P));
  }
}
