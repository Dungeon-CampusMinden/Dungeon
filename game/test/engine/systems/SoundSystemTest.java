package engine.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyFloat;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.components.SoundComponent;
import engine.sound.CoreSounds;
import engine.sound.SoundSpec;
import engine.sound.Sounds;
import engine.sound.player.ISoundPlayer;
import engine.sound.player.PlayHandle;
import engine.utils.Point;
import engine.utils.settings.ClientSettings;
import engine.utils.settings.IntSliderSetting;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Tests for the {@link SoundSystem} class. */
public class SoundSystemTest {

  private SoundSystem soundSystem;
  private ISoundPlayer mockPlayer;
  private PlayHandle mockHandle;
  private int previousMasterVolume;
  private int previousEffectsVolume;

  @BeforeEach
  void setup() {
    previousMasterVolume = ClientSettings.masterVolume();
    previousEffectsVolume = ClientSettings.effectsVolume();
    setVolumes(100, 100);
    mockPlayer = mock(ISoundPlayer.class);
    mockHandle = mock(PlayHandle.class);
    when(mockPlayer.playWithInstance(
            anyLong(), anyString(), anyFloat(), anyBoolean(), anyFloat(), anyFloat(), any()))
        .thenReturn(Optional.of(mockHandle));
    soundSystem = new SoundSystem(() -> mockPlayer);
    Game.add(soundSystem);
  }

  @AfterEach
  void cleanup() {
    setVolumes(previousMasterVolume, previousEffectsVolume);
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

  @Test
  void startVolumeFollowsMasterAndEffectsSettings() {
    setVolumes(50, 40);
    addHeroAt(new Point(0, 0));
    addSoundEntity(new Point(0, 0), SoundSpec.builder("test").volume(0.8f).build());

    soundSystem.execute();

    ArgumentCaptor<Float> volume = ArgumentCaptor.forClass(Float.class);
    verify(mockPlayer)
        .playWithInstance(
            anyLong(), eq("test"), volume.capture(), anyBoolean(), anyFloat(), anyFloat(), any());
    assertEquals(0.8f * 0.5f * 0.4f, volume.getValue(), 1e-6f);
  }

  @Test
  void soundsPlayAppliesVolumeSettingsOnlyOnce() {
    setVolumes(50, 40);
    addHeroAt(new Point(0, 0));

    Sounds.play(CoreSounds.INTERFACE_BUTTON_CLICKED);
    soundSystem.execute();

    ArgumentCaptor<Float> volume = ArgumentCaptor.forClass(Float.class);
    verify(mockPlayer)
        .playWithInstance(
            anyLong(),
            eq(CoreSounds.INTERFACE_BUTTON_CLICKED.soundName()),
            volume.capture(),
            anyBoolean(),
            anyFloat(),
            anyFloat(),
            any());
    assertEquals(CoreSounds.INTERFACE_BUTTON_CLICKED.volume() * 0.2f, volume.getValue(), 1e-6f);
  }

  @Test
  void globalSoundFollowsChangedEffectsSetting() {
    addHeroAt(new Point(0, 0));
    addPlayingSoundEntity(
        new Point(0, 0), SoundSpec.builder("loop").volume(0.8f).looping(true).build());
    soundSystem.execute();

    setVolumes(100, 25);
    soundSystem.execute();

    assertEquals(0.8f * 0.25f, lastUpdatedVolume(), 1e-6f);
  }

  @Test
  void attenuatedSoundFollowsChangedMasterSetting() {
    addHeroAt(new Point(0, 0));
    addPlayingSoundEntity(
        new Point(0, 0),
        SoundSpec.builder("loop").volume(0.8f).looping(true).maxDistance(10f).build());
    soundSystem.execute();
    float fullVolume = lastUpdatedVolume();

    setVolumes(50, 100);
    soundSystem.execute();

    assertEquals(fullVolume * 0.5f, lastUpdatedVolume(), 1e-6f);
  }

  private void addHeroAt(Point position) {
    Entity hero = new Entity();
    hero.add(new PositionComponent(position));
    hero.add(new PlayerComponent());
    Game.add(hero);
  }

  private void addPlayingSoundEntity(Point position, SoundSpec soundSpec) {
    when(mockHandle.isPlaying()).thenReturn(true);
    addSoundEntity(position, soundSpec);
  }

  private void addSoundEntity(Point position, SoundSpec soundSpec) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(position));
    entity.add(new SoundComponent(soundSpec));
    Game.add(entity);
  }

  private float lastUpdatedVolume() {
    ArgumentCaptor<ISoundPlayer.SoundUpdate> update =
        ArgumentCaptor.forClass(ISoundPlayer.SoundUpdate.class);
    verify(mockPlayer, atLeastOnce()).updateSound(anyLong(), update.capture());
    return update.getValue().volume;
  }

  private static void setVolumes(int master, int effects) {
    ((IntSliderSetting) ClientSettings.getSetting(ClientSettings.KEY_MASTER_VOLUME)).value(master);
    ((IntSliderSetting) ClientSettings.getSetting(ClientSettings.KEY_EFFECTS_VOLUME))
        .value(effects);
  }
}
