package contrib.systems;

import contrib.components.ShowImageComponent;
import contrib.hud.DialogUtils;
import core.Entity;
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

  private void execute(SIData d) {
    if (!d.sic.isUIOpen()) {
      return;
    }

    d.sic.isUIOpen(false);
    DialogUtils.showImagePopUp(
        d.sic.imagePath(), d.sic.transitionSpeed(), () -> d.sic.onClose(d.e, d.e));
    d.sic.onOpen(d.e, d.e);
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
