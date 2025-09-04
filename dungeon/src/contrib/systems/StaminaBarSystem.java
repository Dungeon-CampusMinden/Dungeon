package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.StaminaComponent;
import contrib.utils.AttributeBarUtil;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import java.util.HashMap;
import java.util.Map;

/**
 * A system that creates and manages a stamina bar for entities.
 *
 * <p>Entities with {@link StaminaComponent}, {@link PositionComponent}, and {@link DrawComponent}
 * will have a progress bar displayed above them, showing their current stamina as a percentage of
 * the maximum.
 *
 * <p>Stamina bars are automatically created when entities are added to the system and removed when
 * entities are removed. The bars are updated every frame to reflect the current stamina value.
 */
public final class StaminaBarSystem extends System {

  /** Mapping of entity IDs to their corresponding stamina bars. */
  private final Map<Integer, ProgressBar> barMapping = new HashMap<>();

  /** Vertical offset of the stamina bar above the entity. */
  private static final float OFFSET = 40f;

  /**
   * Creates a new {@code StaminaBarSystem}.
   *
   * <p>Registers entity addition and removal listeners. When an entity with the required components
   * is added, a stamina bar is created. When the entity is removed, the corresponding bar is
   * removed.
   */
  public StaminaBarSystem() {
    super(DrawComponent.class, StaminaComponent.class, PositionComponent.class);

    this.onEntityAdd =
        entity ->
            AttributeBarUtil.addBarToEntity(
                entity,
                e -> e.fetch(StaminaComponent.class).orElseThrow(),
                barMapping,
                "staminabar",
                OFFSET);

    this.onEntityRemove =
        entity -> {
          ProgressBar bar = barMapping.remove(entity.id());
          if (bar != null) bar.remove();
        };
  }

  /**
   * Updates all stamina bars for the entities managed by this system.
   *
   * <p>Each entity's {@link StaminaComponent} is queried for the current and maximum stamina, and
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
                          final StaminaComponent ec = e.fetch(StaminaComponent.class).orElseThrow();

                          @Override
                          public float current() {
                            return ec.currentAmount();
                          }

                          @Override
                          public float max() {
                            return ec.maxAmount();
                          }
                        },
                    barMapping,
                    OFFSET));
  }
}
