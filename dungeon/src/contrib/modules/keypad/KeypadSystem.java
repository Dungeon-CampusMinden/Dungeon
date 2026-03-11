package contrib.modules.keypad;

import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.network.messages.c2s.DialogResponseMessage;

/**
 * System that manages the opening and closing of keypad UIs based on the state of KeypadComponents.
 */
public class KeypadSystem extends System {

  /** Creates a new KeypadSystem. */
  public KeypadSystem() {
    super(KeypadComponent.class, DrawComponent.class, PositionComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream(KeypadComponent.class, DrawComponent.class, PositionComponent.class)
        .map(Data::of)
        .forEach(this::execute);
  }

  private void execute(Data data) {
    Entity overlay = data.kc.overlay();

    if (overlay == null && data.kc.isUIOpen()) {
      // Dialog is closed but should be open
      Entity newOverlay = new Entity("keypad-overlay");
      DialogContext context =
          DialogContext.builder()
              .type(DialogType.DefaultTypes.KEYPAD)
              .put(DialogContextKeys.ENTITY, data.e.id())
              .put(DialogContextKeys.OWNER_ENTITY, newOverlay.id())
              .build();
      UIComponent uic = new UIComponent(context, true);
      uic.registerCallback(DialogContextKeys.ON_CLOSE, (ignored) -> data.kc.isUIOpen(false));
      newOverlay.add(uic);
      data.kc.overlay(newOverlay);
      Game.add(newOverlay);

      uic.registerCallback(
          DialogContextKeys.ON_CONFIRM,
          (payload) -> {
            if (payload instanceof DialogResponseMessage.StringValue(String value)) {
              KeypadUI.onButtonPress(data.e, value);
            }
          });

    } else if (overlay != null && !data.kc.isUIOpen()) {
      // Dialog is open but should be closed
      Game.remove(overlay);
      data.kc.overlay(null);
    }
  }

  private record Data(Entity e, KeypadComponent kc, DrawComponent dc, PositionComponent pc) {
    static Data of(Entity e) {
      return new Data(
          e,
          e.fetch(KeypadComponent.class).orElseThrow(),
          e.fetch(DrawComponent.class).orElseThrow(),
          e.fetch(PositionComponent.class).orElseThrow());
    }
  }
}
