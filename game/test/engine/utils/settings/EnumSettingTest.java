package engine.utils.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

// tests for @link EnumSetting; the select box itself (toUIActor) needs graphics and is not tested
class EnumSettingTest {

  private static final String KEY = "test.enum";

  enum Difficulty {
    EASY,
    NORMAL,
    HARD
  }

  @Test
  void allValuesConstructorKeepsKeyAndDefault() {
    EnumSetting<Difficulty> setting = new EnumSetting<>(KEY, Difficulty.NORMAL);
    assertEquals(KEY, setting.translationKey());
    assertEquals(Difficulty.NORMAL, setting.value());
  }

  @Test
  void subsetAsArrayIsAccepted() {
    EnumSetting<Difficulty> setting =
        new EnumSetting<>(
            KEY, Difficulty.EASY, new Difficulty[] {Difficulty.EASY, Difficulty.NORMAL});
    assertEquals(Difficulty.EASY, setting.value());
  }

  @Test
  void subsetAsCollectionIsAccepted() {
    EnumSetting<Difficulty> setting =
        new EnumSetting<>(KEY, Difficulty.HARD, List.of(Difficulty.EASY, Difficulty.HARD));
    assertEquals(Difficulty.HARD, setting.value());
  }

  @Test
  void emptySubsetIsRejected() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new EnumSetting<>(KEY, Difficulty.EASY, new Difficulty[0]));
    assertEquals("EnumSetting requires at least one enum value", exception.getMessage());
  }

  @Test
  void missingSubsetIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new EnumSetting<>(KEY, Difficulty.EASY, (Difficulty[]) null));
  }

  @Test
  void emptyCollectionIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new EnumSetting<>(KEY, Difficulty.EASY, List.<Difficulty>of()));
  }

  @Test
  void defaultOutsideTheSubsetIsRejected() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new EnumSetting<>(
                    KEY, Difficulty.HARD, new Difficulty[] {Difficulty.EASY, Difficulty.NORMAL}));
    assertEquals("Default value must be part of the enum subset", exception.getMessage());
  }

  @Test
  void defaultOutsideTheCollectionIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new EnumSetting<>(KEY, Difficulty.HARD, List.of(Difficulty.EASY)));
  }

  @Test
  void missingDefaultIsRejected() {
    assertThrows(NullPointerException.class, () -> new EnumSetting<Difficulty>(KEY, null));
  }

  // the subset only limits the choices in the UI, value() does not check it
  @Test
  void valueOutsideTheSubsetIsAccepted() {
    EnumSetting<Difficulty> setting =
        new EnumSetting<>(
            KEY, Difficulty.EASY, new Difficulty[] {Difficulty.EASY, Difficulty.NORMAL});
    setting.value(Difficulty.HARD);
    assertEquals(Difficulty.HARD, setting.value());
  }
}
