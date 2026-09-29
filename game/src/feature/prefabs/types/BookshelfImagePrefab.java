package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Server-side bookshelf that shows a configured image to the player interacting with it.
 *
 * <p>The image path is resolved through {@link engine.language.Localization#asset(String)} by the
 * displaying client, so a variant for its language (e.g. {@code image_en.png}) is used if it
 * exists, falling back to the given path otherwise.
 */
public final class BookshelfImagePrefab extends Prefab {

  /** Raised on the server when a player closed the image, with the player as payload. */
  public static final PrefabEvent<Entity> IMAGE_CLOSED = new PrefabEvent<>("image-closed");

  private static final String TYPE = "bookshelf-image";
  private static final String DISPLAY_NAME = "Bookshelf + Image";
  private static final float INTERACTION_RADIUS = 1.5f;

  /** Bottom-left position of the bookshelf. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  /** Asset path of the image to show. */
  public static final PrefabProperty<String> IMAGE =
      PrefabProperty.string(
          "image", "Image Path", "images/binary_hex.jpg", value -> !value.isBlank());

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(POSITION, IMAGE);

  /** Creates the bookshelf-image definition. */
  public BookshelfImagePrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one bookshelf-image instance.
   *
   * @param level owning level
   * @param name instance name
   */
  public BookshelfImagePrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Returns the bookshelf entity for this instance when it is currently spawned.
   *
   * @return the currently live bookshelf entity, if any
   */
  public Optional<Entity> bookshelfEntity() {
    return liveEntities().stream().findFirst();
  }

  /**
   * Registers a listener called on the server whenever a player closed the image of this instance.
   *
   * @param listener receives the player that closed the image
   */
  public void onImageClosed(Consumer<Entity> listener) {
    listen(IMAGE_CLOSED, listener);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity bookshelf = context.createEntity(instance.name());
    DecoFactory.createDeco(bookshelf, value(instance, POSITION), Deco.BookshelfLarge);
    bookshelf.remove(DecoComponent.class);

    String image = value(instance, IMAGE);
    bookshelf.add(
        new InteractionComponent(
            new Interaction(
                (entity, who) ->
                    PrefabDialogs.showImage(
                        image, who, () -> fire(context, instance, IMAGE_CLOSED, who)),
                INTERACTION_RADIUS)));
    return List.of(bookshelf);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    feedback.point(position, instance.name());
    if (selected) {
      feedback.label(position.translate(0, 0.6f), "(" + value(instance, IMAGE) + ")");
    }
  }
}
