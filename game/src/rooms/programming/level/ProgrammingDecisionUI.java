package rooms.programming.level;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.canvas.CanvasUI;
import feature.hud.dialogs.DialogCallbackResolver;
import rooms.programming.modules.decisions.DecisionMaze;

/** The code stays beside the live scene; during travel only the current values remain over it. */
final class ProgrammingDecisionUI extends Group {
  private final String dialogId;
  private final int viewer;
  private final Table panel = new Table();
  private final Table bar = new Table();
  private final Label title = ProgrammingUI.label("", 22, ProgrammingUI.GOLD);
  private final Label values = ProgrammingUI.label("", 18, ProgrammingUI.TEXT);
  private final Label feedback = ProgrammingUI.label("", 17, ProgrammingUI.TEXT);
  private final Label code = ProgrammingUI.label("", 18, ProgrammingUI.TEXT);
  private final Table actions = new Table();
  private ProgrammingDecisions.State state;
  private int revision = -1;
  private boolean showRune = true;
  private com.badlogic.gdx.scenes.scene2d.ui.TextButton viewButton;

  ProgrammingDecisionUI(String dialogId, ProgrammingDecisions.State initial, int viewer) {
    this.dialogId = dialogId;
    this.viewer = viewer;
    addActor(new ProgrammingObservation.View(initial.golemId(), dialogId, true));
    panel.setBackground(ProgrammingUI.background(ProgrammingUI.INK, true));
    panel.pad(20);
    panel.top().left();
    title.setWrap(true);
    panel.add(title).growX().row();
    var scroll = new ScrollPane(code);
    scroll.setScrollingDisabled(false, false);
    panel.add(scroll).grow().padTop(14).row();
    panel.add(actions).growX().padTop(12);
    addActor(panel);
    bar.setBackground(ProgrammingUI.background(ProgrammingUI.INK, true));
    bar.pad(14);
    values.setWrap(true);
    bar.add(values).growX();
    viewButton =
        ProgrammingUI.button(
            "Raum ansehen",
            false,
            () -> {
              showRune = !showRune;
              viewButton.setText(showRune ? "Raum ansehen" : "Rune ansehen");
              panel.setVisible(showRune && !state.moving());
            });
    bar.add(viewButton).width(155).height(40).padLeft(12);
    bar.add(
            ProgrammingUI.button(
                "Schließen",
                false,
                () ->
                    DialogCallbackResolver.createButtonCallback(dialogId, CanvasUI.EVENT_CLOSE)
                        .accept(null)))
        .width(125)
        .height(40)
        .padLeft(12)
        .row();
    feedback.setWrap(true);
    bar.add(feedback).colspan(3).growX().padTop(8);
    addActor(bar);
    update(initial);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    setSize(Game.windowWidth(), Game.windowHeight());
    setPosition(0, 0);
    ProgrammingDecisions.state().ifPresent(this::update);
    float width = Math.min(560, getWidth() * .46f);
    bar.setBounds(16, 16, getWidth() - 32, 128);
    panel.setBounds(16, 160, width, Math.max(180, getHeight() - 176));
  }

  private void update(ProgrammingDecisions.State next) {
    state = next;
    if (revision == next.revision()) return;
    revision = next.revision();
    values.setText(next.values().label() + "     Versuch " + (next.failures() + 1));
    feedback.setText(
        next.feedback()
            + (next.driver() != viewer
                ? " · Du beobachtest. Ein anderer Spieler steuert Nox."
                : ""));
    panel.setVisible(showRune && !next.moving());
    title.setText(
        next.completed()
            ? "Herzfeuer · Die Runen waren Programmcode"
            : "Kreuzung "
                + (next.junction() + 1)
                + " / 6 · "
                + DecisionMaze.TITLES[Math.min(5, next.junction())]);
    code.setText(
        next.completed()
            ?
"""
Eine Bedingung wählt einen Zweig.
Ein Zweig kann weitere Bedingungen enthalten.
Das heißt Verschachtelung.

WENN = if    SONST = else
UND = &&    ODER = ||
WAHR = true    FALSCH = false

"""
                + DecisionMaze.JAVA
            : DecisionMaze.RUNES[Math.min(5, next.junction())]);
    actions.clearChildren();
    if (next.blocked())
      actions.add(ProgrammingUI.button("Weiter", true, () -> send("RESUME"))).growX();
    else if (!next.completed()) {
      actions.add(ProgrammingUI.button("LINKS", true, () -> send("LEFT"))).growX().padRight(12);
      actions.add(ProgrammingUI.button("RECHTS", true, () -> send("RIGHT"))).growX();
    }
    for (var child : actions.getChildren())
      if (child instanceof com.badlogic.gdx.scenes.scene2d.ui.TextButton button)
        button.setDisabled(next.driver() != viewer);
  }

  private void send(String operation) {
    if (state.driver() != viewer) return;
    DialogCallbackResolver.createButtonCallback(dialogId, "intent")
        .accept(
            new DialogResponseMessage.StringValue(
                ProgrammingDecisions.intent(
                    new ProgrammingDecisions.Intent(state.revision(), operation))));
  }
}
