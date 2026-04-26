package contrib.modules.levelhide;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.sound.SoundSpec;
import core.utils.Point;
import core.utils.Rectangle;
import core.utils.Vector2;
import java.util.Optional;

/**
 * Maintains the runtime state of hidden world regions.
 *
 * <p>If the player enters a configured region, the region becomes revealed. If the player leaves it
 * again, the region becomes hidden. The actual visual representation is delegated to the active
 * rendering backend.
 */
public final class LevelHideSystem extends System {
  private static final String SOUND_HIDE = "virix_MENU_B_Back";
  private static final String SOUND_SHOW = "virix_MENU_B_Select";
  private static final float SOUND_VOLUME = 0.05f;

  private Point lastPlayerPosition = null;

  /** Creates a new client-side level-hide system. */
  public LevelHideSystem() {
    super(AuthoritativeSide.CLIENT, LevelHideComponent.class, PositionComponent.class);
    onEntityAdd = this::ensureStateComponent;
  }

  private void ensureStateComponent(Entity entity) {
    if (!entity.isPresent(LevelHideStateComponent.class)) {
      entity.add(new LevelHideStateComponent());
    }
  }

  @Override
  public void execute() {
    Point currentPlayerPosition = currentPlayerPosition().orElse(null);
    if (currentPlayerPosition == null) {
      lastPlayerPosition = null;
      return;
    }

    filteredEntityStream()
        .forEach(entity -> updateRegion(entity, lastPlayerPosition, currentPlayerPosition));
    lastPlayerPosition = currentPlayerPosition;
  }

  private void updateRegion(Entity entity, Point lastPos, Point currentPos) {
    PositionComponent positionComponent = entity.fetch(PositionComponent.class).orElseThrow();
    LevelHideComponent levelHideComponent = entity.fetch(LevelHideComponent.class).orElseThrow();
    LevelHideStateComponent stateComponent =
        entity.fetch(LevelHideStateComponent.class).orElseThrow();

    Rectangle activeRegion =
        levelHideComponent
            .region()
            .expand(levelHideComponent.transitionSize() / 2f)
            .translate(Vector2.of(positionComponent.position()));

    boolean wasInside = lastPos != null && activeRegion.contains(lastPos);
    boolean isInside = activeRegion.contains(currentPos);

    if (!wasInside && isInside) {
      stateComponent.hiding(false);
      playGlobal(SOUND_SHOW);
    } else if (wasInside && !isInside) {
      stateComponent.hiding(true);
      playGlobal(SOUND_HIDE);
    }
  }

  private Optional<Point> currentPlayerPosition() {
    return Game.player()
        .flatMap(player -> player.fetch(PositionComponent.class).map(PositionComponent::position));
  }

  private void playGlobal(String soundId) {
    Game.audio().playGlobal(SoundSpec.builder(soundId).volume(SOUND_VOLUME));
  }
}
