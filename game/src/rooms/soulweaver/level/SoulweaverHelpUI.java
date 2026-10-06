package rooms.soulweaver.level;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import feature.hud.UIUtils;
import java.util.Objects;
import java.util.function.BiConsumer;

/** Shared help overlay. Only the server releases tips or changes a puzzle. */
final class SoulweaverHelpUI extends Table {
  private final String controls;
  private final BiConsumer<String, String> send;
  private final Runnable back;
  private final java.util.function.BooleanSupplier allowed;
  private boolean previousAllowed;
  private SoulweaverHelp.State state;
  private boolean confirming;
  private final Table content = new Table();
  private final ScrollPane scroll;

  SoulweaverHelpUI(String controls, BiConsumer<String, String> send, Runnable back) {
    this(controls, send, back, () -> true);
  }

  SoulweaverHelpUI(
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
    setBackground(SoulweaverUI.background(SoulweaverUI.INK, false));
    rebuild();
  }

  static boolean simplified(String puzzleId) {
    return SoulweaverHelp.state()
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
    SoulweaverHelp.state()
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
    content.add(SoulweaverUI.label(text, size, color)).growX().padBottom(10).row();
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
    line(state == null ? "Hilfe" : state.title(), 25, SoulweaverUI.GOLD);
    if (state != null) {
      line("Dein Ziel", 18, SoulweaverUI.GOLD);
      line(state.goal(), 19, SoulweaverUI.TEXT);
      if (!confirming) {
        line("Tipps · " + state.level() + " / 3", 18, SoulweaverUI.GOLD);
        if (state.level() == 0)
          line("Für dieses Rätsel hast du noch keinen Tipp angefordert.", 17, SoulweaverUI.MUTED);
        for (var tip :
            state.history().stream().filter(t -> t.puzzleId().equals(state.puzzleId())).toList()) {
          line(tip.title() + " · Tipp " + tip.step(), 17, SoulweaverUI.GOLD);
          line(tip.text(), 18, SoulweaverUI.TEXT);
        }
        if (!state.status().isBlank()) line(state.status(), 17, SoulweaverUI.MUTED);
      }
      if (confirming) {
        line("Rätsel wirklich lösen?", 22, SoulweaverUI.GOLD);
        line(
            switch (state.puzzleId()) {
              case "methods" ->
                  "Dein bisheriger Code wird durch die Lösung ersetzt und ausgeführt. Das Rätsel wird für alle Spieler gelöst.";
              case "decisions" ->
                  "Nox nimmt an dieser Kreuzung die Tür des ausgeführten Zweigs. Die folgenden Kreuzungen entscheidest du weiter selbst.";
              default ->
                  "Die Hilfe setzt die Lösung für dieses Rätsel ein. Das Rätsel wird für alle Spieler gelöst.";
            },
            18,
            SoulweaverUI.TEXT);
        content
            .add(
                SoulweaverUI.button(
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
                SoulweaverUI.button(
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
            SoulweaverUI.button(
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
          SoulweaverUI.MUTED);
    if (!confirming) {
      line("Bedienung", 18, SoulweaverUI.GOLD);
      for (String control : controls.split("\n")) line(control, 17, SoulweaverUI.TEXT);
    }
    if (!confirming
        && state != null
        && state.history().stream().anyMatch(t -> !t.puzzleId().equals(state.puzzleId()))) {
      content.add().height(24).row();
      line("Bisherige Tipps aus anderen Rätseln", 18, SoulweaverUI.GOLD);
      for (var tip :
          state.history().stream().filter(t -> !t.puzzleId().equals(state.puzzleId())).toList()) {
        line(tip.title() + " · Tipp " + tip.step(), 17, SoulweaverUI.GOLD);
        line(tip.text(), 17, SoulweaverUI.TEXT);
      }
    }
  }
}
