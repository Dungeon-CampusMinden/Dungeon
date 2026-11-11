package feature.prefabs.types;

import engine.Entity;
import engine.Game;
import engine.level.elements.ILevel;
import feature.components.UIComponent;
import feature.hud.dialogs.BlackFadeCutscene;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Server-side prefab that shows text pages on a black screen to all players, e.g. at the end of the
 * game.
 *
 * <p>{@link #START} shows the outro to every player, and to players who join while it runs. It runs
 * once until the instance is reset. With "End On Black", the screen stays black after the last
 * page, so {@link #FINISHED} can quit the game without the level showing again. While editing
 * levels, the screen never stays black.
 */
public final class OutroTextPrefab extends Prefab {

  private static final String TYPE = "outro-text";
  private static final String DISPLAY_NAME = "Outro Text";
  private static final SpawnState<Outro> OUTROS = new SpawnState<>();

  /** Raised when a player finished the outro, with that player. */
  public static final PrefabEvent<Entity> FINISHED = PrefabEvent.withPlayer("finished", "Finished");

  /** Shows the outro to all players. */
  public static final PrefabAction START =
      PrefabAction.of("start", "Start", OutroTextPrefab::start);

  /** Whether the screen stays black after the last page. */
  public static final PrefabProperty<Boolean> END_ON_BLACK =
      PrefabProperty.bool("end-on-black", "End On Black", true);

  // Pages of the outro.
  private static final PrefabPages PAGES = PrefabPages.create();

  private static final List<PrefabProperty<?>> PROPERTIES = propertyList();

  /** Creates the outro-text definition. */
  public OutroTextPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored outro.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public OutroTextPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(START, FINISHED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    OUTROS.start(context.level(), instance.name(), new Outro());
    return List.of();
  }

  @Override
  public void onPlayerReady(PrefabCreationContext context, PrefabInstance instance, Entity player) {
    OUTROS
        .get(context.level(), instance.name())
        .filter(outro -> outro.started)
        .ifPresent(outro -> show(context, instance, outro, player));
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    OUTROS
        .end(context.level(), instance.name())
        .ifPresent(outro -> PrefabDialogs.closeAll(outro.open));
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(PAGES.summary(instance));
  }

  private static void start(ActionContext context) {
    PrefabCreationContext creation = context.creationContext();
    PrefabInstance instance = context.instance();
    OUTROS
        .get(context.level(), instance.name())
        .filter(outro -> !outro.started)
        .ifPresent(
            outro -> {
              outro.started = true;
              Game.allPlayers().toList().forEach(player -> show(creation, instance, outro, player));
            });
  }

  private static void show(
      PrefabCreationContext context, PrefabInstance instance, Outro outro, Entity player) {
    if (!outro.shownTo.add(player.id())) return;
    Runnable finished = () -> fire(context, instance, FINISHED, player);
    // Editing levels must never end on a black screen, quitting is skipped there.
    if (instance.get(END_ON_BLACK) && !LevelEditing.active()) {
      outro.open.add(
          BlackFadeCutscene.showAndHold(PAGES.messages(instance), true, finished, player.id()));
    } else {
      UIComponent[] shown = new UIComponent[1];
      shown[0] =
          BlackFadeCutscene.show(
              PAGES.messages(instance),
              true,
              true,
              () -> {
                outro.open.remove(shown[0]);
                finished.run();
              },
              player.id());
      outro.open.add(shown[0]);
    }
  }

  private static List<PrefabProperty<?>> propertyList() {
    List<PrefabProperty<?>> properties = new ArrayList<>(PAGES.properties());
    properties.add(END_ON_BLACK);
    return List.copyOf(properties);
  }

  private static final class Outro {
    private boolean started;
    // Entities of the players the outro was shown to.
    private final Set<Integer> shownTo = new HashSet<>();
    // Cutscenes still shown, closed when the instance despawns, e.g. on a reset.
    private final List<UIComponent> open = new ArrayList<>();
  }
}
