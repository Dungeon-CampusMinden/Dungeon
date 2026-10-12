package feature.prefabs.types;

import engine.Entity;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.systems.PositionSync;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/** Server-side decoration that shows a dialog to the player interacting with it. */
public final class DecoDialogPrefab extends Prefab {

  /**
   * Raised on the server when a player finished or closed the dialog, with the player as payload.
   */
  public static final PrefabEvent<Entity> DIALOG_FINISHED = new PrefabEvent<>("dialog-finished");

  private static final float INTERACTION_RADIUS = 1.5f;
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));
  public static final PrefabProperty<String> DECO =
      PrefabProperty.selection(
          "deco",
          "Decoration",
          Deco.BookshelfLarge.name(),
          Arrays.stream(Deco.values()).map(Enum::name).toList());
  public static final PrefabProperty<Vector2> SCALE =
      PrefabProperty.vector2("scale", "Scale", Vector2.ONE);
  public static final PrefabProperty<String> TEXT =
      PrefabProperty.string("text", "Dialog Script", "Hello.", value -> !value.isBlank());
  public static final PrefabProperty<Boolean> TRANSLATE =
      PrefabProperty.bool("translate", "Translate", false);
  public static final PrefabProperty<String> REPETITION = DialogRepetition.property();

  /** Creates the decoration-dialog definition. */
  public DecoDialogPrefab() {
    super(
        "deco-dialog",
        "Decoration + Dialog",
        PrefabSide.SERVER,
        List.of(POSITION, DECO, SCALE, TEXT, TRANSLATE, REPETITION));
  }

  /**
   * Creates a bound view for one authored decoration-dialog instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public DecoDialogPrefab(ILevel level, String name) {
    super(
        "deco-dialog",
        "Decoration + Dialog",
        PrefabSide.SERVER,
        List.of(POSITION, DECO, SCALE, TEXT, TRANSLATE, REPETITION),
        level,
        name);
  }

  /**
   * Returns the decoration entity for this instance when it is currently spawned.
   *
   * @return the currently live decoration entity, if any
   */
  public Optional<Entity> decoEntity() {
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
  protected void validate(PrefabInstance instance) {
    Vector2 scale = value(instance, SCALE);
    if (scale.x() <= 0 || scale.y() <= 0) {
      throw new IllegalArgumentException("Prefab property 'scale' must be positive");
    }
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity deco = context.createEntity(instance.name());
    DecoFactory.createDeco(deco, value(instance, POSITION), Deco.valueOf(value(instance, DECO)));
    deco.remove(DecoComponent.class);

    PositionComponent position = deco.fetch(PositionComponent.class).orElseThrow();
    position.scale(value(instance, SCALE));
    PositionSync.syncPosition(deco);

    String dialog = value(instance, TEXT);
    boolean translate = value(instance, TRANSLATE);
    DialogRepetition repetition = DialogRepetition.fromLabel(value(instance, REPETITION));
    Set<Entity> players = Collections.newSetFromMap(new IdentityHashMap<>());
    InteractionComponent interaction =
        new InteractionComponent(
            new Interaction(
                (entity, who) -> {
                  if (repetition == DialogRepetition.ONCE_PER_PLAYER && !players.add(who)) return;
                  DialogFactory.showDialogDialog(
                      dialog,
                      translate,
                      PrefabDialogs.once(() -> fire(context, instance, DIALOG_FINISHED, who)),
                      who.id());
                  if (repetition == DialogRepetition.ONCE_GLOBALLY) {
                    entity.remove(InteractionComponent.class);
                  }
                },
                INTERACTION_RADIUS));
    deco.add(interaction);
    return List.of(deco);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    feedback.point(position, instance.name());
  }
}
