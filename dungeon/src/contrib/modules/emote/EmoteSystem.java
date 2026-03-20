package contrib.modules.emote;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.utils.Point;

/** System that processes emotes, updating their wobbling. */
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
    if (data.ec().isDone()) {
      Game.remove(data.e());
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
