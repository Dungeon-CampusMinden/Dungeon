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
import feature.inventory.items.HintItem;
import feature.prefabs.ArtifactOutput;
import feature.prefabs.ArtifactType;
import feature.prefabs.ImagePuzzleSettings;
import feature.prefabs.ItemArtifact;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.PrefabWiring;
import feature.prefabs.PuzzlePiecePlacement;
import feature.puzzle.Puzzle;
import feature.puzzle.PuzzleMaker;
import feature.puzzle.PuzzlePieceItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** Server-side image puzzle with configurable piece destinations and item identities. */
public final class ImagePuzzlePrefab extends Prefab {

  private static final String TYPE = "image-puzzle";
  private static final String DISPLAY_NAME = "Image Puzzle";
  private static final String CONTAINER_TITLE = "Container";
  private static final String STATE_CONTAINERS = "containers";
  private static final String STATE_PIECES = "pieces";

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

  /** Raised when a player completed the puzzle, with the player as payload. */
  public static final PrefabEvent<Entity> COMPLETED =
      PrefabEvent.withPlayer("completed", "Completed");

  /** The picture item granted for completing the puzzle, e.g. to require it in a receptacle. */
  public static final ArtifactOutput<ItemArtifact> COMPLETION_ITEM =
      ArtifactOutput.of("completion-item", "Completion Item", ArtifactType.ITEM);

  /** Highest number of pieces that offer an output. */
  public static final int MAX_PIECE_OUTPUTS = 32;

  /**
   * One output per piece, e.g. to hand a piece out as a reward instead of placing it. Instances
   * only offer the outputs of their pieces, up to {@link #MAX_PIECE_OUTPUTS}.
   */
  public static final List<ArtifactOutput<ItemArtifact>> PIECES = pieceOutputs();

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
  protected List<PrefabPort> declaredPorts() {
    List<PrefabPort> ports = new ArrayList<>(List.of(COMPLETED, COMPLETION_ITEM));
    ports.addAll(PIECES);
    return ports;
  }

  @Override
  protected boolean isPortAvailable(PrefabInstance instance, PrefabPort port) {
    int index = PIECES.indexOf(port);
    return index < 0 || index < instance.get(SETTINGS).pieceCount();
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    String imagePath = value(instance, IMAGE);
    ImagePuzzleSettings settings = value(instance, SETTINGS);
    String itemName = value(instance, ITEM_NAME);
    String itemDescription = value(instance, ITEM_DESCRIPTION);
    String hintItemName = value(instance, HINT_ITEM_NAME);
    String hintItemDescription = value(instance, HINT_ITEM_DESCRIPTION);
    BiConsumer<Puzzle, Entity> onComplete =
        (completed, solver) -> {
          PuzzleMaker.defaultOnComplete().accept(completed, solver);
          fire(context, instance, COMPLETED, solver);
        };
    // Scoped by the instance name, so instances with equal settings do not share one puzzle.
    Puzzle puzzle =
        PuzzleMaker.makePuzzle(
            new SimpleIPath(imagePath),
            settings.pieceCount(),
            onComplete,
            settings.seed(),
            false,
            null,
            instance.name());
    publish(
        context,
        instance,
        COMPLETION_ITEM,
        ItemArtifact.of(
            new HintItem(new SimpleIPath(imagePath), hintItemName, hintItemDescription)));

    // A loaded game keeps the pieces that players took, they are saved with the players or the
    // level.
    Optional<JsonNode> saved = restoredState(context, instance);
    Iterator<JsonNode> savedContainers =
        saved.map(state -> state.path(STATE_CONTAINERS).iterator()).orElse(null);
    List<Entity> entities = new ArrayList<>(settings.pieceCount());
    for (int pieceIndex = 0; pieceIndex < settings.pieceCount(); pieceIndex++) {
      PuzzlePieceItem piece =
          puzzle
              .pieces()
              .get(pieceIndex)
              .withDisplayIdentity(itemName, itemDescription)
              .withCompletionHintIdentity(hintItemName, hintItemDescription);
      if (pieceIndex < PIECES.size()) {
        ArtifactOutput<ItemArtifact> output = PIECES.get(pieceIndex);
        publish(context, instance, output, ItemArtifact.of(piece));
        // A held piece is handed out by its holder, e.g. as a reward, and not placed here.
        if (PrefabWiring.isHeld(context.level().activePrefabs(), instance.name(), output.key())) {
          continue;
        }
      }
      PuzzlePiecePlacement placement = settings.pieces().get(pieceIndex);
      List<Item> content = List.of(piece);
      if (savedContainers != null
          && placement.destination() != PuzzlePiecePlacement.Destination.WORLD_ITEM) {
        content =
            savedContainers.hasNext()
                ? PrefabStateJson.itemList(savedContainers.next())
                : List.of();
      }
      switch (placement.destination()) {
        case WORLD_ITEM -> {
          if (saved.isEmpty()) {
            entities.add(WorldItemBuilder.buildWorldItem(piece, placement.position()));
          }
        }
        case CHEST ->
            entities.add(MiscFactory.newChest(new LinkedHashSet<>(content), placement.position()));
        case DECO_INVENTORY ->
            entities.add(
                createPieceContainer(
                    context,
                    instance.name(),
                    pieceIndex,
                    placement.position(),
                    placement.deco(),
                    content));
      }
    }
    // Pieces that were still lying where the puzzle put them when the game was saved.
    saved.ifPresent(
        state -> entities.addAll(PrefabStateJson.restoreWorldItems(state.path(STATE_PIECES))));
    return entities;
  }

  @Override
  public Optional<JsonNode> captureState(
      PrefabCreationContext context, PrefabInstance instance, List<Entity> entities) {
    ObjectNode state = PrefabStateJson.object();
    state.set(STATE_PIECES, PrefabStateJson.worldItems(entities));
    ArrayNode containers = state.putArray(STATE_CONTAINERS);
    // Chests and containers stay in the level and keep the order of their placements.
    entities.stream()
        .flatMap(entity -> entity.fetch(InventoryComponent.class).stream())
        .forEach(inventory -> containers.add(PrefabStateJson.items(inventory)));
    return Optional.of(state);
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
      List<Item> content) {
    Entity container = context.createEntity(instanceName + " piece container " + (pieceIndex + 1));
    DecoFactory.createDeco(container, position, deco);
    InventoryComponent inventory = new InventoryComponent(4);
    content.forEach(inventory::add);
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

  private static List<ArtifactOutput<ItemArtifact>> pieceOutputs() {
    List<ArtifactOutput<ItemArtifact>> outputs = new ArrayList<>(MAX_PIECE_OUTPUTS);
    for (int index = 0; index < MAX_PIECE_OUTPUTS; index++) {
      int number = index + 1;
      outputs.add(ArtifactOutput.of("piece-" + number, "Piece " + number, ArtifactType.ITEM));
    }
    return List.copyOf(outputs);
  }
}
