package feature.audience;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Direction;
import engine.utils.Point;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.ChoiceOptions;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogScript;
import feature.hud.dialogs.DialogType;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * What the audience decides in the current game situation. The dialog open for the hero decides it.
 * Without a dialog the audience moves the hero, by choosing a target in a room that marks reachable
 * {@link AudienceTarget}s and by single steps otherwise.
 *
 * <p>Each context checks and normalizes votes, names a vote for the game screen, and applies the
 * winning vote: a movement step, or the dialog response the dialog's own button would send. A
 * context that wraps a dialog is only applied while that dialog is still open.
 */
sealed interface AudienceContext {
  JsonNodeFactory JSON = JsonNodeFactory.instance;

  /**
   * @return what the audience decides, such as {@code move} or {@code choice}; tracking and the
   *     game screen use it
   */
  String kind();

  /**
   * @return controls the devices show, such as {@code move} or {@code choice}; the kind, unless the
   *     devices show the decision with the controls of another kind
   */
  default String controls() {
    return kind();
  }

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
   * Derives the context for the hero.
   *
   * @param hero the hero the audience controls
   * @param stepper movement of the hero
   * @return the context
   */
  static AudienceContext of(Entity hero, TileStepper stepper) {
    Optional<UIComponent> dialog = dialog(hero);
    if (dialog.isEmpty()) {
      if (stepper.moving()) return new Wait();
      List<Goto.Destination> targets = Goto.reachable(stepper);
      return targets.isEmpty() ? new Move() : new Goto(targets);
    }
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
    if (type == DialogType.DefaultTypes.IMAGE) return new Continue(ui);
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
   * question, the message, or the dialog script as plain text, and the answers of a choice in
   * display order.
   *
   * @param ui the dialog
   * @return {@code title}, {@code text} and {@code answers}, each only when the dialog has it
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
    context
        .find(DialogContextKeys.OPTIONS, ChoiceOptions.class)
        .ifPresent(
            options -> {
              ArrayNode answers = prompt.putArray("answers");
              options.values().forEach(option -> answers.add(option.label().strip()));
            });
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
   * A choice of destinations instead of single steps. The devices show it as a single choice with
   * the names of the targets, and the game screen shows each target's letter above it, as for the
   * options of a question. The counted vote is the name. The hero walks to the winning target and
   * uses it.
   *
   * @param targets the targets the hero can reach, in list order
   */
  record Goto(List<Destination> targets) implements AudienceContext {
    // Sorted by name, the list on the devices is easy to scan.
    private static final Collator ORDER = Collator.getInstance(Locale.GERMAN);

    /**
     * Collects the targets the room offers right now and the hero can reach.
     *
     * @param stepper movement of the hero
     * @return the targets sorted by name, or an empty list when there is none
     */
    static List<Destination> reachable(TileStepper stepper) {
      List<Entity> offered =
          Game.entities(Set.of(AudienceTarget.class))
              .filter(
                  entity ->
                      entity.fetch(AudienceTarget.class).orElseThrow().available().getAsBoolean())
              .toList();
      if (offered.isEmpty()) return List.of();
      Map<Point, Point> reachable = stepper.reachable();
      return offered.stream()
          .filter(entity -> stepper.route(entity, reachable).isPresent())
          .map(Destination::of)
          .sorted(Comparator.comparing(Destination::label, ORDER))
          .toList();
    }

    @Override
    public String kind() {
      return "goto";
    }

    @Override
    public String controls() {
      return "choice";
    }

    @Override
    public boolean startsAtOnce() {
      return false;
    }

    @Override
    public void describe(ObjectNode json) {
      json.put("options", targets.size()).put("multiple", false);
      json.put("title", "Wohin geht es?");
      json.put("text", "Die Figur läuft selbst hin und benutzt, was dort steht.");
      ArrayNode answers = json.putArray("answers");
      targets.forEach(target -> answers.add(target.label()));
    }

    @Override
    public Optional<JsonNode> normalize(JsonNode value) {
      if (!value.canConvertToInt()) return Optional.empty();
      int index = value.asInt() - 1;
      return index >= 0 && index < targets.size()
          ? Optional.of(JSON.stringNode(targets.get(index).label()))
          : Optional.empty();
    }

    @Override
    public String label(JsonNode vote) {
      return vote.asString();
    }

    @Override
    public void apply(JsonNode vote, TileStepper stepper) {
      targets.stream()
          .filter(target -> target.label().equals(vote.asString()))
          .findFirst()
          .flatMap(target -> Game.findEntityById(target.entity()))
          .ifPresent(stepper::goTo);
    }

    /**
     * @return letter, name and marker position of each target, for the game screen
     */
    ArrayNode markers() {
      ArrayNode markers = JSON.arrayNode();
      for (int i = 0; i < targets.size(); i++) {
        Destination target = targets.get(i);
        markers
            .addObject()
            .put("letter", DialogFactory.choiceLetter(i))
            .put("label", target.label())
            .put("x", target.marker().x())
            .put("y", target.marker().y());
      }
      return markers;
    }

    /**
     * A target in the list.
     *
     * @param entity id of the target's entity
     * @param label name of the target
     * @param marker where the game screen shows the letter, above the middle of the tiles the
     *     target takes up
     */
    record Destination(int entity, String label, Point marker) {
      static Destination of(Entity entity) {
        Set<Point> tiles = TileStepper.tiles(entity);
        double left = tiles.stream().mapToDouble(Point::x).min().orElseThrow();
        double right = tiles.stream().mapToDouble(Point::x).max().orElseThrow() + 1;
        double top = tiles.stream().mapToDouble(Point::y).max().orElseThrow() + 1;
        return new Destination(
            entity.id(),
            entity.fetch(AudienceTarget.class).orElseThrow().label(),
            new Point((float) (left + right) / 2, (float) top));
      }
    }
  }

  /**
   * Single or multiple choice. The screens show options with letters starting at A; devices send
   * option numbers starting at one. The counted vote is the option value, or the values of the
   * chosen combination in option order.
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
      List<String> letters = new ArrayList<>();
      for (JsonNode value : vote.isArray() ? vote : JSON.arrayNode().add(vote)) {
        for (int i = 0; i < options.size(); i++) {
          if (options.get(i).value().equals(value.asString())) {
            letters.add(DialogFactory.choiceLetter(i));
          }
        }
      }
      return String.join(" + ", letters);
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
   * A free-input dialog. Answers count without any whitespace and in lower case, so "O(log n)" and
   * "o(logn)" are the same vote; the dialog receives that form. Marked with {@link
   * AudienceSystem#CODE_DIGITS}, it asks for a code: the devices show a digit keypad, and only
   * codes of that length count, with their leading zeros.
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
      String text = value.asString().replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
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
   * A text or an image the audience looks at on the game screen. "Weiter" confirms the text and
   * closes the image the way its close button does.
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
      if (ui.callbacks().containsKey(DialogContextKeys.ON_CONFIRM)) {
        respond(ui, DialogContextKeys.ON_CONFIRM, null);
        return;
      }
      if (!open(ui)) return;
      respond(ui, DialogContextKeys.ON_CLOSE, null);
      UIUtils.closeDialog(ui);
    }
  }
}
