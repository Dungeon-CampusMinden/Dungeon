package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.ChoiceOptions;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.prefabs.ActionContext;
import feature.prefabs.ArtifactSlot;
import feature.prefabs.ArtifactType;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;

/**
 * Server-side dialog script ending in a question with up to {@link #MAX_OPTIONS} answers, shown
 * whenever {@link #SHOW_DIALOG} runs.
 *
 * <p>Every answer has its own event, so each one can trigger something different. The dialog is
 * shown either to the player that caused the triggering event or to every player; each player
 * answers for themselves. If the dialog can be cancelled, a cancel option is added and closing the
 * dialog raises {@link #CANCELLED}. Otherwise the player has to choose an answer.
 *
 * <p>Placeholders are replaced in the script and in the answers. Answers that are translation keys
 * are translated by the displaying client.
 */
public final class MultipleChoiceDialogPrefab extends Prefab {

  /** Maximum number of answers. */
  public static final int MAX_OPTIONS = 6;

  private static final String TYPE = "choice-dialog";
  private static final String DISPLAY_NAME = "Multiple Choice Dialog";
  // Dialogs still shown by a spawned instance, closed without an event when it despawns.
  private static final SpawnState<List<UIComponent>> OPEN = new SpawnState<>();

  /** Shows the dialog to its audience. */
  public static final PrefabAction SHOW_DIALOG =
      PrefabAction.forPlayer("show-dialog", "Show Dialog", MultipleChoiceDialogPrefab::show);

  /** Raised on the server when a player cancelled the dialog, with the player as payload. */
  public static final PrefabEvent<Entity> CANCELLED =
      PrefabEvent.withPlayer("cancelled", "Cancelled");

  public static final PrefabProperty<String> TEXT =
      PrefabProperty.string(
          "text", "Dialog Script", "Do you want to continue?", value -> !value.isBlank());
  public static final PrefabProperty<Boolean> TRANSLATE =
      PrefabProperty.bool("translate", "Translate", false);

  /** Who sees the dialog. */
  public static final PrefabProperty<String> AUDIENCE =
      PrefabAudience.property("Show For", PrefabAudience.TRIGGERING_PLAYER);

  /** Whether the dialog has a cancel option and can be closed without choosing an answer. */
  public static final PrefabProperty<Boolean> CAN_CANCEL =
      PrefabProperty.bool("can-cancel", "Can Cancel", false);

  /** Number of answers. */
  public static final PrefabProperty<Integer> OPTION_COUNT =
      PrefabProperty.integer("option-count", "Answer Count", 2, 1, MAX_OPTIONS);

  /** Text of every answer, only shown up to {@link #OPTION_COUNT}. */
  public static final List<PrefabProperty<String>> OPTIONS = optionProperties();

  /**
   * Event of every answer, raised on the server when a player chose it, with the player as payload.
   * Instances only offer the events of their used answers.
   */
  public static final List<PrefabEvent<Entity>> OPTION_CHOSEN = optionEvents();

  /** Values shown in the script and the answers through {@code {alias}} placeholders. */
  public static final ArtifactSlot<String> VALUES =
      ArtifactSlot.multiple("values", "Placeholder Values", ArtifactType.TEXT)
          .withPlaceholdersIn(TEXT);

  private static final List<PrefabProperty<?>> PROPERTIES = propertyList();

  /** Creates the multiple-choice-dialog definition. */
  public MultipleChoiceDialogPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored multiple-choice dialog.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public MultipleChoiceDialogPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Registers a listener called on the server whenever a player chose an answer of this instance.
   *
   * @param option zero-based answer index
   * @param listener receives the choosing player
   */
  public void onOptionChosen(int option, Consumer<Entity> listener) {
    listen(OPTION_CHOSEN.get(option), listener);
  }

  /**
   * Registers a listener called on the server whenever a player cancelled the dialog of this
   * instance.
   *
   * @param listener receives the cancelling player
   */
  public void onCancelled(Consumer<Entity> listener) {
    listen(CANCELLED, listener);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    List<PrefabPort> ports = new ArrayList<>();
    ports.add(SHOW_DIALOG);
    ports.addAll(OPTION_CHOSEN);
    ports.add(CANCELLED);
    ports.add(VALUES);
    return ports;
  }

  @Override
  protected boolean isPortAvailable(PrefabInstance instance, PrefabPort port) {
    if (port == CANCELLED) return instance.get(CAN_CANCEL);
    int index = OPTION_CHOSEN.indexOf(port);
    return index < 0 || index < instance.get(OPTION_COUNT);
  }

  @Override
  public String portDisplayName(PrefabInstance instance, PrefabPort port) {
    int index = OPTION_CHOSEN.indexOf(port);
    if (index < 0 || index >= instance.get(OPTION_COUNT)) return port.displayName();
    return port.displayName()
        + " \""
        + PrefabDialogs.preview(OPTIONS.get(index).get(instance))
        + "\"";
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
    return Optional.of(PrefabDialogs.preview(value(instance, TEXT)));
  }

  private static void show(ActionContext context) {
    PrefabCreationContext creation = context.creationContext();
    PrefabInstance instance = context.instance();
    boolean canCancel = instance.get(CAN_CANCEL);
    List<ChoiceOption> options = new ArrayList<>();
    for (int index = 0; index < instance.get(OPTION_COUNT); index++) {
      options.add(ChoiceOption.of(OPTIONS.get(index).get(instance), String.valueOf(index)));
    }
    Map<String, String> placeholders =
        PrefabDialogs.placeholders(input(creation, instance, VALUES));
    List<UIComponent> open = OPEN.get(creation.level(), instance.name()).orElse(null);
    if (open == null) return;

    for (Entity player : PrefabAudience.players(context, AUDIENCE)) {
      DialogContext.Builder dialog =
          DialogContext.builder()
              .type(DialogType.DefaultTypes.MULTIPLE_CHOICE)
              .put(DialogContextKeys.DIALOG, instance.get(TEXT))
              .put(DialogContextKeys.OPTIONS, new ChoiceOptions(options))
              .put(DialogContextKeys.CAN_CANCEL, canCancel);
      if (instance.get(TRANSLATE)) dialog.put(DialogContextKeys.DIALOG_IS_TRANSLATION_KEY, true);
      // The displaying client replaces the placeholders after resolving translation keys.
      dialog.placeholders(placeholders);

      UIComponent ui = DialogFactory.show(dialog.build(), true, canCancel, player.id());
      open.add(ui);
      // Choosing closes the dialog, which must not count as cancelling it. A dialog that is no
      // longer open was answered already or closed by the despawn of its instance.
      ui.registerCallback(
          DialogContextKeys.ON_OPTION_SELECTED,
          data -> {
            if (!open.remove(ui)) return;
            // Forced, since a dialog without cancel option cannot be closed by the player.
            UIUtils.closeDialog(ui, true);
            chosenIndex(data, options.size())
                .ifPresent(index -> fire(creation, instance, OPTION_CHOSEN.get(index), player));
          });
      if (!canCancel) continue;
      Runnable cancel =
          () -> {
            if (open.remove(ui)) fire(creation, instance, CANCELLED, player);
          };
      ui.registerCallback(
          DialogContextKeys.ON_CANCEL,
          data -> {
            cancel.run();
            UIUtils.closeDialog(ui);
          });
      ui.registerCallback(DialogContextKeys.ON_CLOSE, data -> cancel.run());
    }
  }

  private static Optional<Integer> chosenIndex(DialogResponseMessage.Payload data, int count) {
    if (!(data instanceof DialogResponseMessage.StringValue(String value))) return Optional.empty();
    try {
      int index = Integer.parseInt(value);
      return index >= 0 && index < count ? Optional.of(index) : Optional.empty();
    } catch (NumberFormatException exception) {
      return Optional.empty();
    }
  }

  private static List<PrefabProperty<?>> propertyList() {
    List<PrefabProperty<?>> properties = new ArrayList<>();
    properties.add(TEXT);
    properties.add(TRANSLATE);
    properties.add(AUDIENCE);
    properties.add(CAN_CANCEL);
    properties.add(OPTION_COUNT);
    properties.addAll(OPTIONS);
    return List.copyOf(properties);
  }

  private static List<PrefabProperty<String>> optionProperties() {
    List<PrefabProperty<String>> properties = new ArrayList<>(MAX_OPTIONS);
    for (int index = 0; index < MAX_OPTIONS; index++) {
      int number = index + 1;
      String defaultValue =
          switch (index) {
            case 0 -> "Yes";
            case 1 -> "No";
            default -> "Answer " + number;
          };
      PrefabProperty<String> property =
          PrefabProperty.string(
              "option-" + number, "Answer " + number, defaultValue, value -> !value.isBlank());
      property.visibleWhen(values -> optionCount(values) >= number);
      properties.add(property);
    }
    return List.copyOf(properties);
  }

  private static List<PrefabEvent<Entity>> optionEvents() {
    List<PrefabEvent<Entity>> events = new ArrayList<>(MAX_OPTIONS);
    for (int index = 0; index < MAX_OPTIONS; index++) {
      int number = index + 1;
      events.add(PrefabEvent.withPlayer("option-" + number, "Answer " + number));
    }
    return List.copyOf(events);
  }

  private static int optionCount(Map<String, JsonNode> values) {
    JsonNode count = values.get(OPTION_COUNT.key());
    return count == null || !count.isInt() ? OPTION_COUNT.defaultValue() : count.asInt();
  }
}
