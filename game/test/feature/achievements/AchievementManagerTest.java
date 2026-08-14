package feature.achievements;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
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
}
