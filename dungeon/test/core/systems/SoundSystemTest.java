package core.systems;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.SoundComponent;
import core.sound.player.IPlayHandle;
import core.sound.player.ISoundPlayer;
import core.utils.Point;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link SoundSystem} class. */
public class SoundSystemTest {

  private SoundSystem soundSystem;
  private ISoundPlayer mockPlayer;
  private IPlayHandle mockHandle;

  @BeforeEach
  void setup() {
    mockPlayer = mock(ISoundPlayer.class);
    mockHandle = mock(IPlayHandle.class);
    when(mockPlayer.play(anyString(), anyFloat(), anyBoolean(), anyFloat(), anyFloat()))
        .thenReturn(Optional.of(mockHandle));
    soundSystem = new SoundSystem(mockPlayer);
    Game.add(soundSystem);
  }

  @AfterEach
  void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    if (Game.soundPlayer() != null) {
      Game.soundPlayer().dispose();
    }
  }

  @Test
  void testSoundSystemFiltersCorrectComponents() {
    Entity hero = new Entity();
    hero.add(new PositionComponent(new Point(0, 0)));
    hero.add(new PlayerComponent());
    Game.add(hero);

    Entity entity = new Entity();
    entity.add(new PositionComponent(new Point(0, 0)));
    entity.add(new SoundComponent("test", 0.8f, false, 1f, 10f, 0.1f, () -> {}));
    Game.add(entity);

    soundSystem.execute();

    verify(mockPlayer).play("test", 0.8f, false, 1f, 0f);
  }

  @Test
  void testSoundSystemWithHero() {
    Entity hero = new Entity();
    hero.add(new PositionComponent(new Point(5, 5)));
    hero.add(new PlayerComponent());
    Game.add(hero);

    Entity soundEntity = new Entity();
    soundEntity.add(new PositionComponent(new Point(0, 0)));
    Runnable onFinish = mock(Runnable.class);
    soundEntity.add(new SoundComponent("test", 0.8f, false, 1, 10f, 0.1f, onFinish));
    Game.add(soundEntity);

    soundSystem.execute();

    verify(mockPlayer).play("test", 0.8f, false, 1f, 0f);
    verify(mockHandle).onFinished(onFinish);
  }

  @Test
  void testSoundSystemWithLoopingSound() {
    Entity hero = new Entity();
    hero.add(new PositionComponent(new Point(0, 0)));
    hero.add(new PlayerComponent());
    Game.add(hero);

    Entity entity = new Entity();
    entity.add(new PositionComponent(new Point(0, 0)));
    entity.add(new SoundComponent("loop", 0.5f, true, 1, 20f, 0.05f, () -> {}));
    Game.add(entity);

    soundSystem.execute();

    verify(mockPlayer).play("loop", 0.5f, true, 1f, 0f);
  }

  @Test
  void testSoundSystemNoHero() {
    Entity soundEntity = new Entity();
    soundEntity.add(new PositionComponent(new Point(0, 0)));
    soundEntity.add(new SoundComponent("fireball", 0.8f, false, 1, 10f, 0.1f, () -> {}));
    Game.add(soundEntity);

    soundSystem.execute();

    verify(mockPlayer, never()).play(anyString(), anyFloat(), anyBoolean());
  }

  @Test
  void testSoundSystemEntityRemoved() {
    Entity hero = new Entity();
    hero.add(new PositionComponent(new Point(0, 0)));
    hero.add(new PlayerComponent());
    Game.add(hero);

    Entity entity = new Entity();
    entity.add(new PositionComponent(new Point(0, 0)));
    entity.add(new SoundComponent("test", 0.8f, true, 1, 10f, 0.1f, () -> {}));
    Game.add(entity);

    soundSystem.execute(); // Adds handle

    Game.remove(entity);

    soundSystem.execute();
    assertFalse(mockHandle.isPlaying());
  }
}
