package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.HealthComponent;
import contrib.utils.AttributeBarUtil;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A system that displays health bars above entities.
 *
 * <p>Entities with {@link HealthComponent}, {@link PositionComponent}, and {@link DrawComponent}
 * will have a progress bar rendered above them, showing their current health points relative to the
 * maximum health.
 *
 * <p>Health bars are automatically created when entities are added to the system and removed when
 * entities are removed. The bars are updated each frame to reflect the current health of the
 * entity.
 */
public final class HealthBarSystem extends System {

  private static final Logger LOGGER = Logger.getLogger(HealthBarSystem.class.getSimpleName());

  /** Vertical offset of the health bar above the entity. */
  private static final float VERTICAL_OFFSET = 0f;

  /** Mapping from entity IDs to their corresponding health bars. */
  private final Map<Integer, ProgressBar> barMapping = new HashMap<>();

  /**
   * Creates a new {@code HealthBarSystem}.
   *
   * <p>Registers listeners for entity addition and removal. When an entity with the required
   * components is added, a health bar is created and attached. When the entity is removed, the
   * corresponding health bar is removed.
   */
  public HealthBarSystem() {
    super(DrawComponent.class, HealthComponent.class, PositionComponent.class);

    this.onEntityAdd =
        entity -> {
          LOGGER.fine("Adding health bar for entity " + entity.id());
          AttributeBarUtil.addBarToEntity(
              entity,
              e ->
                  new AttributeBarUtil.AttributeProvider() {
                    final HealthComponent hc = e.fetch(HealthComponent.class).orElseThrow();

                    @Override
                    public float current() {
                      return hc.currentHealthpoints();
                    }

                    @Override
                    public float max() {
                      return hc.maximalHealthpoints();
                    }
                  },
              barMapping,
              "healthbar",
              VERTICAL_OFFSET);
        };

    this.onEntityRemove =
        entity -> {
          ProgressBar bar = barMapping.remove(entity.id());
          if (bar != null) bar.remove();
        };

    LOGGER.info("HealthBarSystem created");
  }

  /**
   * Updates all health bars for the entities managed by this system.
   *
   * <p>Each entity's {@link HealthComponent} is queried for current and maximum health points, and
   * the corresponding progress bar is updated accordingly.
   */
  @Override
  public void execute() {
    filteredEntityStream()
        .forEach(
            entity ->
                AttributeBarUtil.updateBar(
                    entity,
                    e ->
                        new AttributeBarUtil.AttributeProvider() {
                          final HealthComponent hc = e.fetch(HealthComponent.class).orElseThrow();

                          @Override
                          public float current() {
                            return hc.currentHealthpoints();
                          }

                          @Override
                          public float max() {
                            return hc.maximalHealthpoints();
                          }
                        },
                    barMapping,
                    VERTICAL_OFFSET));
  }
}
