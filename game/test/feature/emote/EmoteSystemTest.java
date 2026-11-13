package feature.emote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.Vector2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Verifies that an emote bound to an entity moves with it and disappears with it. */
class EmoteSystemTest {

  @AfterEach
  void cleanup() {
    Game.removeAllEntities();
  }

  @Test
  void boundEmoteFollowsTargetAndDisappearsWithIt() {
    Entity target = new Entity("target");
    PositionComponent targetPosition = new PositionComponent(new Point(2, 3));
    target.add(targetPosition);
    Entity emote = EmoteFactory.createEmote(target, Vector2.of(0.5f, 0.5f), Emote.DROP, 10_000);
    PositionComponent emotePosition = emote.fetch(PositionComponent.class).orElseThrow();
    Game.add(target);
    Game.add(emote);
    EmoteSystem system = new EmoteSystem();

    targetPosition.position(new Point(5, 3));
    system.execute();
    assertEquals(5, emotePosition.position().x(), 0.001f);
    // the wobble moves the emote by at most 0.1 above or below its place
    assertEquals(4, emotePosition.position().y(), 0.11f);

    Game.remove(target);
    system.execute();
    assertFalse(Game.existInGame(emote));
  }
}
