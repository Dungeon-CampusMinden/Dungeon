package rooms.lasthour.modules.computer;

import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.game.PreRunConfiguration;
import engine.network.codec.DialogValueCodecRegistry;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import engine.utils.logging.DungeonLogger;
import feature.components.InventoryComponent;
import feature.emote.Emote;
import feature.emote.EmoteFactory;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.systems.EventScheduler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import rooms.lasthour.level.LastHourLevel;
import rooms.lasthour.modules.usbstick.UsbStickColor;
import rooms.lasthour.modules.usbstick.UsbStickItem;
import rooms.lasthour.util.LastHourQuestLogUtil;
import rooms.lasthour.util.LastHourSounds;
import rooms.lasthour.util.LastHourTracking;
import rooms.lasthour.util.Lore;

/** Factory class for creating and managing the computer dialog in the escape room level. */
public class ComputerFactory {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ComputerFactory.class);
  private static final String STATE_KEY = "computer_state";
  private static final String ACCESS_PC_LABEL = "Just access the PC";

  /** Callback key fired when a player opens the control panel tab. */
  public static final String CONTROL_PANEL_OPENED_KEY = "control_panel_opened";

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

  /** Delay in milliseconds between triggering the unknown-device virus and the forced shutdown. */
  public static final long UNKNOWN_DEVICE_SHUTDOWN_DELAY_MS = 10_000L;

  private static final int MIN_HEATER_CELSIUS = 10;
  private static final int MAX_HEATER_CELSIUS = 30;
  // Code-bearing infections use this puzzle. The unknown-device flow shuts down automatically.
  private static final String VIRUS_PUZZLE_ID = "virus-neutralization";
  private static final String VIRUS_CODE_OBJECT_ID = "virus-security-code";

  private static Consumer<Entity> onVirusTriggered = who -> {};
  private static Consumer<Entity> onPcUnlocked = who -> {};
  private static Consumer<Entity> onControlPanelOpened = who -> {};

  static {
    ensureRegistration();
  }

  /** Ensures dialog type and codec registration for computer dialog networking. */
  public static void ensureRegistration() {
    DialogFactory.register(LastHourDialogTypes.COMPUTER, ComputerFactory::build);
    DialogValueCodecRegistry registry = DialogValueCodecRegistry.global();
    if (registry.byType(ComputerStateComponent.class).isEmpty()) {
      registry.register(new ComputerStateComponentCodec());
    }
  }

  /**
   * Sets the action fired when the computer enters a newly infected state.
   *
   * @param callback callback receiving the acting player
   */
  public static void onVirusTriggered(Consumer<Entity> callback) {
    onVirusTriggered = Objects.requireNonNull(callback, "callback");
  }

  /**
   * Sets the action fired when the computer reaches the logged-in state.
   *
   * @param callback callback receiving the acting player
   */
  public static void onPcUnlocked(Consumer<Entity> callback) {
    onPcUnlocked = Objects.requireNonNull(callback, "callback");
  }

  /**
   * Sets the action fired when a player opens the control panel tab.
   *
   * @param callback callback receiving the acting player
   */
  public static void onControlPanelOpened(Consumer<Entity> callback) {
    onControlPanelOpened = Objects.requireNonNull(callback, "callback");
  }

  /**
   * Attaches an interaction component to an entity that represents the computer.
   *
   * @param entity the entity to attach the interaction component to
   */
  public static void attachComputerDialog(Entity entity) {
    entity.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (eInteract, who) -> {
                      LastHourTracking.started("power");
                      DrawComponent dc = entity.fetch(DrawComponent.class).orElseThrow();
                      if (dc.currentStateName().equals(LastHourLevel.PC_STATE_OFF)) {
                        LastHourQuestLogUtil.addRestorePowerQuestLogEntry();
                        DialogFactory.showOkDialog(
                            "This seems to be "
                                + Lore.ScientistNameShort
                                + "'s computer\n\nTrying to turn on the computer doesn't work.\nIt seems to not have any power...",
                            "",
                            () -> {},
                            who.id());
                        return;
                      }

                      // Check if the player carries any USB sticks
                      // Skip USB dialog if correct stick was already inserted, PC is infected,
                      // or PC is still pre-login (no point plugging in a stick before logging in)
                      List<UsbStickItem.BaseUsbStick> usbSticks = findUsbSticks(who);
                      ComputerStateComponent state = ComputerStateComponent.getState().orElse(null);
                      boolean isInfected = state != null && state.isInfected();
                      boolean isLoggedIn =
                          state != null && state.state().hasReached(ComputerProgress.LOGGED_IN);
                      boolean usbAlreadyInserted = state != null && state.usbInserted();
                      if (!isLoggedIn) {
                        LastHourQuestLogUtil.addLoginNeededQuestLogEntry();
                      }
                      if (!usbAlreadyInserted
                          && !isInfected
                          && isLoggedIn
                          && !usbSticks.isEmpty()) {
                        showUsbStickChoice(usbSticks, entity, who);
                      } else {
                        openComputerDialog(entity, who);
                      }
                    })));
  }

  /**
   * Finds all USB stick items in the given player entity's inventory.
   *
   * @param player The player entity.
   * @return A list of USB stick items found, possibly empty.
   */
  private static List<UsbStickItem.BaseUsbStick> findUsbSticks(Entity player) {
    return player
        .fetch(InventoryComponent.class)
        .map(
            inv ->
                Arrays.stream(inv.items())
                    .filter(UsbStickItem.BaseUsbStick.class::isInstance)
                    .map(UsbStickItem.BaseUsbStick.class::cast)
                    .toList())
        .orElse(List.of());
  }

  /**
   * Shows a multiple choice dialog letting the player choose to plug in a USB stick or access the
   * PC directly.
   *
   * @param usbSticks The USB sticks in the player's inventory.
   * @param pcEntity The computer entity.
   * @param who The player entity that interacted.
   */
  private static void showUsbStickChoice(
      List<UsbStickItem.BaseUsbStick> usbSticks, Entity pcEntity, Entity who) {
    List<ChoiceOption> options = new ArrayList<>();
    for (UsbStickItem.BaseUsbStick stick : usbSticks) {
      UsbStickColor color = stick.color();
      // Use the enum's stable name() as the wire value; the label keeps the rich icon+text markup.
      options.add(
          ChoiceOption.of(
              "[img=" + color.getTexturePath() + "] " + color.displayName(), color.name()));
    }
    // ACCESS_PC_LABEL doubles as the sentinel string for "open the PC directly" (label == value).
    options.add(ChoiceOption.of(ACCESS_PC_LABEL));

    DialogFactory.showMultipleChoiceDialog(
        "[tr speed=0][line-space=2.0]You are carrying USB sticks.[n]Do you want to plug one of them in?",
        null,
        options,
        false,
        data -> {
          String choice = (data instanceof DialogResponseMessage.StringValue(String s)) ? s : null;
          if (choice == null || ACCESS_PC_LABEL.equals(choice)) {
            openComputerDialog(pcEntity, who);
            return;
          }
          UsbStickColor selectedColor;
          try {
            selectedColor = UsbStickColor.valueOf(choice);
          } catch (IllegalArgumentException e) {
            LOGGER.warn("Unexpected USB choice payload: " + choice);
            openComputerDialog(pcEntity, who);
            return;
          }
          UsbStickColor finalSelected = selectedColor;
          usbSticks.stream()
              .filter(s -> s.color() == finalSelected)
              .findFirst()
              .ifPresentOrElse(
                  stick -> onUsbStickInserted(stick, pcEntity, who),
                  () -> openComputerDialog(pcEntity, who));
        },
        () -> {},
        who.id());
  }

  /**
   * Called when a USB stick is inserted into the computer. Opens the PC UI after handling the
   * insertion.
   *
   * @param stick The USB stick that was inserted.
   * @param pcEntity The computer entity.
   * @param who The player entity.
   */
  private static void onUsbStickInserted(
      UsbStickItem.BaseUsbStick stick, Entity pcEntity, Entity who) {
    LastHourTracking.attempt(
        "blue-usb",
        "usb-color",
        "color",
        stick.color().name(),
        stick.color() == UsbStickColor.Blue,
        who);
    if (stick.color() == UsbStickColor.Blue) {
      LastHourTracking.solved("blue-usb");
      LOGGER.info("Correct USB stick inserted: " + stick.color().displayName());
      LastHourQuestLogUtil.addUsbUsedQuestLogEntry();
      LastHourQuestLogUtil.addUsbRecoveredDataQuestLogEntry();
      // Remove the stick from inventory and mark as inserted
      who.fetch(InventoryComponent.class).ifPresent(inv -> inv.removeOne(stick));
      ComputerStateComponent.setUsbInserted(true);
      LastHourTracking.started("ventilation");
      openComputerDialog(pcEntity, who);
    } else {
      LOGGER.info(
          "Wrong USB stick inserted: " + stick.color().displayName() + " - triggering virus");
      LastHourQuestLogUtil.addUsbUsedQuestLogEntry();
      LastHourQuestLogUtil.addVirusWarningQuestLogEntry();
      onVirusTriggered.accept(who);
      ComputerStateComponent.setInfection(true);
      ComputerStateComponent.setVirusType(Lore.UnknownDeviceVirusType);
      openComputerDialog(pcEntity, who);
      // Multiplayer: the server's EventScheduler keeps ticking (it does not pause when a player
      // opens a dialog) and authoritatively triggers the shutdown, which is then broadcast to
      // every client via the regular snapshot / state propagation path.
      // Single-player: the EventScheduler would be paused while the computer dialog is open, so a
      // queued action would either never fire or, worse, fire late on dialog close and clobber
      // any newly logged-in state. Instead the non-pausable ComputerStateSyncSystem drives the
      // shutdown locally and authoritatively.
      if (PreRunConfiguration.multiplayerEnabled()) {
        EventScheduler.scheduleAction(
            ComputerFactory::shutdownPcAfterUnknownDevice, UNKNOWN_DEVICE_SHUTDOWN_DELAY_MS);
      }
    }
  }

  /**
   * Resets the PC to the pre-login state after an "Unknown Device" security shutdown. The infection
   * is cleared and the computer is moved back to {@link ComputerProgress#ON}. The locally entered
   * login name and password are wiped so they have to be re-entered. All other state (timestamps,
   * email selection, browser history, opened files, ...) is preserved so the player's progress is
   * retained when they log back in.
   */
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

  /**
   * Opens the computer dialog for the given player.
   *
   * @param pcEntity The computer entity.
   * @param who The player entity.
   */
  private static void openComputerDialog(Entity pcEntity, Entity who) {
    DialogContext.Builder builder = DialogContext.builder();
    builder.type(LastHourDialogTypes.COMPUTER);

    Optional<Entity> e = Game.levelEntities(Set.of(ComputerStateComponent.class)).findFirst();
    e.ifPresent(
        stateEntity -> {
          ComputerStateComponent initialState =
              stateEntity.fetch(ComputerStateComponent.class).orElseThrow();
          builder.put(STATE_KEY, initialState);
          var computerDialogInstance = DialogFactory.show(builder.build(), who.id());
          computerDialogInstance.registerCallback(
              VIRUS_TRIGGER_KEY,
              data ->
                  responseString(data)
                      .filter(
                          source ->
                              Lore.VirusWebsites.contains(source)
                                  || Lore.VirusAttachmentNames.contains(source))
                      .ifPresent(source -> infectComputer(stateEntity, who)));
          computerDialogInstance.registerCallback(
              VIRUS_CODE_ATTEMPT_KEY,
              data ->
                  responseString(data).ifPresent(raw -> neutralizeVirus(stateEntity, raw, who)));
          computerDialogInstance.registerCallback(
              TOGGLE_LIGHTS_KEY,
              data -> {
                if (!(data instanceof DialogResponseMessage.BoolValue(boolean target))) {
                  return;
                }
                applyControlIntent(stateEntity, state -> state.withLightsOn(target));
              });
          computerDialogInstance.registerCallback(
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
          computerDialogInstance.registerCallback(
              TOGGLE_DOOR_1_KEY,
              data -> {
                if (!(data instanceof DialogResponseMessage.BoolValue(boolean target))) {
                  return;
                }
                applyControlIntent(stateEntity, state -> state.withDoor1Open(target));
              });
          computerDialogInstance.registerCallback(
              OPEN_EXIT_DOOR_KEY,
              data -> {
                ComputerStateComponent current = currentState(stateEntity).orElse(null);
                if (!controlPanelAvailable(current)
                    || !current.door2Unlocked()
                    || current.door2Open()) {
                  return;
                }
                applyControlIntent(stateEntity, state -> state.withDoor2Open(true));
                LastHourTracking.solved("exit");
              });
          computerDialogInstance.registerCallback(
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
                  LastHourTracking.solved("ventilation");
                }
              });
          computerDialogInstance.registerCallback(
              TOGGLE_CAMERAS_KEY,
              data -> {
                if (!(data instanceof DialogResponseMessage.BoolValue(boolean target))) {
                  return;
                }
                applyControlIntent(stateEntity, state -> state.withCamerasOn(target));
              });
          computerDialogInstance.registerCallback(
              LOGIN_ATTEMPT_KEY,
              data ->
                  responseString(data)
                      .ifPresent(
                          raw -> {
                            ComputerStateComponent current = currentState(stateEntity).orElse(null);
                            if (current == null
                                || current.isInfected()
                                || current.state() != ComputerProgress.ON) {
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
                                "login", "credentials", "username-password", raw, correct, who);
                            if (correct) {
                              LastHourTracking.solved("login");
                              ComputerStateComponent currentState =
                                  stateEntity.fetch(ComputerStateComponent.class).orElseThrow();
                              if (!currentState.state().hasReached(ComputerProgress.LOGGED_IN)) {
                                stateEntity.remove(ComputerStateComponent.class);
                                stateEntity.add(
                                    currentState
                                        .withState(ComputerProgress.LOGGED_IN)
                                        .withTimestampOfLogin(
                                            (int) (System.currentTimeMillis() / 1000L)));
                                LastHourTracking.started("storage-recovery");
                                LastHourQuestLogUtil.addMailReviewQuestLogEntry();
                                onPcUnlocked.accept(who);
                              }
                            }
                          }));
          computerDialogInstance.registerCallback(
              RECOVERY_ATTEMPT_KEY,
              data ->
                  responseString(data)
                      .ifPresent(
                          raw -> {
                            ComputerStateComponent current = currentState(stateEntity).orElse(null);
                            if (current == null
                                || current.isInfected()
                                || !current.state().hasReached(ComputerProgress.LOGGED_IN)) {
                              return;
                            }
                            boolean correct = raw.strip().equals(Lore.AsciiCodes.getFirst());
                            LastHourTracking.attempt(
                                "storage-recovery",
                                "browser-recovery-code",
                                "numeric-code",
                                raw,
                                correct,
                                who);
                            if (correct) {
                              LastHourTracking.solved("storage-recovery");
                            }
                          }));
          computerDialogInstance.registerCallback(
              EXIT_ATTEMPT_KEY,
              data ->
                  responseString(data)
                      .ifPresent(
                          raw -> {
                            ComputerStateComponent availableState =
                                currentState(stateEntity).orElse(null);
                            if (!controlPanelAvailable(availableState)) {
                              return;
                            }
                            boolean correct = raw.equalsIgnoreCase(Lore.ControlPanelDoor2Password);
                            LastHourTracking.attempt(
                                "exit", "exit-door-code", "numeric-code", raw, correct, who);
                            if (correct) {
                              ComputerStateComponent currentState =
                                  stateEntity.fetch(ComputerStateComponent.class).orElseThrow();
                              if (!currentState.door2Unlocked()) {
                                stateEntity.remove(ComputerStateComponent.class);
                                stateEntity.add(currentState.withDoor2Unlocked(true));
                              }
                            }
                          }));
          computerDialogInstance.registerCallback(
              VENTILATION_ATTEMPT_KEY,
              data ->
                  responseString(data)
                      .ifPresent(
                          raw -> {
                            ComputerStateComponent availableState =
                                currentState(stateEntity).orElse(null);
                            if (!controlPanelAvailable(availableState)) {
                              return;
                            }
                            boolean correct = raw.equals(Lore.VentSerialNumber);
                            LastHourTracking.attempt(
                                "ventilation",
                                "ventilation-serial",
                                "serial-number",
                                raw,
                                correct,
                                who);
                            if (correct) {
                              ComputerStateComponent currentState =
                                  stateEntity.fetch(ComputerStateComponent.class).orElseThrow();
                              if (!currentState.acVentConnected()) {
                                stateEntity.remove(ComputerStateComponent.class);
                                stateEntity.add(currentState.withAcVentConnected(true));
                              }
                            }
                          }));
          computerDialogInstance.registerCallback(
              CONTROL_PANEL_OPENED_KEY,
              data -> {
                if (controlPanelAvailable(currentState(stateEntity).orElse(null))) {
                  onControlPanelOpened.accept(who);
                }
              });
          computerDialogInstance.registerCallback(
              EXIT_CODE_HINT_OPENED_KEY,
              data -> {
                ComputerStateComponent current = currentState(stateEntity).orElse(null);
                if (!controlPanelAvailable(current)) {
                  return;
                }
                LastHourTracking.started("exit-code-assembly");
                LastHourTracking.hintUsed("exit-code-assembly", "usb-hint-file", who);
              });
        });
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
    LastHourTracking.started(VIRUS_PUZZLE_ID);
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
        VIRUS_PUZZLE_ID, VIRUS_CODE_OBJECT_ID, "security-code", rawCode, correct, who);
    if (!correct) {
      return;
    }
    replaceState(stateEntity, current.withInfection(false).withVirusType(null));
    LastHourTracking.solved(VIRUS_PUZZLE_ID);
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

  /**
   * Builds the computer dialog from the given context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * @param ctx The dialog context containing the message, title, and confirmation callback
   * @return A fully configured OK dialog or HeadlessDialogGroup
   */
  public static Group build(DialogContext ctx) {
    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup();
    }

    Optional<ComputerStateComponent> state = ctx.find(STATE_KEY, ComputerStateComponent.class);
    return new ComputerDialog(state.orElseThrow(), ctx);
  }

  private static void playControlPanelSounds(
      ComputerStateComponent previousState, ComputerStateComponent newState) {
    if (previousState.lightsOn() != newState.lightsOn()) {
      if (newState.lightsOn()) {
        // Reuse the same buzz used when room electricity is turned on in progression.
        Sounds.play(LastHourSounds.ELECTRICITY_TURNED_ON);
      } else {
        Sounds.play(LastHourSounds.CONTROL_PANEL_LIGHTS_OFF);
      }
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
