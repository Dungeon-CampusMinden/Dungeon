package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;
import feature.inventory.items.HintItem;
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
import feature.trash.TrashMinigameFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import tools.jackson.databind.JsonNode;

/**
 * Server-side prefab of a trash can that players search in a minigame, digging through crumpled
 * papers. It can hide a reward, found once by one player.
 *
 * <p>The reward is an item, a hint item showing an image, or an item linked from another prefab,
 * e.g. a puzzle piece. A linked item is held by the trash can: the prefab producing it does not
 * place it. Resetting the instance hides the reward again.
 */
public final class TrashMinigamePrefab extends Prefab {

  private static final String TYPE = "trash-minigame";
  private static final String DISPLAY_NAME = "Trash Minigame";
  private static final String REWARD_NONE = "None";
  private static final String REWARD_ITEM = "Item";
  private static final String REWARD_HINT = "Hint Image";
  private static final String REWARD_LINKED = "Linked Item";
  private static final String STATE_AWARDED = "awarded";
  private static final float INTERACTION_RADIUS = 1.5f;
  // Whether the reward of a spawned instance was found.
  private static final SpawnState<boolean[]> AWARDED = new SpawnState<>();

  /** Raised when a player found the reward, with that player. */
  public static final PrefabEvent<Entity> ITEM_FOUND =
      PrefabEvent.withPlayer("item-found", "Item Found");

  /** Position of the trash can. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  /** Look of the trash can. */
  public static final PrefabProperty<String> DECO =
      PrefabProperty.decoration("deco", "Decoration", Deco.TrashCanBlue);

  /** Number of crumpled papers to dig through. */
  public static final PrefabProperty<Integer> PAPER_COUNT =
      PrefabProperty.integer("paper-count", "Paper Count", 30, 1, 100);

  /** Kind of reward hidden in the trash can. */
  public static final PrefabProperty<String> REWARD =
      PrefabProperty.selection(
          "reward",
          "Reward",
          REWARD_NONE,
          List.of(REWARD_NONE, REWARD_ITEM, REWARD_HINT, REWARD_LINKED));

  /** Item hidden as reward. */
  public static final PrefabProperty<String> ITEM =
      PrefabProperty.openSelection("item", "Item", "ItemKey", PrefabItems::choices)
          .visibleWhen(reward(REWARD_ITEM));

  /** Image of the hint item hidden as reward. */
  public static final PrefabProperty<String> IMAGE =
      PrefabProperty.string("image", "Image", "items/rpg/item_paper.png", value -> !value.isBlank())
          .visibleWhen(reward(REWARD_HINT));

  /** Name of the hint item. */
  public static final PrefabProperty<String> HINT_NAME =
      PrefabProperty.string("hint-name", "Hint Name", "Note", value -> !value.isBlank())
          .visibleWhen(reward(REWARD_HINT));

  /** Description of the hint item. */
  public static final PrefabProperty<String> HINT_DESCRIPTION =
      PrefabProperty.string(
              "hint-description", "Hint Description", "A crumpled note.", value -> true)
          .visibleWhen(reward(REWARD_HINT));

  /** Item of another prefab hidden as reward, held by the trash can. */
  public static final ArtifactSlot<ItemArtifact> REWARD_ITEM_SLOT =
      ArtifactSlot.single("reward-item", "Reward Item", ArtifactType.ITEM).holding();

  private static final List<PrefabProperty<?>> PROPERTIES =
      List.of(POSITION, DECO, PAPER_COUNT, REWARD, ITEM, IMAGE, HINT_NAME, HINT_DESCRIPTION);

  /** Creates the trash-minigame definition. */
  public TrashMinigamePrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored trash can.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public TrashMinigamePrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(REWARD_ITEM_SLOT, ITEM_FOUND);
  }

  @Override
  protected boolean isPortAvailable(PrefabInstance instance, PrefabPort port) {
    return port != REWARD_ITEM_SLOT || instance.get(REWARD).equals(REWARD_LINKED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity can = context.createEntity(instance.name());
    DecoFactory.createDeco(can, value(instance, POSITION), Deco.valueOf(value(instance, DECO)));
    // The trash can belongs to this prefab, not to the level's decorations.
    can.remove(DecoComponent.class);

    boolean[] awarded = AWARDED.start(context.level(), instance.name(), new boolean[1]);
    awarded[0] =
        restoredState(context, instance)
            .map(saved -> saved.path(STATE_AWARDED).asBoolean())
            .orElse(false);
    ArtifactInput<ItemArtifact> linked = input(context, instance, REWARD_ITEM_SLOT);
    can.add(
        new InteractionComponent(
            new Interaction(
                (self, who) -> {
                  // A new item every time, the minigame hands out the one that is found.
                  Item reward = awarded[0] ? null : reward(instance, linked).orElse(null);
                  TrashMinigameFactory.show(
                      who,
                      reward,
                      value(instance, PAPER_COUNT),
                      () -> !awarded[0],
                      () -> {
                        awarded[0] = true;
                        fire(context, instance, ITEM_FOUND, who);
                      });
                },
                INTERACTION_RADIUS)));
    return List.of(can);
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    AWARDED.end(context.level(), instance.name());
  }

  @Override
  public Optional<JsonNode> captureState(
      PrefabCreationContext context, PrefabInstance instance, List<Entity> entities) {
    return AWARDED
        .get(context.level(), instance.name())
        .filter(awarded -> awarded[0])
        .map(awarded -> PrefabStateJson.object().put(STATE_AWARDED, true));
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    feedback.point(position, instance.name());
    if (selected) {
      feedback.label(position.translate(0f, -0.2f), "(" + value(instance, REWARD) + ")");
    }
  }

  private static Optional<Item> reward(
      PrefabInstance instance, ArtifactInput<ItemArtifact> linked) {
    return switch (instance.get(REWARD)) {
      case REWARD_ITEM -> PrefabItems.create(instance.get(ITEM));
      case REWARD_HINT ->
          Optional.of(
              new HintItem(
                  new SimpleIPath(instance.get(IMAGE)),
                  instance.get(HINT_NAME),
                  instance.get(HINT_DESCRIPTION)));
      case REWARD_LINKED -> linked.value().flatMap(ItemArtifact::create);
      default -> Optional.empty();
    };
  }

  private static Predicate<Map<String, JsonNode>> reward(String kind) {
    return values -> {
      JsonNode reward = values.get(REWARD.key());
      return reward != null && kind.equals(reward.asText());
    };
  }
}
