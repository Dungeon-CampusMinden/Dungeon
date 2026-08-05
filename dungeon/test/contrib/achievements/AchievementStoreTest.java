package contrib.achievements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
  void defaultStatusPathUsesLocalUnlockFile() throws Exception {
    AchievementStore store = new AchievementStore();

    assertEquals(Path.of("achievement-unlocks.json"), statusPath(store));
  }

  @Test
  void registerDefinitionsChangesDefinitionPath() throws Exception {
    AchievementStore store = new AchievementStore();

    store.registerDefinitions(" custom/achievements.json ");

    assertEquals("custom/achievements.json", definitionPath(store));
  }

  @Test
  void registerDefinitionsClearsLoadedState() throws Exception {
    Path statusPath = Files.createTempFile("achievement-unlocks", ".json");
    Files.deleteIfExists(statusPath);
    AchievementStore store = new AchievementStore("old.json", statusPath);
    addDefinition(store, achievement("Lights On"));
    store.unlock("Lights On");

    store.registerDefinitions("new.json");

    assertFalse(store.isUnlocked("Lights On"));
    assertEquals(List.of(), store.achievementsForMenu());
  }

  @Test
  void registerDefinitionsRejectsBlankPath() {
    AchievementStore store = new AchievementStore();

    assertThrows(IllegalArgumentException.class, () -> store.registerDefinitions(" "));
  }

  @Test
  void parseAchievementIgnoresUnlockedFromDefinitionJson() throws Exception {
    AchievementStore store = new AchievementStore("unused.json", Path.of("unused-status.json"));
    Map<String, Object> definition = new LinkedHashMap<>();
    definition.put("imagePath", "icon.png");
    definition.put("name", "Test Achievement");
    definition.put("description", "Static definition");
    definition.put("hidden", true);
    definition.put("unlocked", true);

    parseAchievement(store, definition);

    assertFalse(
        Arrays.stream(Achievement.class.getRecordComponents())
            .anyMatch(component -> component.getName().equals("unlocked")));
  }

  @Test
  void saveStatusWritesFlatUnlockedList() throws Exception {
    Path statusPath = Files.createTempFile("achievement-unlocks", ".json");
    Files.deleteIfExists(statusPath);
    AchievementStore store = new AchievementStore("unused.json", statusPath);
    addDefinition(store, achievement("Lights On"));

    store.unlock("Lights On");

    Map<String, Object> saved = JsonHandler.readJson(Files.readString(statusPath));
    assertEquals(List.of("Lights On"), saved.get("unlocked"));
  }

  @Test
  void corruptedStatusFileIsIgnored() throws Exception {
    Path statusPath = Files.createTempFile("achievement-unlocks", ".json");
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
    return new Achievement(
        "icon.png", name, "Description", "", "", false, AchievementUnlockScope.ALL_PLAYERS, false);
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
