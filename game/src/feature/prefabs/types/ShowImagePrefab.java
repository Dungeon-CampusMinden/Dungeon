package feature.prefabs.types;

import engine.Entity;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.Region;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Server-side rectangular trigger that shows an image when a player enters it.
 *
 * <p>A region collapsed to a single point is never entered, so the image is only shown through
 * {@link #SHOW_IMAGE}. The repetition applies to both ways of showing the image.
 *
 * <p>The image path is resolved through {@link engine.language.Localization#asset(String)} by the
 * displaying client, so a variant for its language (e.g. {@code image_en.png}) is used if it
 * exists, falling back to the given path otherwise.
 */
public final class ShowImagePrefab extends Prefab {

  /** Raised on the server when a player closed the image, with the player as payload. */
  public static final PrefabEvent<Entity> IMAGE_CLOSED =
      PrefabEvent.withPlayer("image-closed", "Image Closed");

  /** Shows the image to the player that caused the triggering event. */
  public static final PrefabAction SHOW_IMAGE =
      PrefabAction.forPlayer(
          "show-image",
          "Show Image",
          context -> {
            for (Entity trigger : context.liveEntities()) {
              for (Entity player : context.players()) {
                showImage(context.creationContext(), context.instance(), trigger, player);
              }
            }
          });

  private static final String TYPE = "show-image";
  private static final String DISPLAY_NAME = "Show Image";
  public static final PrefabProperty<Region> REGION =
      PrefabProperty.region("region", "Region", new Region(new Point(0, 0), new Point(1, 1)));
  public static final PrefabProperty<String> IMAGE =
      PrefabProperty.string(
          "image", "Image Path", "images/binary_hex.jpg", value -> !value.isBlank());
  public static final PrefabProperty<String> REPETITION = DialogRepetition.property();

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(REGION, IMAGE, REPETITION);

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
   * Returns the trigger entity for this instance when it is currently spawned.
   *
   * @return the currently live trigger entity, if any
   */
  public Optional<Entity> triggerEntity() {
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
  protected List<PrefabPort> declaredPorts() {
    return List.of(IMAGE_CLOSED, SHOW_IMAGE);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Region region = value(instance, REGION);
    Point bottomLeft = region.bottomLeft();
    Point topRight = region.topRight();
    DialogRepetition repetition = DialogRepetition.fromLabel(value(instance, REPETITION));

    Entity trigger = context.createEntity(instance.name());
    trigger.add(new PositionComponent(bottomLeft));
    trigger.add(new DialogRepetition.History(repetition));
    if (bottomLeft.equals(topRight)) return List.of(trigger);
    trigger.add(
        new CollideComponent(
                Vector2.ZERO,
                Vector2.of(topRight.x() - bottomLeft.x(), topRight.y() - bottomLeft.y()),
                (self, who, direction) -> {
                  if (who.isPresent(PlayerComponent.class)) {
                    showImage(context, instance, self, who);
                  }
                },
                CollideComponent.DEFAULT_COLLIDER)
            .isSolid(false));
    return List.of(trigger);
  }

  private static void showImage(
      PrefabCreationContext context, PrefabInstance instance, Entity trigger, Entity who) {
    boolean allowed =
        trigger
            .fetch(DialogRepetition.History.class)
            .map(history -> history.tryShow(who))
            .orElse(false);
    if (!allowed) return;
    PrefabDialogs.showImage(
        instance.get(IMAGE), who, () -> fire(context, instance, IMAGE_CLOSED, who));
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Region region = value(instance, REGION);
    feedback.region(region, instance.name());
    if (selected) {
      feedback.label(region.bottomLeft().translate(0, -0.45f), "(" + value(instance, IMAGE) + ")");
    }
  }
}
