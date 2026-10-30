package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import feature.components.UIComponent;
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
  // Images still shown by a spawned instance, closed without an event when it despawns.
  private static final SpawnState<List<UIComponent>> OPEN = new SpawnState<>();
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
    OPEN.start(context.level(), instance.name(), new ArrayList<>());
    return List.of();
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    OPEN.end(context.level(), instance.name()).ifPresent(PrefabDialogs::closeAll);
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(value(instance, IMAGE));
  }

  private static void show(ActionContext context) {
    PrefabCreationContext creation = context.creationContext();
    PrefabInstance instance = context.instance();
    List<UIComponent> open = OPEN.get(creation.level(), instance.name()).orElse(null);
    if (open == null) return;
    for (Entity player : PrefabAudience.players(context, AUDIENCE)) {
      UIComponent[] shown = new UIComponent[1];
      shown[0] =
          PrefabDialogs.showImage(
              instance.get(IMAGE),
              player,
              () -> {
                if (open.remove(shown[0])) fire(creation, instance, IMAGE_CLOSED, player);
              });
      open.add(shown[0]);
    }
  }
}
