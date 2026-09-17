package feature.prefabs.types;

import engine.Entity;
import engine.utils.Point;
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
import java.util.List;

/** Client-side bookshelf that opens a configured image when interacted with. */
public final class BookshelfImagePrefab extends Prefab {

  private static final float INTERACTION_RADIUS = 1.5f;
  private static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));
  private static final PrefabProperty<String> IMAGE =
      PrefabProperty.string(
          "image", "Image Path", "images/binary_hex.jpg", value -> !value.isBlank());

  /** Creates the bookshelf-image definition. */
  public BookshelfImagePrefab() {
    super("bookshelf-image", "Bookshelf + Image", PrefabSide.CLIENT, List.of(POSITION, IMAGE));
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity bookshelf = context.createEntity(instance.name());
    DecoFactory.createDeco(bookshelf, value(instance, POSITION), Deco.BookshelfLarge);
    bookshelf.remove(DecoComponent.class);

    ShowImageComponent showImage = new ShowImageComponent(value(instance, IMAGE));
    bookshelf.add(showImage);
    bookshelf.add(
        new InteractionComponent(
            new Interaction((entity, who) -> showImage.isUIOpen(true), INTERACTION_RADIUS)));
    return List.of(bookshelf);
  }

  @Override
  public void renderEditorFeedback(
      PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    feedback.point(position, instance.name());
    if(selected){
      feedback.label(position.translate(0, 0.6f), "("+value(instance, IMAGE)+")");
    }
  }
}
