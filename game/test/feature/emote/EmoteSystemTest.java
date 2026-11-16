package feature.emote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.systems.AttachmentSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Verifies that an emote follows its entity, wobbles above it and disappears with it. */
class EmoteSystemTest {

  @AfterEach
  void cleanup() {
    Game.removeAllEntities();
  }

  @Test
  void emoteWobblesAboveMovingTargetAndDisappearsWithIt() {
    Entity target = new Entity("target");
    PositionComponent targetPosition = new PositionComponent(new Point(2, 3));
    target.add(targetPosition);
    Entity emote = EmoteFactory.createEmote(target, Vector2.of(0.5f, 0.5f), Emote.DROP, 10_000);
    PositionComponent emotePosition = emote.fetch(PositionComponent.class).orElseThrow();
    Game.add(target);
    Game.add(emote);
    AttachmentSystem attachment = new AttachmentSystem();
    EmoteSystem emotes = new EmoteSystem();

    targetPosition.position(new Point(5, 3));
    // a quarter of the wobble cycle brings the emote to its highest point
    for (int tick = 0; tick < Math.round(0.25f / Game.TICK_SECONDS); tick++) {
      attachment.execute();
      emotes.execute();
    }
    assertEquals(5, emotePosition.position().x(), 0.001f);
    assertEquals(4.1f, emotePosition.position().y(), 0.01f);

    Game.remove(target);
    attachment.execute();
    assertFalse(Game.existInGame(emote));
  }
}
