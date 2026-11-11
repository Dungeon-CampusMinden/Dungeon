package feature.prefabs.types;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.level.elements.ILevel;
import feature.hud.dialogs.BlackFadeCutscene;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Server-side prefab that shows text pages on a black screen to every player who enters the level,
 * once per player name.
 *
 * <p>The intro starts on a black screen, so joining players never see the level before it. Play
 * time only runs for players who finished the intro. While editing levels, the intro is skipped. A
 * level should have at most one intro.
 */
public final class IntroTextPrefab extends Prefab {

  private static final String TYPE = "intro-text";
  private static final String DISPLAY_NAME = "Intro Text";
  private static final String STATE_SEEN = "seen";
  private static final SpawnState<Intro> INTROS = new SpawnState<>();

  /** Raised when a player finished the intro, with that player. */
  public static final PrefabEvent<Entity> FINISHED = PrefabEvent.withPlayer("finished", "Finished");

  // Pages of the intro.
  private static final PrefabPages PAGES = PrefabPages.create();

  private static final List<PrefabProperty<?>> PROPERTIES = PAGES.properties();

  /** Creates the intro-text definition. */
  public IntroTextPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored intro.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public IntroTextPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(FINISHED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Intro intro = INTROS.start(context.level(), instance.name(), new Intro());
    // A continued game does not show the intro again to players who saw it.
    restoredState(context, instance)
        .ifPresent(
            saved -> saved.path(STATE_SEEN).forEach(name -> intro.seen.add(name.asString())));
    // Play time waits until the players finished the intro.
    if (!LevelEditing.active()) Game.playClock().playingParticipants(intro.playing);
    return List.of();
  }

  @Override
  public void onPlayerReady(PrefabCreationContext context, PrefabInstance instance, Entity player) {
    Intro intro = INTROS.get(context.level(), instance.name()).orElse(null);
    if (intro == null) return;
    String name = playerName(player);
    if (LevelEditing.active() || intro.seen.contains(name)) {
      play(intro, player);
      return;
    }
    if (!intro.showing.add(player.id())) return;
    BlackFadeCutscene.show(
        PAGES.messages(instance),
        false,
        true,
        () -> {
          intro.showing.remove(player.id());
          intro.seen.add(name);
          play(intro, player);
          fire(context, instance, FINISHED, player);
        },
        player.id());
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    // Without the intro, nobody has to finish it before play time runs.
    if (INTROS.end(context.level(), instance.name()).isPresent()) {
      Game.playClock().allParticipantsPlaying();
    }
  }

  @Override
  public Optional<JsonNode> captureState(
      PrefabCreationContext context, PrefabInstance instance, List<Entity> entities) {
    return INTROS
        .get(context.level(), instance.name())
        .filter(intro -> !intro.seen.isEmpty())
        .map(
            intro -> {
              ObjectNode state = PrefabStateJson.object();
              ArrayNode seen = state.putArray(STATE_SEEN);
              intro.seen.forEach(seen::add);
              return state;
            });
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(PAGES.summary(instance));
  }

  private static void play(Intro intro, Entity player) {
    intro.playing.add(player.id());
    Game.playClock().playingParticipants(intro.playing);
    Game.playClock().ready();
  }

  private static String playerName(Entity player) {
    return player
        .fetch(PlayerComponent.class)
        .map(PlayerComponent::playerName)
        .orElse("entity-" + player.id());
  }

  private static final class Intro {
    // Names of the players who finished the intro, kept by saves.
    private final Set<String> seen = new LinkedHashSet<>();
    // Entities of the players who are shown the intro right now.
    private final Set<Integer> showing = new HashSet<>();
    // Entities of the players whose play time runs.
    private final Set<Integer> playing = new HashSet<>();
  }
}
