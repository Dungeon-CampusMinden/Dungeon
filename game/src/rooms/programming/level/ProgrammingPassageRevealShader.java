package rooms.programming.level;

import engine.Game;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.components.draw.shader.LevelHideShader;
import feature.systems.LevelEditorSystem;
import java.util.List;

/** Keeps the next area black until the synchronized masonry gate actually opens. */
final class ProgrammingPassageRevealShader extends LevelHideShader {
  private final Point gate;

  ProgrammingPassageRevealShader(Point start, Point end, Point gate) {
    super(true, new Rectangle(end.x() - start.x(), end.y() - start.y(), start.x(), start.y()));
    this.gate = gate;
  }

  @Override
  public boolean enabled() {
    return super.enabled() && !LevelEditorSystem.active();
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    boolean closed =
        Game.currentLevel()
            .flatMap(level -> level.tileAt(gate))
            .filter(DoorTile.class::isInstance)
            .map(DoorTile.class::cast)
            .map(door -> !door.isOpen())
            .orElse(true);
    if (hiding() != closed) hiding(closed);
    return super.getUniforms(actualUpscale);
  }
}
