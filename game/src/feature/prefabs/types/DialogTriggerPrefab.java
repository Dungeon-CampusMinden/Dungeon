package feature.prefabs.types;

import engine.Entity;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import feature.hud.dialogs.DialogFactory;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.Region;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/** Server-side rectangular trigger that starts a dialog script when a player enters it. */
public final class DialogTriggerPrefab extends Prefab {

  /**
   * Raised on the server when a player finished or closed the dialog, with the player as payload.
   */
  public static final PrefabEvent<Entity> DIALOG_FINISHED = new PrefabEvent<>("dialog-finished");

  private static final String TYPE = "dialog-trigger";
  public static final PrefabProperty<Region> REGION =
      PrefabProperty.region("region", "Region", new Region(new Point(0, 0), new Point(1, 1)));
  public static final PrefabProperty<String> TEXT =
      PrefabProperty.string("text", "Dialog Script", "Hello.", value -> !value.isBlank());
  public static final PrefabProperty<Boolean> TRANSLATE =
      PrefabProperty.bool("translate", "Translate", false);
  public static final PrefabProperty<String> REPETITION = DialogRepetition.property();

  /** Creates the dialog-trigger definition. */
  public DialogTriggerPrefab() {
    super(TYPE, "Dialog Trigger", PrefabSide.SERVER, List.of(REGION, TEXT, TRANSLATE, REPETITION));
  }

  /**
   * Creates a bound view for one authored dialog-trigger instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public DialogTriggerPrefab(ILevel level, String name) {
    super(
        TYPE,
        "Dialog Trigger",
        PrefabSide.SERVER,
        List.of(REGION, TEXT, TRANSLATE, REPETITION),
        level,
        name);
  }

  /**
   * Returns the trigger entity for this instance when it is currently spawned.
   *
   * @return the currently live trigger entity, if any
   */
  public Optional<Entity> triggerEntity() {
    return liveEntities().stream().findFirst();
  }

  /**
   * Registers a listener called on the server whenever a player finished or closed the dialog of
   * this instance.
   *
   * @param listener receives the player that read the dialog
   */
  public void onDialogFinished(Consumer<Entity> listener) {
    listen(DIALOG_FINISHED, listener);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Region region = value(instance, REGION);
    Point bottomLeft = region.bottomLeft();
    Point topRight = region.topRight();
    float width = topRight.x() - bottomLeft.x();
    float height = topRight.y() - bottomLeft.y();
    String text = value(instance, TEXT);
    boolean translate = value(instance, TRANSLATE);
    DialogRepetition repetition = DialogRepetition.fromLabel(value(instance, REPETITION));
    TriggerState state = new TriggerState();

    Entity trigger = context.createEntity(instance.name());
    trigger.add(new PositionComponent(bottomLeft));
    trigger.add(
        new CollideComponent(
                Vector2.ZERO,
                Vector2.of(width, height),
                (self, who, direction) -> {
                  if (!who.isPresent(PlayerComponent.class)) return;
                  if (repetition == DialogRepetition.ONCE_GLOBALLY) {
                    if (state.triggeredForAll) return;
                    state.triggeredForAll = true;
                  } else if (repetition == DialogRepetition.ONCE_PER_PLAYER
                      && !state.players.add(who)) {
                    return;
                  }
                  DialogFactory.showDialogDialog(
                      text,
                      translate,
                      PrefabDialogs.once(() -> fire(context, instance, DIALOG_FINISHED, who)),
                      who.id());
                },
                CollideComponent.DEFAULT_COLLIDER)
            .isSolid(false));
    return List.of(trigger);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Region region = value(instance, REGION);
    feedback.point(region.bottomLeft(), null);
    feedback.point(region.topRight(), null);
    feedback.rectangle(region.bottomLeft(), region.topRight());
    feedback.label(midpoint(region.bottomLeft(), region.topRight()), instance.name());
  }

  private static Point midpoint(Point first, Point second) {
    return new Point(first.x() * 0.5f + second.x() * 0.5f, first.y() * 0.5f + second.y() * 0.5f);
  }

  /** Mutable, spawn-scoped trigger history captured by the collision callback. */
  private static final class TriggerState {
    private boolean triggeredForAll;
    private final Set<Entity> players = Collections.newSetFromMap(new IdentityHashMap<>());
  }
}
