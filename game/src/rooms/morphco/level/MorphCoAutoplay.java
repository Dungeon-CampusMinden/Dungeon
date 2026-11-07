package rooms.morphco.level;

import com.badlogic.gdx.utils.TimeUtils;
import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Point;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContextKeys;
import feature.interaction.InteractionComponent;
import feature.systems.EventScheduler;
import feature.systems.LevelEditorSystem;
import feature.utils.EntityUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import rooms.morphco.puzzles.AffixPuzzle;
import rooms.morphco.puzzles.WordClassPuzzle;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoText;

/**
 * Debug playthrough for {@code --autoplay}.
 *
 * <p>The first player walks from station to station, triggers the real interactions and answers the
 * opened dialogs through the same server callbacks a client would use. Every step waits long enough
 * to watch the dialog. It replaces manual playtests after changes to the room flow.
 */
final class MorphCoAutoplay {

  private static final long STEP_MS = 2600;

  private final MorphCoLevel level;
  private final List<Step> steps = new ArrayList<>();
  private int next;
  private long nextAt;

  private record Step(String name, long waitMs, Consumer<Entity> action) {}

  MorphCoAutoplay(MorphCoLevel level) {
    this.level = level;
    nextAt = TimeUtils.millis() + 4000;
    script();
  }

  /** Runs the next step once its time has come. */
  void tick() {
    if (next >= steps.size() || TimeUtils.millis() < nextAt) return;
    Optional<Entity> player = Game.allPlayers().findFirst();
    if (player.isEmpty()) return;
    if (next == 0) LevelEditorSystem.active(false);
    Step step = steps.get(next++);
    System.out.println("AUTOPLAY " + next + "/" + steps.size() + ": " + step.name());
    step.action().accept(player.get());
    nextAt = TimeUtils.millis() + step.waitMs();
  }

  private void add(String name, Consumer<Entity> action) {
    steps.add(new Step(name, STEP_MS, action));
  }

  private void show(String name, Consumer<Entity> action) {
    steps.add(new Step(name, STEP_MS * 2, action));
  }

  private void script() {
    for (int index = 0; index < 4; index++) add("intro", player -> dismiss());
    add("entrance", player -> go(player, "card-slot"));
    add("card slot", player -> use(player, "card-slot"));
    add("console", player -> dismissAndGo(player, "console"));
    show("alarm buttons", player -> use(player, "console"));
    add("press alarm", player -> answer(DialogContextKeys.ON_OPTION_SELECTED, "alarm"));
    add("phone", player -> dismissAndGo(player, "phone"));
    add("call", player -> use(player, "phone"));
    add("message", player -> answer(DialogContextKeys.ON_OPTION_SELECTED, "messages"));
    add("computer", player -> dismissAndGo(player, "pc"));
    show("status screen", player -> use(player, "pc"));
    add("corridor", player -> dismissAndGo(player, "sign_f04"));
    add("hall F-04", player -> go(player, "f04_enter"));
    add("shelf", player -> dismissAndGo(player, "shelf-f04-a"));
    add("books", player -> use(player, "shelf-f04-a"));
    show("handbook", player -> answer(DialogContextKeys.ON_OPTION_SELECTED, "handbook"));
    add("shelf again", player -> dismissThen(() -> use(player, "shelf-f04-a")));
    show("morpheme types 1", player -> answer(DialogContextKeys.ON_OPTION_SELECTED, "morph1"));
    add("crates", player -> dismissAndGo(player, "crate1"));
    show("word classes", player -> use(player, "crate1"));
    add("sort crates", player -> submit(WordClassPuzzle.encode(wordClasses())));
    add("machine", player -> dismissAndGo(player, "flexion-machine"));
    add("inspect machine", player -> use(player, "flexion-machine"));
    add("hall A-12", player -> dismissAndGo(player, "hatch-lock"));
    add("lock", player -> use(player, "hatch-lock"));
    add("answer lock", player -> answer(DialogContextKeys.ON_CONFIRM, "8"));
    add("hatch", player -> dismissThen(() -> use(player, "hatch")));
    show("affix sorter", player -> dismiss());
    add("sort affixes", player -> submit(AffixPuzzle.encodeWords(affixes())));
    show("input shafts", player -> dismiss());
    add("fill shafts", player -> submit(AffixPuzzle.encodeShafts(shafts())));
    add("pile", player -> dismissAndGo(player, "pile"));
    add("take parts", player -> use(player, "pile"));
    add("yes", player -> answer(DialogContextKeys.ON_YES, null));
    add("station", player -> dismissAndGo(player, "station"));
    for (String pair : List.of("GE|E", "BE|IG", "GE|EN")) {
      show("open station", player -> dismissThen(() -> use(player, "station")));
      add("assemble " + pair, player -> submit(pair));
    }
    add("back to F-04", player -> dismissAndGo(player, "flexion-machine"));
    show("repair machine", player -> use(player, "flexion-machine"));
    add("control room", player -> dismissAndGo(player, "pc"));
    add("restart", player -> use(player, "pc"));
    show("security answer", player -> submit("4"));
    for (int index = 0; index < 4; index++) show("ending", player -> dismiss());
  }

  // Goes to a named area of the level or in front of an object.
  private void go(Entity player, String target) {
    Point position =
        level.namedPoints().containsKey(target)
            ? level.point(target)
            : Game.levelEntities()
                .filter(entity -> target.equals(entity.name()))
                .findFirst()
                .flatMap(entity -> entity.fetch(PositionComponent.class))
                .map(PositionComponent::position)
                .orElseThrow(() -> new IllegalStateException("No object " + target));
    EntityUtils.teleportEntityTo(player, position.translate(0.4f, -1.1f));
  }

  private void dismissAndGo(Entity player, String point) {
    dismiss();
    go(player, point);
  }

  private void dismissThen(Runnable action) {
    dismiss();
    action.run();
  }

  // Steps in front of an object, like a player who walks up to it, and uses it.
  private static void use(Entity player, String name) {
    // An open popup pauses the game; the player first clicks it away.
    dismiss();
    Game.levelEntities()
        .filter(entity -> name.equals(entity.name()))
        .findFirst()
        .ifPresent(
            entity -> {
              Point center = EntityUtils.getPosition(entity);
              EntityUtils.teleportEntityTo(player, center.translate(-0.5f, -1.5f));
              // The collider follows the new position in the next frames.
              EventScheduler.scheduleAction(
                  () ->
                      entity
                          .fetch(InteractionComponent.class)
                          .ifPresent(
                              component -> {
                                System.out.printf(
                                    "AUTOPLAY use %s at distance %.2f (reach %.2f)%n",
                                    name,
                                    EntityUtils.getDistance(entity, player),
                                    component.interaction().range());
                                component.interaction().interact(entity, player);
                              }),
                  300);
            });
  }

  private static List<UIComponent> dialogs() {
    return Game.levelEntities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .toList();
  }

  /** Confirms or closes every open dialog like a player who clicks "Weiter". */
  private static void dismiss() {
    for (UIComponent ui : dialogs()) {
      Map<String, Consumer<DialogResponseMessage.Payload>> callbacks = ui.callbacks();
      if (callbacks.containsKey(DialogContextKeys.ON_RESUME)) {
        callbacks.get(DialogContextKeys.ON_RESUME).accept(null);
      } else if (callbacks.containsKey(DialogContextKeys.ON_CONFIRM)
          && !callbacks.containsKey(DialogContextKeys.ON_CANCEL)) {
        callbacks.get(DialogContextKeys.ON_CONFIRM).accept(null);
      } else {
        UIUtils.closeDialog(ui, true);
      }
    }
  }

  private static void answer(String key, String value) {
    DialogResponseMessage.Payload payload =
        value == null ? null : new DialogResponseMessage.StringValue(value);
    for (UIComponent ui : dialogs()) {
      Consumer<DialogResponseMessage.Payload> callback = ui.callbacks().get(key);
      if (callback != null) {
        callback.accept(payload);
        return;
      }
    }
    System.out.println("AUTOPLAY no dialog for " + key + " " + MorphCoText.KEY_PREFIX);
  }

  private static void submit(String value) {
    answer(MorphCoDialogs.SUBMIT, value);
  }

  private static Map<String, WordClassPuzzle.Crate> wordClasses() {
    Map<String, WordClassPuzzle.Crate> placement = new LinkedHashMap<>();
    WordClassPuzzle.words()
        .forEach(word -> word.target().ifPresent(crate -> placement.put(word.id(), crate)));
    return placement;
  }

  private static Map<String, AffixPuzzle.WordAnswer> affixes() {
    Map<String, AffixPuzzle.WordAnswer> answers = new HashMap<>();
    AffixPuzzle.Bin p = AffixPuzzle.Bin.PRAEFIX;
    AffixPuzzle.Bin s = AffixPuzzle.Bin.SUFFIX;
    AffixPuzzle.Bin z = AffixPuzzle.Bin.ZIRKUMFIX;
    AffixPuzzle.Bin k = AffixPuzzle.Bin.KEIN_AFFIX;
    answers.put("w0", answer(List.of(2, 4, 8), p, p, null, s));
    answers.put("w1", answer(List.of(), k));
    answers.put("w2", answer(List.of(2, 6), z, null, z));
    answers.put("w3", answer(List.of(4), null, s));
    answers.put("w4", answer(List.of(3), null, s));
    answers.put("w5", answer(List.of(), k));
    answers.put("w6", answer(List.of(), k));
    return answers;
  }

  private static AffixPuzzle.WordAnswer answer(List<Integer> cuts, AffixPuzzle.Bin... bins) {
    return new AffixPuzzle.WordAnswer(cuts, new ArrayList<>(Arrays.asList(bins)));
  }

  private static Map<String, AffixPuzzle.Shaft> shafts() {
    Map<String, AffixPuzzle.Shaft> placement = new LinkedHashMap<>();
    AffixPuzzle.affixes().forEach(affix -> placement.put(affix.id(), affix.target()));
    return placement;
  }
}
