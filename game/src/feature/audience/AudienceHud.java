package feature.audience;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.System;
import engine.network.messages.c2s.InputMessage;
import engine.systems.input.InputManager;
import engine.time.PlayClockRuntime;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.hud.dialogs.PauseDialog;
import feature.input.configuration.KeyboardConfig;
import io.nayuki.qrcodegen.QrCode;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tracking.core.TrackingJson;

/**
 * Game-screen overlay of audience mode: how to join, the countdown, the open voting window with its
 * live distribution, the result of the last window, and the thank-you screen with the post-survey
 * count.
 *
 * <p>It also reads the host's keys: Enter starts the round in the lobby and closes the session at
 * the end, the End key ends the game while it runs. A click on the join panel during play pauses
 * the game and shows the join screen in full size for latecomers; Enter or a click resumes.
 * Register it with {@link #register()} in the client's configuration step and add it with {@link
 * #install()} in the client setup.
 */
public final class AudienceHud extends System {
  private static final String DOT_FONT = "fonts/Doto_Rounded-ExtraBold.ttf";
  private static final String TEXT_FONT = "fonts/Lexend-Regular.ttf";
  private static final String BOLD_FONT = "fonts/Lexend-Bold.ttf";
  private static final Color GROUND = Color.valueOf("14161ce6");
  private static final Color PANEL = Color.valueOf("1b1f27f0");
  private static final Color LINE = Color.valueOf("2e3440ff");
  private static final Color TEXT = Color.valueOf("e8eaefff");
  private static final Color MUTED = Color.valueOf("8a91a0ff");
  private static final Color ACCENT = Color.valueOf("f0b43cff");
  private static final float MARGIN = 16f;
  private static final int TALLY_BAR_WIDTH = 220;
  private static final float GAP = 16f;
  private static final float COMPACT = 0.65f;
  private static final int LABEL_LENGTH = 28;

  private static JsonNode state = TrackingJson.object();
  private static long stateReceivedAt;
  // The host opened the full join screen during play; the game pauses while it is open.
  private static boolean joinScreen;
  // Whether the pause menu was open in the previous frame.
  private static boolean pauseMenuOpenBefore;

  private Table root;
  private Table joinFull;
  private Table joinCompact;
  private Table statusFull;
  private Table statusCompact;
  private String layout = "";
  private String qrFor = "";
  private Texture qrTexture;

  private AudienceHud() {
    super(AuthoritativeSide.CLIENT);
  }

  /**
   * Registers the overlay message and numbers the options of choice dialogs, so the audience can
   * vote by number. Call it in the client's configuration step, before the client connects: the
   * server sends the message right after the connection.
   */
  public static void register() {
    AudienceHudMessage.registerConverter();
    DialogFactory.numberChoiceOptions(true);
  }

  /** Adds the overlay to the game-screen client; call it in the client setup. */
  public static void install() {
    Game.network()
        .messageDispatcher()
        .registerHandler(
            AudienceHudMessage.class,
            (session, message) -> {
              state = TrackingJson.object(message.json());
              stateReceivedAt = java.lang.System.nanoTime();
            });
    PlayClockRuntime.pauseWhile(() -> joinScreen);
    Game.add(new AudienceHud());
  }

  @Override
  public void execute() {
    hostKeys();
    if (Game.isHeadless()) return;
    Optional<Stage> stage = Game.stage();
    if (stage.isEmpty()) return;
    if (root == null) {
      root = new Table();
      root.setFillParent(true);
      root.addListener(clicked(() -> joinScreen = false));
    }
    if (root.getStage() != stage.get()) stage.get().addActor(root);
    root.toFront();
    // The overlay stays in front of the game's dialogs, but the host's pause menu must stay usable.
    root.setVisible(!pauseMenuOpen());
    String phase = state.path("phase").asString("");
    if (!phase.equals("playing")) joinScreen = false;
    // Only the join panel takes clicks during play; the full join screen takes them anywhere.
    root.setTouchable(joinScreen ? Touchable.enabled : Touchable.childrenOnly);
    String next = joinScreen + "|" + content();
    if (!next.equals(layout)) {
      layout = next;
      root.clearChildren();
      root.clearActions();
      root.setBackground((Drawable) null);
      root.top().left();
      switch (phase) {
        case "lobby" -> lobby(false);
        case "countdown" -> countdown();
        case "playing" -> {
          if (joinScreen) lobby(true);
          else playing();
        }
        case "ended" -> ended();
        default -> {}
      }
    }
    if (phase.equals("playing") && !joinScreen) place(stage.get());
  }

  private static boolean pauseMenuOpen() {
    return pauseMenu().isPresent();
  }

  private static Optional<UIComponent> pauseMenu() {
    return Game.entities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType() == DialogType.DefaultTypes.PAUSE_MENU)
        .findFirst();
  }

  private void hostKeys() {
    String phase = state.path("phase").asString("");
    if (phase.equals("playing")) pauseOverDialogs();
    // Dialogs hold the keyboard focus; the host's keys must work regardless.
    if (phase.equals("lobby") && InputManager.isKeyJustPressed(Input.Keys.ENTER, true)) {
      send(AudienceSystem.START);
    } else if (phase.equals("playing")
        && joinScreen
        && InputManager.isKeyJustPressed(Input.Keys.ENTER, true)) {
      joinScreen = false;
    } else if (phase.equals("playing") && InputManager.isKeyJustPressed(Input.Keys.END, true)) {
      send(AudienceSystem.END);
    } else if (phase.equals("ended") && InputManager.isKeyJustPressed(Input.Keys.ENTER, true)) {
      send(AudienceSystem.CLOSE);
    }
  }

  // A dialog is open during most of the game, and the engine's pause keys fail there: a dialog that
  // holds the keyboard focus swallows them, and Esc only asks the dialog to close, which the server
  // ignores in audience mode. The host needs the pause menu anyway, so the keys open and close it
  // here whenever the engine does not.
  private static void pauseOverDialogs() {
    Optional<UIComponent> menu = pauseMenu();
    boolean openBefore = pauseMenuOpenBefore;
    pauseMenuOpenBefore = menu.isPresent();
    int pause = KeyboardConfig.PAUSE_MENU.value();
    int close = KeyboardConfig.CLOSE_UI.value();
    if (!InputManager.isKeyJustPressed(pause, true)
        && !InputManager.isKeyJustPressed(close, true)) {
      return;
    }
    // The engine may handle the key earlier in the same frame; then it already closed the menu.
    if (openBefore && menu.isEmpty()) return;
    // The engine's Esc toggles the menu only when nothing else closable lies on top: it opens the
    // menu without a dialog and closes it when the menu is the topmost closable UI.
    boolean engineHandlesEsc =
        InputManager.isKeyJustPressed(close)
            && Game.hud()
                .topmostCloseRequestUI()
                .map(topmost -> menu.isPresent() && topmost.b() == menu.get())
                .orElse(menu.isEmpty());
    boolean engineToggles = InputManager.isKeyJustPressed(pause) || engineHandlesEsc;
    if (menu.isPresent()) {
      if (!engineToggles) UIUtils.closeDialog(menu.get());
      return;
    }
    if (engineToggles) return;
    // Next frame, so the engine's Esc handling in this frame cannot close the new menu right away.
    Gdx.app.postRunnable(
        () -> {
          if (pauseMenuOpen()) return;
          // The pause menu refuses to open while something holds the keyboard focus.
          Game.stage().ifPresent(stage -> stage.setKeyboardFocus(null));
          Game.player().ifPresent(PauseDialog::showPauseDialog);
        });
  }

  private static void send(String route) {
    Game.network().sendInput(InputMessage.custom(route));
  }

  /**
   * @return everything the overlay shows, apart from timers; the overlay is rebuilt when it changes
   */
  private static String content() {
    JsonNode copy = state.deepCopy();
    if (copy instanceof ObjectNode object) {
      object.remove("phaseMs");
      if (object.get("context") instanceof ObjectNode context) {
        context.remove("remainingMs");
      }
    }
    return copy.toString();
  }

  // --- layouts

  // The join screen: before the round, or paused during play when the host opened it.
  private void lobby(boolean paused) {
    Table panel = panel();
    panel.add(qr()).size(420).padRight(48);
    Table text = new Table();
    text.defaults().left();
    text.add(label("Mitspielen auf", TEXT_FONT, 30, MUTED)).row();
    text.add(label(joinHost(), BOLD_FONT, 40, TEXT)).padBottom(28).row();
    text.add(label("mit dem Code", TEXT_FONT, 30, MUTED)).row();
    text.add(label(spacedCode(), DOT_FONT, 128, ACCENT)).padBottom(28).row();
    text.add(label(playerLine(), TEXT_FONT, 28, TEXT)).row();
    if (!state.path("online").asBoolean(true)) {
      text.add(label("Keine Verbindung zum Relay, Details im Log.", BOLD_FONT, 24, ACCENT))
          .padTop(20)
          .row();
    }
    String next = paused ? "Pausiert. Enter oder Klick setzt fort." : "Enter startet die Runde.";
    text.add(label(next, TEXT_FONT, 24, MUTED)).padTop(20).row();
    panel.add(text);
    dimmed(panel);
  }

  private void countdown() {
    Label number = label("", DOT_FONT, 320, ACCENT);
    Label title = label("Gleich geht es los", TEXT_FONT, 36, TEXT);
    Table panel = new Table();
    panel.add(title).row();
    panel.add(number);
    dimmed(panel);
    root.addAction(
        Actions.forever(
            Actions.run(
                () ->
                    number.setText(
                        Long.toString(Math.max(1, (remaining("phaseMs") + 999) / 1000))))));
  }

  private void playing() {
    joinFull = panel();
    joinFull.pad(12);
    joinFull.add(qr()).size(104).padRight(12);
    Table join = new Table();
    join.defaults().left();
    join.add(label(joinHost(), TEXT_FONT, 16, MUTED)).row();
    join.add(label(spacedCode(), DOT_FONT, 40, ACCENT));
    joinFull.add(join);
    joinCompact = panel();
    joinCompact.pad(10);
    joinCompact.defaults().left();
    joinCompact.add(label(joinHost(), TEXT_FONT, 14, MUTED)).row();
    joinCompact.add(label(spacedCode(), DOT_FONT, 32, ACCENT));
    for (Table panel : new Table[] {joinFull, joinCompact}) {
      panel.addListener(clicked(() -> joinScreen = true));
    }

    statusFull = status(1f);
    statusCompact = status(COMPACT);
    for (Table panel : new Table[] {joinFull, joinCompact, statusFull, statusCompact}) {
      if (panel == null) continue;
      if (panel == statusFull || panel == statusCompact) panel.setTouchable(Touchable.disabled);
      panel.pack();
      root.addActor(panel);
    }
  }

  // The voting or result panel, with a warning while the relay is unreachable; null when there is
  // nothing to show.
  private Table status(float scale) {
    JsonNode result = state.path("result");
    JsonNode context = state.path("context");
    Table status = null;
    if (result.isObject()) {
      status = resultPanel(result, scale);
    } else if (!context.path("kind").asString("wait").equals("wait")) {
      status = votingPanel(context, scale);
    }
    if (!state.path("online").asBoolean(true)) {
      Table warning = panel();
      warning.pad(20 * scale);
      warning.add(
          label(
              "Keine Verbindung zum Server, Stimmen kommen gerade nicht an.",
              BOLD_FONT,
              size(22, scale),
              ACCENT));
      status = status == null ? warning : column(status, warning);
    }
    return status;
  }

  private static Table column(Table top, Table bottom) {
    Table column = new Table();
    column.add(top).right().row();
    column.add(bottom).right().padTop(12);
    return column;
  }

  // Puts the join panel and the voting panel into corners that no dialog covers. Each shrinks to
  // its compact form when its full size fits nowhere: the join panel to the code alone, the voting
  // panel to smaller type and the three most frequent votes.
  private void place(Stage stage) {
    List<Rectangle> taken = obstacles(stage);
    float width = stage.getWidth();
    float height = stage.getHeight();
    Optional<Rectangle> full = free(joinFull, List.of(Corner.TOP_LEFT), taken, width, height);
    joinFull.setVisible(full.isPresent());
    joinCompact.setVisible(full.isEmpty());
    Table join = full.isPresent() ? joinFull : joinCompact;
    Rectangle joinBounds =
        full.or(() -> free(joinCompact, List.of(Corner.values()), taken, width, height))
            .orElseGet(() -> Corner.TOP_LEFT.bounds(joinCompact, width, height));
    join.setPosition(joinBounds.x, joinBounds.y);
    taken.add(joinBounds);
    if (statusFull == null) return;
    List<Corner> corners =
        List.of(Corner.BOTTOM_RIGHT, Corner.TOP_RIGHT, Corner.BOTTOM_LEFT, Corner.TOP_LEFT);
    Optional<Rectangle> fullStatus = free(statusFull, corners, taken, width, height);
    statusFull.setVisible(fullStatus.isPresent());
    statusCompact.setVisible(fullStatus.isEmpty());
    Table status = fullStatus.isPresent() ? statusFull : statusCompact;
    Rectangle statusBounds =
        fullStatus
            .or(() -> free(statusCompact, corners, taken, width, height))
            .orElseGet(() -> Corner.BOTTOM_RIGHT.bounds(statusCompact, width, height));
    status.setPosition(statusBounds.x, statusBounds.y);
  }

  private static Optional<Rectangle> free(
      Table panel, List<Corner> corners, List<Rectangle> taken, float width, float height) {
    for (Corner corner : corners) {
      Rectangle bounds = corner.bounds(panel, width, height);
      if (taken.stream().noneMatch(bounds::overlaps)) return Optional.of(bounds);
    }
    return Optional.empty();
  }

  // Collects what other HUD elements show: per top-level actor, the union of its visible parts.
  // Dialogs often fill the screen with transparent containers; only their window, buttons, and
  // texts count.
  private List<Rectangle> obstacles(Stage stage) {
    List<Rectangle> obstacles = new ArrayList<>();
    for (Actor actor : stage.getActors()) {
      if (actor == root || !actor.isVisible()) continue;
      Rectangle union = new Rectangle();
      boolean[] any = {false};
      visibleParts(
          actor,
          stage,
          part -> {
            Vector2 a = part.localToStageCoordinates(new Vector2(0, 0));
            Vector2 b =
                part.localToStageCoordinates(new Vector2(part.getWidth(), part.getHeight()));
            Rectangle bounds =
                new Rectangle(
                    Math.min(a.x, b.x),
                    Math.min(a.y, b.y),
                    Math.abs(b.x - a.x),
                    Math.abs(b.y - a.y));
            if (any[0]) union.merge(bounds);
            else union.set(bounds);
            any[0] = true;
          });
      if (any[0]) {
        obstacles.add(
            new Rectangle(
                union.x - GAP, union.y - GAP, union.width + 2 * GAP, union.height + 2 * GAP));
      }
    }
    return obstacles;
  }

  // Visible leaves, plus tables that draw a background smaller than the screen, such as the
  // frame of a dialog window.
  private static void visibleParts(Actor actor, Stage stage, Consumer<Actor> parts) {
    if (!actor.isVisible() || actor.getColor().a == 0) return;
    if (actor instanceof Table table
        && table.getBackground() != null
        && (table.getWidth() < stage.getWidth() * 0.9f
            || table.getHeight() < stage.getHeight() * 0.9f)) {
      parts.accept(table);
    }
    if (actor instanceof Group group) {
      for (Actor child : group.getChildren()) visibleParts(child, stage, parts);
    } else if (actor.getWidth() > 0 && actor.getHeight() > 0) {
      parts.accept(actor);
    }
  }

  /** Screen corners for the overlay panels, each with the margin to the edges. */
  private enum Corner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT;

    Rectangle bounds(Table panel, float width, float height) {
      boolean left = this == TOP_LEFT || this == BOTTOM_LEFT;
      boolean top = this == TOP_LEFT || this == TOP_RIGHT;
      float x = left ? MARGIN : width - MARGIN - panel.getWidth();
      float y = top ? height - MARGIN - panel.getHeight() : MARGIN;
      return new Rectangle(x, y, panel.getWidth(), panel.getHeight());
    }
  }

  private Table votingPanel(JsonNode context, float scale) {
    Table panel = panel();
    panel.pad(20 * scale);
    String kind = context.path("kind").asString("");
    // Countdown and time bar only while a window runs; before that they would only take room.
    boolean timed = context.has("remainingMs");
    Table head = new Table();
    head.add(label(title(kind, context), BOLD_FONT, size(30, scale), TEXT)).left().expandX();
    panel.add(head).growX().row();
    if (timed) {
      // Two digits at layout time, so the panel is wide enough for the countdown.
      Label timer = label("00", DOT_FONT, size(64, scale), ACCENT);
      head.add(timer).right().padLeft(40 * scale);
      Image progress = new Image(tinted(ACCENT));
      Table track = new Table();
      track.setBackground(tinted(LINE));
      track.add(progress).height(6).left().expandX();
      panel.add(track).growX().height(6).padTop(8).padBottom(12).row();
      root.addAction(
          Actions.forever(
              Actions.run(
                  () -> {
                    long remaining = remaining("remainingMs");
                    long window = Math.max(1, state.path("context").path("windowMs").asLong(1));
                    timer.setText(Long.toString((remaining + 999) / 1000));
                    track.getCell(progress).width(track.getWidth() * remaining / window);
                    track.invalidate();
                  })));
    }
    JsonNode tally = context.path("tally");
    if (tally.isEmpty()) {
      String waiting =
          kind.equals("move") ? "Die Runde startet mit der ersten Stimme." : "Noch keine Stimmen.";
      panel.add(label(waiting, TEXT_FONT, size(24, scale), MUTED)).left().padTop(timed ? 0 : 6);
    } else {
      panel.add(tallyTable(tally, scale)).left();
    }
    return panel;
  }

  private Table resultPanel(JsonNode result, float scale) {
    Table panel = panel();
    panel.pad(20 * scale);
    panel.add(label("Gewählt", TEXT_FONT, size(24, scale), MUTED)).left().row();
    panel
        .add(
            label(
                shortened(result.path("chosen").asString("")), BOLD_FONT, size(44, scale), ACCENT))
        .left()
        .row();
    if (!result.path("tally").isEmpty()) {
      panel.add(tallyTable(result.path("tally"), scale)).left().padTop(12 * scale);
    }
    return panel;
  }

  // The compact panel shows the three most frequent votes only.
  private Table tallyTable(JsonNode tally, float scale) {
    Table table = new Table();
    int top = Math.max(1, tally.path(0).path("count").asInt(1));
    int rows = scale < 1 ? 3 : Integer.MAX_VALUE;
    for (JsonNode entry : tally) {
      if (rows-- == 0) break;
      int count = entry.path("count").asInt();
      table
          .add(label(shortened(entry.path("label").asString("")), TEXT_FONT, size(24, scale), TEXT))
          .left()
          .padRight(20 * scale);
      Image bar = new Image(tinted(count == top ? ACCENT : MUTED));
      table
          .add(bar)
          .width(Math.max(6, TALLY_BAR_WIDTH * scale * count / top))
          .height(14 * scale)
          .left();
      table
          .add(label(count + " ×", DOT_FONT, size(28, scale), TEXT))
          .right()
          .padLeft(16 * scale)
          .row();
    }
    return table;
  }

  // Free-text answers can be long; the panels must still fit into a corner.
  private static String shortened(String label) {
    return label.length() <= LABEL_LENGTH ? label : label.substring(0, LABEL_LENGTH - 1) + "…";
  }

  private static int size(int size, float scale) {
    return Math.round(size * scale);
  }

  private void ended() {
    JsonNode players = state.path("players");
    int open = players.path("post").asInt();
    int done = players.path("done").asInt();
    Table panel = panel();
    panel.add(label("Danke fürs Mitspielen!", BOLD_FONT, 48, TEXT)).row();
    panel
        .add(
            label("Bitte füllt jetzt die kurze Umfrage auf eurem Gerät aus.", TEXT_FONT, 28, MUTED))
        .padTop(8)
        .row();
    panel.add(label(done + " / " + (open + done), DOT_FONT, 128, ACCENT)).padTop(24).row();
    panel.add(label("Umfragen abgeschickt", TEXT_FONT, 26, TEXT)).row();
    panel.add(label("Enter beendet die Sitzung.", TEXT_FONT, 22, MUTED)).padTop(28);
    dimmed(panel);
  }

  // --- helpers

  private static String title(String kind, JsonNode context) {
    return switch (kind) {
      case "move" -> "Wohin geht es?";
      case "choice" -> context.path("multiple").asBoolean() ? "Antworten wählen" : "Antwort wählen";
      case "text" -> "Antwort eintippen";
      case "code" -> "Code eingeben";
      case "continue" -> "Weiterlesen?";
      default -> "";
    };
  }

  private static long remaining(String field) {
    JsonNode node =
        field.equals("phaseMs") ? state.path("phaseMs") : state.path("context").path(field);
    long elapsed = (java.lang.System.nanoTime() - stateReceivedAt) / 1_000_000;
    return Math.max(0, node.asLong() - elapsed);
  }

  // Voting devices out of all joined devices; the rest watch or still answer the pre survey.
  private static String playerLine() {
    JsonNode players = state.path("players");
    int joined = players.path("joined").asInt();
    int voting = players.path("play").asInt();
    return joined == 0 ? "Noch niemand dabei" : voting + "/" + joined + " spielen mit";
  }

  private static String joinUrl() {
    return state.path("relay").asString("") + "/?code=" + state.path("code").asString("");
  }

  private static String joinHost() {
    URI relay = URI.create(state.path("relay").asString("https://localhost"));
    return relay.getHost() + (relay.getPort() > 0 ? ":" + relay.getPort() : "") + relay.getPath();
  }

  private static String spacedCode() {
    String code = state.path("code").asString("");
    return code.length() == 6 ? code.substring(0, 3) + " " + code.substring(3) : "······";
  }

  private static ClickListener clicked(Runnable action) {
    return new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        // A click on the join panel must not reach the root, which would close the screen again.
        event.stop();
        return super.touchDown(event, x, y, pointer, button);
      }

      @Override
      public void clicked(InputEvent event, float x, float y) {
        action.run();
      }
    };
  }

  private Table panel() {
    Table panel = new Table();
    panel.setBackground(tinted(PANEL));
    panel.pad(28);
    return panel;
  }

  private void dimmed(Table content) {
    root.setBackground(tinted(GROUND));
    root.center();
    root.add(content);
  }

  private Image qr() {
    String url = joinUrl();
    if (!url.equals(qrFor) || qrTexture == null) {
      if (qrTexture != null) qrTexture.dispose();
      qrTexture = qrTexture(url);
      qrFor = url;
    }
    return new Image(qrTexture);
  }

  // Renders a QR code with a quiet zone, dark modules on a light ground for reliable scanning.
  private static Texture qrTexture(String url) {
    QrCode qr = QrCode.encodeText(url, QrCode.Ecc.MEDIUM);
    int border = 2;
    int size = qr.size + border * 2;
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.valueOf("f4f5f7ff"));
    pixmap.fill();
    pixmap.setColor(Color.valueOf("14161cff"));
    for (int y = 0; y < qr.size; y++) {
      for (int x = 0; x < qr.size; x++) {
        if (qr.getModule(x, y)) pixmap.drawPixel(x + border, y + border);
      }
    }
    Texture texture = new Texture(pixmap);
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    pixmap.dispose();
    return texture;
  }

  // A plain area of the color, from the skin's white pixel.
  private static Drawable tinted(Color color) {
    return UIUtils.defaultSkin().newDrawable("white", color);
  }

  private static Label label(String text, String font, int size, Color color) {
    Label label = Scene2dElementFactory.createLabel(text, FontSpec.of(font, size, color));
    label.setAlignment(Align.left);
    return label;
  }
}
