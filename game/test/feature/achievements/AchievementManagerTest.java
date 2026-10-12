package feature.achievements;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AchievementManagerTest {

  @AfterEach
  void resetInstance() throws Exception {
    Field field = AchievementManager.class.getDeclaredField("instance");
    field.setAccessible(true);
    field.set(null, null);
  }

  @Test
  void registerAchievementsRejectsBlankDefinitionPath() {
    assertThrows(
        IllegalArgumentException.class,
        () -> AchievementManager.registerAchievements(" ", "game-achievement-unlock.json"));
  }

  @Test
  void registerAchievementsRejectsBlankStatusPath() {
    assertThrows(
        IllegalArgumentException.class,
        () -> AchievementManager.registerAchievements("achievement.json", " "));
  }

  @Test
  void constructorRejectsBlankDefinitionPath() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AchievementManager(" ", "game-achievement-unlock.json"));
  }

  @Test
  void constructorRejectsBlankStatusPath() {
    assertThrows(
        IllegalArgumentException.class, () -> new AchievementManager("achievement.json", " "));
  }

  @Test
  void popupUnlockRecordsOnlyTheFirstOccurrence() {
    AchievementStore store = mock(AchievementStore.class);
    Achievement achievement = new Achievement("icon.png", "first", false, true, false);
    when(store.definition("first")).thenReturn(Optional.of(achievement));
    when(store.unlock("first")).thenReturn(true, false);
    new AchievementManager(store);

    assertTrue(AchievementManager.markUnlockedFromPopup("first"));
    assertFalse(AchievementManager.markUnlockedFromPopup("first"));
    verify(store, times(2)).unlock("first");
  }
}
