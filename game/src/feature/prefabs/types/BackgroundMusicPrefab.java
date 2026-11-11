package feature.prefabs.types;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALMusic;
import engine.Entity;
import engine.Game;
import engine.level.elements.ILevel;
import engine.utils.logging.DungeonLogger;
import engine.utils.settings.ClientSettings;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabRuntime;
import feature.prefabs.PrefabSide;
import java.util.List;
import java.util.Optional;
import org.lwjgl.openal.AL10;

/**
 * Client-side prefab that plays a looping music track, following the music and master volume of
 * each player's settings.
 *
 * <p>{@link #START} and {@link #STOP} change whether the track plays for all players, also for
 * players who join later. Starting a playing track again does not restart it.
 */
public final class BackgroundMusicPrefab extends Prefab {

  private static final String TYPE = "background-music";
  private static final String DISPLAY_NAME = "Background Music";
  private static final String SOUNDS_DIRECTORY = "sounds/";
  private static final String SOUND_EXTENSION = ".wav";
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(BackgroundMusicPrefab.class);
  private static final SpawnState<Track> TRACKS = new SpawnState<>();

  /** Played track, a sound file name. */
  public static final PrefabProperty<String> TRACK =
      PrefabProperty.openSelection("track", "Track", "background", SoundEffectPrefab::soundNames);

  /** Volume relative to the music volume of the player's settings. */
  public static final PrefabProperty<Float> VOLUME =
      PrefabProperty.numberSlider("volume", "Volume", 1f, 0f, 4f, 0.05f);

  /** Playback speed and pitch, 1 is the original. */
  public static final PrefabProperty<Float> PITCH =
      PrefabProperty.numberSlider("pitch", "Pitch", 1f, 0.5f, 2f, 0.05f);

  /** Whether the track plays, changed at runtime by the actions. */
  public static final PrefabProperty<Boolean> PLAYING =
      PrefabProperty.bool("playing", "Start Playing", true);

  /** Plays the track. */
  public static final PrefabAction START =
      PrefabAction.of("start", "Start", context -> setPlaying(context, true));

  /** Stops the track. */
  public static final PrefabAction STOP =
      PrefabAction.of("stop", "Stop", context -> setPlaying(context, false));

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(TRACK, VOLUME, PITCH, PLAYING);

  /** Creates the background-music definition. */
  public BackgroundMusicPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.CLIENT, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored music track.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public BackgroundMusicPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.CLIENT, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(START, STOP);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    if (Game.isHeadless() || !value(instance, PLAYING)) return List.of();
    Music music;
    try {
      music =
          Gdx.audio.newMusic(
              Gdx.files.internal(SOUNDS_DIRECTORY + value(instance, TRACK) + SOUND_EXTENSION));
    } catch (RuntimeException exception) {
      LOGGER.warn("Prefab '" + instance.name() + "' can not play its track", exception);
      return List.of();
    }
    float volume = value(instance, VOLUME);
    music.setLooping(true);
    music.setVolume(volume(volume));
    music.play();
    // The OpenAL source exists once the music plays.
    if (music instanceof OpenALMusic openAl) {
      AL10.alSourcef(openAl.getSourceId(), AL10.AL_PITCH, value(instance, PITCH));
    }
    Runnable stopListening =
        ClientSettings.addVolumeListener((key, changed) -> music.setVolume(volume(volume)));
    TRACKS.start(context.level(), instance.name(), new Track(music, stopListening));
    return List.of();
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    TRACKS
        .end(context.level(), instance.name())
        .ifPresent(
            track -> {
              track.stopListening().run();
              track.music().stop();
              track.music().dispose();
            });
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(value(instance, TRACK));
  }

  private static void setPlaying(ActionContext context, boolean playing) {
    if (context.value(PLAYING) == playing) return;
    PrefabRuntime.update(
        context.level(), context.instance().name(), instance -> instance.with(PLAYING, playing));
  }

  private static float volume(float base) {
    float user = ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f;
    return Math.min(1f, base * user);
  }

  private record Track(Music music, Runnable stopListening) {}
}
