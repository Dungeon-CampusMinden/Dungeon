package engine.utils.settings;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

// tests for @link IntSliderSetting; the slider itself (toUIActor) needs graphics and is not tested
class IntSliderSettingTest {

  private static final String KEY = "test.int_slider";

  @Test
  void shortConstructorKeepsKeyAndDefault() {
    IntSliderSetting setting = new IntSliderSetting(KEY, 30);
    assertEquals(KEY, setting.translationKey());
    assertEquals(30, setting.value());
  }

  @Test
  void rangeConstructorKeepsDefault() {
    assertEquals(50, new IntSliderSetting(KEY, 50, 0, 100, 5).value());
  }

  @Test
  void formatterConstructorKeepsDefault() {
    assertEquals(100, new IntSliderSetting(KEY, 100, 50, 150, 10, v -> v + " %").value());
  }

  @Test
  void missingFormatterIsAllowed() {
    assertDoesNotThrow(() -> new IntSliderSetting(KEY, 1, 0, 10, 1, null));
  }

  // min and max are only used by the slider in the UI, value() does not check them
  @Test
  void valueOutsideTheRangeIsAccepted() {
    IntSliderSetting setting = new IntSliderSetting(KEY, 50, 0, 100, 5);
    setting.value(150);
    assertEquals(150, setting.value());
  }

  @Test
  void defaultOutsideTheRangeIsAccepted() {
    assertEquals(500, new IntSliderSetting(KEY, 500, 0, 100, 5).value());
  }

  @Test
  void listenerReceivesNewValue() {
    List<Integer> received = new ArrayList<>();
    IntSliderSetting setting = new IntSliderSetting(KEY, 50, 0, 100, 5);
    setting.onChange(received::add);
    setting.value(55);
    assertEquals(List.of(55), received);
  }
}
