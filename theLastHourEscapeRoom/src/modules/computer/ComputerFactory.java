package modules.computer;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.components.InventoryComponent;
import contrib.hud.dialogs.ChoiceOption;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.HeadlessDialogGroup;
import contrib.modules.emote.Emote;
import contrib.modules.emote.EmoteFactory;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.systems.EventScheduler;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.game.PreRunConfiguration;
import core.network.codec.DialogValueCodecRegistry;
import core.network.messages.c2s.DialogResponseMessage;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import level.LastHourLevel;
import modules.usbstick.UsbStickColor;
import modules.usbstick.UsbStickItem;
import util.LastHourSounds;
import util.Lore;

/** Factory class for creating and managing the computer dialog in the escape room level. */
public class ComputerFactory {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ComputerFactory.class);
  private static final String STATE_KEY = "computer_state";
  private static final String ACCESS_PC_LABEL = "Just access the PC";


  /** Key for updating the computer state from the dialog callbacks. */
  public static final String UPDATE_STATE_KEY = "update_state";

  /** Delay in milliseconds between triggering the unknown-device virus and the forced shutdown. */
  public static final long UNKNOWN_DEVICE_SHUTDOWN_DELAY_MS = 10_000L;

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
            () ->
                new Interaction(
                    (eInteract, who) -> {
                      DrawComponent dc = entity.fetch(DrawComponent.class).orElseThrow();
                      if (dc.currentStateName().equals(LastHourLevel.PC_STATE_OFF)) {
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
    if (stick.color() == UsbStickColor.Blue) {
      LOGGER.info("Correct USB stick inserted: " + stick.color().displayName());
      // Remove the stick from inventory and mark as inserted
      who.fetch(InventoryComponent.class).ifPresent(inv -> inv.removeOne(stick));
      ComputerStateComponent.setUsbInserted(true);
      openComputerDialog(pcEntity, who);
    } else {
      LOGGER.info(
          "Wrong USB stick inserted: " + stick.color().displayName() + " - triggering virus");
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
          ComputerStateComponent state =
              stateEntity.fetch(ComputerStateComponent.class).orElseThrow();
          builder.put(STATE_KEY, state);
          var computerDialogInstance = DialogFactory.show(builder.build(), who.id());
          computerDialogInstance.registerCallback(
              UPDATE_STATE_KEY,
              data -> {
                ComputerStateComponent newState = null;
                if (data instanceof ComputerStateComponent csc) {
                  newState = csc;
                } else if (data instanceof DialogResponseMessage.CustomPayload(var wrappedValue)
                    && wrappedValue instanceof ComputerStateComponent csc) {
                  newState = csc;
                }
                if (newState == null) return;

                ComputerStateComponent previousState =
                    stateEntity.fetch(ComputerStateComponent.class).orElse(null);
                if (previousState == null) return;

                boolean wasInfected =
                    ComputerStateComponent.getState().map(ComputerStateComponent::isInfected).orElse(false);
                stateEntity.remove(ComputerStateComponent.class);
                stateEntity.add(newState);

                // In multiplayer, only the server should emit sounds so clients receive them via
                // sound network messages from the authoritative callback execution.
                if (!PreRunConfiguration.multiplayerEnabled()
                    || PreRunConfiguration.isNetworkServer()) {
                  playControlPanelSounds(previousState, newState);
                }

                if (newState.isInfected() && !wasInfected) {
                  Game.add(
                      EmoteFactory.createEmote(
                          LastHourLevel.getInstance().getPoint("pc-main").translate(1f, 1.5f),
                          Emote.FACE_ANGRY,
                          3000));
                }
              });
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
      Sounds.play(newState.acOn() ? LastHourSounds.CONTROL_PANEL_AC_ON : LastHourSounds.CONTROL_PANEL_AC_OFF);
    }
  }
}
