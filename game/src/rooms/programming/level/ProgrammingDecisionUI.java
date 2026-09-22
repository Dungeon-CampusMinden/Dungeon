package rooms.programming.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.systems.CameraSystem;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import rooms.programming.modules.decisions.DecisionMaze;

/** Rune book below the rider, with choices attached to the two passage entrances. */
final class ProgrammingDecisionUI extends Group {
  private final String dialogId;
  private final int viewer;
  private final Table panel = new Table();
  private final Table bar = new Table();
  private final Label values = ProgrammingUI.label("", 17, ProgrammingUI.TEXT);
  private final Label feedback = ProgrammingUI.label("", 16, ProgrammingUI.TEXT);
  private final Label code = ProgrammingUI.label("", 18, ProgrammingUI.TEXT);
  private final ScrollPane scroll;
  private final TextButton heading;
  private final TextButton left;
  private final TextButton right;
  private final TextButton resume;
  private final Vector3 projected = new Vector3();
  private final Vector2 stagePoint = new Vector2();
  private ProgrammingDecisions.State state;
  private int revision = -1;
  private boolean showRune = true;
  private String title = "";

  ProgrammingDecisionUI(String dialogId, ProgrammingDecisions.State initial, int viewer) {
    this.dialogId = dialogId;
    this.viewer = viewer;
    addActor(new ProgrammingRiderCamera(initial.golemId()));
    panel.setBackground(ProgrammingUI.background(ProgrammingUI.INK, true));
    panel.top();
    heading =
        ProgrammingUI.button(
            "",
            true,
            () -> {
              showRune = !showRune;
              arrangePanel();
            });
    code.setWrap(false);
    scroll = new ScrollPane(code, UIUtils.defaultSkin());
    var style = new ScrollPane.ScrollPaneStyle(scroll.getStyle());
    style.background = null;
    scroll.setStyle(style);
    scroll.setScrollingDisabled(false, false);
    scroll.setFlickScroll(false);
    scroll.setFadeScrollBars(false);
    addActor(panel);
    bar.setBackground(ProgrammingUI.background(ProgrammingUI.INK, true));
    bar.pad(10);
    values.setWrap(true);
    feedback.setWrap(true);
    bar.add(values).growX();
    resume = ProgrammingUI.button("Weiter", true, () -> send("RESUME"));
    bar.add(resume).width(110).height(38).padLeft(10).row();
    bar.add(feedback).colspan(2).growX().padTop(5);
    addActor(bar);
    left = ProgrammingUI.button("LINKS", true, () -> send("LEFT"));
    right = ProgrammingUI.button("RECHTS", true, () -> send("RIGHT"));
    addActor(left);
    addActor(right);
    update(initial);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    setSize(
        getStage() == null ? Game.windowWidth() : getStage().getWidth(),
        getStage() == null ? Game.windowHeight() : getStage().getHeight());
    setPosition(0, 0);
    ProgrammingDecisions.state().ifPresent(this::update);
    // Measure wrapped status text at its actual cell width before sizing the book.
    values.setWidth(getWidth() - 24 - 20 - 120);
    feedback.setWidth(getWidth() - 24 - 20);
    float barHeight = 20 + Math.max(38, values.getPrefHeight()) + 5 + feedback.getPrefHeight();
    bar.setBounds(12, 12, getWidth() - 24, barHeight);
    float width = Math.min(560, getWidth() - 40);
    heading.getLabel().setWidth(width - 20);
    float headingHeight = Math.max(48, heading.getLabel().getPrefHeight() + 20);
    panel.getCell(heading).height(headingHeight);
    float bottom = bar.getY() + barHeight + 8;
    // Keep the book's upper edge below Nox even on a short window.
    float height = showRune ? Math.max(100, getHeight() * .45f - bottom) : headingHeight;
    panel.setBounds((getWidth() - width) / 2, bottom, width, height);
    placeChoice(left, true);
    placeChoice(right, false);
  }

  private void arrangePanel() {
    panel.clearChildren();
    heading.setText((showRune ? "v  " : ">  ") + title);
    panel.add(heading).growX().height(48).row();
    if (showRune) panel.add(scroll).grow().minSize(0).pad(12);
  }

  /**
   * Camera projection uses bottom-up pixels; Scene2D's screen conversion expects top-down.
   *
   * @param button choice button to position
   * @param isLeft whether this is the left branch
   */
  private void placeChoice(TextButton button, boolean isLeft) {
    if (!button.isVisible() || getStage() == null) return;
    var point = ProgrammingDecisionWorld.choicePoint(state.junction(), isLeft);
    CameraSystem.camera().project(projected.set(point.x(), point.y() + .8f, 0));
    stagePoint.set(projected.x, Gdx.graphics.getHeight() - projected.y);
    getStage().screenToStageCoordinates(stagePoint);
    stageToLocalCoordinates(stagePoint);
    float width = 124;
    float height = 44;
    // Clamp within the matching half of the screen without crossing the rider in the center.
    float minX = isLeft ? 12 : getWidth() / 2 + 72;
    float maxX = isLeft ? getWidth() / 2 - 72 - width : getWidth() - width - 12;
    float x = Math.clamp(stagePoint.x - width / 2, minX, maxX);
    float y = Math.clamp(stagePoint.y + 8, getHeight() * .52f, getHeight() - height - 12);
    button.setBounds(x, y, width, height);
  }

  private void update(ProgrammingDecisions.State next) {
    if (revision == next.revision()) return;
    boolean arrived =
        state == null
            || state.junction() != next.junction()
            || (state.moving() && !next.moving() && !next.blocked());
    boolean newCode =
        state == null
            || state.junction() != next.junction()
            || state.completed() != next.completed();
    state = next;
    revision = next.revision();
    if (arrived) showRune = true;
    values.setText(next.values().label() + "     Versuch " + (next.failures() + 1));
    feedback.setText(
        next.feedback() + (next.driver() != viewer ? " · Ein anderer Spieler reitet Nox." : ""));
    title =
        next.completed()
            ? "Herzfeuer · Die Runen waren Programmcode"
            : "Kreuzung "
                + (next.junction() + 1)
                + " / 6 · "
                + DecisionMaze.TITLES[Math.min(5, next.junction())];
    if (newCode) {
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
      scroll.setScrollX(0);
      scroll.setScrollY(0);
      scroll.updateVisualScroll();
    }
    arrangePanel();
    boolean choosing = !next.moving() && !next.blocked() && !next.completed();
    left.setVisible(choosing);
    right.setVisible(choosing);
    left.setDisabled(next.driver() != viewer);
    right.setDisabled(next.driver() != viewer);
    resume.setVisible(next.blocked());
    resume.setDisabled(next.driver() != viewer);
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
