package modules.computer;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.HeadlessDialogGroup;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.Game;

import java.util.Optional;
import java.util.Set;

public class ComputerFactory {

  private static final String STATE_KEY = "computer_state";
  public static UIComponent computerDialogInstance;


  static {
    DialogFactory.register(LastHourDialogTypes.COMPUTER, ComputerFactory::build);
  }

  public static void attachComputerDialog(Entity entity){
    entity.add(new InteractionComponent(() -> new Interaction((eInteract, who) -> {
      DialogContext.Builder builder = DialogContext.builder();
      builder.type(LastHourDialogTypes.COMPUTER);

      Optional<Entity> e = Game.levelEntities(Set.of(ComputerStateComponent.class)).findFirst();
      e.ifPresent(stateEntity -> {
        ComputerStateComponent state = stateEntity.fetch(ComputerStateComponent.class).orElseThrow();
        builder.put(STATE_KEY, state);
        computerDialogInstance = DialogFactory.show(builder.build(), who.id());
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
    return create(state.orElseThrow());
  }

  private static Group create(ComputerStateComponent state){
    return new ComputerDialog(state);
  }

}
