package engine.utils.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.language.Language;
import engine.language.Localization;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// tests for @link ClientSettings
class ClientSettingsTest {

  // keys of the settings that ClientSettings registers itself, in registration order
  private static final List<String> DEFAULT_KEYS =
      List.of(
          ClientSettings.KEY_MASTER_VOLUME,
          ClientSettings.KEY_EFFECTS_VOLUME,
          ClientSettings.KEY_MUSIC_VOLUME,
          ClientSettings.KEY_LANGUAGE,
          ClientSettings.KEY_BRIGHTNESS,
          ClientSettings.KEY_REDUCE_EFFECTS);

  // every setting registered by a test starts with this
  private static final String TEST_PREFIX = "test.client_settings.";
  private static final String INGAME_ONLY_KEY = TEST_PREFIX + "ingame_only";

  private static Field instanceField;
  private ClientSettings previousInstance;
  private Language savedLanguage;

  @BeforeAll
  static void findInstanceField() throws NoSuchFieldException {
    instanceField = ClientSettings.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
  }

  // ClientSettings is a singleton, so changes from earlier tests would still be there. Every test
  // gets a fresh instance with the default values, and the previous instance (with its values and
  // its volume callback) is put back afterwards.
  @BeforeEach
  void useFreshInstance() throws IllegalAccessException {
    savedLanguage = Localization.getInstance().currentLanguage();
    previousInstance = (ClientSettings) instanceField.get(null);
    instanceField.set(null, null);
  }

  @AfterEach
  void restorePreviousInstance() throws IllegalAccessException {
    instanceField.set(null, previousInstance);
    Localization.getInstance().currentLanguage(savedLanguage);
  }

  @SuppressWarnings("unchecked")
  private static <T> SettingValue<T> setting(String key) {
    return (SettingValue<T>) ClientSettings.getSetting(key);
  }

  @Test
  void alwaysReturnsTheSameInstance() {
    assertSame(ClientSettings.getInstance(), ClientSettings.getInstance());
  }

  @Test
  void defaultSettingsAreRegistered() {
    for (String key : DEFAULT_KEYS) {
      assertNotNull(ClientSettings.getSetting(key), key);
    }
  }

  @Test
  void defaultSettingsKeepTheirRegistrationOrder() {
    List<String> keys = new ArrayList<>(ClientSettings.getSettings().keySet());
    keys.retainAll(DEFAULT_KEYS);
    assertEquals(DEFAULT_KEYS, keys);
  }

  @Test
  void defaultValuesMatchTheGameDefaults() {
    assertEquals(70, ClientSettings.masterVolume());
    assertEquals(70, ClientSettings.effectsVolume());
    assertEquals(5, ClientSettings.musicVolume());
    assertEquals(100, ClientSettings.brightness());
    assertFalse(ClientSettings.reduceEffects());
  }

  @Test
  void shortcutsReturnTheCurrentValues() {
    setting(ClientSettings.KEY_MASTER_VOLUME).value(10);
    setting(ClientSettings.KEY_EFFECTS_VOLUME).value(20);
    setting(ClientSettings.KEY_MUSIC_VOLUME).value(30);
    setting(ClientSettings.KEY_BRIGHTNESS).value(120);
    setting(ClientSettings.KEY_REDUCE_EFFECTS).value(true);
    assertEquals(10, ClientSettings.masterVolume());
    assertEquals(20, ClientSettings.effectsVolume());
    assertEquals(30, ClientSettings.musicVolume());
    assertEquals(120, ClientSettings.brightness());
    assertTrue(ClientSettings.reduceEffects());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        ClientSettings.KEY_MASTER_VOLUME,
        ClientSettings.KEY_EFFECTS_VOLUME,
        ClientSettings.KEY_MUSIC_VOLUME
      })
  void volumeChangeIsReportedWithItsKey(String key) {
    List<String> calls = new ArrayList<>();
    ClientSettings.setOnVolumeChange((changedKey, value) -> calls.add(changedKey + "=" + value));
    setting(key).value(40);
    assertEquals(List.of(key + "=40"), calls);
  }

  @Test
  void brightnessChangeIsNotReportedAsVolumeChange() {
    List<String> calls = new ArrayList<>();
    ClientSettings.setOnVolumeChange((changedKey, value) -> calls.add(changedKey));
    setting(ClientSettings.KEY_BRIGHTNESS).value(120);
    assertTrue(calls.isEmpty());
  }

  @Test
  void languageSettingChangesTheGameLanguage() {
    Language other = savedLanguage == Language.DE ? Language.EN : Language.DE;
    setting(ClientSettings.KEY_LANGUAGE).value(other);
    assertEquals(other, Localization.getInstance().currentLanguage());
  }

  @Test
  void registeredSettingCanBeFound() {
    BoolSetting flag = new BoolSetting(TEST_PREFIX + "flag", true);
    ClientSettings.registerSetting(flag);
    assertSame(flag, ClientSettings.getSetting(TEST_PREFIX + "flag"));
  }

  @Test
  void registeringTheSameKeyReplacesTheOldSetting() {
    String key = TEST_PREFIX + "replaced";
    ClientSettings.registerSetting(new BoolSetting(key, true));
    int size = ClientSettings.getSettings().size();
    BoolSetting replacement = new BoolSetting(key, false);
    ClientSettings.registerSetting(replacement);
    assertSame(replacement, ClientSettings.getSetting(key));
    assertEquals(size, ClientSettings.getSettings().size());
  }

  @Test
  void registeringNullIsRejected() {
    assertThrows(NullPointerException.class, () -> ClientSettings.registerSetting(null));
  }

  @Test
  void unknownKeyReturnsNull() {
    assertNull(ClientSettings.getSetting(TEST_PREFIX + "unknown"));
    assertNull(ClientSettings.getSettingValue(TEST_PREFIX + "unknown", Integer.class));
  }

  @Test
  void settingValueIsReturnedWithTheRequestedType() {
    ClientSettings.registerSetting(new IntSliderSetting(TEST_PREFIX + "number", 42));
    Integer value = ClientSettings.getSettingValue(TEST_PREFIX + "number", Integer.class);
    assertEquals(42, value);
  }

  @Test
  void settingValueWithWrongTypeThrows() {
    ClientSettings.registerSetting(new BoolSetting(TEST_PREFIX + "flag", true));
    assertThrows(
        ClassCastException.class,
        () -> ClientSettings.getSettingValue(TEST_PREFIX + "flag", Integer.class));
  }

  @Test
  void settingIsShownInBothMenusByDefault() {
    String key = TEST_PREFIX + "everywhere";
    ClientSettings.registerSetting(new BoolSetting(key, true));
    assertFalse(ClientSettings.isOnlyIngame(key));
    assertTrue(ClientSettings.getSettings(false).containsKey(key));
    assertTrue(ClientSettings.getSettings(true).containsKey(key));
  }

  @Test
  void ingameOnlySettingIsHiddenInTheMainMenu() {
    ClientSettings.registerSetting(new BoolSetting(INGAME_ONLY_KEY, true));
    ClientSettings.setOnlyIngame(INGAME_ONLY_KEY, true);
    assertTrue(ClientSettings.isOnlyIngame(INGAME_ONLY_KEY));
    assertFalse(ClientSettings.getSettings(false).containsKey(INGAME_ONLY_KEY));
    assertTrue(ClientSettings.getSettings(true).containsKey(INGAME_ONLY_KEY));
  }

  @Test
  void ingameOnlyCanBeUndone() {
    ClientSettings.registerSetting(new BoolSetting(INGAME_ONLY_KEY, true));
    ClientSettings.setOnlyIngame(INGAME_ONLY_KEY, true);
    ClientSettings.setOnlyIngame(INGAME_ONLY_KEY, false);
    assertTrue(ClientSettings.getSettings(false).containsKey(INGAME_ONLY_KEY));
  }

  @Test
  void ingameOnlyRequiresAKey() {
    assertThrows(NullPointerException.class, () -> ClientSettings.setOnlyIngame(null, true));
  }

  @Test
  void filteredSettingsKeepTheRegistrationOrder() {
    List<String> all = new ArrayList<>(ClientSettings.getSettings().keySet());
    List<String> ingame = new ArrayList<>(ClientSettings.getSettings(true).keySet());
    assertEquals(all, ingame);
  }
}
