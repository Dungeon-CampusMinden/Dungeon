package rooms.programming.level;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import feature.hud.UIUtils;
import java.util.Objects;
import java.util.function.BiConsumer;

/** Shared help overlay. Only the server releases tips or changes a puzzle. */
final class ProgrammingHelpUI extends Table {
  private final String controls;
  private final BiConsumer<String, String> send;
  private final Runnable back;
  private final java.util.function.BooleanSupplier allowed;
  private boolean previousAllowed;
  private ProgrammingHelp.State state;
  private boolean confirming;
  private final Table content = new Table();
  private final ScrollPane scroll;

  ProgrammingHelpUI(String controls, BiConsumer<String, String> send, Runnable back) {
    this(controls, send, back, () -> true);
  }

  ProgrammingHelpUI(
      String controls,
      BiConsumer<String, String> send,
      Runnable back,
      java.util.function.BooleanSupplier allowed) {
    this.allowed = allowed;
    this.controls = controls;
    this.send = send;
    this.back = back;
    content.top().left().pad(20);
    scroll = new ScrollPane(content, UIUtils.defaultSkin());
    var style = new ScrollPane.ScrollPaneStyle(scroll.getStyle());
    style.background = null;
    scroll.setStyle(style);
    scroll.setScrollingDisabled(true, false);
    scroll.setFlickScroll(false);
    scroll.setFadeScrollBars(false);
    add(scroll).grow().minSize(0);
    setBackground(ProgrammingUI.background(ProgrammingUI.INK, false));
    rebuild();
  }

  static boolean simplified(String puzzleId) {
    return ProgrammingHelp.state()
        .filter(s -> s.puzzleId().equals(puzzleId) && s.level() >= 3)
        .isPresent();
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (previousAllowed != allowed.getAsBoolean()) {
      previousAllowed = allowed.getAsBoolean();
      if (!previousAllowed) confirming = false;
      rebuild();
    }
    ProgrammingHelp.state()
        .ifPresent(
            next -> {
              if (Objects.equals(state, next)) return;
              if (state == null || !state.puzzleId().equals(next.puzzleId()) || !next.canSolve())
                confirming = false;
              state = next;
              rebuild();
            });
  }

  private void line(String text, int size, com.badlogic.gdx.graphics.Color color) {
    content.add(ProgrammingUI.label(text, size, color)).growX().padBottom(10).row();
  }

  private void event(String name) {
    if (state != null) send.accept(name, state.puzzleId());
  }

  void returnToPuzzle() {
    dismissConfirmation();
    event("help.return");
    rebuild();
    back.run();
  }

  /** Records a user dismissal once; forced dialog removal does not call this hook. */
  void dismissConfirmation() {
    if (!confirming) return;
    confirming = false;
    event("help.cancel");
  }

  private void rebuild() {
    content.clearChildren();
    line(state == null ? "Hilfe" : state.title(), 25, ProgrammingUI.GOLD);
    if (state != null) {
      line("Dein Ziel", 18, ProgrammingUI.GOLD);
      line(state.goal(), 19, ProgrammingUI.TEXT);
      if (!confirming) {
        line("Tipps · " + state.level() + " / 3", 18, ProgrammingUI.GOLD);
        if (state.level() == 0)
          line("Für dieses Rätsel hast du noch keinen Tipp angefordert.", 17, ProgrammingUI.MUTED);
        for (var tip :
            state.history().stream().filter(t -> t.puzzleId().equals(state.puzzleId())).toList()) {
          line(tip.title() + " · Tipp " + tip.step(), 17, ProgrammingUI.GOLD);
          line(tip.text(), 18, ProgrammingUI.TEXT);
        }
        if (!state.status().isBlank()) line(state.status(), 17, ProgrammingUI.MUTED);
      }
      if (confirming) {
        line("Rätsel wirklich lösen?", 22, ProgrammingUI.GOLD);
        line(
            state.puzzleId().equals("methods")
                ? "Dein bisheriger Code wird durch die Lösung ersetzt und ausgeführt. Das Rätsel wird für alle Spieler gelöst."
                : "Die Hilfe setzt die Lösung für dieses Rätsel ein. Das Rätsel wird für alle Spieler gelöst.",
            18,
            ProgrammingUI.TEXT);
        content
            .add(
                ProgrammingUI.button(
                    "Ja, Rätsel lösen",
                    true,
                    () -> {
                      if (!allowed.getAsBoolean()) return;
                      event("help.solve");
                      confirming = false;
                      rebuild();
                    }))
            .growX()
            .minHeight(44)
            .padBottom(8)
            .row();
        content
            .add(
                ProgrammingUI.button(
                    "Abbrechen",
                    false,
                    () -> {
                      event("help.cancel");
                      confirming = false;
                      rebuild();
                    }))
            .growX()
            .minHeight(44)
            .padBottom(14)
            .row();
      } else {
        var next =
            ProgrammingUI.button(
                state.level() >= 3 ? "Rätsel lösen ..." : "Nächster Tipp",
                true,
                () -> {
                  if (state.level() >= 3) {
                    event("help.solve.request");
                    confirming = true;
                    rebuild();
                  } else event("help.next");
                });
        next.setDisabled(
            state.level() >= 3
                ? !state.canSolve() || !allowed.getAsBoolean()
                : !state.canRequest());
        content.add(next).growX().minHeight(44).padBottom(18).row();
      }
    }
    if (state != null && state.level() >= 3 && !allowed.getAsBoolean())
      line(
          "Nur der bearbeitende Spieler kann die Lösung einsetzen. Warte, bis alle Änderungen gespeichert sind.",
          17,
          ProgrammingUI.MUTED);
    if (!confirming) {
      line("Bedienung", 18, ProgrammingUI.GOLD);
      for (String control : controls.split("\n")) line(control, 17, ProgrammingUI.TEXT);
    }
    if (!confirming
        && state != null
        && state.history().stream().anyMatch(t -> !t.puzzleId().equals(state.puzzleId()))) {
      content.add().height(24).row();
      line("Bisherige Tipps aus anderen Rätseln", 18, ProgrammingUI.GOLD);
      for (var tip :
          state.history().stream().filter(t -> !t.puzzleId().equals(state.puzzleId())).toList()) {
        line(tip.title() + " · Tipp " + tip.step(), 17, ProgrammingUI.GOLD);
        line(tip.text(), 17, ProgrammingUI.TEXT);
      }
    }
  }
}
