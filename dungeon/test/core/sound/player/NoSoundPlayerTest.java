package core.sound.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import core.Game;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link NoSoundPlayer} class. */
public class NoSoundPlayerTest {

  private final NoSoundPlayer player = new NoSoundPlayer();

  @Test
  void testPlayReturnsMock() {
    Runnable mockRunnable = Mockito.mock(Runnable.class);
    Game.audio().registerOnFinished(1, mockRunnable);
    Optional<IPlayHandle> handle =
        player.playWithInstance(1, "test", 0.8f, false, 1f, 0f, mockRunnable);

    assertTrue(handle.isPresent());
    verify(mockRunnable).run();
    IPlayHandle playHandle = handle.get();
    assertDoesNotThrow(playHandle::stop);
    assertDoesNotThrow(() -> playHandle.volume(0.5f));
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
