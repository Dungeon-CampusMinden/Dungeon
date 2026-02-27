package starter;

import contrib.entities.CharacterClass;
import contrib.entities.HeroBuilder;
import contrib.hud.dialogs.DialogFactory;
import contrib.modules.keypad.KeypadComponent;
import contrib.modules.worldTimer.WorldTimerComponent;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.configuration.KeyboardConfig;
import core.game.PreRunConfiguration;
import core.level.loader.DungeonLoader;
import core.network.config.NetworkConfig;
import core.network.messages.s2c.EntitySpawnEvent;
import core.utils.Tuple;
import core.utils.components.draw.DrawComponentFactory;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import level.LastHourLevelClient;
import modules.computer.*;
import modules.trash.TrashMinigameUI;
import network.LastHourEntitySpawnStrategy;
import network.LastHourSnapshotTranslator;
import util.ui.BlackFadeCutscene;

/** The main class for the Multiplayer Client for development and testing purposes. */
public final class LastHourClient {
  private static final String METADATA_TYPE = "lh.type";
  private static final String TYPE_COMPUTER = "computer-state";
  private static final String TYPE_KEYPAD = "keypad";
  private static final String TYPE_WORLD_TIMER = "world-timer";
  private static final String METADATA_PROGRESS = "progress";
  private static final String METADATA_INFECTED = "isInfected";
  private static final String METADATA_VIRUS_TYPE = "virusType";
  private static final String METADATA_KEYPAD_CORRECT_DIGITS = "keypad.correctDigits";
  private static final String METADATA_KEYPAD_ENTERED_DIGITS = "keypad.enteredDigits";
  private static final String METADATA_KEYPAD_UNLOCKED = "keypad.isUnlocked";
  private static final String METADATA_KEYPAD_SHOW_DIGIT_COUNT = "keypad.showDigitCount";
  private static final String METADATA_WORLD_TIMER_TIMESTAMP = "worldTimer.timestamp";
  private static final String METADATA_WORLD_TIMER_DURATION = "worldTimer.duration";

  /**
   * Main method to start the dev client.
   *
   * @param args command line arguments
   * @throws IOException if an I/O error occurs
   */
  public static void main(String[] args) throws IOException {
    // PreRun configuration for multiplayer client
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    PreRunConfiguration.networkServerAddress("127.0.0.1");
    PreRunConfiguration.networkPort(7777);
    PreRunConfiguration.username("Player1");

    registerCustomDialogs();

    DungeonLoader.addLevel(Tuple.of("lasthour", LastHourLevelClient.class));

    // Game Settings
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(60);
    Game.windowTitle("Dev Client - " + PreRunConfiguration.username());
    NetworkConfig.SNAPSHOT_TRANSLATOR = new LastHourSnapshotTranslator();
    NetworkConfig.ENTITY_SPAWN_STRATEGY = new LastHourEntitySpawnStrategy();
    Game.userOnSetup(
        () -> {
          registerEntitySpawnHandler();
          Game.add(new Debugger());
          System.out.println("DevClient started");
        });

    // Start the game
    Game.run();
  }

  private static void registerCustomDialogs() {
    ComputerFactory.ensureRegistration();
    DialogFactory.register(LastHourDialogTypes.TRASHCAN, TrashMinigameUI::build);
    DialogFactory.register(LastHourDialogTypes.TEXT_CUTSCENE, BlackFadeCutscene::build);
  }

  /**
   * Registers a custom spawn handler that supports metadata-only entities for computer state
   * synchronization.
   */
  private static void registerEntitySpawnHandler() {
    Game.network()
        .messageDispatcher()
        .registerHandler(
            EntitySpawnEvent.class,
            (ctx, event) -> {
              if (Game.allEntities().anyMatch(e -> e.id() == event.entityId())) {
                return;
              }

              if (event.playerComponent() != null) {
                spawnPlayer(event);
                return;
              }

              Entity newEntity = new Entity(event.entityId());
              if (event.positionComponent() != null) {
                newEntity.add(event.positionComponent());
              }
              if (event.drawInfo() != null) {
                newEntity.add(DrawComponentFactory.fromDrawInfo(event.drawInfo()));
              }
              computerStateFromMetadata(event.metadata()).ifPresent(newEntity::add);
              keypadStateFromMetadata(event.metadata()).ifPresent(newEntity::add);
              worldTimerStateFromMetadata(event.metadata()).ifPresent(newEntity::add);
              newEntity.persistent(event.isPersistent());
              Game.add(newEntity);
            });
  }

  private static void spawnPlayer(EntitySpawnEvent event) {
    PlayerComponent playerComponent = event.playerComponent();
    if (playerComponent == null) {
      return;
    }

    boolean alreadyGotAHero = Game.player().isPresent();
    boolean isLocal = Objects.equals(playerComponent.playerName(), PreRunConfiguration.username());
    if (alreadyGotAHero && isLocal) {
      return;
    }

    Game.add(
        HeroBuilder.builder()
            .id(event.entityId())
            .characterClass(CharacterClass.fromByteId(event.characterClassId()))
            .isLocalPlayer(isLocal)
            .username(playerComponent.playerName())
            .build());
  }

  private static Optional<ComputerStateComponent> computerStateFromMetadata(
      Map<String, String> metadata) {
    if (!TYPE_COMPUTER.equals(metadata.get(METADATA_TYPE))
        && !metadata.containsKey(METADATA_PROGRESS)) {
      return Optional.empty();
    }

    String progressRaw = metadata.get(METADATA_PROGRESS);
    if (progressRaw == null || progressRaw.isBlank()) {
      return Optional.empty();
    }

    ComputerProgress progress;
    try {
      progress = ComputerProgress.valueOf(progressRaw);
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }

    boolean infected = Boolean.parseBoolean(metadata.getOrDefault(METADATA_INFECTED, "false"));
    String virusType = metadata.getOrDefault(METADATA_VIRUS_TYPE, "");
    return Optional.of(new ComputerStateComponent(progress, infected, virusType));
  }

  private static Optional<KeypadComponent> keypadStateFromMetadata(Map<String, String> metadata) {
    if (!TYPE_KEYPAD.equals(metadata.get(METADATA_TYPE))) {
      return Optional.empty();
    }

    String correctDigitsRaw = metadata.get(METADATA_KEYPAD_CORRECT_DIGITS);
    if (correctDigitsRaw == null) {
      return Optional.empty();
    }

    KeypadComponent keypadComponent =
        new KeypadComponent(
            parseDigits(correctDigitsRaw),
            () -> {},
            Boolean.parseBoolean(metadata.getOrDefault(METADATA_KEYPAD_SHOW_DIGIT_COUNT, "true")));
    keypadComponent
        .enteredDigits()
        .addAll(parseDigits(metadata.getOrDefault(METADATA_KEYPAD_ENTERED_DIGITS, "")));
    keypadComponent.isUnlocked(
        Boolean.parseBoolean(metadata.getOrDefault(METADATA_KEYPAD_UNLOCKED, "false")));
    return Optional.of(keypadComponent);
  }

  private static ArrayList<Integer> parseDigits(String value) {
    if (value == null || value.isBlank()) {
      return new ArrayList<>();
    }
    try {
      return Arrays.stream(value.split(","))
          .map(String::trim)
          .map(Integer::parseInt)
          .collect(Collectors.toCollection(ArrayList::new));
    } catch (NumberFormatException ex) {
      return new ArrayList<>();
    }
  }

  private static Optional<WorldTimerComponent> worldTimerStateFromMetadata(
      Map<String, String> metadata) {
    if (!TYPE_WORLD_TIMER.equals(metadata.get(METADATA_TYPE))) {
      return Optional.empty();
    }

    String timestampRaw = metadata.get(METADATA_WORLD_TIMER_TIMESTAMP);
    String durationRaw = metadata.get(METADATA_WORLD_TIMER_DURATION);
    if (timestampRaw == null || durationRaw == null) {
      return Optional.empty();
    }

    try {
      return Optional.of(
          new WorldTimerComponent(Integer.parseInt(timestampRaw), Integer.parseInt(durationRaw)));
    } catch (NumberFormatException ex) {
      return Optional.empty();
    }
  }
}
