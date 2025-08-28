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
 * Creates a progress bar that follows an entity and shows its current energy percentage.
 *
 * <p>Entities with {@link StaminaComponent}, {@link PositionComponent}, and {@link DrawComponent}
 * will be processed by this system.
 */
public final class StaminaBarSystem extends System {

  private final Map<Integer, ProgressBar> barMapping = new HashMap<>();
  private static final float OFFSET = 40f;

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
