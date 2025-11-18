package core.sound.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import core.Game;
import core.sound.AudioApi;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/** Tests for the {@link NoSoundPlayer} class. */
public class NoSoundPlayerTest {

  private static MockedStatic<Game> mockedGame;

  @BeforeAll
  static void setup() {
    mockedGame = Mockito.mockStatic(Game.class);
    mockedGame.when(Game::soundPlayer).thenReturn(new NoSoundPlayer());
    mockedGame.when(Game::audio).thenReturn(new AudioApi());
  }

  @AfterAll
  static void tearDown() {
    mockedGame.close();
  }

  @Test
  void testPlayReturnsMock() {
    Runnable mockRunnable = Mockito.mock(Runnable.class);
    Game.audio().registerOnFinished(1, mockRunnable);
    Optional<IPlayHandle> handle =
        Game.soundPlayer().playWithInstance(1, "test", 0.8f, false, 1f, 0f, mockRunnable);

    assertTrue(handle.isPresent());
    Game.soundPlayer().update(0.01f);
    verify(mockRunnable).run();
    IPlayHandle playHandle = handle.get();
    assertDoesNotThrow(playHandle::stop);
    assertDoesNotThrow(() -> playHandle.volume(0.5f));
  }

  @Test
  void testUpdateDoesNothing() {
    assertDoesNotThrow(() -> Game.soundPlayer().update(0.016f));
  }

  @Test
  void testDisposeDoesNothing() {
    assertDoesNotThrow(Game.soundPlayer()::dispose);
  }
}
