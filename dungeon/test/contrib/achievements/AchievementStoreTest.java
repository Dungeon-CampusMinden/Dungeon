package contrib.achievements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import core.utils.JsonHandler;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AchievementStoreTest {

  @Test
  void constructorStoresDefinitionPath() throws Exception {
    AchievementStore store =
        new AchievementStore(
            " custom/achievements.json ", Path.of("custom-achievement-unlock.json"));

    assertEquals("custom/achievements.json", definitionPath(store));
  }

  @Test
  void constructorStoresStatusPath() throws Exception {
    AchievementStore store =
        new AchievementStore("custom/achievements.json", Path.of("custom-achievement-unlock.json"));

    assertEquals(Path.of("custom-achievement-unlock.json"), statusPath(store));
  }

  @Test
  void constructorRejectsBlankDefinitionPath() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AchievementStore(" ", Path.of("custom-achievement-unlock.json")));
  }

  @Test
  void constructorRejectsBlankStatusPath() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AchievementStore("achievement.json", Path.of("")));
  }

  @Test
  void parseAchievementIgnoresUnlockedFromDefinitionJson() throws Exception {
    AchievementStore store = new AchievementStore("unused.json", Path.of("unused-status.json"));
    Map<String, Object> definition = validDefinition();
    definition.put("unlocked", true);

    parseAchievement(store, definition);

    assertFalse(
        Arrays.stream(Achievement.class.getRecordComponents())
            .anyMatch(component -> component.getName().equals("unlocked")));
  }

  @Test
  void parseAchievementDefaultsMissingUnlockForAllToAllPlayers() throws Exception {
    AchievementStore store = new AchievementStore("unused.json", Path.of("unused-status.json"));

    Achievement achievement = parseAchievement(store, validDefinition());

    assertTrue(achievement.forAll());
  }

  @Test
  void parseAchievementReadsFalseUnlockForAllBoolean() throws Exception {
    AchievementStore store = new AchievementStore("unused.json", Path.of("unused-status.json"));
    Map<String, Object> definition = validDefinition();
    definition.put("unlockForAll", false);

    Achievement achievement = parseAchievement(store, definition);

    assertFalse(achievement.forAll());
  }

  @Test
  void parseAchievementRejectsNonBooleanUnlockForAll() {
    AchievementStore store = new AchievementStore("unused.json", Path.of("unused-status.json"));
    Map<String, Object> definition = validDefinition();
    definition.put("unlockForAll", "false");

    assertThrows(IllegalArgumentException.class, () -> parseAchievement(store, definition));
  }

  @Test
  void saveStatusWritesFlatUnlockedList() throws Exception {
    Path statusPath = Files.createTempFile("achievement-unlock", ".json");
    Files.deleteIfExists(statusPath);
    AchievementStore store = new AchievementStore("unused.json", statusPath);
    addDefinition(store, achievement("Lights On"));

    store.unlock("Lights On");

    Map<String, Object> saved = JsonHandler.readJson(Files.readString(statusPath));
    assertEquals(List.of("Lights On"), saved.get("unlocked"));
  }

  @Test
  void corruptedStatusFileIsIgnored() throws Exception {
    Path statusPath = Files.createTempFile("achievement-unlock", ".json");
    Files.writeString(statusPath, "{ \"unlocked\": [\"Lights On\", }");
    AchievementStore store = new AchievementStore("unused.json", statusPath);
    addDefinition(store, achievement("Lights On"));

    assertFalse(store.isUnlocked("Lights On"));
  }

  private static Path statusPath(AchievementStore store) throws Exception {
    Field field = AchievementStore.class.getDeclaredField("statusPath");
    field.setAccessible(true);
    return (Path) field.get(store);
  }

  private static String definitionPath(AchievementStore store) throws Exception {
    Field field = AchievementStore.class.getDeclaredField("definitionPath");
    field.setAccessible(true);
    return (String) field.get(store);
  }

  private static Achievement parseAchievement(
      AchievementStore store, Map<String, Object> definition) throws Exception {
    Method method = AchievementStore.class.getDeclaredMethod("parseAchievement", Object.class);
    method.setAccessible(true);
    return (Achievement) invoke(method, store, definition);
  }

  @SuppressWarnings("unchecked")
  private static void addDefinition(AchievementStore store, Achievement achievement)
      throws Exception {
    Field field = AchievementStore.class.getDeclaredField("definitions");
    field.setAccessible(true);
    Map<String, Achievement> definitions = (Map<String, Achievement>) field.get(store);
    definitions.put(achievement.name(), achievement);
  }

  private static Achievement achievement(String name) {
    return new Achievement("icon.png", name, "Description", "", "", false, true, false);
  }

  private static Map<String, Object> validDefinition() {
    Map<String, Object> definition = new LinkedHashMap<>();
    definition.put("imagePath", "icon.png");
    definition.put("name", "Test Achievement");
    definition.put("description", "Static definition");
    definition.put("hidden", true);
    return definition;
  }

  private static Object invoke(Method method, Object target, Object... args) throws Exception {
    try {
      return method.invoke(target, args);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof Exception exception) {
        throw exception;
      }
      throw e;
    }
  }
}
