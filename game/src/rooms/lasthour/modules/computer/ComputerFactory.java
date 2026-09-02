package rooms.lasthour.modules.computer;

import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.game.PreRunConfiguration;
import engine.network.codec.DialogValueCodecRegistry;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.logging.DungeonLogger;
import feature.components.InventoryComponent;
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
import java.util.Optional;
import java.util.Set;
import rooms.lasthour.level.LastHourLevel;
import rooms.lasthour.modules.usbstick.UsbStickColor;
import rooms.lasthour.modules.usbstick.UsbStickItem;
import rooms.lasthour.util.LastHourPuzzle;
import rooms.lasthour.util.LastHourQuestLogUtil;
import rooms.lasthour.util.LastHourTracking;
import rooms.lasthour.util.Lore;

/** Factory class for creating and managing the computer dialog in the escape room level. */
public class ComputerFactory {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ComputerFactory.class);
  private static final String STATE_KEY = "computer_state";
  private static final String ACCESS_PC_LABEL = "Just access the PC";

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
   * Attaches an interaction component to an entity that represents the computer.
   *
   * @param entity the entity to attach the interaction component to
   */
  public static void attachComputerDialog(Entity entity) {
    entity.add(
        new InteractionComponent(
            new Interaction(
                (eInteract, who) -> {
                  LastHourTracking.started(LastHourPuzzle.POWER);
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
                  if (!usbAlreadyInserted && !isInfected && isLoggedIn && !usbSticks.isEmpty()) {
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
        LastHourPuzzle.BLUE_USB,
        "usb-color",
        "color",
        stick.color().name(),
        stick.color() == UsbStickColor.Blue,
        who);
    if (stick.color() == UsbStickColor.Blue) {
      LastHourTracking.solved(LastHourPuzzle.BLUE_USB);
      LOGGER.info("Correct USB stick inserted: " + stick.color().displayName());
      LastHourQuestLogUtil.addUsbUsedQuestLogEntry();
      LastHourQuestLogUtil.addUsbRecoveredDataQuestLogEntry();
      // Remove the stick from inventory and mark as inserted
      who.fetch(InventoryComponent.class).ifPresent(inv -> inv.removeOne(stick));
      ComputerStateComponent.setUsbInserted(true);
      LastHourTracking.started(LastHourPuzzle.VENTILATION);
      openComputerDialog(pcEntity, who);
    } else {
      LOGGER.info(
          "Wrong USB stick inserted: " + stick.color().displayName() + " - triggering virus");
      LastHourQuestLogUtil.addUsbUsedQuestLogEntry();
      LastHourQuestLogUtil.addVirusWarningQuestLogEntry();
      ComputerCallbacks.notifyVirusTriggered(who);
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
            ComputerCallbacks::shutdownPcAfterUnknownDevice,
            ComputerCallbacks.UNKNOWN_DEVICE_SHUTDOWN_DELAY_MS);
      }
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
          ComputerCallbacks.registerCallbacks(computerDialogInstance, stateEntity, who);
        });
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
}
