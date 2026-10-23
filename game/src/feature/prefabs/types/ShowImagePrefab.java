package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Server-side image shown whenever {@link #SHOW_IMAGE} runs, e.g. when a player interacted with a
 * bookshelf.
 *
 * <p>The image is shown either to the player that caused the triggering event or to every player.
 * If the event carries no player, it is shown to every player.
 *
 * <p>The image path is resolved through {@link engine.language.Localization#asset(String)} by the
 * displaying client, so a variant for its language (e.g. {@code image_en.png}) is used if it
 * exists, falling back to the given path otherwise.
 */
public final class ShowImagePrefab extends Prefab {

  /** Raised on the server when a player closed the image, with the player as payload. */
  public static final PrefabEvent<Entity> IMAGE_CLOSED =
      PrefabEvent.withPlayer("image-closed", "Image Closed");

  /** Shows the image to its audience. */
  public static final PrefabAction SHOW_IMAGE =
      PrefabAction.forPlayer("show-image", "Show Image", ShowImagePrefab::show);

  private static final String TYPE = "show-image";
  private static final String DISPLAY_NAME = "Show Image";
  public static final PrefabProperty<String> IMAGE =
      PrefabProperty.string(
          "image", "Image Path", "images/binary_hex.jpg", value -> !value.isBlank());

  /** Who sees the image. */
  public static final PrefabProperty<String> AUDIENCE =
      PrefabAudience.property("Show For", PrefabAudience.TRIGGERING_PLAYER);

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(IMAGE, AUDIENCE);

  /** Creates the show-image definition. */
  public ShowImagePrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one show-image instance.
   *
   * @param level owning level
   * @param name instance name
   */
  public ShowImagePrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
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
  protected List<PrefabPort> declaredPorts() {
    return List.of(SHOW_IMAGE, IMAGE_CLOSED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    return List.of();
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(value(instance, IMAGE));
  }

  private static void show(ActionContext context) {
    PrefabCreationContext creation = context.creationContext();
    PrefabInstance instance = context.instance();
    for (Entity player : PrefabAudience.players(context, AUDIENCE)) {
      PrefabDialogs.showImage(
          instance.get(IMAGE), player, () -> fire(creation, instance, IMAGE_CLOSED, player));
    }
  }
}
