package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.components.InventoryComponent;
import feature.components.ItemComponent;
import feature.components.UIComponent;
import feature.entities.MiscFactory;
import feature.entities.WorldItemBuilder;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;
import feature.prefabs.ImagePuzzleSettings;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.PuzzlePiecePlacement;
import feature.puzzle.Puzzle;
import feature.puzzle.PuzzleMaker;
import feature.puzzle.PuzzlePieceItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Server-side image puzzle with configurable piece destinations and item identities. */
public final class ImagePuzzlePrefab extends Prefab {

  private static final String TYPE = "image-puzzle";
  private static final String DISPLAY_NAME = "Image Puzzle";
  private static final String CONTAINER_TITLE = "Container";

  /** Source image asset path (resolved on each client when fragment textures are generated). */
  public static final PrefabProperty<String> IMAGE =
      PrefabProperty.string(
          "image", "Image Path", "images/binary_hex.jpg", value -> !value.isBlank());

  /** Piece count, slicing seed, and per-piece world placement and representation. */
  public static final PrefabProperty<ImagePuzzleSettings> SETTINGS =
      PrefabProperty.imagePuzzleSettings(
          "settings", "Puzzle Settings", ImagePuzzleSettings.defaults());

  /** Display name assigned to each generated piece item. */
  public static final PrefabProperty<String> ITEM_NAME =
      PrefabProperty.string(
          "pieceItemName", "Piece Item Name", "Puzzle Piece", value -> !value.isBlank());

  /** Description assigned to each generated piece item. */
  public static final PrefabProperty<String> ITEM_DESCRIPTION =
      PrefabProperty.string(
          "pieceItemDescription",
          "Piece Item Description",
          "A fragment of an image puzzle.",
          value -> !value.isBlank());

  /** Display name assigned to the hint item granted when the puzzle is completed. */
  public static final PrefabProperty<String> HINT_ITEM_NAME =
      PrefabProperty.string(
          "hintItemName", "Hint Item Name", "Puzzle Hint", value -> !value.isBlank());

  /** Description assigned to the hint item granted when the puzzle is completed. */
  public static final PrefabProperty<String> HINT_ITEM_DESCRIPTION =
      PrefabProperty.string(
          "hintItemDescription",
          "Hint Item Description",
          "A picture revealed after completing the puzzle.",
          value -> !value.isBlank());

  private static final List<PrefabProperty<?>> PROPERTIES =
      List.of(IMAGE, ITEM_NAME, ITEM_DESCRIPTION, HINT_ITEM_NAME, HINT_ITEM_DESCRIPTION, SETTINGS);

  /** Creates the image-puzzle definition. */
  public ImagePuzzlePrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored image-puzzle instance.
   *
   * @param level owning level
   * @param name instance name
   */
  public ImagePuzzlePrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Returns the current source image path for this bound instance.
   *
   * @return image path, or empty if the instance is inactive
   */
  public Optional<String> imagePath() {
    return currentInstance().map(instance -> value(instance, IMAGE));
  }

  /**
   * Returns the current typed puzzle settings for this bound instance.
   *
   * @return puzzle settings, or empty if the instance is inactive
   */
  public Optional<ImagePuzzleSettings> settings() {
    return currentInstance().map(instance -> value(instance, SETTINGS));
  }

  /**
   * Returns the current piece count for this bound instance.
   *
   * @return piece count, or empty if the instance is inactive
   */
  public Optional<Integer> pieceCount() {
    return settings().map(ImagePuzzleSettings::pieceCount);
  }

  /**
   * Returns the current deterministic slicing seed for this bound instance.
   *
   * @return slicing seed, or empty if the instance is inactive
   */
  public Optional<Long> seed() {
    return settings().map(ImagePuzzleSettings::seed);
  }

  /**
   * Returns the live world-item entities for this instance's puzzle fragments.
   *
   * @return live world-item entities
   */
  public List<Entity> pieceEntities() {
    return liveEntities().stream()
        .filter(entity -> entity.fetch(ItemComponent.class).isPresent())
        .toList();
  }

  /**
   * Returns the live deco containers for this instance.
   *
   * @return live container entities
   */
  public List<Entity> containers() {
    return liveEntities().stream()
        .filter(entity -> entity.fetch(InventoryComponent.class).isPresent())
        .toList();
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    String imagePath = value(instance, IMAGE);
    ImagePuzzleSettings settings = value(instance, SETTINGS);
    String itemName = value(instance, ITEM_NAME);
    String itemDescription = value(instance, ITEM_DESCRIPTION);
    String hintItemName = value(instance, HINT_ITEM_NAME);
    String hintItemDescription = value(instance, HINT_ITEM_DESCRIPTION);
    Puzzle puzzle =
        PuzzleMaker.makePuzzle(
            new SimpleIPath(imagePath), settings.pieceCount(), null, settings.seed());

    List<Entity> entities = new ArrayList<>(settings.pieceCount());
    for (int pieceIndex = 0; pieceIndex < settings.pieceCount(); pieceIndex++) {
      PuzzlePieceItem piece =
          puzzle
              .pieces()
              .get(pieceIndex)
              .withDisplayIdentity(itemName, itemDescription)
              .withCompletionHintIdentity(hintItemName, hintItemDescription);
      PuzzlePiecePlacement placement = settings.pieces().get(pieceIndex);
      switch (placement.destination()) {
        case WORLD_ITEM ->
            entities.add(WorldItemBuilder.buildWorldItem(piece, placement.position()));
        case CHEST -> entities.add(MiscFactory.newChest(Set.<Item>of(piece), placement.position()));
        case DECO_INVENTORY ->
            entities.add(
                createPieceContainer(
                    context,
                    instance.name(),
                    pieceIndex,
                    placement.position(),
                    placement.deco(),
                    piece));
      }
    }
    return entities;
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    ImagePuzzleSettings settings = value(instance, SETTINGS);
    for (int index = 0; index < settings.pieceCount(); index++) {
      Point point = settings.piecePosition(index);
      feedback.point(point, index == 0 ? instance.name() : Integer.toString(index + 1));
    }
  }

  private static Entity createPieceContainer(
      PrefabCreationContext context,
      String instanceName,
      int pieceIndex,
      Point position,
      Deco deco,
      Item piece) {
    Entity container = context.createEntity(instanceName + " piece container " + (pieceIndex + 1));
    DecoFactory.createDeco(container, position, deco);
    InventoryComponent inventory = new InventoryComponent(4);
    inventory.add(piece);
    container.add(inventory);
    container.add(
        new InteractionComponent(
            new Interaction(
                (interacted, interactor) ->
                    interactor
                        .fetch(InventoryComponent.class)
                        .ifPresent(
                            ignored -> {
                              DialogContext dialog =
                                  DialogContext.builder()
                                      .type(DialogType.DefaultTypes.DUAL_INVENTORY)
                                      .put(DialogContextKeys.ENTITY, interactor.id())
                                      .put(DialogContextKeys.SECONDARY_ENTITY, interacted.id())
                                      .put(DialogContextKeys.SECONDARY_TITLE, CONTAINER_TITLE)
                                      .put(DialogContextKeys.OWNER_ENTITY, interactor.id())
                                      .build();
                              interactor.add(new UIComponent(dialog, true, interactor.id()));
                            }))));
    return container;
  }
}
