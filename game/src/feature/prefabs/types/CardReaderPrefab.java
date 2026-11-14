package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.cardreader.CardReaderComponent;
import feature.interaction.cardreader.CardReaderFactory;
import feature.interaction.cardreader.Pace;
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
import java.util.List;

/**
 * Server-side card reader that raises an event when a player swiped a card through it at the right
 * pace. Link {@link #SWIPE_ACCEPTED} to e.g. {@link DoorControlPrefab#OPEN} to open a door with it.
 *
 * <p>Optionally the reader needs a card, linked from another prefab, e.g. an {@link
 * ItemSpawnerPrefab}. Players without the card see a dialog instead of the reader.
 *
 * <p>Like every prefab state, whether a single-use reader accepted a card lasts for the game
 * session only: after a restart or a reset of the instance, the reader takes a swipe again and
 * raises {@link #SWIPE_ACCEPTED} once more.
 */
public final class CardReaderPrefab extends Prefab {

  private static final String TYPE = "card-reader";
  private static final String DISPLAY_NAME = "Card Reader";
  private static final String CARD_PLACEHOLDER = "{card}";
  private static final int MAX_SWIPE_MS = 60_000;

  /** Raised when the reader accepted a swipe, with the player as payload. */
  public static final PrefabEvent<Entity> SWIPE_ACCEPTED =
      PrefabEvent.withPlayer("swipe-accepted", "Swipe Accepted");

  /** Raised when the reader rejected a swipe, with the player as payload. */
  public static final PrefabEvent<Entity> SWIPE_REJECTED =
      PrefabEvent.withPlayer("swipe-rejected", "Swipe Rejected");

  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0), new Point(0.5f, 0.5f));

  /** Fastest accepted swipe, in milliseconds. */
  public static final PrefabProperty<Integer> MIN_SWIPE =
      PrefabProperty.integer(
          "min-swipe-ms",
          "Fastest Swipe (ms)",
          (int) CardReaderFactory.DEFAULT_PACE.minMillis(),
          0,
          MAX_SWIPE_MS);

  /** Slowest accepted swipe, in milliseconds. */
  public static final PrefabProperty<Integer> MAX_SWIPE =
      PrefabProperty.integer(
          "max-swipe-ms",
          "Slowest Swipe (ms)",
          (int) CardReaderFactory.DEFAULT_PACE.maxMillis(),
          0,
          MAX_SWIPE_MS);

  /** Whether the reader stays locked once it accepted a swipe. */
  public static final PrefabProperty<Boolean> SINGLE_USE =
      PrefabProperty.bool("single-use", "Single Use", true);

  /** Whether an accepted swipe removes the card from the inventory of the player. */
  public static final PrefabProperty<Boolean> CONSUME_CARD =
      PrefabProperty.bool("consume-card", "Consume Card", false);

  /** Dialog script shown to players without the card, {@code {card}} is replaced by its name. */
  public static final PrefabProperty<String> MISSING_CARD_DIALOG =
      PrefabProperty.string(
          "missing-card-dialog",
          "Dialog Without Card ({card} = card name)",
          "You need this card to use the card reader: {card}.",
          value -> !value.isBlank());

  /** Kind of item players have to hold to use the reader; without a link, nobody needs one. */
  public static final ArtifactSlot<ItemArtifact> CARD =
      ArtifactSlot.single("card", "Card", ArtifactType.ITEM);

  private static final List<PrefabProperty<?>> PROPERTIES =
      List.of(POSITION, MIN_SWIPE, MAX_SWIPE, SINGLE_USE, CONSUME_CARD, MISSING_CARD_DIALOG);

  /** Creates the card reader definition. */
  public CardReaderPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored card reader.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public CardReaderPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(SWIPE_ACCEPTED, SWIPE_REJECTED, CARD);
  }

  @Override
  protected void validate(PrefabInstance instance) {
    if (value(instance, MIN_SWIPE) > value(instance, MAX_SWIPE)) {
      throw new IllegalArgumentException(
          "Prefab property 'min-swipe-ms' must not be greater than 'max-swipe-ms'");
    }
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity reader = context.createEntity(instance.name());
    CardReaderFactory.createCardReader(
        reader,
        value(instance, POSITION),
        new Pace(value(instance, MIN_SWIPE), value(instance, MAX_SWIPE)));
    CardReaderComponent component = reader.fetch(CardReaderComponent.class).orElseThrow();
    component.singleUse(value(instance, SINGLE_USE));
    ArtifactInput<ItemArtifact> card = input(context, instance, CARD);
    if (card.isLinked()) {
      component.requireCard(
          item -> card.value().filter(kind -> kind.matches(item)).isPresent(),
          value(instance, CONSUME_CARD),
          who -> {
            String name = card.value().map(ItemArtifact::displayName).orElse("card");
            DialogFactory.showDialogDialog(
                value(instance, MISSING_CARD_DIALOG).replace(CARD_PLACEHOLDER, name),
                () -> {},
                who.id());
          });
    }
    component.onAccepted(who -> fire(context, instance, SWIPE_ACCEPTED, who));
    component.onRejected(who -> fire(context, instance, SWIPE_REJECTED, who));
    return List.of(reader);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    Point offset = POSITION.editorFeedbackOffset();
    Point marker = position.translate(offset.x(), offset.y());
    feedback.point(marker, instance.name());
    if (selected && instance.links(CARD).isEmpty()) {
      feedback.label(marker.translate(0f, -0.45f), "(no card needed)");
    }
  }
}
