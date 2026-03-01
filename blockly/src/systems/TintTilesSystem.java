package systems;

import components.TintDirectionComponent;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.Coordinate;
import core.utils.Direction;
import core.utils.components.MissingComponentException;
import entities.monster.StraightRangeAI;
import java.util.HashSet;
import java.util.Set;

/**
 * The TintTilesSystem is responsible for applying a tinting effect to tiles in the range of an
 * entity.
 *
 * <p>Entities with the {@link TintDirectionComponent} will be processed by this system.
 *
 * @see TintDirectionComponent
 * @see StraightRangeAI
 */
public class TintTilesSystem extends System {

  /**
   * Creates a new TintTilesSystem.
   *
   * <p>This system processes all entities with a {@link TintDirectionComponent} and applies the
   * tinting effect to the tiles in the range of the entity.
   *
   * <p>It also removes the tinting effect when the entity is removed.
   */
  public TintTilesSystem() {
    super(TintDirectionComponent.class, PositionComponent.class);

    onEntityRemove = this::removeTint;
  }

  /** Implements the functionality of the system. */
  @Override
  public void execute() {
    filteredEntityStream().map(this::buildDataObject).forEach(this::applyTint);
  }

  private void removeTint(Entity entity) {
    TintTileSystemData data = buildDataObject(entity);
    removeTint(data);
  }

  /**
   * Applies the tint to the affected Tiles. Removes tint from tiles that are no longer affected.
   *
   * @param data The data object containing the entity's view direction and tinting information.
   */
  private void applyTint(TintTileSystemData data) {
    Set<Coordinate> currentlyAffectedCoordinates = new HashSet<>();
    ILevel level = Game.currentLevel().orElse(null);
    if (level == null) return;

    Direction viewDirection = data.viewDirection;
    TintDirectionComponent tintViewComp = data.tintDirectionComponent;
    if (tintViewComp == null) return;

    // Get currently affected tiles
    for (Tile tile : tintViewComp.affectedTiles(viewDirection)) {
      Coordinate coord = tile.coordinate();
      currentlyAffectedCoordinates.add(coord);

      // Apply tint if not already applied
      tintViewComp.originalColor(tile.coordinate(), tile.tintColor());
      tile.tintColor(tintViewComp.color());
    }

    tintViewComp.originalColors().stream()
        .filter(coord -> !currentlyAffectedCoordinates.contains(coord))
        .forEach(
            coord -> {
              level
                  .tileAt(coord)
                  .ifPresent(
                      tile -> {
                        tintTile(tile, tintViewComp.originalColor(coord));
                        tintViewComp.removeOriginalColor(coord);
                      });
            });
  }

  /**
   * Removes the tint from all affected Tiles.
   *
   * @param data The data object containing the entity's view direction and tinting information.
   */
  private void removeTint(TintTileSystemData data) {
    ILevel level = Game.currentLevel().orElse(null);
    if (level == null) return;

    TintDirectionComponent tintViewComp = data.tintDirectionComponent;
    if (tintViewComp == null) return;

    tintViewComp
        .originalColors()
        .forEach(
            coord -> {
              level
                  .tileAt(coord)
                  .ifPresent(
                      tile -> {
                        tintTile(tile, tintViewComp.originalColor(coord));
                        tintViewComp.removeOriginalColor(coord);
                      });
            });

    // Clear the original colors map
    tintViewComp.clearOriginalColors();
  }

  private void tintTile(Tile tile, int color) {
    if (tile == null) return;
    tile.tintColor(color);
  }

  private TintTileSystemData buildDataObject(Entity entity) {
    TintDirectionComponent tintDirectionComponent =
        entity
            .fetch(TintDirectionComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(entity, TintDirectionComponent.class));
    PositionComponent positionComponent =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    return new TintTileSystemData(tintDirectionComponent, positionComponent.viewDirection());
  }

  private record TintTileSystemData(
      TintDirectionComponent tintDirectionComponent, Direction viewDirection) {}
}
