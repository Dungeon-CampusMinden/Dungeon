package core.systems;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.SoundComponent;
import core.sound.SoundSpec;
import core.sound.player.ISoundPlayer;
import core.sound.player.PlayHandle;
import core.utils.Point;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link SoundSystem} class. */
public class SoundSystemTest {

  private SoundSystem soundSystem;
  private ISoundPlayer mockPlayer;
  private PlayHandle mockHandle;

  @BeforeEach
  void setup() {
    mockPlayer = mock(ISoundPlayer.class);
    mockHandle = mock(PlayHandle.class);
    when(mockPlayer.playWithInstance(
            anyLong(), anyString(), anyFloat(), anyBoolean(), anyFloat(), anyFloat(), any()))
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
    entity.add(
        new SoundComponent(
            SoundSpec.builder("test").volume(0.8f).maxDistance(10f).attenuation(0.1f).build()));
    Game.add(entity);

    soundSystem.execute();

    verify(mockPlayer)
        .playWithInstance(anyLong(), eq("test"), eq(0.8f), eq(false), eq(1f), eq(0f), any());
  }

  @Test
  void testSoundSystemWithHero() {
    Entity hero = new Entity();
    hero.add(new PositionComponent(new Point(5, 5)));
    hero.add(new PlayerComponent());
    Game.add(hero);

    Entity soundEntity = new Entity();
    soundEntity.add(new PositionComponent(new Point(0, 0)));
    soundEntity.add(
        new SoundComponent(
            SoundSpec.builder("test").volume(0.8f).maxDistance(10f).attenuation(0.1f).build()));
    Game.add(soundEntity);

    soundSystem.execute();

    verify(mockPlayer)
        .playWithInstance(eq(0L), eq("test"), eq(0.8f), eq(false), eq(1f), eq(0f), any());
  }

  @Test
  void testSoundSystemWithLoopingSound() {
    Entity hero = new Entity();
    hero.add(new PositionComponent(new Point(0, 0)));
    hero.add(new PlayerComponent());
    Game.add(hero);

    Entity entity = new Entity();
    entity.add(new PositionComponent(new Point(0, 0)));
    entity.add(
        new SoundComponent(
            SoundSpec.builder("loop")
                .volume(0.5f)
                .looping(true)
                .maxDistance(20f)
                .attenuation(0.05f)
                .build()));
    Game.add(entity);

    soundSystem.execute();

    verify(mockPlayer)
        .playWithInstance(eq(0L), eq("loop"), eq(0.5f), eq(true), eq(1f), eq(0f), any());
  }

  @Test
  void testSoundSystemNoHero() {
    Entity soundEntity = new Entity();
    soundEntity.add(new PositionComponent(new Point(0, 0)));
    soundEntity.add(
        new SoundComponent(
            SoundSpec.builder("test").volume(0.8f).maxDistance(10f).attenuation(0.1f).build()));
    Game.add(soundEntity);

    soundSystem.execute();

    verify(mockPlayer, never())
        .playWithInstance(
            anyLong(), anyString(), anyFloat(), anyBoolean(), anyFloat(), anyFloat(), any());
  }

  @Test
  void testSoundSystemEntityRemoved() {
    Entity hero = new Entity();
    hero.add(new PositionComponent(new Point(0, 0)));
    hero.add(new PlayerComponent());
    Game.add(hero);

    Entity entity = new Entity();
    entity.add(new PositionComponent(new Point(0, 0)));
    entity.add(
        new SoundComponent(
            SoundSpec.builder("test")
                .volume(0.8f)
                .looping(true)
                .maxDistance(10f)
                .attenuation(0.1f)
                .build()));
    Game.add(entity);

    soundSystem.execute(); // Adds handle

    Game.remove(entity);

    soundSystem.execute();
    assertFalse(mockHandle.isPlaying());
  }
}
