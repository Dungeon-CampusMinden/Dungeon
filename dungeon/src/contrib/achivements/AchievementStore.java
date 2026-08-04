package contrib.achivements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import core.Game;
import core.components.PlayerComponent;
import core.game.PreRunConfiguration;
import core.utils.JsonHandler;
import core.utils.logging.DungeonLogger;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Loads achievement definitions and persists unlock state in a separate runtime JSON file. */
final class AchievementStore {

  static final String DEFAULT_DEFINITION_PATH = "achievement.json";
  private static final String DEFAULT_STATUS_PATH = "build/achievements/achievement-unlocks.json";
  private static final String KEY_ACHIEVEMENTS = "achievements";
  private static final String KEY_IMAGE_PATH = "imagePath";
  private static final String KEY_NAME = "name";
  private static final String KEY_DESCRIPTION = "description";
  private static final String KEY_BESCHREIBUNG = "beschreibung";
  private static final String KEY_NESCHREIBUNG = "neschreibung";
  private static final String KEY_NAME_KEY = "nameKey";
  private static final String KEY_DESCRIPTION_KEY = "descriptionKey";
  private static final String KEY_HIDDEN = "hidden";
  private static final String KEY_UNLOCKED = "unlocked";
  private static final String KEY_UNLOCK_FOR_ALL = "unlockForAll";
  private static final String KEY_UNLOCK_SCOPE = "unlockScope";
  private static final String KEY_SCOPE = "scope";
  private static final String KEY_PLATINUM = "platinum";
  private static final String KEY_GLOBAL = "global";
  private static final String KEY_PLAYERS = "players";
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AchievementStore.class);

  private final String definitionPath;
  private final Path statusPath;
  private final Map<String, Achievement> definitions = new LinkedHashMap<>();
  private final Set<String> globalUnlocks = new LinkedHashSet<>();
  private final Map<String, Set<String>> playerUnlocks = new LinkedHashMap<>();
  private boolean loaded;

  AchievementStore() {
    this(DEFAULT_DEFINITION_PATH, defaultStatusPath());
  }

  AchievementStore(String definitionPath, Path statusPath) {
    this.definitionPath = definitionPath;
    this.statusPath = statusPath;
  }

  private static Path defaultStatusPath() {
    return Path.of(System.getProperty("dungeon.achievements.save", DEFAULT_STATUS_PATH));
  }

  boolean hasDefinitions() {
    ensureLoaded();
    return !definitions.isEmpty();
  }

  List<Achievement> achievementsForMenu() {
    ensureLoaded();
    PlayerComponent viewer = localPlayerComponent().orElse(null);
    return definitions.values().stream()
        .map(achievement -> achievement.withUnlocked(isUnlockedFor(viewer, achievement.name())))
        .toList();
  }

  Optional<Achievement> definition(String name) {
    ensureLoaded();
    return Optional.ofNullable(definitions.get(name));
  }

  boolean unlockGlobally(String name) {
    ensureLoaded();
    if (!definitions.containsKey(name) || isGloballyUnlocked(name)) {
      return false;
    }
    globalUnlocks.add(name);
    saveStatus();
    return true;
  }

  boolean unlockForPlayer(PlayerComponent player, String name) {
    ensureLoaded();
    if (!definitions.containsKey(name)) {
      return false;
    }
    String playerName = playerName(player);
    Set<String> unlocks = playerUnlocks.computeIfAbsent(playerName, key -> new LinkedHashSet<>());
    if (unlocks.contains(name) || isGloballyUnlocked(name)) {
      return false;
    }
    unlocks.add(name);
    saveStatus();
    return true;
  }

  Optional<Achievement> platinumAchievement() {
    ensureLoaded();
    return definitions.values().stream().filter(Achievement::platinum).findFirst();
  }

  boolean allNonPlatinumAchievementsUnlocked(PlayerComponent player) {
    ensureLoaded();
    return definitions.values().stream()
        .filter(achievement -> !achievement.platinum())
        .allMatch(achievement -> isUnlockedFor(player, achievement.name()));
  }

  boolean isUnlockedFor(PlayerComponent player, String name) {
    ensureLoaded();
    return isGloballyUnlocked(name) || playerUnlocksFor(player).contains(name);
  }

  private boolean isGloballyUnlocked(String name) {
    Achievement definition = definitions.get(name);
    return globalUnlocks.contains(name) || (definition != null && definition.unlocked());
  }

  private Set<String> playerUnlocksFor(PlayerComponent player) {
    return playerUnlocks.getOrDefault(playerName(player), Set.of());
  }

  private String playerName(PlayerComponent player) {
    if (player == null || player.playerName() == null || player.playerName().isBlank()) {
      return fallbackPlayerName();
    }
    return player.playerName();
  }

  private String fallbackPlayerName() {
    String username = PreRunConfiguration.username();
    return username == null || username.isBlank() ? "Player" : username;
  }

  private Optional<PlayerComponent> localPlayerComponent() {
    return Game.player().flatMap(player -> player.fetch(PlayerComponent.class));
  }

  private void ensureLoaded() {
    if (loaded) {
      return;
    }
    loaded = true;
    loadDefinitions();
    loadStatus();
  }

  private void loadDefinitions() {
    readInternal(definitionPath)
        .ifPresent(
            json -> {
              Map<String, Object> root = JsonHandler.readJson(normalizeDefinitionJson(json));
              Object achievementsNode = root.get(KEY_ACHIEVEMENTS);
              if (!(achievementsNode instanceof List<?> achievements)) {
                throw new IllegalArgumentException(
                    "achievement.json must contain an 'achievements' array");
              }
              for (Object node : achievements) {
                Achievement achievement = parseAchievement(node);
                definitions.put(achievement.name(), achievement);
              }
            });
  }

  private String normalizeDefinitionJson(String json) {
    String trimmed = json.trim();
    if (trimmed.startsWith("[")) {
      return "{\"" + KEY_ACHIEVEMENTS + "\":" + trimmed + "}";
    }
    return json;
  }

  private Optional<String> readInternal(String path) {
    if (Gdx.files == null) {
      return Optional.empty();
    }
    FileHandle file = Gdx.files.internal(path);
    if (!file.exists()) {
      return Optional.empty();
    }
    return Optional.of(file.readString(StandardCharsets.UTF_8.name()));
  }

  private Achievement parseAchievement(Object node) {
    if (!(node instanceof Map<?, ?> rawMap)) {
      throw new IllegalArgumentException("achievement entries must be JSON objects");
    }
    Map<String, Object> map = asStringObjectMap(rawMap);
    String imagePath = stringValue(map, KEY_IMAGE_PATH);
    String name = stringValue(map, KEY_NAME);
    String description =
        optionalStringValue(map, KEY_DESCRIPTION)
            .or(() -> optionalStringValue(map, KEY_BESCHREIBUNG))
            .or(() -> optionalStringValue(map, KEY_NESCHREIBUNG))
            .orElse("");
    String nameKey = optionalStringValue(map, KEY_NAME_KEY).orElse("");
    String descriptionKey = optionalStringValue(map, KEY_DESCRIPTION_KEY).orElse("");
    boolean hidden = booleanValue(map, KEY_HIDDEN, false);
    boolean unlocked = booleanValue(map, KEY_UNLOCKED, false);
    Object scope =
        map.containsKey(KEY_UNLOCK_SCOPE) ? map.get(KEY_UNLOCK_SCOPE) : map.get(KEY_SCOPE);
    AchievementUnlockScope unlockScope =
        AchievementUnlockScope.fromJson(map.get(KEY_UNLOCK_FOR_ALL), scope);
    boolean platinum = booleanValue(map, KEY_PLATINUM, false);
    return new Achievement(
        imagePath,
        name,
        description,
        nameKey,
        descriptionKey,
        hidden,
        unlocked,
        unlockScope,
        platinum);
  }

  private Map<String, Object> asStringObjectMap(Map<?, ?> rawMap) {
    Map<String, Object> result = new LinkedHashMap<>();
    rawMap.forEach((key, value) -> result.put(String.valueOf(key), value));
    return result;
  }

  private String stringValue(Map<String, Object> map, String key) {
    return optionalStringValue(map, key)
        .orElseThrow(() -> new IllegalArgumentException("Missing achievement field: " + key));
  }

  private Optional<String> optionalStringValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value == null) {
      return Optional.empty();
    }
    String text = String.valueOf(value).trim();
    return text.isEmpty() ? Optional.empty() : Optional.of(text);
  }

  private boolean booleanValue(Map<String, Object> map, String key, boolean fallback) {
    Object value = map.get(key);
    if (value instanceof Boolean bool) {
      return bool;
    }
    if (value instanceof String text) {
      return Boolean.parseBoolean(text);
    }
    return fallback;
  }

  private void loadStatus() {
    if (!Files.exists(statusPath)) {
      return;
    }
    try {
      Map<String, Object> root =
          JsonHandler.readJson(Files.readString(statusPath, StandardCharsets.UTF_8));
      addStringList(root.get(KEY_UNLOCKED), globalUnlocks);
      addStringList(root.get(KEY_GLOBAL), globalUnlocks);
      Object playersNode = root.get(KEY_PLAYERS);
      if (playersNode instanceof Map<?, ?> players) {
        players.forEach(
            (player, list) -> {
              Set<String> unlocks =
                  playerUnlocks.computeIfAbsent(String.valueOf(player), k -> new LinkedHashSet<>());
              addStringList(list, unlocks);
            });
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read achievement status: " + statusPath, e);
    } catch (IllegalArgumentException e) {
      LOGGER.warn("Ignoring invalid achievement status '{}': {}", statusPath, e.getMessage());
    }
  }

  private void addStringList(Object node, Set<String> target) {
    if (!(node instanceof List<?> list)) {
      return;
    }
    list.stream().map(String::valueOf).filter(definitions::containsKey).forEach(target::add);
  }

  private void saveStatus() {
    Map<String, Object> root = new LinkedHashMap<>();
    root.put(KEY_GLOBAL, sortedList(globalUnlocks));

    Map<String, Object> players = new LinkedHashMap<>();
    playerUnlocks.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> players.put(entry.getKey(), sortedList(entry.getValue())));
    root.put(KEY_PLAYERS, players);

    try {
      Path parent = statusPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Files.writeString(statusPath, JsonHandler.writeJson(root, true), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not write achievement status: " + statusPath, e);
    }
  }

  private List<Object> sortedList(Set<String> values) {
    return values.stream().sorted(Comparator.naturalOrder()).map(Object.class::cast).toList();
  }

  void resetForTests() {
    loaded = false;
    definitions.clear();
    globalUnlocks.clear();
    playerUnlocks.clear();
  }
}
