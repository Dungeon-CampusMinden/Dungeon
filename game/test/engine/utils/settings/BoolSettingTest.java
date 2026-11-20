package engine.utils.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

// tests for @link BoolSetting; the checkbox itself (toUIActor) needs graphics and is not tested
class BoolSettingTest {

  private static final String KEY = "test.bool";

  @Test
  void keepsKeyAndDefaultValue() {
    BoolSetting on = new BoolSetting(KEY, true);
    BoolSetting off = new BoolSetting(KEY, false);
    assertEquals(KEY, on.translationKey());
    assertTrue(on.value());
    assertFalse(off.value());
  }

  @Test
  void descriptionConstructorKeepsKeyAndDefaultValue() {
    BoolSetting setting = new BoolSetting(KEY, true, "test.bool_description");
    assertEquals(KEY, setting.translationKey());
    assertTrue(setting.value());
  }

  // the description key is wrapped with Optional.of, which does not accept null
  @Test
  void missingDescriptionKeyThrows() {
    assertThrows(NullPointerException.class, () -> new BoolSetting(KEY, true, null));
  }

  @Test
  void valueCanBeToggled() {
    List<Boolean> received = new ArrayList<>();
    BoolSetting setting = new BoolSetting(KEY, true);
    setting.onChange(received::add);
    setting.value(false);
    assertFalse(setting.value());
    assertEquals(List.of(false), received);
  }
}
