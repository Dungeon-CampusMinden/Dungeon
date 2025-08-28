package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.components.ManaComponent;
import contrib.utils.AttributeBarUtil;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Displays a mana bar above entities that have a {@link ManaComponent}, {@link PositionComponent},
 * and {@link DrawComponent}.
 */
public final class ManaBarSystem extends System {

  private static final Logger LOGGER = Logger.getLogger(ManaBarSystem.class.getSimpleName());
  private static final float VERTICAL_OFFSET = 20f;

  private final Map<Integer, ProgressBar> barMapping = new HashMap<>();

  public ManaBarSystem() {
    super(DrawComponent.class, ManaComponent.class, PositionComponent.class);

    this.onEntityAdd =
        entity -> {
          LOGGER.fine("Adding mana bar for entity " + entity.id());
          AttributeBarUtil.addBarToEntity(
              entity,
              e ->
                  new AttributeBarUtil.AttributeProvider() {
                    final ManaComponent mc = e.fetch(ManaComponent.class).orElseThrow();

                    @Override
                    public float current() {
                      return mc.currentAmount();
                    }

                    @Override
                    public float max() {
                      return mc.maxAmount();
                    }
                  },
              barMapping,
              "manabar",
              VERTICAL_OFFSET);
        };

    this.onEntityRemove =
        entity -> {
          ProgressBar bar = barMapping.remove(entity.id());
          if (bar != null) bar.remove();
        };

    LOGGER.info("ManaBarSystem created");
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
                          final ManaComponent mc = e.fetch(ManaComponent.class).orElseThrow();

                          @Override
                          public float current() {
                            return mc.currentAmount();
                          }

                          @Override
                          public float max() {
                            return mc.maxAmount();
                          }
                        },
                    barMapping,
                    VERTICAL_OFFSET));
  }
}
