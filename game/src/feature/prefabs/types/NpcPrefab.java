package feature.prefabs.types;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.systems.VelocitySystem;
import engine.utils.Direction;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import feature.components.UIComponent;
import feature.entities.NPCFactory;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.utils.EntityUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;

/**
 * Server-side character players can interact with.
 *
 * <p>What happens on an interaction depends on the mode:
 *
 * <ul>
 *   <li>Dialog: says the next dialog of its list. The last one is repeated from then on.
 *   <li>Multiple Choice: shows one dialog ending in a question. Every answer raises its own event,
 *       like a {@link MultipleChoiceDialogPrefab}.
 *   <li>Advanced: only raises {@link #INTERACTED}, the behavior is wired up in the editor.
 * </ul>
 *
 * <p>Resetting the instance starts its list of dialogs from the beginning.
 */
public final class NpcPrefab extends Prefab {

  /** Maximum number of dialogs in dialog mode. */
  public static final int MAX_DIALOGS = 8;

  private static final String TYPE = "npc";
  private static final String DISPLAY_NAME = "NPC";
  private static final String MODE_DIALOG = "Dialog";
  private static final String MODE_CHOICE = "Multiple Choice";
  private static final String MODE_ADVANCED = "Advanced";
  private static final float INTERACTION_RADIUS = 1.5f;
  private static final SpawnState<Conversation> CONVERSATIONS = new SpawnState<>();
  private static final String STATE_NEXT_DIALOG = "next-dialog";

  /** Raised in advanced mode when a player interacted, with the player as payload. */
  public static final PrefabEvent<Entity> INTERACTED =
      PrefabEvent.withPlayer("interacted", "Interacted");

  /** Bottom-left position of the character. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  /** Look of the character. */
  public static final PrefabProperty<String> CHARACTER =
      PrefabProperty.selection(
          "character",
          "Character",
          Appearance.VILLAGER.label,
          Arrays.stream(Appearance.values()).map(appearance -> appearance.label).toList());

  /** What happens when a player interacts with the character. */
  public static final PrefabProperty<String> MODE =
      PrefabProperty.selection(
          "mode", "Mode", MODE_DIALOG, List.of(MODE_DIALOG, MODE_CHOICE, MODE_ADVANCED));

  /** Direction the character looks at when it spawns. */
  public static final PrefabProperty<String> VIEW_DIRECTION =
      PrefabProperty.selection(
          "view-direction", "View Direction", "Down", List.of("Down", "Up", "Left", "Right"));

  /** Whether the character turns to whoever interacted with it. */
  public static final PrefabProperty<Boolean> FACE_INTERACTOR =
      PrefabProperty.bool("face-interactor", "Face Interactor", true);

  /** Whether the dialogs and answers are translation keys. */
  public static final PrefabProperty<Boolean> TRANSLATE =
      PrefabProperty.bool("translate", "Translate", false)
          .visibleWhen(values -> !isMode(values, MODE_ADVANCED));

  /** Number of dialogs in dialog mode. */
  public static final PrefabProperty<Integer> DIALOG_COUNT =
      PrefabProperty.integer("dialog-count", "Dialog Count", 1, 1, MAX_DIALOGS)
          .visibleWhen(values -> isMode(values, MODE_DIALOG));

  /** Script of every dialog in dialog mode, only shown up to {@link #DIALOG_COUNT}. */
  public static final List<PrefabProperty<String>> DIALOGS =
      PrefabTextList.entries(
          DIALOG_COUNT,
          MAX_DIALOGS,
          "dialog",
          "Dialog",
          index -> "Hello.",
          values -> isMode(values, MODE_DIALOG));

  /** Dialog script ending in the question in multiple-choice mode. */
  public static final PrefabProperty<String> QUESTION =
      PrefabProperty.string("text", "Dialog Script", "Do you want to continue?", v -> !v.isBlank())
          .visibleWhen(values -> isMode(values, MODE_CHOICE));

  /** Whether the question has a cancel option and can be closed without choosing an answer. */
  public static final PrefabProperty<Boolean> CAN_CANCEL =
      PrefabProperty.bool("can-cancel", "Can Cancel", false)
          .visibleWhen(values -> isMode(values, MODE_CHOICE));

  /** Number of answers in multiple-choice mode. */
  public static final PrefabProperty<Integer> OPTION_COUNT =
      PrefabProperty.integer(
              "option-count", "Answer Count", 2, 1, MultipleChoiceDialogPrefab.MAX_OPTIONS)
          .visibleWhen(values -> isMode(values, MODE_CHOICE));

  /** Text of every answer in multiple-choice mode, only shown up to {@link #OPTION_COUNT}. */
  public static final List<PrefabProperty<String>> OPTIONS =
      PrefabTextList.entries(
          OPTION_COUNT,
          MultipleChoiceDialogPrefab.MAX_OPTIONS,
          "option",
          "Answer",
          PrefabDialogs::defaultAnswer,
          values -> isMode(values, MODE_CHOICE));

  private static final List<PrefabProperty<?>> PROPERTIES = propertyList();

  /** Creates the NPC definition. */
  public NpcPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored NPC.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public NpcPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    List<PrefabPort> ports = new ArrayList<>();
    ports.add(INTERACTED);
    ports.addAll(MultipleChoiceDialogPrefab.OPTION_CHOSEN);
    ports.add(MultipleChoiceDialogPrefab.CANCELLED);
    return ports;
  }

  @Override
  protected boolean isPortAvailable(PrefabInstance instance, PrefabPort port) {
    if (port == INTERACTED) return instance.get(MODE).equals(MODE_ADVANCED);
    boolean asksQuestion = instance.get(MODE).equals(MODE_CHOICE);
    if (port == MultipleChoiceDialogPrefab.CANCELLED) {
      return asksQuestion && instance.get(CAN_CANCEL);
    }
    int option = MultipleChoiceDialogPrefab.OPTION_CHOSEN.indexOf(port);
    return option < 0 || (asksQuestion && option < instance.get(OPTION_COUNT));
  }

  @Override
  public String portDisplayName(PrefabInstance instance, PrefabPort port) {
    int option = MultipleChoiceDialogPrefab.OPTION_CHOSEN.indexOf(port);
    if (option < 0 || option >= instance.get(OPTION_COUNT)) return port.displayName();
    return PrefabTextList.portName(port, OPTIONS.get(option).get(instance));
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Appearance appearance = Appearance.fromLabel(value(instance, CHARACTER));
    Entity npc = context.createEntity(instance.name());
    NPCFactory.createNPC(npc, value(instance, POSITION), appearance.sprite);
    if (appearance.oneTileWide) {
      // The factory's hitbox fits two-tile sprites.
      npc.remove(CollideComponent.class);
      npc.add(new CollideComponent(Vector2.of(0.1f, 0.1f), Vector2.of(0.8f, 0.8f)).isSolid(true));
    }
    face(npc, Direction.fromString(value(instance, VIEW_DIRECTION)));

    Conversation conversation =
        CONVERSATIONS.start(context.level(), instance.name(), new Conversation());
    restoredState(context, instance)
        .ifPresent(saved -> conversation.nextDialog = saved.path(STATE_NEXT_DIALOG).asInt());
    npc.add(
        new InteractionComponent(
            new Interaction(
                (self, who) -> interact(context, instance, conversation, self, who),
                INTERACTION_RADIUS)));
    return List.of(npc);
  }

  @Override
  public Optional<JsonNode> captureState(
      PrefabCreationContext context, PrefabInstance instance, List<Entity> entities) {
    return CONVERSATIONS
        .get(context.level(), instance.name())
        .filter(conversation -> conversation.nextDialog > 0)
        .map(
            conversation ->
                PrefabStateJson.object().put(STATE_NEXT_DIALOG, conversation.nextDialog));
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    CONVERSATIONS
        .end(context.level(), instance.name())
        .ifPresent(conversation -> PrefabDialogs.closeAll(conversation.open));
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    feedback.point(value(instance, POSITION), instance.name());
  }

  private static void interact(
      PrefabCreationContext context,
      PrefabInstance instance,
      Conversation conversation,
      Entity npc,
      Entity who) {
    if (instance.get(FACE_INTERACTOR)) face(npc, directionTo(npc, who));
    switch (instance.get(MODE)) {
      case MODE_DIALOG -> say(instance, conversation, who);
      case MODE_CHOICE -> ask(context, instance, conversation, who);
      default -> fire(context, instance, INTERACTED, who);
    }
  }

  private static void say(PrefabInstance instance, Conversation conversation, Entity who) {
    int index = Math.min(conversation.nextDialog, instance.get(DIALOG_COUNT) - 1);
    conversation.nextDialog = index + 1;
    UIComponent[] shown = new UIComponent[1];
    shown[0] =
        DialogFactory.showDialogDialog(
            DIALOGS.get(index).get(instance),
            instance.get(TRANSLATE),
            () -> conversation.open.remove(shown[0]),
            who.id());
    conversation.open.add(shown[0]);
  }

  private static void ask(
      PrefabCreationContext context,
      PrefabInstance instance,
      Conversation conversation,
      Entity who) {
    PrefabDialogs.ask(
        new PrefabDialogs.Question(
            instance.get(QUESTION),
            instance.get(TRANSLATE),
            PrefabTextList.values(instance, OPTION_COUNT, OPTIONS),
            instance.get(CAN_CANCEL),
            Map.of()),
        who,
        conversation.open,
        option ->
            fire(context, instance, MultipleChoiceDialogPrefab.OPTION_CHOSEN.get(option), who),
        () -> fire(context, instance, MultipleChoiceDialogPrefab.CANCELLED, who));
  }

  /**
   * Turns a character without moving it.
   *
   * @param npc character with a position and draw component
   * @param direction new view direction
   */
  private static void face(Entity npc, Direction direction) {
    npc.fetch(PositionComponent.class).ifPresent(position -> position.viewDirection(direction));
    npc.fetch(DrawComponent.class)
        .ifPresent(draw -> draw.sendSignal(VelocitySystem.IDLE_SIGNAL, direction));
  }

  private static Direction directionTo(Entity npc, Entity who) {
    Vector2 offset = EntityUtils.getPosition(npc).vectorTo(EntityUtils.getPosition(who));
    if (Math.abs(offset.x()) >= Math.abs(offset.y())) {
      return offset.x() > 0 ? Direction.RIGHT : Direction.LEFT;
    }
    return offset.y() > 0 ? Direction.UP : Direction.DOWN;
  }

  private static boolean isMode(Map<String, JsonNode> values, String mode) {
    JsonNode current = values.get(MODE.key());
    return mode.equals(current == null ? MODE.defaultValue() : current.asText());
  }

  private static List<PrefabProperty<?>> propertyList() {
    List<PrefabProperty<?>> properties =
        new ArrayList<>(
            List.of(POSITION, CHARACTER, MODE, VIEW_DIRECTION, FACE_INTERACTOR, TRANSLATE));
    properties.add(DIALOG_COUNT);
    properties.addAll(DIALOGS);
    properties.add(QUESTION);
    properties.add(CAN_CANCEL);
    properties.add(OPTION_COUNT);
    properties.addAll(OPTIONS);
    return List.copyOf(properties);
  }

  /** Runtime state of one spawned NPC. */
  private static final class Conversation {
    // Dialogs still shown by the NPC, closed without an event when it despawns.
    private final List<UIComponent> open = new ArrayList<>();
    private int nextDialog;
  }

  /** Character sprites with animations for all four view directions. */
  private enum Appearance {
    VILLAGER("Villager", "character/char03", false),
    ROGUE("Rogue", "character/rogue", false),
    KNIGHT("Knight", "character/knight", true),
    WIZARD("Wizard", "character/wizard", true),
    ROBOT("Robot", "character/roboter_cleaner", true);

    private final String label;
    private final String sprite;
    private final boolean oneTileWide;

    Appearance(String label, String sprite, boolean oneTileWide) {
      this.label = label;
      this.sprite = sprite;
      this.oneTileWide = oneTileWide;
    }

    private static Appearance fromLabel(String label) {
      for (Appearance appearance : values()) {
        if (appearance.label.equals(label)) return appearance;
      }
      throw new IllegalArgumentException("Unknown NPC character: " + label);
    }
  }
}
