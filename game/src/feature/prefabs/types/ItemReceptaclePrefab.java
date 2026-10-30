package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.components.DecoComponent;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;
import feature.prefabs.ArtifactInput;
import feature.prefabs.ArtifactSlot;
import feature.prefabs.ArtifactType;
import feature.prefabs.ItemArtifact;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Server-side container players can put items into, e.g. a chest that reacts when the picture of a
 * completed puzzle is placed inside.
 *
 * <p>The item kind it waits for is linked from another prefab, such as {@link
 * ImagePuzzlePrefab#COMPLETION_ITEM}. The container starts empty, it only reacts to the items
 * players put inside.
 */
public final class ItemReceptaclePrefab extends Prefab {

  private static final String TYPE = "item-receptacle";
  private static final String DISPLAY_NAME = "Item Receptacle";
  private static final int CAPACITY = 8;

  /** Position of the container. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  /** Decoration shown for the container. */
  public static final PrefabProperty<String> DECO =
      PrefabProperty.selection(
          "deco",
          "Decoration",
          Deco.Cabinet.name(),
          Arrays.stream(Deco.values()).map(Enum::name).toList());

  /** Title of the container in the inventory dialog. */
  public static final PrefabProperty<String> TITLE =
      PrefabProperty.string("title", "Title", "Container", value -> !value.isBlank());

  /** Whether the required item disappears once it was placed inside. */
  public static final PrefabProperty<Boolean> CONSUME =
      PrefabProperty.bool("consume", "Consume Required Item", false);

  /** Kind of item the container waits for. */
  public static final ArtifactSlot<ItemArtifact> REQUIRED_ITEM =
      ArtifactSlot.single("required-item", "Required Item", ArtifactType.ITEM);

  /** Raised when a player placed the required item inside, with the player as payload. */
  public static final PrefabEvent<Entity> REQUIRED_ITEM_PLACED =
      PrefabEvent.withPlayer("required-item-placed", "Required Item Placed");

  /** Raised when the required item was taken out again. */
  public static final PrefabEvent<Void> REQUIRED_ITEM_REMOVED =
      PrefabEvent.signal("required-item-removed", "Required Item Removed");

  /** Raised when a player placed any other item inside, with the player as payload. */
  public static final PrefabEvent<Entity> OTHER_ITEM_PLACED =
      PrefabEvent.withPlayer("other-item-placed", "Other Item Placed");

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(POSITION, DECO, TITLE, CONSUME);

  /** Creates the item-receptacle definition. */
  public ItemReceptaclePrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored item receptacle.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public ItemReceptaclePrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Returns the container entity for this instance when it is currently spawned.
   *
   * @return the currently live container entity, if any
   */
  public Optional<Entity> containerEntity() {
    return liveEntities().stream().findFirst();
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(REQUIRED_ITEM, REQUIRED_ITEM_PLACED, REQUIRED_ITEM_REMOVED, OTHER_ITEM_PLACED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity container = context.createEntity(instance.name());
    DecoFactory.createDeco(
        container, value(instance, POSITION), Deco.valueOf(value(instance, DECO)));
    container.remove(DecoComponent.class);
    InventoryComponent inventory = new InventoryComponent(CAPACITY);
    container.add(inventory);

    ArtifactInput<ItemArtifact> required = input(context, instance, REQUIRED_ITEM);
    boolean consume = value(instance, CONSUME);
    boolean[] consuming = {false};
    inventory.onItemAdded(
        item -> {
          if (item == null) return;
          if (matches(required, item)) {
            if (consume) {
              consuming[0] = true;
              try {
                inventory.remove(item);
              } finally {
                consuming[0] = false;
              }
            }
            fire(context, instance, REQUIRED_ITEM_PLACED, mover(inventory));
          } else {
            fire(context, instance, OTHER_ITEM_PLACED, mover(inventory));
          }
        });
    inventory.onItemRemoved(
        item -> {
          if (item != null && !consuming[0] && matches(required, item)) {
            fire(context, instance, REQUIRED_ITEM_REMOVED, null);
          }
        });

    String title = value(instance, TITLE);
    container.add(
        new InteractionComponent(
            new Interaction(
                (interacted, interactor) -> {
                  if (interactor.fetch(InventoryComponent.class).isEmpty()) return;
                  DialogContext dialog =
                      DialogContext.builder()
                          .type(DialogType.DefaultTypes.DUAL_INVENTORY)
                          .put(DialogContextKeys.ENTITY, interactor.id())
                          .put(DialogContextKeys.SECONDARY_ENTITY, interacted.id())
                          .put(DialogContextKeys.SECONDARY_TITLE, title)
                          .put(DialogContextKeys.OWNER_ENTITY, interactor.id())
                          .build();
                  interactor.add(new UIComponent(dialog, true, interactor.id()));
                })));
    return List.of(container);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    feedback.point(value(instance, POSITION), instance.name());
  }

  // HeroController records the moving player before it moves an item, so it is known here.
  private static Entity mover(InventoryComponent inventory) {
    return inventory.lastMove().map(InventoryComponent.Move::player).orElse(null);
  }

  private static boolean matches(ArtifactInput<ItemArtifact> required, Item item) {
    return required.value().filter(kind -> kind.matches(item)).isPresent();
  }
}
