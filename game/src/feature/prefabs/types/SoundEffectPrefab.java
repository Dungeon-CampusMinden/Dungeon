package feature.prefabs.types;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import engine.Entity;
import engine.Game;
import engine.level.elements.ILevel;
import engine.sound.SoundSpec;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Server-side sound effect that plays whenever {@link #PLAY} runs, e.g. a chime when a puzzle was
 * solved.
 *
 * <p>The sound is heard everywhere, either by every player or only by the player that caused the
 * triggering event. If the event carries no player, a sound for the triggering player is played for
 * every player.
 */
public final class SoundEffectPrefab extends Prefab {

  private static final String TYPE = "sound-effect";
  private static final String DISPLAY_NAME = "Sound Effect";
  private static final String SOUNDS_DIRECTORY = "sounds/";
  private static final String SOUND_EXTENSION = ".wav";
  private static List<String> soundNames;

  /** Name of the sound asset, i.e. its file name in the sounds directory without extension. */
  public static final PrefabProperty<String> SOUND =
      PrefabProperty.openSelection(
          "sound", "Sound", "kenney_ui_confirmation_001", SoundEffectPrefab::soundNames);

  /** Playback volume, in the range {@link SoundSpec} accepts. */
  public static final PrefabProperty<Float> VOLUME =
      PrefabProperty.numberSlider("volume", "Volume", SoundSpec.DEFAULT_VOLUME, 0f, 1f, 0.05f);

  /**
   * Playback speed and pitch, 1 is the original. The sound player clamps pitches outside of [0.5,
   * 2] on non-desktop platforms, so only that range is offered.
   */
  public static final PrefabProperty<Float> PITCH =
      PrefabProperty.numberSlider("pitch", "Pitch", SoundSpec.DEFAULT_PITCH, 0.5f, 2f, 0.05f);

  /** Who hears the sound. */
  public static final PrefabProperty<String> AUDIENCE =
      PrefabAudience.property("Play For", PrefabAudience.EVERYONE);

  /** Plays the sound. */
  public static final PrefabAction PLAY = PrefabAction.of("play", "Play", SoundEffectPrefab::play);

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(SOUND, VOLUME, PITCH, AUDIENCE);

  /** Creates the sound-effect definition. */
  public SoundEffectPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored sound effect.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public SoundEffectPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(PLAY);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    return List.of();
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(value(instance, SOUND));
  }

  private static void play(ActionContext context) {
    SoundSpec.Builder sound =
        SoundSpec.builder(context.value(SOUND))
            .volume(context.value(VOLUME))
            .pitch(context.value(PITCH));
    if (context.value(AUDIENCE).equals(PrefabAudience.TRIGGERING_PLAYER)) {
      sound.targets(context.players().stream().mapToInt(Entity::id).toArray());
    }
    Game.audio().playGlobal(sound);
  }

  /**
   * Lists the playable sound assets.
   *
   * @return sound names, sorted; empty if the asset list is not available
   */
  private static List<String> soundNames() {
    if (soundNames != null) return soundNames;
    if (Gdx.files == null) return List.of();
    FileHandle assets = Gdx.files.internal("internal_assets.txt");
    if (!assets.exists()) return List.of();
    soundNames =
        Arrays.stream(assets.readString().split("\n"))
            .map(String::strip)
            .filter(path -> path.startsWith(SOUNDS_DIRECTORY) && path.endsWith(SOUND_EXTENSION))
            .map(
                path ->
                    path.substring(
                        path.lastIndexOf('/') + 1, path.length() - SOUND_EXTENSION.length()))
            .distinct()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();
    return soundNames;
  }
}
