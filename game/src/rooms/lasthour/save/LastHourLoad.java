package rooms.lasthour.save;

import engine.utils.JsonHandler;
import feature.inventory.ItemRegistry;
import feature.puzzle.PuzzlePieceItem;
import feature.questlog.QuestLogEntry;
import feature.questlog.QuestLogUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import rooms.lasthour.modules.computer.ComputerStateComponent;
import rooms.lasthour.modules.computer.ComputerStateComponentCodec;
import rooms.lasthour.modules.usbstick.UsbStickItem;
import rooms.lasthour.petrinet.LastHourMilestone;
import rooms.lasthour.petrinet.LastHourProgressNet;
import rooms.lasthour.util.LastHourAchievements;
import rooms.lasthour.util.LastHourQuestLogUtil;

/** Reads and restores The Last Hour save without replaying gameplay callbacks. */
public final class LastHourLoad {
  private static final ComputerStateComponentCodec COMPUTER_CODEC =
      new ComputerStateComponentCodec();

  private LastHourLoad() {}

  /** Reads and validates a save file. */
  public static Optional<LastHourSave.SaveData> read(Path path) {
    if (path == null || !Files.isRegularFile(path)) return Optional.empty();
    try {
      return Optional.of(parse(Files.readString(path, StandardCharsets.UTF_8)));
    } catch (IOException | RuntimeException ignored) {
      return Optional.empty();
    }
  }

  /** Reads the default save file selected by the main menu. */
  public static Optional<LastHourSave.SaveData> read() {
    return read(LastHourSave.DEFAULT_PATH);
  }

  /** Returns whether a readable save exists at the default menu location. */
  public static boolean exists() {
    return read().isPresent();
  }

  /** Restores the milestone marking, quest log and run achievement progress silently. */
  public static boolean restoreRuntime(LastHourSave.SaveData data) {
    if (data == null) return false;
    try {
      if (!LastHourProgressNet.restore(data.milestones())) return false;
      restoreQuestLog(data.questLog());
      LastHourQuestLogUtil.restoreAddedEntryKeys(data.questKeys());
      LastHourAchievements.restoreRunProgress(data.achievements());
      return true;
    } catch (RuntimeException ignored) {
      return false;
    }
  }

  private static void restoreQuestLog(List<LastHourSave.QuestEntryData> entries) {
    QuestLogUtil.getQuestLogComponent()
        .ifPresent(
            component -> {
              component
                  .getEntries()
                  .forEach(
                      (tab, existing) -> existing.forEach(entry -> component.remove(tab, entry)));
              entries.forEach(saved -> component.add(saved.tab(), saved.entry()));
            });
  }

  private static LastHourSave.SaveData parse(String json) {
    Map<String, Object> root = JsonHandler.readJson(json);
    if (integer(root.get("formatVersion")) != LastHourSave.FORMAT_VERSION) {
      throw new IllegalArgumentException("Unsupported save format");
    }
    EnumSet<LastHourMilestone> milestones = EnumSet.noneOf(LastHourMilestone.class);
    for (Object value : list(root.get("milestones"))) {
      milestones.add(LastHourMilestone.valueOf(string(value)));
    }
    ComputerStateComponent computer =
        COMPUTER_CODEC.decode(Base64.getDecoder().decode(string(root.get("computer"))));
    List<LastHourSave.QuestEntryData> questLog = new ArrayList<>();
    for (Object value : list(root.get("questLog"))) questLog.add(parseQuestEntry(map(value)));
    Set<String> questKeys = new LinkedHashSet<>();
    for (Object value : list(root.get("questKeys"))) questKeys.add(string(value));
    List<LastHourSave.PlayerData> players = new ArrayList<>();
    for (Object value : list(root.get("players"))) players.add(parsePlayer(map(value)));
    Map<String, List<String>> achievements = new LinkedHashMap<>();
    map(root.get("achievements"))
        .forEach(
            (key, value) ->
                achievements.put(key, list(value).stream().map(LastHourLoad::string).toList()));
    return new LastHourSave.SaveData(
        UUID.fromString(string(root.get("runId"))),
        milestones,
        computer,
        integer(root.get("blogElapsedSeconds")),
        longValue(root.get("unknownDeviceShutdownRemainingMs")),
        bool(root.get("keypadUnlocked")),
        integer(root.get("wrongCodeAttempts")),
        bool(root.get("storageDoorOpen")),
        integer(root.get("remainingSeconds")),
        bool(root.get("timerExpired")),
        parsePhone(map(root.get("phone"))),
        bool(root.get("trashNoteAwarded")),
        bool(root.get("blueTrashAwarded")),
        questLog,
        questKeys,
        players,
        achievements,
        nullableBool(root.get("trackingConsent")));
  }

  private static LastHourSave.PhoneData parsePhone(Map<String, Object> map) {
    return new LastHourSave.PhoneData(
        bool(map.get("firstTriggered")),
        bool(map.get("secondScheduled")),
        bool(map.get("ringing")),
        string(map.get("dialog")),
        longValue(map.get("firstDelayMs")),
        longValue(map.get("secondDelayMs")));
  }

  private static LastHourSave.QuestEntryData parseQuestEntry(Map<String, Object> map) {
    return new LastHourSave.QuestEntryData(
        string(map.get("tab")),
        new QuestLogEntry(
            string(map.get("text")),
            integer(map.get("timestamp")),
            bool(map.get("userCreated")),
            string(map.get("owner")),
            bool(map.get("onlyForCreator"))));
  }

  private static LastHourSave.PlayerData parsePlayer(Map<String, Object> map) {
    List<LastHourSave.ItemData> items = new ArrayList<>();
    for (Object value : list(map.get("items"))) items.add(parseItem(map(value)));
    Object x = map.get("x");
    Object y = map.get("y");
    if (!(x instanceof Number xValue) || !(y instanceof Number yValue)) {
      throw new IllegalArgumentException("Invalid saved player position");
    }
    return new LastHourSave.PlayerData(
        string(map.get("name")),
        xValue.floatValue(),
        yValue.floatValue(),
        bool(map.get("introShown")),
        items);
  }

  private static LastHourSave.ItemData parseItem(Map<String, Object> map) {
    Map<String, String> itemData = new LinkedHashMap<>();
    map(map.get("data")).forEach((key, value) -> itemData.put(key, string(value)));
    int slot = integer(map.get("slot"));
    engine.network.messages.s2c.ItemState state =
        new engine.network.messages.s2c.ItemState(
            string(map.get("type")),
            integer(map.get("stackSize")),
            integer(map.get("maxStackSize")),
            itemData);
    if (slot < 0) throw new IllegalArgumentException("Invalid inventory slot");
    UsbStickItem.ensureRegistration();
    PuzzlePieceItem.ensureRegistration();
    if (ItemRegistry.lookup(state.itemType()).isEmpty()) {
      throw new IllegalArgumentException("Unknown item type in save");
    }
    return new LastHourSave.ItemData(slot, state);
  }

  private static Map<String, Object> map(Object value) {
    if (!(value instanceof Map<?, ?> raw)) throw new IllegalArgumentException("Expected object");
    Map<String, Object> result = new LinkedHashMap<>();
    raw.forEach((key, entry) -> result.put(string(key), entry));
    return result;
  }

  private static List<?> list(Object value) {
    if (!(value instanceof List<?> result)) throw new IllegalArgumentException("Expected array");
    return result;
  }

  private static String string(Object value) {
    if (!(value instanceof String result)) throw new IllegalArgumentException("Expected string");
    return result;
  }

  private static boolean bool(Object value) {
    if (!(value instanceof Boolean result)) throw new IllegalArgumentException("Expected boolean");
    return result;
  }

  private static Boolean nullableBool(Object value) {
    if (value == null) return null;
    if (!(value instanceof Boolean result)) throw new IllegalArgumentException("Expected boolean");
    return result;
  }

  private static int integer(Object value) {
    long result = longValue(value);
    if (result < Integer.MIN_VALUE || result > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Invalid integer");
    }
    return (int) result;
  }

  private static long longValue(Object value) {
    if (!(value instanceof Number number)) throw new IllegalArgumentException("Expected number");
    return number.longValue();
  }
}
