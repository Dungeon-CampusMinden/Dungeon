package rooms.systemRecovery.network;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.draw.shader.OutlineShader;
import java.util.Map;
import rooms.systemRecovery.util.shaders.EnergyGlow;

/** Reconstructs the current manual-sort comparison visuals from server metadata. */
final class ManualSortVisualSync {

  private int lastLeftEntity = Integer.MIN_VALUE;
  private int lastRightEntity = Integer.MIN_VALUE;
  private int lastDisplayEntity = Integer.MIN_VALUE;
  private boolean initialized;

  /**
   * Applies one authoritative comparison pair to the local sort entities.
   *
   * @param display entity publishing the comparison metadata
   * @param metadata synchronized comparison metadata
   */
  void apply(Entity display, Map<String, String> metadata) {
    String leftValue = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_SORT_LEFT_ENTITY);
    String rightValue = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_SORT_RIGHT_ENTITY);
    if (leftValue == null || rightValue == null) return;

    int leftEntityId = parseEntityId(leftValue);
    int rightEntityId = parseEntityId(rightValue);
    if (initialized
        && display.id() == lastDisplayEntity
        && leftEntityId == lastLeftEntity
        && rightEntityId == lastRightEntity) {
      return;
    }

    clear();
    Game.levelEntities()
        .filter(entity -> entity.name() != null && entity.name().startsWith("sort_data_"))
        .forEach(this::addSortFill);
    addComparisonHighlight(leftEntityId);
    addComparisonHighlight(rightEntityId);
    lastLeftEntity = leftEntityId;
    lastRightEntity = rightEntityId;
    lastDisplayEntity = display.id();
    initialized = true;
  }

  /** Removes all sort-specific shaders and forgets the previous comparison pair. */
  void reset() {
    clear();
    lastLeftEntity = Integer.MIN_VALUE;
    lastRightEntity = Integer.MIN_VALUE;
    lastDisplayEntity = Integer.MIN_VALUE;
    initialized = false;
  }

  private void clear() {
    Game.levelEntities()
        .forEach(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw -> {
                          draw.shaders().remove("sortComparison");
                          draw.shaders().remove("sortComparisonFill");
                        }));
  }

  private void addComparisonHighlight(int entityId) {
    if (entityId < 0) return;
    Game.findEntityById(entityId)
        .ifPresent(
            entity ->
                entity
                    .fetch(DrawComponent.class)
                    .ifPresent(
                        draw ->
                            draw.shaders()
                                .add("sortComparison", new OutlineShader(2, Color.CYAN))));
  }

  private void addSortFill(Entity entity) {
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            draw -> {
              draw.shaders()
                  .add(
                      "sortComparisonFill",
                      new EnergyFillShader(
                              sortValue(entity) / 100f, Color.CYAN, "objects/tech/CryoBox.png")
                          .animMagnitude(0));
              EnergyGlow.addTo(draw);
            });
  }

  private static int sortValue(Entity entity) {
    String name = entity.name();
    int separator = name.lastIndexOf('_');
    if (separator < 0) return 0;
    try {
      return Integer.parseInt(name.substring(separator + 1));
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private static int parseEntityId(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ignored) {
      return -1;
    }
  }
}
