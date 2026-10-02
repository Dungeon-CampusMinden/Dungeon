package rooms.lasthour.save;

import engine.network.messages.s2c.ItemState;
import engine.utils.JsonHandler;
import feature.questlog.QuestLogEntry;
import feature.questlog.QuestLogUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import rooms.lasthour.modules.computer.ComputerCallbacks;
import rooms.lasthour.modules.computer.ComputerStateComponent;
import rooms.lasthour.modules.computer.ComputerStateComponentCodec;
import rooms.lasthour.modules.computer.LastHourBlogTime;
import rooms.lasthour.petrinet.LastHourMilestone;
import rooms.lasthour.petrinet.LastHourProgressNet;
import rooms.lasthour.starter.TheLastHour;
import rooms.lasthour.util.LastHourQuestLogUtil;

/** Versioned, atomically written server save for The Last Hour. */
public final class LastHourSave {
  public static final Path DEFAULT_PATH = Path.of("the-last-hour-save.json");
  static final int FORMAT_VERSION = 1;
  private static final ComputerStateComponentCodec COMPUTER_CODEC =
      new ComputerStateComponentCodec();

  private LastHourSave() {}

  /**
   * Captures all persistent run state after the level has gathered its room-local values.
   *
   * @param context room-local values captured by the level
   * @return validated snapshot of the current run
   */
  public static SaveData capture(CaptureContext context) {
    List<QuestEntryData> questLog = new ArrayList<>();
    QuestLogUtil.getQuestLogComponent()
        .ifPresent(
            component ->
                component
                    .getEntries()
                    .forEach(
                        (tab, entries) ->
                            entries.forEach(
                                entry -> questLog.add(new QuestEntryData(tab, entry)))));
    questLog.sort(
        java.util.Comparator.comparing(QuestEntryData::tab)
            .thenComparingInt(entry -> entry.entry().timestamp())
            .thenComparing(entry -> entry.entry().text()));
    ComputerStateComponent computer = ComputerStateComponent.getState().orElseThrow();
    int blogElapsed =
        LastHourBlogTime.elapsedSeconds(
            computer.timestampOfLogin(), (int) (System.currentTimeMillis() / 1000L));
    return new SaveData(
        TheLastHour.runId(),
        LastHourProgressNet.completedMilestones(),
        computer,
        blogElapsed,
        ComputerCallbacks.unknownDeviceShutdownRemainingMs(),
        context.keypadUnlocked(),
        context.wrongCodeAttempts(),
        context.storageDoorOpen(),
        context.remainingSeconds(),
        context.timerExpired(),
        context.phone(),
        context.trashNoteAwarded(),
        context.blueTrashAwarded(),
        questLog,
        LastHourQuestLogUtil.addedEntryKeys(),
        context.players(),
        TheLastHour.trackingConsent());
  }

  /**
   * Returns whether a readable save exists at the default path.
   *
   * @return true if a valid save is available
   */
  public static boolean exists() {
    return LastHourLoad.exists();
  }

  /**
   * Deletes the save at the default path.
   *
   * @throws IOException if the save cannot be deleted
   */
  public static void delete() throws IOException {
    Files.deleteIfExists(DEFAULT_PATH);
  }

  /**
   * Updates the consent decision in an existing run save, if one has been written.
   *
   * @param consent tracking decision to persist
   * @throws IOException if the updated save cannot be written
   */
  public static void updateTrackingConsent(Boolean consent) throws IOException {
    Optional<SaveData> existing = LastHourLoad.read();
    if (existing.isPresent()) write(existing.get().withTrackingConsent(consent));
  }

  /**
   * Writes a save to the default path.
   *
   * @param data validated state to write
   * @throws IOException if the save cannot be written
   */
  public static void write(SaveData data) throws IOException {
    write(DEFAULT_PATH, data);
  }

  /**
   * Writes a save atomically to the given path.
   *
   * @param path destination file
   * @param data validated state to write
   * @throws IOException if the save cannot be written
   */
  public static void write(Path path, SaveData data) throws IOException {
    Path absolute = path.toAbsolutePath();
    Path parent = absolute.getParent();
    if (parent != null) Files.createDirectories(parent);
    Path temporary = Files.createTempFile(parent, "last-hour-", ".tmp");
    try {
      Files.writeString(temporary, toJson(data), StandardCharsets.UTF_8);
      try {
        Files.move(
            temporary,
            absolute,
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException ignored) {
        Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
      }
    } finally {
      Files.deleteIfExists(temporary);
    }
  }

  static String toJson(SaveData data) {
    Map<String, Object> root = new LinkedHashMap<>();
    root.put("formatVersion", FORMAT_VERSION);
    root.put("runId", data.runId().toString());
    root.put("milestones", data.milestones().stream().map(Enum::name).sorted().toList());
    root.put(
        "computer", Base64.getEncoder().encodeToString(COMPUTER_CODEC.encode(data.computer())));
    root.put("blogElapsedSeconds", data.blogElapsedSeconds());
    root.put("unknownDeviceShutdownRemainingMs", data.unknownDeviceShutdownRemainingMs());
    root.put("keypadUnlocked", data.keypadUnlocked());
    root.put("wrongCodeAttempts", data.wrongCodeAttempts());
    root.put("storageDoorOpen", data.storageDoorOpen());
    root.put("remainingSeconds", data.remainingSeconds());
    root.put("timerExpired", data.timerExpired());
    root.put("phone", data.phone().toMap());
    root.put("trashNoteAwarded", data.trashNoteAwarded());
    root.put("blueTrashAwarded", data.blueTrashAwarded());
    root.put("questLog", data.questLog().stream().map(QuestEntryData::toMap).toList());
    root.put("questKeys", data.questKeys().stream().sorted().toList());
    root.put("players", data.players().stream().map(PlayerData::toMap).toList());
    root.put("trackingConsent", data.trackingConsent());
    return JsonHandler.writeJson(root, true);
  }

  /**
   * Immutable snapshot of run state required to resume The Last Hour.
   *
   * @param runId identifier shared by this run's saves and tracking events
   * @param milestones completed irreversible Petri net transitions
   * @param computer saved computer state
   * @param blogElapsedSeconds elapsed blog session time
   * @param unknownDeviceShutdownRemainingMs remaining delay before an unknown-device shutdown
   * @param keypadUnlocked whether the storage keypad has been unlocked
   * @param wrongCodeAttempts number of incorrect keypad submissions
   * @param storageDoorOpen whether the storage door is open
   * @param remainingSeconds seconds remaining on the run timer
   * @param timerExpired whether the run timer has expired
   * @param phone saved phone state
   * @param trashNoteAwarded whether the trash note has been awarded
   * @param blueTrashAwarded whether the blue trash item has been awarded
   * @param questLog saved quest log entries
   * @param questKeys quest log keys already added by room logic
   * @param players saved player positions, intro state and inventories
   * @param trackingConsent saved tracking decision, or {@code null} while undecided
   */
  public record SaveData(
      UUID runId,
      Set<LastHourMilestone> milestones,
      ComputerStateComponent computer,
      int blogElapsedSeconds,
      long unknownDeviceShutdownRemainingMs,
      boolean keypadUnlocked,
      int wrongCodeAttempts,
      boolean storageDoorOpen,
      int remainingSeconds,
      boolean timerExpired,
      PhoneData phone,
      boolean trashNoteAwarded,
      boolean blueTrashAwarded,
      List<QuestEntryData> questLog,
      Set<String> questKeys,
      List<PlayerData> players,
      Boolean trackingConsent) {
    /**
     * Copies the snapshot with an updated tracking decision.
     *
     * @param consent tracking decision to store
     * @return a copy of this snapshot with the supplied decision
     */
    public SaveData withTrackingConsent(Boolean consent) {
      return new SaveData(
          runId,
          milestones,
          computer,
          blogElapsedSeconds,
          unknownDeviceShutdownRemainingMs,
          keypadUnlocked,
          wrongCodeAttempts,
          storageDoorOpen,
          remainingSeconds,
          timerExpired,
          phone,
          trashNoteAwarded,
          blueTrashAwarded,
          questLog,
          questKeys,
          players,
          consent);
    }

    /**
     * Creates a validated immutable save snapshot.
     *
     * @param runId identifier shared by this run's saves and tracking events
     * @param milestones completed irreversible Petri net transitions
     * @param computer saved computer state
     * @param blogElapsedSeconds elapsed blog session time
     * @param unknownDeviceShutdownRemainingMs remaining delay before an unknown-device shutdown
     * @param keypadUnlocked whether the storage keypad has been unlocked
     * @param wrongCodeAttempts number of incorrect keypad submissions
     * @param storageDoorOpen whether the storage door is open
     * @param remainingSeconds seconds remaining on the run timer
     * @param timerExpired whether the run timer has expired
     * @param phone saved phone state
     * @param trashNoteAwarded whether the trash note has been awarded
     * @param blueTrashAwarded whether the blue trash item has been awarded
     * @param questLog saved quest log entries
     * @param questKeys quest log keys already added by room logic
     * @param players saved player positions, intro state and inventories
     * @param trackingConsent saved tracking decision, or {@code null} while undecided
     */
    public SaveData {
      Set<LastHourMilestone> immutableMilestones = Set.copyOf(milestones);
      milestones = immutableMilestones;
      questLog = List.copyOf(questLog);
      questKeys = Set.copyOf(questKeys);
      players = List.copyOf(players);
      if (runId == null || computer == null || phone == null)
        throw new IllegalArgumentException("Missing save state");
      if (blogElapsedSeconds < 0
          || unknownDeviceShutdownRemainingMs < -1
          || wrongCodeAttempts < 0
          || remainingSeconds < 0) {
        throw new IllegalArgumentException("Invalid saved time or counter");
      }
      if (immutableMilestones.stream()
          .anyMatch(milestone -> !immutableMilestones.containsAll(milestone.prerequisites()))) {
        throw new IllegalArgumentException("Invalid Petri marking in save");
      }
      Set<String> names = new HashSet<>();
      Set<String> uniqueItems = new HashSet<>();
      for (PlayerData player : players) {
        if (player.name().isBlank() || !names.add(player.name())) {
          throw new IllegalArgumentException("Duplicate or empty player name in save");
        }
        Set<Integer> slots = new HashSet<>();
        for (ItemData item : player.items()) {
          if (!slots.add(item.slot()))
            throw new IllegalArgumentException("Duplicate inventory slot");
          String type = item.state().itemType();
          String itemIdentity = type + new TreeMap<>(item.state().itemData());
          if (isUniqueRoomItem(type) && !uniqueItems.add(itemIdentity)) {
            throw new IllegalArgumentException("Duplicate room item in save");
          }
          if (type.equals("BlueUsbStick")
              && milestones.contains(LastHourMilestone.BLUE_USB_INSERTED)) {
            throw new IllegalArgumentException("Inserted blue USB cannot also be carried");
          }
        }
      }
    }
  }

  /**
   * Values owned by the level while the save layer captures shared run state.
   *
   * @param keypadUnlocked whether the storage keypad has been unlocked
   * @param wrongCodeAttempts number of incorrect keypad submissions
   * @param storageDoorOpen whether the storage door is open
   * @param remainingSeconds seconds remaining on the run timer
   * @param timerExpired whether the run timer has expired
   * @param phone saved phone state
   * @param trashNoteAwarded whether the trash note has been awarded
   * @param blueTrashAwarded whether the blue trash item has been awarded
   * @param players saved player positions, intro state and inventories
   */
  public record CaptureContext(
      boolean keypadUnlocked,
      int wrongCodeAttempts,
      boolean storageDoorOpen,
      int remainingSeconds,
      boolean timerExpired,
      PhoneData phone,
      boolean trashNoteAwarded,
      boolean blueTrashAwarded,
      List<PlayerData> players) {
    /**
     * Copies the supplied player state.
     *
     * @param keypadUnlocked whether the storage keypad has been unlocked
     * @param wrongCodeAttempts number of incorrect keypad submissions
     * @param storageDoorOpen whether the storage door is open
     * @param remainingSeconds seconds remaining on the run timer
     * @param timerExpired whether the run timer has expired
     * @param phone saved phone state
     * @param trashNoteAwarded whether the trash note has been awarded
     * @param blueTrashAwarded whether the blue trash item has been awarded
     * @param players saved player positions, intro state and inventories
     */
    public CaptureContext {
      players = List.copyOf(players);
    }
  }

  private static boolean isUniqueRoomItem(String type) {
    return type.equals("RedUsbStick")
        || type.equals("GreenUsbStick")
        || type.equals("YellowUsbStick")
        || type.equals("BlueUsbStick")
        || type.equals("PuzzlePieceItem")
        || type.equals("HintItem");
  }

  /**
   * Saved timing and dialog state for the phone.
   *
   * @param firstTriggered whether the first call has been triggered
   * @param secondScheduled whether a second call is scheduled
   * @param ringing whether the phone is currently ringing
   * @param dialog dialog text for the active call
   * @param firstDelayMs remaining delay before the first call
   * @param secondDelayMs remaining delay before the second call
   */
  public record PhoneData(
      boolean firstTriggered,
      boolean secondScheduled,
      boolean ringing,
      String dialog,
      long firstDelayMs,
      long secondDelayMs) {
    /**
     * Validates the saved phone state.
     *
     * @param firstTriggered whether the first call has been triggered
     * @param secondScheduled whether a second call is scheduled
     * @param ringing whether the phone is currently ringing
     * @param dialog dialog text for the active call
     * @param firstDelayMs remaining delay before the first call
     * @param secondDelayMs remaining delay before the second call
     */
    public PhoneData {
      if (dialog == null || firstDelayMs < -1 || secondDelayMs < -1) {
        throw new IllegalArgumentException("Invalid saved phone state");
      }
      if (ringing && dialog.isBlank()) {
        throw new IllegalArgumentException("Ringing phone has no dialog");
      }
    }

    Map<String, Object> toMap() {
      return Map.of(
          "firstTriggered",
          firstTriggered,
          "secondScheduled",
          secondScheduled,
          "ringing",
          ringing,
          "dialog",
          dialog,
          "firstDelayMs",
          firstDelayMs,
          "secondDelayMs",
          secondDelayMs);
    }
  }

  /**
   * Quest log entry paired with its tab name.
   *
   * @param tab quest log tab containing the entry
   * @param entry quest log entry to save
   */
  public record QuestEntryData(String tab, QuestLogEntry entry) {
    Map<String, Object> toMap() {
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("tab", tab);
      result.put("text", entry.text());
      result.put("timestamp", entry.timestamp());
      result.put("userCreated", entry.userCreated());
      result.put("owner", entry.owner());
      result.put("onlyForCreator", entry.onlyForCreator());
      return result;
    }
  }

  /**
   * Saved position, intro state and inventory for a named player.
   *
   * @param name player name used to match the player on restore
   * @param x saved horizontal position
   * @param y saved vertical position
   * @param introShown whether the player's introduction was shown
   * @param items saved inventory items
   */
  public record PlayerData(
      String name, float x, float y, boolean introShown, List<ItemData> items) {
    /**
     * Validates the player state and copies its inventory.
     *
     * @param name player name used to match the player on restore
     * @param x saved horizontal position
     * @param y saved vertical position
     * @param introShown whether the player's introduction was shown
     * @param items saved inventory items
     */
    public PlayerData {
      if (name == null || name.isBlank() || !Float.isFinite(x) || !Float.isFinite(y)) {
        throw new IllegalArgumentException("Invalid saved player state");
      }
      items = List.copyOf(items);
    }

    Map<String, Object> toMap() {
      return Map.of(
          "name",
          name,
          "x",
          x,
          "y",
          y,
          "introShown",
          introShown,
          "items",
          items.stream().map(ItemData::toMap).toList());
    }
  }

  /**
   * Saved item and its inventory slot.
   *
   * @param slot inventory slot containing the item
   * @param state serialized item state
   */
  public record ItemData(int slot, ItemState state) {
    /**
     * Validates the saved slot and item stack.
     *
     * @param slot inventory slot containing the item
     * @param state serialized item state
     */
    public ItemData {
      if (slot < 0
          || state == null
          || state.stackSize() <= 0
          || state.maxStackSize() <= 0
          || state.stackSize() > state.maxStackSize()) {
        throw new IllegalArgumentException("Invalid saved item state");
      }
    }

    Map<String, Object> toMap() {
      return Map.of(
          "slot",
          slot,
          "type",
          state.itemType(),
          "stackSize",
          state.stackSize(),
          "maxStackSize",
          state.maxStackSize(),
          "data",
          state.itemData());
    }
  }
}
