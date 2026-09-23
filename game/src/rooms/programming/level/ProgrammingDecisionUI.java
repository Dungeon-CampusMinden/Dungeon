package rooms.programming.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.systems.CameraSystem;
import engine.utils.Cursors;
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
  private final Cell<Label> feedbackCell;
  private final Label code = ProgrammingUI.label("", 18, ProgrammingUI.TEXT);
  private final ScrollPane scroll;
  private final TextButton heading;
  private final TextButton left;
  private final TextButton right;
  private final TextButton resume;
  private final TextButton helpButton;
  private final ProgrammingHelpUI helpView;
  private final Vector3 projected = new Vector3();
  private final Vector2 stagePoint = new Vector2();
  private ProgrammingDecisions.State state;
  private int revision = -1;
  private boolean showRune = true;
  private boolean helpOpen;
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
    bar.add(resume).width(110).height(38).padLeft(10);
    helpView =
        new ProgrammingHelpUI(
            "Entscheiden: LINKS oder RECHTS über der Tür wählen.\n"
                + "Rune: Überschrift anklicken zum Ein- oder Ausklappen.\n"
                + "Weiter: Setzt Nox nach einer Unterbrechung oder am START fort.",
            this::helpEvent,
            () -> help(false));
    helpButton =
        ProgrammingUI.referenceButton(
            "Hilfe",
            () -> {
              if (helpOpen) {
                helpView.returnToPuzzle();
                return;
              }
              ProgrammingHelp.state().ifPresent(s -> helpEvent("help.open", s.puzzleId()));
              help(true);
            });
    helpButton.setUserObject(Cursors.HELP);
    bar.add(helpButton).width(95).height(38).padLeft(10);
    bar.add(
            ProgrammingUI.button(
                "Quest-Log",
                false,
                () ->
                    ProgrammingHelp.state()
                        .ifPresent(s -> helpEvent("help.questlog", s.puzzleId()))))
        .width(120)
        .height(38)
        .padLeft(8)
        .row();
    feedbackCell = bar.add(feedback).colspan(4).growX().padTop(5);
    addActor(bar);
    helpView.setVisible(false);
    addActor(helpView);
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
    values.setWidth(getWidth() - 24 - 20 - 120 - 105 - 128);
    feedback.setWidth(getWidth() - 24 - 20);
    // Without a status message the bar collapses to the value row.
    boolean hasFeedback = feedback.getText().length() > 0;
    float feedbackHeight = hasFeedback ? feedback.getPrefHeight() : 0;
    feedbackCell.padTop(hasFeedback ? 5 : 0).height(feedbackHeight);
    float barHeight =
        20 + Math.max(38, values.getPrefHeight()) + (hasFeedback ? 5 : 0) + feedbackHeight;
    bar.setBounds(12, 12, getWidth() - 24, barHeight);
    float width = Math.min(560, getWidth() - 40);
    heading.getLabel().setWidth(width - 20);
    float headingHeight = Math.max(48, heading.getLabel().getPrefHeight() + 20);
    panel.getCell(heading).height(headingHeight);
    float bottom = bar.getY() + barHeight + 8;
    // Keep the book's upper edge below Nox even on a short window.
    float expanded = Math.max(100, getHeight() * .45f - bottom);
    panel.setBounds((getWidth() - width) / 2, bottom, width, showRune ? expanded : headingHeight);
    helpView.setBounds(panel.getX(), bottom, width, expanded);
    // Help level changes do not alter the decision revision; the simplified book follows them.
    String rune = runeText(state);
    if (!code.getText().toString().equals(rune)) code.setText(rune);
    placeChoice(left, true);
    placeChoice(right, false);
  }

  private void help(boolean open) {
    helpOpen = open;
    helpButton.setChecked(open);
    helpView.setVisible(open);
    panel.setVisible(!open);
  }

  private void helpEvent(String action, String puzzleId) {
    DialogCallbackResolver.createButtonCallback(dialogId, action)
        .accept(new DialogResponseMessage.StringValue(puzzleId));
  }

  /**
   * @param state current decision snapshot
   * @return current rune; after the third tip with Nox' values substituted into its conditions
   */
  private static String runeText(ProgrammingDecisions.State state) {
    String rune = DecisionMaze.RUNES[Math.min(5, state.junction())];
    if (!ProgrammingHelpUI.simplified("decisions")) return rune;
    var values = state.values();
    return rune.replace("Kraft", String.valueOf(values.kraft()))
        .replace("Energie", String.valueOf(values.energie()))
        .replace("Temperatur", String.valueOf(values.temperatur()));
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
    boolean newCode = state == null || state.junction() != next.junction();
    state = next;
    revision = next.revision();
    if (arrived) showRune = true;
    values.setText(next.values().label() + "     Versuch " + (next.failures() + 1));
    feedback.setText(
        next.driver() == viewer
            ? next.feedback()
            : (next.feedback().isEmpty() ? "" : next.feedback() + " · ")
                + "Ein anderer Spieler reitet Nox.");
    // The completed state may arrive just before the server closes this dialog.
    int rune = Math.min(5, next.junction());
    title = "Kreuzung " + (rune + 1) + " / 6 · " + DecisionMaze.TITLES[rune];
    if (newCode) {
      code.setText(runeText(next));
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
