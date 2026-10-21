package rooms.soulweaver.level;

import engine.Game;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.components.draw.shader.LevelHideShader;
import feature.systems.LevelEditorSystem;
import java.util.List;

/** Keeps the next area black until the synchronized masonry gate actually opens. */
final class SoulweaverPassageRevealShader extends LevelHideShader {
  private final Point gate;

  SoulweaverPassageRevealShader(Point start, Point end, Point gate) {
    super(true, new Rectangle(end.x() - start.x(), end.y() - start.y(), start.x(), start.y()));
    this.gate = gate;
  }

  @Override
  public boolean enabled() {
    return super.enabled() && !LevelEditorSystem.active();
  }

  /**
   * Whether the gate is still closed; follows the gate as soon as anything asks, even while the
   * area is off screen, so the light inside it comes on with the opening gate.
   *
   * @return whether the area is hidden
   */
  @Override
  public boolean hiding() {
    boolean closed =
        Game.currentLevel()
            .flatMap(level -> level.tileAt(gate))
            .filter(DoorTile.class::isInstance)
            .map(DoorTile.class::cast)
            .map(door -> !door.isOpen())
            .orElse(true);
    if (super.hiding() != closed) hiding(closed);
    return closed;
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    hiding();
    return super.getUniforms(actualUpscale);
  }
}
