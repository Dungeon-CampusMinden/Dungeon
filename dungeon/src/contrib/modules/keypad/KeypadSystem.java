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

  private void execute(Data d) {
    Entity overlay = d.kc.overlay();

    if (overlay == null && d.kc.isUIOpen()) {
      // Dialog is closed but should be open
      Entity newOverlay = new Entity("keypad-overlay");
      DialogContext context =
          DialogContext.builder()
              .type(DialogType.DefaultTypes.KEYPAD)
              .put(DialogContextKeys.ENTITY, d.e.id())
              .put(DialogContextKeys.OWNER_ENTITY, newOverlay.id())
              .build();
      UIComponent uic = new UIComponent(context, true);
      uic.registerCallback(DialogContextKeys.ON_CLOSE, (ignored) -> d.kc.isUIOpen(false));
      newOverlay.add(uic);
      d.kc.overlay(newOverlay);
      Game.add(newOverlay);

    } else if (overlay != null && !d.kc.isUIOpen()) {
      // Dialog is open but should be closed
      Game.remove(overlay);
      d.kc.overlay(null);
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
