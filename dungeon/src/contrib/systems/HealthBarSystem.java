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
 * Displays a health bar above entities with a {@link HealthComponent}, {@link PositionComponent},
 * and {@link DrawComponent}.
 */
public final class HealthBarSystem extends System {

  private static final Logger LOGGER = Logger.getLogger(HealthBarSystem.class.getSimpleName());
  private static final float VERTICAL_OFFSET = 0f;

  private final Map<Integer, ProgressBar> barMapping = new HashMap<>();

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
