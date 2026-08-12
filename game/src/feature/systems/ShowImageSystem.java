package feature.systems;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import feature.components.ShowImageComponent;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogType;

/**
 * System that handles showing images in fullscreen when an entity with a ShowImageComponent is
 * interacted with.
 */
public class ShowImageSystem extends System {

  /**
   * Creates a new ShowImageSystem that processes entities with ShowImageComponent, DrawComponent,
   * and PositionComponent.
   */
  public ShowImageSystem() {
    super(ShowImageComponent.class, DrawComponent.class, PositionComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream(ShowImageComponent.class, DrawComponent.class, PositionComponent.class)
        .map(this::buildDataObject)
        .forEach(this::execute);
  }

  /**
   * Executes the system logic.
   *
   * @param d the data object containing the entity and its components
   */
  public void execute(SIData d) {
    Entity overlay = d.sic.overlay();

    if (overlay == null && d.sic.isUIOpen()) {
      // Dialog is closed but should be open
      Entity newOverlay = new Entity("show-image-overlay");
      DialogContext context =
          DialogContext.builder()
              .type(DialogType.DefaultTypes.IMAGE)
              .put(DialogContextKeys.IMAGE, d.sic.imagePath())
              .put(DialogContextKeys.OWNER_ENTITY, newOverlay.id())
              .build();
      UIComponent uic = new UIComponent(context, true);
      // Register close callback
      uic.registerCallback(
          "onClose",
          data -> {
            d.sic.isUIOpen(false);
            d.sic.onClose(d.e, newOverlay);
          });
      newOverlay.add(uic);
      d.sic.overlay(newOverlay);
      Game.add(newOverlay);
      d.sic.onOpen(d.e, newOverlay);

    } else if (overlay != null && !d.sic.isUIOpen()) {
      // Dialog is open but should be closed
      Game.remove(overlay);
      d.sic.overlay(null);
    }
  }

  private SIData buildDataObject(Entity e) {
    return new SIData(
        e,
        e.fetch(ShowImageComponent.class).orElseThrow(),
        e.fetch(DrawComponent.class).orElseThrow(),
        e.fetch(PositionComponent.class).orElseThrow());
  }

  private record SIData(Entity e, ShowImageComponent sic, DrawComponent dc, PositionComponent pc) {}
}
