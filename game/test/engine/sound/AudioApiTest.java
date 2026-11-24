package engine.sound;

import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.game.PreRunConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link AudioApi}. */
class AudioApiTest {

  @AfterEach
  void tearDown() {
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
  }

  /** A sound a multiplayer client plays itself never shares its ID with a server sound. */
  @Test
  void multiplayerClientPlaysLocalSoundsWithNegativeIds() {
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);

    long instanceId = Game.audio().playOnEntity(new Entity("emitter"), SoundSpec.builder("click"));

    assertTrue(instanceId < 0);
  }
}
