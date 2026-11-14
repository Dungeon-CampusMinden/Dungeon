package feature.emote;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.PositionComponent;
import engine.utils.Point;
import feature.utils.EntityUtils;
import java.util.Optional;

/**
 * System that processes emotes, updating their wobbling and keeping them above the entity they
 * follow.
 */
public class EmoteSystem extends System {

  /** Create a new EmoteSystem that processes emote entities. */
  public EmoteSystem() {
    super(AuthoritativeSide.SERVER, EmoteComponent.class, PositionComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream().map(Data::of).forEach(this::process);
  }

  private void process(Data data) {
    data.ec().update(1f / Game.frameRate());
    Optional<Entity> target = data.ec().target();
    if (data.ec().isDone() || (target.isPresent() && !Game.existInGame(target.get()))) {
      Game.remove(data.e());
    } else if (target.isPresent()) {
      Point anchor = EntityUtils.getPosition(target.get()).translate(data.ec().offset());
      data.pc().position(anchor.translate(0, data.ec().getWobbleOffset()));
    } else {
      Point pos = data.pc.position();
      float translate = data.ec().getWobbleMoveFrame();
      data.pc().position(pos.translate(0, translate));
    }
  }

  @Override
  public void stop() {}

  private record Data(Entity e, PositionComponent pc, EmoteComponent ec) {
    static Data of(Entity e) {
      return new Data(
          e,
          e.fetch(PositionComponent.class).orElseThrow(),
          e.fetch(EmoteComponent.class).orElseThrow());
    }
  }
}
