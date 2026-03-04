package modules.computer;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.HeadlessDialogGroup;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.network.codec.DialogValueCodecRegistry;
import core.network.messages.c2s.DialogResponseMessage;
import java.util.Optional;
import java.util.Set;
import level.LastHourLevel;
import util.Lore;

/** Factory class for creating and managing the computer dialog in the escape room level. */
public class ComputerFactory {

  private static final String STATE_KEY = "computer_state";

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

                      DialogContext.Builder builder = DialogContext.builder();
                      builder.type(LastHourDialogTypes.COMPUTER);

                      Optional<Entity> e =
                          Game.levelEntities(Set.of(ComputerStateComponent.class)).findFirst();
                      e.ifPresent(
                          stateEntity -> {
                            ComputerStateComponent state =
                                stateEntity.fetch(ComputerStateComponent.class).orElseThrow();
                            builder.put(STATE_KEY, state);
                            var computerDialogInstance =
                                DialogFactory.show(builder.build(), who.id());
                            computerDialogInstance.registerCallback(
                                UPDATE_STATE_KEY,
                                data -> {
                                  if (data
                                      instanceof
                                      ComputerStateComponent(
                                          ComputerProgress computerState,
                                          boolean isInfected,
                                          String virusType)) {
                                    ComputerStateComponent.setState(computerState);
                                    ComputerStateComponent.setInfection(isInfected);
                                    ComputerStateComponent.setVirusType(virusType);
                                  } else if (data
                                      instanceof
                                      DialogResponseMessage.CustomPayload(var wrappedValue)) {
                                    if (wrappedValue
                                        instanceof
                                        ComputerStateComponent(
                                            ComputerProgress state1,
                                            boolean isInfected,
                                            String virusType)) {
                                      ComputerStateComponent.setState(state1);
                                      ComputerStateComponent.setInfection(isInfected);
                                      ComputerStateComponent.setVirusType(virusType);
                                    }
                                  }
                                });
                          });
                    })));
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
