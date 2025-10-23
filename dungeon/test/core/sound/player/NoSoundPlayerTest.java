package core.sound.player;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Tests for the {@link NoSoundPlayer} class. */
public class NoSoundPlayerTest {

  private final NoSoundPlayer player = new NoSoundPlayer();

  @Test
  void testPlayReturnsEmpty() {
    Optional<IPlayHandle> handle = player.play("test", 0.8f, false);
    assertFalse(handle.isPresent());
  }

  @Test
  void testUpdateDoesNothing() {
    assertDoesNotThrow(() -> player.update(0.016f));
  }

  @Test
  void testDisposeDoesNothing() {
    assertDoesNotThrow(player::dispose);
  }
}
