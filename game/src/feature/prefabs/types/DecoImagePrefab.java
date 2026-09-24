package feature.prefabs.types;

import engine.Entity;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.DecoComponent;
import feature.components.ShowImageComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
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

/** Client-side decoration that opens an image when interacted with. */
public final class DecoImagePrefab extends Prefab {

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
  private static final PrefabProperty<String> IMAGE =
      PrefabProperty.string(
          "image", "Image Path", "images/binary_hex.jpg", value -> !value.isBlank());

  /** Creates the decoration-image definition. */
  public DecoImagePrefab() {
    super(
        "deco-image",
        "Decoration + Image",
        PrefabSide.CLIENT,
        List.of(POSITION, DECO, SCALE, IMAGE));
  }

  /**
   * Creates a bound view for one authored decoration-image instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public DecoImagePrefab(ILevel level, String name) {
    super(
        "deco-image",
        "Decoration + Image",
        PrefabSide.CLIENT,
        List.of(POSITION, DECO, SCALE, IMAGE),
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
    if (!Float.isFinite(scale.x())
        || !Float.isFinite(scale.y())
        || scale.x() <= 0
        || scale.y() <= 0) {
      throw new IllegalArgumentException(
          "Prefab property 'scale' must contain positive finite components");
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

    ShowImageComponent showImage = new ShowImageComponent(value(instance, IMAGE));
    deco.add(showImage);
    deco.add(
        new InteractionComponent(
            new Interaction((entity, who) -> showImage.isUIOpen(true), INTERACTION_RADIUS)));
    return List.of(deco);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    feedback.point(value(instance, POSITION), instance.name());
  }
}
