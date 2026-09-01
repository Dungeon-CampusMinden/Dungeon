package rooms.lasthour.modules.computer;

import engine.Entity;
import engine.Game;
import engine.game.PreRunConfiguration;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import feature.components.UIComponent;
import feature.emote.Emote;
import feature.emote.EmoteFactory;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import rooms.lasthour.level.LastHourLevel;
import rooms.lasthour.modules.computer.content.BlogTab;
import rooms.lasthour.util.LastHourPuzzle;
import rooms.lasthour.util.LastHourQuestLogUtil;
import rooms.lasthour.util.LastHourSounds;
import rooms.lasthour.util.LastHourTracking;
import rooms.lasthour.util.Lore;

/**
 * Defines and registers the computer dialog's callbacks.
 *
 * <p>This keeps dialog construction in {@link ComputerFactory} separate from authoritative payload
 * validation, computer state transitions, and tracking events.
 */
public final class ComputerCallbacks {

  /** Callback key fired when a player opens the control panel tab. */
  public static final String CONTROL_PANEL_OPENED_KEY = "control_panel_opened";

  /** Callback carrying a newly viewed blog comment's stable identifier. */
  public static final String BLOG_COMMENT_VIEWED_KEY = "blog_comment_viewed";

  /** Callback fired when a player opens the exit-code hint file. */
  public static final String EXIT_CODE_HINT_OPENED_KEY = "exit_code_hint_opened";

  /** Callback fired when a player follows a known malicious link or attachment. */
  public static final String VIRUS_TRIGGER_KEY = "virus_trigger";

  /** Callback carrying the code entered to neutralize the current virus. */
  public static final String VIRUS_CODE_ATTEMPT_KEY = "virus_code_attempt";

  /** Control-panel intent callbacks. */
  public static final String TOGGLE_LIGHTS_KEY = "toggle_lights";

  public static final String ADJUST_HEATER_KEY = "adjust_heater";
  public static final String TOGGLE_DOOR_1_KEY = "toggle_door_1";
  public static final String OPEN_EXIT_DOOR_KEY = "open_exit_door";
  public static final String TOGGLE_AC_KEY = "toggle_ac";
  public static final String TOGGLE_CAMERAS_KEY = "toggle_cameras";

  /** Callback carrying the raw username and password entered in the login form. */
  public static final String LOGIN_ATTEMPT_KEY = "login_attempt";

  /** Callback carrying the raw recovery code entered in the browser. */
  public static final String RECOVERY_ATTEMPT_KEY = "recovery_attempt";

  /** Callback carrying the raw exit-door password entered in the control panel. */
  public static final String EXIT_ATTEMPT_KEY = "exit_attempt";

  /** Callback carrying the raw ventilation serial entered in the control panel. */
  public static final String VENTILATION_ATTEMPT_KEY = "ventilation_attempt";

  /** Delay between triggering the unknown-device virus and the forced shutdown. */
  static final long UNKNOWN_DEVICE_SHUTDOWN_DELAY_MS = 10_000L;

  private static final int MIN_HEATER_CELSIUS = 10;
  private static final int MAX_HEATER_CELSIUS = 30;
  private static final String VIRUS_CODE_OBJECT_ID = "virus-security-code";

  private static Consumer<Entity> onVirusTriggered = who -> {};
  private static Consumer<Entity> onPcUnlocked = who -> {};
  private static Consumer<Entity> onControlPanelOpened = who -> {};

  private ComputerCallbacks() {}

  /**
   * Sets the action fired when the computer enters a newly infected state.
   *
   * @param callback action receiving the player who triggered the infection
   */
  public static void onVirusTriggered(Consumer<Entity> callback) {
    onVirusTriggered = Objects.requireNonNull(callback, "callback");
  }

  /**
   * Sets the action fired when the computer reaches the logged-in state.
   *
   * @param callback action receiving the player who unlocked the computer
   */
  public static void onPcUnlocked(Consumer<Entity> callback) {
    onPcUnlocked = Objects.requireNonNull(callback, "callback");
  }

  /**
   * Sets the action fired when a player opens the control panel tab.
   *
   * @param callback action receiving the player who opened the control panel
   */
  public static void onControlPanelOpened(Consumer<Entity> callback) {
    onControlPanelOpened = Objects.requireNonNull(callback, "callback");
  }

  /**
   * Registers every server-side intent accepted by an open computer dialog.
   *
   * @param dialog open dialog receiving client intents
   * @param stateEntity entity holding the authoritative computer state
   * @param who player interacting with the computer
   */
  static void registerCallbacks(UIComponent dialog, Entity stateEntity, Entity who) {
    dialog.registerCallback(
        VIRUS_TRIGGER_KEY,
        data ->
            responseString(data)
                .filter(
                    source ->
                        Lore.VirusWebsites.contains(source)
                            || Lore.VirusAttachmentNames.contains(source))
                .ifPresent(source -> infectComputer(stateEntity, who)));
    dialog.registerCallback(
        VIRUS_CODE_ATTEMPT_KEY,
        data -> responseString(data).ifPresent(raw -> neutralizeVirus(stateEntity, raw, who)));
    dialog.registerCallback(
        TOGGLE_LIGHTS_KEY,
        data -> {
          if (!(data instanceof DialogResponseMessage.BoolValue(boolean target))) {
            return;
          }
          applyControlIntent(stateEntity, state -> state.withLightsOn(target));
        });
    dialog.registerCallback(
        ADJUST_HEATER_KEY,
        data -> {
          if (!(data instanceof DialogResponseMessage.IntValue(int delta))
              || (delta != -1 && delta != 1)) {
            return;
          }
          applyControlIntent(
              stateEntity,
              state ->
                  state.withHeaterCelsius(
                      Math.max(
                          MIN_HEATER_CELSIUS,
                          Math.min(MAX_HEATER_CELSIUS, state.heaterCelsius() + delta))));
        });
    dialog.registerCallback(
        TOGGLE_DOOR_1_KEY,
        data -> {
          if (!(data instanceof DialogResponseMessage.BoolValue(boolean target))) {
            return;
          }
          applyControlIntent(stateEntity, state -> state.withDoor1Open(target));
        });
    dialog.registerCallback(
        OPEN_EXIT_DOOR_KEY,
        data -> {
          ComputerStateComponent current = currentState(stateEntity).orElse(null);
          if (!controlPanelAvailable(current) || !current.door2Unlocked() || current.door2Open()) {
            return;
          }
          applyControlIntent(stateEntity, state -> state.withDoor2Open(true));
          LastHourTracking.solved(LastHourPuzzle.EXIT);
        });
    dialog.registerCallback(
        TOGGLE_AC_KEY,
        data -> {
          if (!(data instanceof DialogResponseMessage.BoolValue(boolean target))) {
            return;
          }
          ComputerStateComponent current = currentState(stateEntity).orElse(null);
          if (!controlPanelAvailable(current) || !current.acVentConnected()) {
            return;
          }
          applyControlIntent(stateEntity, state -> state.withAcOn(target));
          if (target && !current.acOn()) {
            LastHourTracking.solved(LastHourPuzzle.VENTILATION);
          }
        });
    dialog.registerCallback(
        TOGGLE_CAMERAS_KEY,
        data -> {
          if (!(data instanceof DialogResponseMessage.BoolValue(boolean target))) {
            return;
          }
          applyControlIntent(stateEntity, state -> state.withCamerasOn(target));
        });
    dialog.registerCallback(
        LOGIN_ATTEMPT_KEY,
        data -> responseString(data).ifPresent(raw -> handleLoginAttempt(stateEntity, raw, who)));
    dialog.registerCallback(
        RECOVERY_ATTEMPT_KEY,
        data ->
            responseString(data).ifPresent(raw -> handleRecoveryAttempt(stateEntity, raw, who)));
    dialog.registerCallback(
        EXIT_ATTEMPT_KEY,
        data -> responseString(data).ifPresent(raw -> handleExitAttempt(stateEntity, raw, who)));
    dialog.registerCallback(
        VENTILATION_ATTEMPT_KEY,
        data ->
            responseString(data).ifPresent(raw -> handleVentilationAttempt(stateEntity, raw, who)));
    dialog.registerCallback(
        CONTROL_PANEL_OPENED_KEY,
        data -> {
          if (controlPanelAvailable(currentState(stateEntity).orElse(null))) {
            onControlPanelOpened.accept(who);
          }
        });
    dialog.registerCallback(
        BLOG_COMMENT_VIEWED_KEY,
        data ->
            responseString(data)
                .flatMap(BlogTab::comment)
                .ifPresent(
                    comment -> {
                      ComputerStateComponent current = currentState(stateEntity).orElse(null);
                      if (current == null
                          || current.isInfected()
                          || !current.state().hasReached(ComputerProgress.LOGGED_IN)
                          || !BlogTab.isCommentVisible(comment, current.timestampOfLogin())) {
                        return;
                      }
                      LastHourTracking.started(LastHourPuzzle.STORAGE_RECOVERY);
                      LastHourTracking.hintUsed(LastHourPuzzle.STORAGE_RECOVERY, comment.id(), who);
                    }));
    dialog.registerCallback(
        EXIT_CODE_HINT_OPENED_KEY,
        data -> {
          if (!controlPanelAvailable(currentState(stateEntity).orElse(null))) {
            return;
          }
          LastHourTracking.started(LastHourPuzzle.EXIT_CODE_ASSEMBLY);
          LastHourTracking.hintUsed(LastHourPuzzle.EXIT_CODE_ASSEMBLY, "usb-hint-file", who);
        });
  }

  private static void handleLoginAttempt(Entity stateEntity, String raw, Entity who) {
    ComputerStateComponent current = currentState(stateEntity).orElse(null);
    if (current == null || current.isInfected() || current.state() != ComputerProgress.ON) {
      return;
    }
    String[] credentials = raw.split("\\n", 2);
    String username = credentials.length > 0 ? credentials[0] : "";
    String password = credentials.length > 1 ? credentials[1] : "";
    boolean correct =
        (username.equalsIgnoreCase(Lore.LoginEmail)
                && password.equalsIgnoreCase(Lore.LoginPassword))
            || username.equals("skipp");
    LastHourTracking.attempt(
        LastHourPuzzle.LOGIN, "credentials", "username-password", raw, correct, who);
    if (!correct) {
      return;
    }
    LastHourTracking.solved(LastHourPuzzle.LOGIN);
    ComputerStateComponent currentState =
        stateEntity.fetch(ComputerStateComponent.class).orElseThrow();
    if (!currentState.state().hasReached(ComputerProgress.LOGGED_IN)) {
      replaceState(
          stateEntity,
          currentState
              .withState(ComputerProgress.LOGGED_IN)
              .withTimestampOfLogin((int) (System.currentTimeMillis() / 1000L)));
      LastHourTracking.started(LastHourPuzzle.STORAGE_RECOVERY);
      LastHourQuestLogUtil.addMailReviewQuestLogEntry();
      onPcUnlocked.accept(who);
    }
  }

  private static void handleRecoveryAttempt(Entity stateEntity, String raw, Entity who) {
    ComputerStateComponent current = currentState(stateEntity).orElse(null);
    if (current == null
        || current.isInfected()
        || !current.state().hasReached(ComputerProgress.LOGGED_IN)) {
      return;
    }
    boolean correct = raw.strip().equals(Lore.AsciiCodes.getFirst());
    LastHourTracking.attempt(
        LastHourPuzzle.STORAGE_RECOVERY,
        "browser-recovery-code",
        "numeric-code",
        raw,
        correct,
        who);
    if (correct) {
      LastHourTracking.solved(LastHourPuzzle.STORAGE_RECOVERY);
      LastHourTracking.started(LastHourPuzzle.STORAGE_ACCESS);
    }
  }

  private static void handleExitAttempt(Entity stateEntity, String raw, Entity who) {
    ComputerStateComponent current = currentState(stateEntity).orElse(null);
    if (!controlPanelAvailable(current)) {
      return;
    }
    boolean correct = raw.equalsIgnoreCase(Lore.ControlPanelDoor2Password);
    LastHourTracking.attempt(
        LastHourPuzzle.EXIT, "exit-door-code", "numeric-code", raw, correct, who);
    if (correct) {
      ComputerStateComponent currentState =
          stateEntity.fetch(ComputerStateComponent.class).orElseThrow();
      if (!currentState.door2Unlocked()) {
        replaceState(stateEntity, currentState.withDoor2Unlocked(true));
      }
    }
  }

  private static void handleVentilationAttempt(Entity stateEntity, String raw, Entity who) {
    ComputerStateComponent current = currentState(stateEntity).orElse(null);
    if (!controlPanelAvailable(current)) {
      return;
    }
    boolean correct = raw.equals(Lore.VentSerialNumber);
    LastHourTracking.attempt(
        LastHourPuzzle.VENTILATION, "ventilation-serial", "serial-number", raw, correct, who);
    if (correct) {
      ComputerStateComponent currentState =
          stateEntity.fetch(ComputerStateComponent.class).orElseThrow();
      if (!currentState.acVentConnected()) {
        replaceState(stateEntity, currentState.withAcVentConnected(true));
      }
    }
  }

  private static void infectComputer(Entity stateEntity, Entity who) {
    ComputerStateComponent current = currentState(stateEntity).orElse(null);
    if (current == null
        || current.isInfected()
        || !current.state().hasReached(ComputerProgress.LOGGED_IN)) {
      return;
    }
    String virusType =
        Lore.CodePageIndexToVirusType.get(
            (int) (Math.random() * Lore.CodePageIndexToVirusType.size()));
    replaceState(stateEntity, current.withVirusType(virusType).withInfection(true));
    LastHourTracking.started(LastHourPuzzle.VIRUS_NEUTRALIZATION);
    LastHourQuestLogUtil.addVirusWarningQuestLogEntry();
    onVirusTriggered.accept(who);
    Game.add(
        EmoteFactory.createEmote(
            LastHourLevel.getInstance().getPoint("pc-main").translate(1f, 1.5f),
            Emote.FACE_ANGRY,
            3000));
  }

  private static void neutralizeVirus(Entity stateEntity, String rawCode, Entity who) {
    ComputerStateComponent current = currentState(stateEntity).orElse(null);
    if (current == null || !current.isInfected()) {
      return;
    }
    String expected = Lore.VirusTypeToCode.get(current.virusType());
    boolean correct =
        expected != null
            && rawCode.replaceAll("\\s+", "").equalsIgnoreCase(expected.replaceAll("\\s+", ""));
    LastHourTracking.attempt(
        LastHourPuzzle.VIRUS_NEUTRALIZATION,
        VIRUS_CODE_OBJECT_ID,
        "security-code",
        rawCode,
        correct,
        who);
    if (!correct) {
      return;
    }
    replaceState(stateEntity, current.withInfection(false).withVirusType(null));
    LastHourTracking.solved(LastHourPuzzle.VIRUS_NEUTRALIZATION);
  }

  static void notifyVirusTriggered(Entity who) {
    onVirusTriggered.accept(who);
  }

  /** Resets the PC after an unknown-device infection reaches its shutdown deadline. */
  static void shutdownPcAfterUnknownDevice() {
    if (ComputerStateComponent.getState().isEmpty()) return;
    ComputerStateComponent.setInfection(false);
    ComputerStateComponent.setVirusType(null);
    ComputerStateComponent.setState(ComputerProgress.ON);
    if (!Game.isHeadless()) {
      ComputerStateLocal local = ComputerStateLocal.getInstance();
      local.username("");
      local.password("");
    }
  }

  private static void applyControlIntent(
      Entity stateEntity, UnaryOperator<ComputerStateComponent> transition) {
    ComputerStateComponent previous = currentState(stateEntity).orElse(null);
    if (!controlPanelAvailable(previous)) {
      return;
    }
    ComputerStateComponent next = transition.apply(previous);
    if (next.equals(previous)) {
      return;
    }
    replaceState(stateEntity, next);
    if (!PreRunConfiguration.multiplayerEnabled() || PreRunConfiguration.isNetworkServer()) {
      playControlPanelSounds(previous, next);
    }
  }

  private static boolean controlPanelAvailable(ComputerStateComponent state) {
    return state != null
        && state.usbInserted()
        && state.state().hasReached(ComputerProgress.LOGGED_IN)
        && !state.isInfected();
  }

  private static Optional<ComputerStateComponent> currentState(Entity stateEntity) {
    return stateEntity.fetch(ComputerStateComponent.class);
  }

  private static void replaceState(Entity stateEntity, ComputerStateComponent state) {
    stateEntity.remove(ComputerStateComponent.class);
    stateEntity.add(state);
  }

  private static Optional<String> responseString(Object data) {
    if (data instanceof DialogResponseMessage.StringValue(String value)) {
      return Optional.of(value);
    }
    return Optional.empty();
  }

  private static void playControlPanelSounds(
      ComputerStateComponent previousState, ComputerStateComponent newState) {
    if (previousState.lightsOn() != newState.lightsOn()) {
      Sounds.play(
          newState.lightsOn()
              ? LastHourSounds.ELECTRICITY_TURNED_ON
              : LastHourSounds.CONTROL_PANEL_LIGHTS_OFF);
    }
    if (previousState.door1Open() != newState.door1Open()) {
      Sounds.play(newState.door1Open() ? CoreSounds.DOOR_OPEN : CoreSounds.DOOR_CLOSE);
    }
    if (previousState.door2Open() != newState.door2Open()) {
      Sounds.play(newState.door2Open() ? CoreSounds.DOOR_OPEN : CoreSounds.DOOR_CLOSE);
    }
    if (previousState.acOn() != newState.acOn()) {
      Sounds.play(
          newState.acOn()
              ? LastHourSounds.CONTROL_PANEL_AC_ON
              : LastHourSounds.CONTROL_PANEL_AC_OFF);
    }
  }
}
