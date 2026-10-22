package feature.audience;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Direction;
import feature.components.UIComponent;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.ChoiceOptions;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogScript;
import feature.hud.dialogs.DialogType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * What the audience decides in the current game situation, derived from the dialog open for the
 * hero or, without one, movement.
 *
 * <p>Each context checks and normalizes votes, names a vote for the game screen, and applies the
 * winning vote: a movement step, or the dialog response the dialog's own button would send. A
 * context that wraps a dialog is only applied while that dialog is still open.
 */
sealed interface AudienceContext {
  JsonNodeFactory JSON = JsonNodeFactory.instance;

  /**
   * @return kind the devices use to pick their controls, such as {@code move} or {@code choice}
   */
  String kind();

  /**
   * @return whether the voting window starts at once instead of with the first vote
   */
  default boolean startsAtOnce() {
    return true;
  }

  /**
   * @return the dialog this context answers, if any
   */
  default Optional<UIComponent> dialog() {
    return Optional.empty();
  }

  /**
   * Adds the fields the devices need for their controls.
   *
   * @param json context object of the public state
   */
  default void describe(ObjectNode json) {}

  /**
   * Checks a device's vote and brings it into the form that is counted.
   *
   * @param value vote as sent by the device
   * @return the counted form, or empty when the vote is invalid here
   */
  Optional<JsonNode> normalize(JsonNode value);

  /**
   * @param vote counted vote
   * @return short label of the vote for the game screen
   */
  String label(JsonNode vote);

  /**
   * Applies the winning vote.
   *
   * @param vote winning vote
   * @param stepper movement of the hero
   */
  void apply(JsonNode vote, TileStepper stepper);

  /**
   * @return whether this context still matches the game; a dialog context ends when its dialog
   *     closes
   */
  default boolean current() {
    return dialog().map(AudienceContext::open).orElse(true);
  }

  /**
   * Derives the context for the hero.
   *
   * @param hero the hero the audience controls
   * @param stepper movement of the hero
   * @return the context
   */
  static AudienceContext of(Entity hero, TileStepper stepper) {
    Optional<UIComponent> dialog = dialog(hero);
    if (dialog.isEmpty()) return stepper.moving() ? new Wait() : new Move();
    UIComponent ui = dialog.get();
    DialogType type = ui.dialogContext().dialogType();
    if (type == DialogType.DefaultTypes.MULTIPLE_CHOICE) {
      List<ChoiceOption> options =
          ui.dialogContext().require(DialogContextKeys.OPTIONS, ChoiceOptions.class).values();
      boolean multiple =
          ui.dialogContext()
              .find(DialogContextKeys.MULTIPLE_SELECTION, Boolean.class)
              .orElse(false);
      return new Choice(ui, options, multiple);
    }
    if (type == DialogType.DefaultTypes.YES_NO) return new YesNo(ui);
    if (type == DialogType.DefaultTypes.FREE_INPUT) {
      return new Text(
          ui, ui.dialogContext().find(AudienceSystem.CODE_DIGITS, Integer.class).orElse(0));
    }
    if ((type == DialogType.DefaultTypes.OK
            || type == DialogType.DefaultTypes.TEXT
            || type == DialogType.DefaultTypes.DIALOG_DIALOG)
        && ui.callbacks().containsKey(DialogContextKeys.ON_CONFIRM)) {
      return new Continue(ui);
    }
    return new Wait();
  }

  /**
   * Finds the newest open dialog shown to the hero, including dialogs shown to everybody.
   *
   * @param hero the hero the audience controls
   * @return the dialog, if one is open
   */
  static Optional<UIComponent> dialog(Entity hero) {
    return Game.entities(Set.of(UIComponent.class))
        .filter(
            entity -> entity.fetch(UIComponent.class).map(ui -> targets(ui, hero)).orElse(false))
        .max(Comparator.comparingInt(Entity::id))
        .flatMap(entity -> entity.fetch(UIComponent.class));
  }

  /**
   * Reads what a dialog asks, for the devices to show above their controls: the title and the
   * question, the message, or the dialog script as plain text.
   *
   * @param ui the dialog
   * @return {@code title} and {@code text}, each only when the dialog has it
   */
  static ObjectNode prompt(UIComponent ui) {
    DialogContext context = DialogFactory.localized(ui.dialogContext());
    ObjectNode prompt = JSON.objectNode();
    context
        .find(DialogContextKeys.TITLE, String.class)
        .filter(title -> !title.isBlank())
        .ifPresent(title -> prompt.put("title", title.strip()));
    context
        .find(DialogContextKeys.QUESTION, String.class)
        // Unlike the other texts, the free-input dialog translates its question itself.
        .map(question -> Game.localization().getCurrentTranslator().translate(question))
        .or(() -> context.find(DialogContextKeys.MESSAGE, String.class))
        .or(
            () ->
                context
                    .find(DialogContextKeys.DIALOG, String.class)
                    .map(script -> DialogScript.toHeadlessText(DialogScript.parse(script))))
        .filter(text -> !text.isBlank())
        .ifPresent(text -> prompt.put("text", text.strip()));
    return prompt;
  }

  private static boolean targets(UIComponent ui, Entity hero) {
    int[] targets = ui.targetEntityIds();
    if (targets.length == 0) return true;
    for (int target : targets) if (target == hero.id()) return true;
    return false;
  }

  private static boolean open(UIComponent ui) {
    return ui.dialogContext().ownerEntity().fetch(UIComponent.class).orElse(null) == ui;
  }

  // Runs a callback of a dialog that is still open, as if its client had answered.
  private static void respond(UIComponent ui, String key, DialogResponseMessage.Payload payload) {
    if (!open(ui)) return;
    Consumer<DialogResponseMessage.Payload> callback = ui.callbacks().get(key);
    if (callback != null) callback.accept(payload);
  }

  /** Nothing to decide, for example while the hero walks or an unsupported dialog is open. */
  record Wait() implements AudienceContext {
    @Override
    public String kind() {
      return "wait";
    }

    @Override
    public Optional<JsonNode> normalize(JsonNode value) {
      return Optional.empty();
    }

    @Override
    public String label(JsonNode vote) {
      return "";
    }

    @Override
    public void apply(JsonNode vote, TileStepper stepper) {}
  }

  /** One step in a screen direction, or an interaction with the tile in front of the hero. */
  record Move() implements AudienceContext {
    private static final List<String> VALUES = List.of("up", "down", "left", "right", "interact");

    @Override
    public String kind() {
      return "move";
    }

    @Override
    public boolean startsAtOnce() {
      return false;
    }

    @Override
    public Optional<JsonNode> normalize(JsonNode value) {
      return value.isString() && VALUES.contains(value.asString())
          ? Optional.of(value)
          : Optional.empty();
    }

    @Override
    public String label(JsonNode vote) {
      return switch (vote.asString()) {
        case "up" -> "oben";
        case "down" -> "unten";
        case "left" -> "links";
        case "right" -> "rechts";
        default -> "Benutzen";
      };
    }

    @Override
    public void apply(JsonNode vote, TileStepper stepper) {
      switch (vote.asString()) {
        case "up" -> stepper.step(Direction.UP);
        case "down" -> stepper.step(Direction.DOWN);
        case "left" -> stepper.step(Direction.LEFT);
        case "right" -> stepper.step(Direction.RIGHT);
        default -> stepper.interact();
      }
    }
  }

  /**
   * Single or multiple choice. Devices send option numbers starting at one; the counted vote is the
   * option value, or the values of the chosen combination in option order.
   *
   * @param ui the dialog
   * @param options options in display order
   * @param multiple whether several options can be chosen
   */
  record Choice(UIComponent ui, List<ChoiceOption> options, boolean multiple)
      implements AudienceContext {
    @Override
    public String kind() {
      return "choice";
    }

    @Override
    public Optional<UIComponent> dialog() {
      return Optional.of(ui);
    }

    @Override
    public void describe(ObjectNode json) {
      json.put("options", options.size()).put("multiple", multiple);
    }

    @Override
    public Optional<JsonNode> normalize(JsonNode value) {
      if (!multiple) return number(value).map(index -> JSON.stringNode(options.get(index).value()));
      if (!value.isArray() || value.isEmpty()) return Optional.empty();
      Set<Integer> chosen = new TreeSet<>();
      for (JsonNode item : value) {
        Optional<Integer> index = number(item);
        if (index.isEmpty()) return Optional.empty();
        chosen.add(index.get());
      }
      ArrayNode values = JSON.arrayNode();
      chosen.forEach(index -> values.add(options.get(index).value()));
      return Optional.of(values);
    }

    private Optional<Integer> number(JsonNode value) {
      if (!value.canConvertToInt()) return Optional.empty();
      int index = value.asInt() - 1;
      return index >= 0 && index < options.size() ? Optional.of(index) : Optional.empty();
    }

    @Override
    public String label(JsonNode vote) {
      List<String> numbers = new ArrayList<>();
      for (JsonNode value : vote.isArray() ? vote : JSON.arrayNode().add(vote)) {
        for (int i = 0; i < options.size(); i++) {
          if (options.get(i).value().equals(value.asString())) numbers.add(Integer.toString(i + 1));
        }
      }
      return String.join(" + ", numbers);
    }

    @Override
    public void apply(JsonNode vote, TileStepper stepper) {
      DialogResponseMessage.Payload payload;
      if (multiple) {
        List<String> values = new ArrayList<>();
        vote.forEach(value -> values.add(value.asString()));
        payload = new DialogResponseMessage.StringList(values.toArray(String[]::new));
      } else {
        payload = new DialogResponseMessage.StringValue(vote.asString());
      }
      respond(ui, DialogContextKeys.ON_OPTION_SELECTED, payload);
    }
  }

  /**
   * A yes/no question, shown on the devices as a choice of two.
   *
   * @param ui the dialog
   */
  record YesNo(UIComponent ui) implements AudienceContext {
    @Override
    public String kind() {
      return "choice";
    }

    @Override
    public Optional<UIComponent> dialog() {
      return Optional.of(ui);
    }

    @Override
    public void describe(ObjectNode json) {
      json.put("options", 2).put("multiple", false);
      json.putArray("labels").add("Ja").add("Nein");
    }

    @Override
    public Optional<JsonNode> normalize(JsonNode value) {
      if (!value.canConvertToInt()) return Optional.empty();
      return switch (value.asInt()) {
        case 1 -> Optional.of(JSON.stringNode("yes"));
        case 2 -> Optional.of(JSON.stringNode("no"));
        default -> Optional.empty();
      };
    }

    @Override
    public String label(JsonNode vote) {
      return vote.asString().equals("yes") ? "Ja" : "Nein";
    }

    @Override
    public void apply(JsonNode vote, TileStepper stepper) {
      String key =
          vote.asString().equals("yes") ? DialogContextKeys.ON_YES : DialogContextKeys.ON_NO;
      respond(ui, key, null);
    }
  }

  /**
   * A free-input dialog. Answers are compared after removing leading and trailing whitespace.
   * Marked with {@link AudienceSystem#CODE_DIGITS}, it asks for a code: the devices show a digit
   * keypad, and only codes of that length count, with their leading zeros.
   *
   * @param ui the dialog
   * @param digits number of digits of the code, or {@code 0} for free text
   */
  record Text(UIComponent ui, int digits) implements AudienceContext {
    private static final int MAX_LENGTH = 120;

    @Override
    public String kind() {
      return digits > 0 ? "code" : "text";
    }

    @Override
    public Optional<UIComponent> dialog() {
      return Optional.of(ui);
    }

    @Override
    public void describe(ObjectNode json) {
      if (digits > 0) json.put("digits", digits);
    }

    @Override
    public Optional<JsonNode> normalize(JsonNode value) {
      if (!value.isString()) return Optional.empty();
      String text = value.asString().strip();
      boolean valid =
          digits > 0
              ? text.matches("[0-9]{" + digits + "}")
              : !text.isEmpty() && text.length() <= MAX_LENGTH;
      return valid ? Optional.of(JSON.stringNode(text)) : Optional.empty();
    }

    @Override
    public String label(JsonNode vote) {
      return digits > 0 ? vote.asString() : "\"" + vote.asString() + "\"";
    }

    @Override
    public void apply(JsonNode vote, TileStepper stepper) {
      respond(
          ui, DialogContextKeys.ON_CONFIRM, new DialogResponseMessage.StringValue(vote.asString()));
    }
  }

  /**
   * A text the audience reads on the game screen; "Weiter" confirms it.
   *
   * @param ui the dialog
   */
  record Continue(UIComponent ui) implements AudienceContext {
    @Override
    public String kind() {
      return "continue";
    }

    @Override
    public Optional<UIComponent> dialog() {
      return Optional.of(ui);
    }

    @Override
    public Optional<JsonNode> normalize(JsonNode value) {
      return Optional.of(JSON.stringNode("continue"));
    }

    @Override
    public String label(JsonNode vote) {
      return "Weiter";
    }

    @Override
    public void apply(JsonNode vote, TileStepper stepper) {
      respond(ui, DialogContextKeys.ON_CONFIRM, null);
    }
  }
}
