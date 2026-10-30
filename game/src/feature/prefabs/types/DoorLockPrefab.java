package feature.prefabs.types;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.Point;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;
import feature.prefabs.ActionContext;
import feature.prefabs.ArtifactInput;
import feature.prefabs.ArtifactSlot;
import feature.prefabs.ArtifactType;
import feature.prefabs.ItemArtifact;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
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
 * Server-side chain lock on a door, which players open with a key item, like the archive door in
 * System Recovery.
 *
 * <p>The key is linked from another prefab, e.g. an {@link ItemSpawnerPrefab}. Players without the
 * key see the locked dialog, players with the key are asked whether to use it. The door stays
 * closed until the lock is opened, and is closed again when the instance is reset.
 */
public final class DoorLockPrefab extends Prefab {

  private static final String TYPE = "door-lock";
  private static final String DISPLAY_NAME = "Door Lock";
  private static final SimpleIPath CHAIN_TEXTURE = new SimpleIPath("other/chain_lock.png");
  private static final float INTERACTION_RADIUS = 2f;
  // The chain hangs slightly below the door tile, like the System Recovery door blocker.
  private static final float CHAIN_OFFSET_Y = -0.4f;
  private static final String KEY_PLACEHOLDER = "{key}";

  /** Position of the locked door tile. */
  public static final PrefabProperty<Point> DOOR_POSITION =
      PrefabProperty.point("doorPosition", "Door Position", new Point(0, 0), new Point(0.5f, 0.5f));

  /** Dialog script shown to players without the key. */
  public static final PrefabProperty<String> LOCKED_DIALOG =
      PrefabProperty.string(
          "locked-dialog",
          "Dialog Without Key",
          "The door is locked with a chain. You need a key to open it.",
          value -> !value.isBlank());

  /** Title of the question shown to players with the key. */
  public static final PrefabProperty<String> KEY_TITLE =
      PrefabProperty.string(
          "key-title", "Title With Key", "Locked Door", value -> !value.isBlank());

  /** Question shown to players with the key, {@code {key}} is replaced by the key name. */
  public static final PrefabProperty<String> KEY_QUESTION =
      PrefabProperty.string(
          "key-question",
          "Question With Key ({key} = key name)",
          "Do you want to use your {key} to open the door?",
          value -> !value.isBlank());

  /** Whether the key is removed from the inventory of the player that opened the lock. */
  public static final PrefabProperty<Boolean> CONSUME_KEY =
      PrefabProperty.bool("consume-key", "Consume Key", true);

  /** Kind of item that opens the lock. */
  public static final ArtifactSlot<ItemArtifact> KEY =
      ArtifactSlot.single("key", "Key", ArtifactType.ITEM);

  /**
   * Raised when the lock was opened, with the key or through {@link #UNLOCK}, with the player that
   * caused it as payload, if any.
   */
  public static final PrefabEvent<Entity> UNLOCKED = PrefabEvent.withPlayer("unlocked", "Unlocked");

  /** Opens the lock and the door without a key. */
  public static final PrefabAction UNLOCK =
      PrefabAction.of("unlock", "Unlock", DoorLockPrefab::unlock);

  private static final List<PrefabProperty<?>> PROPERTIES =
      List.of(DOOR_POSITION, LOCKED_DIALOG, KEY_TITLE, KEY_QUESTION, CONSUME_KEY);

  /** Creates the door-lock definition. */
  public DoorLockPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored door lock.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public DoorLockPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(KEY, UNLOCKED, UNLOCK);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Point doorPosition = value(instance, DOOR_POSITION);
    Optional<DoorTile> door = doorAt(context.level(), doorPosition);
    if (door.isEmpty()) return List.of();
    door.get().close();

    Entity chain = context.createEntity(instance.name());
    chain.add(new PositionComponent(doorPosition.translate(0f, CHAIN_OFFSET_Y)));
    chain.add(new DrawComponent(new Animation(CHAIN_TEXTURE)));
    ArtifactInput<ItemArtifact> key = input(context, instance, KEY);
    chain.add(
        new InteractionComponent(
            new Interaction(
                (interacted, who) -> interact(context, instance, door.get(), key, interacted, who),
                INTERACTION_RADIUS)));
    return List.of(chain);
  }

  // Without its lock, the door is no longer kept closed.
  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    doorAt(context.level(), value(instance, DOOR_POSITION)).ifPresent(DoorTile::open);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, DOOR_POSITION);
    Point offset = DOOR_POSITION.editorFeedbackOffset();
    Point marker = position.translate(offset.x(), offset.y());
    boolean hasDoor = doorAt(level, position).isPresent();
    feedback.point(marker, hasDoor ? instance.name() : instance.name() + " (missing door)");
    if (selected && instance.links(KEY).isEmpty()) {
      feedback.label(marker.translate(0f, -0.45f), "(no key linked)");
    }
  }

  private static void interact(
      PrefabCreationContext context,
      PrefabInstance instance,
      DoorTile door,
      ArtifactInput<ItemArtifact> key,
      Entity chain,
      Entity who) {
    Optional<InventoryComponent> inventory = who.fetch(InventoryComponent.class);
    if (inventory.isEmpty()) return;
    Optional<ItemArtifact> keyKind = key.value();
    if (keyKind.isEmpty() || findKey(inventory.get(), keyKind.get()).isEmpty()) {
      DialogFactory.showDialogDialog(instance.get(LOCKED_DIALOG), () -> {}, who.id());
      return;
    }
    UIComponent question =
        DialogFactory.show(
            DialogContext.builder()
                .type(DialogType.DefaultTypes.YES_NO)
                .put(DialogContextKeys.TITLE, instance.get(KEY_TITLE))
                .put(
                    DialogContextKeys.MESSAGE,
                    instance
                        .get(KEY_QUESTION)
                        .replace(KEY_PLACEHOLDER, keyKind.get().displayName()))
                .build(),
            who.id());
    question.registerCallback(
        DialogContextKeys.ON_YES,
        data -> {
          UIUtils.closeDialog(question);
          // The key may have been dropped while the question was open.
          Optional<Item> held = findKey(inventory.get(), keyKind.get());
          if (held.isEmpty() || Game.findEntityById(chain.id()).isEmpty()) return;
          if (instance.get(CONSUME_KEY)) inventory.get().removeOne(held.get());
          Game.remove(chain);
          door.open();
          fire(context, instance, UNLOCKED, who);
        });
    question.registerCallback(DialogContextKeys.ON_NO, data -> UIUtils.closeDialog(question));
  }

  private static void unlock(ActionContext context) {
    List<Entity> chains = context.liveEntities();
    // Already unlocked, or there is no door to lock.
    if (chains.isEmpty()) return;
    chains.forEach(Game::remove);
    doorAt(context.level(), context.value(DOOR_POSITION)).ifPresent(DoorTile::open);
    fire(context.creationContext(), context.instance(), UNLOCKED, context.player().orElse(null));
  }

  private static Optional<Item> findKey(InventoryComponent inventory, ItemArtifact key) {
    return Arrays.stream(inventory.items()).filter(key::matches).findFirst();
  }

  private static Optional<DoorTile> doorAt(ILevel level, Point position) {
    return level.tileAt(position).filter(DoorTile.class::isInstance).map(DoorTile.class::cast);
  }
}
