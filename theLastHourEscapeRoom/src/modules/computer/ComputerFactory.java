package modules.computer;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.HeadlessDialogGroup;
import contrib.modules.interaction.ISimpleIInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.Game;

import java.util.Optional;
import java.util.Set;

public class ComputerFactory {

  static {
    DialogFactory.register(LastHourDialogTypes.COMPUTER, ComputerFactory::build);
  }

  public static void attachComputerDialog(Entity entity){
    entity.add(new InteractionComponent(() -> new Interaction((e, who) -> {
      DialogFactory.show(DialogContext.builder().type(LastHourDialogTypes.COMPUTER).build());
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

    return create();
  }

  private static Group create(){
    Optional<Entity> e = Game.levelEntities(Set.of(ComputerStateComponent.class)).findFirst();
    ComputerStateComponent state = e.flatMap(t -> t.fetch(ComputerStateComponent.class)).orElseThrow();
    return new ComputerDialog(state);
  }

}
