package contrib.modules.levelHide;

import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.systems.DrawSystem;
import core.utils.Point;
import core.utils.Rectangle;
import core.utils.Vector2;
import core.utils.components.draw.shader.LevelHideShader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The LevelHideSystem manages all hidden Regions in the world. If the player steps into a region,
 * it is revealed. If the player leaves the region again, it's hidden.
 */
public class LevelHideSystem extends System {

  private static final String SOUND_HIDE = "virix_MENU_B_Back";
  private static final String SOUND_SHOW = "virix_MENU_B_Select";

  private Point lastPlayerPosition = null;
  private static int entityIdCounter = 0;
  private final Map<Data, Integer> trackedEntities = new HashMap<>();

  /** Constructs new LevelHideSystem. */
  public LevelHideSystem() {
    super(LevelHideComponent.class, PositionComponent.class);
    onEntityAdd = this::onEntityAdd;
    onEntityRemove = this::onEntityRemove;
  }

  private void onEntityAdd(Entity entity) {
    getDrawSystem()
        .ifPresent(
            ds -> {
              Data data = Data.of(entity);
              trackedEntities.put(data, entityIdCounter++);
              String shaderIdentifier = getShaderIdentifier(data);
              Vector2 regionPos =
                  Vector2.of(data.pc.position().translate(data.lhc.region().offset()));
              Rectangle region = new Rectangle(data.lhc.region().size(), regionPos);
              ds.sceneShaders()
                  .add(
                      shaderIdentifier,
                      new LevelHideShader(true, region).transitionSize(data.lhc.transitionSize()));
            });
  }

  private void onEntityRemove(Entity entity) {
    getDrawSystem()
        .ifPresent(
            ds -> {
              Data data = Data.of(entity);
              trackedEntities.remove(data);
              String shaderIdentifier = getShaderIdentifier(data);
              ds.sceneShaders().remove(shaderIdentifier);
            });
  }

  @Override
  public void execute() {
    Point currentPos = EntityUtils.getPlayerPosition();
    getDrawSystem()
        .ifPresent(
            ds -> {
              filteredEntityStream()
                  .map(Data::of)
                  .forEach(
                      entity -> {
                        checkRegion(ds, entity, lastPlayerPosition, currentPos);
                      });
            });
    lastPlayerPosition = currentPos;
  }

  private void checkRegion(DrawSystem ds, Data d, Point lastPos, Point currentPos) {
    String shaderIdentifier = getShaderIdentifier(d);
    if (!(ds.sceneShaders().get(shaderIdentifier) instanceof LevelHideShader lhs)) return;

    LevelHideComponent lhc = d.lhc;
    Rectangle region =
        lhc.region().expand(lhc.transitionSize() / 2f).translate(Vector2.of(d.pc.position()));
    boolean wasInside = lastPos != null && region.contains(lastPos);
    boolean isInside = region.contains(currentPos);

    if (!wasInside && isInside) {
      Game.soundPlayer().play(SOUND_SHOW, 0.05f);
      lhs.hiding(false);
    } else if (wasInside && !isInside) {
      Game.soundPlayer().play(SOUND_HIDE, 0.05f);
      lhs.hiding(true);
    }
  }

  private Optional<DrawSystem> getDrawSystem() {
    if (!(Game.systems().get(DrawSystem.class) instanceof DrawSystem ds)) return Optional.empty();
    return Optional.of(ds);
  }

  private String getShaderIdentifier(Data d) {
    return "LevelHiderSystem#" + trackedEntities.get(d);
  }

  private record Data(Entity e, PositionComponent pc, LevelHideComponent lhc) {
    static Data of(Entity e) {
      return new Data(
          e,
          e.fetch(PositionComponent.class).orElseThrow(),
          e.fetch(LevelHideComponent.class).orElseThrow());
    }
  }
}
