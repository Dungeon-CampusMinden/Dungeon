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
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.systems.PositionSync;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Client-side decoration that opens a dialog when interacted with. */
public final class DecoDialogPrefab extends Prefab {

  private static final float INTERACTION_RADIUS = 1.5f;
  private static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));
  private static final PrefabProperty<String> DECO =
      PrefabProperty.selection(
          "deco",
          "Decoration",
          Deco.BookshelfLarge.name(),
          Arrays.stream(Deco.values()).map(Enum::name).toList());
  private static final PrefabProperty<Vector2> SCALE =
      PrefabProperty.vector2("scale", "Scale", Vector2.ONE);
  private static final PrefabProperty<String> TEXT =
      PrefabProperty.string("text", "Dialog Script", "Hello.", value -> !value.isBlank());
  private static final PrefabProperty<Boolean> ONCE = PrefabProperty.bool("once", "Once", false);

  /** Creates the decoration-dialog definition. */
  public DecoDialogPrefab() {
    super(
        "deco-dialog",
        "Decoration + Dialog",
        PrefabSide.CLIENT,
        List.of(POSITION, DECO, SCALE, TEXT, ONCE));
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
        PrefabSide.CLIENT,
        List.of(POSITION, DECO, SCALE, TEXT, ONCE),
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
    boolean once = value(instance, ONCE);
    InteractionComponent interaction =
        new InteractionComponent(
            new Interaction(
                (entity, who) -> {
                  DialogFactory.showDialogDialog(dialog, () -> {}, who.id());
                  if (once) entity.remove(InteractionComponent.class);
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
