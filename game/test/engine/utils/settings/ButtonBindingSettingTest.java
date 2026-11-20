package engine.utils.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.Input;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

// tests for @link ButtonBindingSetting; the key button itself (toUIActor) needs graphics and is
// not tested
class ButtonBindingSettingTest {

  private static final String KEY = "test.button_binding";

  @Test
  void keepsKeyAndDefaultKeyCode() {
    ButtonBindingSetting setting = new ButtonBindingSetting(KEY, Input.Keys.W);
    assertEquals(KEY, setting.translationKey());
    assertEquals(Input.Keys.W, setting.value());
  }

  @Test
  void notEditableSettingKeepsDefaultKeyCode() {
    assertEquals(Input.Keys.E, new ButtonBindingSetting(KEY, Input.Keys.E, false).value());
  }

  // "editable" only blocks the button in the UI, the value can still be changed in code
  @Test
  void notEditableSettingCanStillBeChangedInCode() {
    ButtonBindingSetting setting = new ButtonBindingSetting(KEY, Input.Keys.E, false);
    setting.value(Input.Keys.Q);
    assertEquals(Input.Keys.Q, setting.value());
  }

  @Test
  void listenerReceivesNewKeyCode() {
    List<Integer> received = new ArrayList<>();
    ButtonBindingSetting setting = new ButtonBindingSetting(KEY, Input.Keys.W);
    setting.onChange(received::add);
    setting.value(Input.Keys.UP);
    assertEquals(List.of(Input.Keys.UP), received);
  }
}
