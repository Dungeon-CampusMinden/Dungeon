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
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.network.codec.DialogValueCodecRegistry;
import core.network.messages.c2s.DialogResponseMessage;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import level.LastHourLevel;
import modules.usbstick.UsbStickColor;
import modules.usbstick.UsbStickItem;
import util.Lore;

/** Factory class for creating and managing the computer dialog in the escape room level. */
public class ComputerFactory {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ComputerFactory.class);
  private static final String STATE_KEY = "computer_state";
  private static final String ACCESS_PC_LABEL = "Access PC. This will have wild implications idk I just want to test the line wrapping. So here is another sentence to make this really long.";

  /** Tracks whether the correct USB stick has already been inserted this session. */
  private static boolean usbAlreadyInserted = false;

  /** Key for updating the computer state from the dialog callbacks. */
  public static final String UPDATE_STATE_KEY = "update_state";

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
                      // Skip USB dialog if correct stick was already inserted or PC is infected
                      List<UsbStickItem.BaseUsbStick> usbSticks = findUsbSticks(who);
                      boolean isInfected =
                          ComputerStateComponent.getState()
                              .map(ComputerStateComponent::isInfected)
                              .orElse(false);
                      if (!usbAlreadyInserted && !isInfected && !usbSticks.isEmpty()) {
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
      options.add(ChoiceOption.of(color.displayName(), color.getTexturePath()));
    }
    options.add(ChoiceOption.of(ACCESS_PC_LABEL));

    DialogFactory.showMultipleChoiceDialog(
        "You are carrying USB sticks.\nWhat do you want to do?",
        "",
        "You can plug in a USB stick or just use the computer.",
        options,
        false,
        data -> {
          if (data instanceof DialogResponseMessage.StringValue(String val)) {
            if (val.equals(ACCESS_PC_LABEL)) {
              openComputerDialog(pcEntity, who);
            } else {
              // Find the selected USB stick by display name
              usbSticks.stream()
                  .filter(s -> s.color().displayName().equals(val))
                  .findFirst()
                  .ifPresent(stick -> onUsbStickInserted(stick, pcEntity, who));
            }
          }
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
      usbAlreadyInserted = true;
      openComputerDialog(pcEntity, who);
    } else {
      LOGGER.info(
          "Wrong USB stick inserted: " + stick.color().displayName() + " - triggering virus");
      // Pick a random virus type and infect the computer
      List<String> virusTypes = new ArrayList<>(Lore.VirusTypeToCode.keySet());
      String randomVirus = virusTypes.get(new java.util.Random().nextInt(virusTypes.size()));
      ComputerStateComponent.setInfection(true);
      ComputerStateComponent.setVirusType(randomVirus);
      DialogFactory.showOkDialog(
          "You plugged in the "
              + stick.color().displayName()
              + ".\n\n"
              + "The screen flickers and a warning appears:\n"
              + "\""
              + randomVirus
              + " detected! System locked.\"",
          "",
          () -> {},
          who.id());
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
                boolean isNowInfected = false;
                if (data
                    instanceof
                    ComputerStateComponent(
                        ComputerProgress computerState,
                        boolean isInfected,
                        String virusType,
                        int timestampOfLogin)) {
                  ComputerStateComponent.setState(computerState);
                  ComputerStateComponent.setInfection(isInfected);
                  ComputerStateComponent.setVirusType(virusType);
                  ComputerStateComponent.setTimestampOfLogin(timestampOfLogin);
                  isNowInfected = isInfected;
                } else if (data instanceof DialogResponseMessage.CustomPayload(var wrappedValue)) {
                  if (wrappedValue
                      instanceof
                      ComputerStateComponent(
                          ComputerProgress state1,
                          boolean isInfected,
                          String virusType,
                          int timestampOfLogin)) {
                    ComputerStateComponent.setState(state1);
                    ComputerStateComponent.setInfection(isInfected);
                    ComputerStateComponent.setVirusType(virusType);
                    ComputerStateComponent.setTimestampOfLogin(timestampOfLogin);
                    isNowInfected = isInfected;
                  }
                }

                if (isNowInfected) {
                  Game.add(
                      EmoteFactory.createEmote(
                          LastHourLevel.getInstance().getPoint("pc-main").translate(0.5f, 2f),
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
}
