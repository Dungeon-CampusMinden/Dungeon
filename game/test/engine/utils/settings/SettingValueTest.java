package engine.utils.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.scenes.scene2d.Actor;
import engine.language.Localization;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

// tests for @link SettingValue
class SettingValueTest {

  private static final String KEY = "test.setting_value.key";

  // smallest possible setting, the UI part is not needed for these tests
  private static final class TestSetting extends SettingValue<String> {
    TestSetting(String translationKey, String defaultValue) {
      super(translationKey, defaultValue);
    }

    TestSetting(String translationKey, String defaultValue, Consumer<String> onChange) {
      super(translationKey, defaultValue, onChange);
    }

    @Override
    public Actor toUIActor() {
      return null;
    }
  }

  @Test
  void keepsTranslationKeyAndDefaultValue() {
    TestSetting setting = new TestSetting(KEY, "default");
    assertEquals(KEY, setting.translationKey());
    assertEquals("default", setting.value());
  }

  @Test
  void valueCanBeChanged() {
    TestSetting setting = new TestSetting(KEY, "old");
    setting.value("new");
    assertEquals("new", setting.value());
  }

  @Test
  void listenerFromConstructorReceivesNewValue() {
    List<String> received = new ArrayList<>();
    TestSetting setting = new TestSetting(KEY, "old", received::add);
    assertTrue(received.isEmpty());
    setting.value("new");
    assertEquals(List.of("new"), received);
  }

  @Test
  void newListenerReplacesTheOldOne() {
    List<String> first = new ArrayList<>();
    List<String> second = new ArrayList<>();
    TestSetting setting = new TestSetting(KEY, "old", first::add);
    setting.onChange(second::add);
    setting.value("new");
    assertTrue(first.isEmpty());
    assertEquals(List.of("new"), second);
  }

  @Test
  void listenerCanBeRemovedWithNull() {
    List<String> received = new ArrayList<>();
    TestSetting setting = new TestSetting(KEY, "old", received::add);
    setting.onChange(null);
    setting.value("new");
    assertTrue(received.isEmpty());
    assertEquals("new", setting.value());
  }

  @Test
  void listenerIsCalledEvenIfTheValueStaysTheSame() {
    List<String> received = new ArrayList<>();
    TestSetting setting = new TestSetting(KEY, "same", received::add);
    setting.value("same");
    assertEquals(List.of("same"), received);
  }

  @Test
  void nameIsTranslatedByTheLocalization() {
    TestSetting setting = new TestSetting(ClientSettings.KEY_MASTER_VOLUME, "x");
    String expected = Localization.getInstance().text(ClientSettings.KEY_MASTER_VOLUME);
    assertEquals(expected, setting.name());
    assertNotEquals("{" + ClientSettings.KEY_MASTER_VOLUME + "}", setting.name());
  }

  @Test
  void missingTranslationShowsTheKeyInBraces() {
    TestSetting setting = new TestSetting("test.setting_value.missing", "x");
    assertEquals("{test.setting_value.missing}", setting.name());
  }
}
