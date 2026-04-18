package contrib.systems;

import contrib.components.ShowImageComponent;
import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import contrib.hud.showimage.ShowImageText;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;

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
      Entity newOverlay = new Entity("show-image-overlay");

      var builder =
        DialogContext.builder()
          .type(DialogType.DefaultTypes.IMAGE)
          .put(DialogContextKeys.IMAGE, d.sic.imagePath())
          .put(DialogContextKeys.IMAGE_TRANSITION_SPEED, d.sic.transitionSpeed())
          .put(DialogContextKeys.IMAGE_MAX_SIZE, d.sic.maxSize())
          .put(DialogContextKeys.OWNER_ENTITY, newOverlay.id());

      ShowImageText textConfig = d.sic.textConfig();
      if (textConfig != null && textConfig.text() != null && !textConfig.text().isBlank()) {
        builder
          .put(DialogContextKeys.IMAGE_TEXT, textConfig.text())
          .put(DialogContextKeys.IMAGE_TEXT_SCALE, textConfig.scale())
          .put(DialogContextKeys.IMAGE_TEXT_COLOR_RGBA8888, textConfig.rgba8888Color());
      }

      DialogContext context = builder.build();

      UIComponent uic = new UIComponent(context, true);

      uic.onClose(
        _ -> {
          d.sic.isUIOpen(false);
          d.sic.onClose(d.e, newOverlay);
        });

      newOverlay.add(uic);
      d.sic.overlay(newOverlay);
      Game.add(newOverlay);
      d.sic.onOpen(d.e, newOverlay);

    } else if (overlay != null && !d.sic.isUIOpen()) {
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

  private record SIData(
    Entity e,
    ShowImageComponent sic,
    DrawComponent dc,
    PositionComponent pc) {}
}
