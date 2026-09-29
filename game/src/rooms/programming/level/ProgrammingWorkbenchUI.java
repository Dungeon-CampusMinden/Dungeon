package rooms.programming.level;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import engine.utils.Cursors;
import feature.canvas.CanvasLayout;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasOptions;
import feature.canvas.CanvasSnapshot;
import feature.canvas.CanvasUI;
import feature.canvas.NodeOrigin;
import feature.hud.UIUtils;
import java.util.List;

/**
 * Fixed navigation around a scrollable workspace; drawing and drag coordinates remain canvas-local.
 */
abstract class ProgrammingWorkbenchUI extends CanvasUI {
  protected final Table shell = new Table();
  protected final Table actions = new Table();
  protected final Table footer = new Table();
  protected final Label feedback = ProgrammingUI.label("", 17, ProgrammingUI.GOLD);
  protected final Label status = ProgrammingUI.label("", 16, ProgrammingUI.MUTED);
  private final Table canvasContent = new Table();
  private final ScrollPane scroll;
  private final Stack body = new Stack();
  private ProgrammingHelpUI reference;
  private com.badlogic.gdx.scenes.scene2d.ui.TextButton helpButton;
  private boolean referenceOpen;

  ProgrammingWorkbenchUI(
      String id, String dialogId, String title, String goal, List<CanvasNode> nodes) {
    super(
        id,
        new CanvasLayout(
            "",
            1,
            1,
            new CanvasOptions()
                .backgroundColor(ProgrammingUI.INK)
                .grid(32, false)
                .selectionEnabled(false)
                .multiSelectEnabled(false)
                .rubberBandEnabled(false)
                .keyboardShortcutsEnabled(false)
                .panButton(-1)
                .panWithSpace(false)
                .zoom(1, 1),
            false),
        new CanvasSnapshot(
            nodes.stream()
                .map(CanvasNode::toState)
                .map(s -> s.withOrigin(NodeOrigin.DEFAULT))
                .toList()),
        dialogId,
        nodes);
    clearChildren();
    setUserObject(Cursors.DEFAULT);
    shell.setFillParent(true);
    shell.top().pad(20);
    shell.setBackground(ProgrammingUI.background(ProgrammingUI.INK, false));
    shell.add(ProgrammingUI.header(title, actions, this::requestClose)).growX().padBottom(8).row();
    shell.add(ProgrammingUI.label(goal, 16, ProgrammingUI.MUTED)).growX().padBottom(16).row();
    canvasContent.top();
    canvasContent.add(area()).growX();
    scroll = new ScrollPane(canvasContent, UIUtils.defaultSkin());
    var scrollStyle = new ScrollPane.ScrollPaneStyle(scroll.getStyle());
    scrollStyle.background = null;
    scroll.setStyle(scrollStyle);
    scroll.setScrollingDisabled(true, false);
    scroll.setFadeScrollBars(false);
    scroll.setFlickScroll(false);
    scroll.addCaptureListener(
        new InputListener() {
          @Override
          public boolean scrolled(
              InputEvent event, float x, float y, float amountX, float amountY) {
            if (scroll.getMaxY() <= 0) return false;
            scroll.setScrollY(scroll.getScrollY() + amountY * 50);
            event.stop();
            return true;
          }
        });
    body.add(scroll);
    shell.add(body).grow().minSize(0).row();
    footer.add(feedback).growX().padTop(12).row();
    footer.add(status).growX().padTop(6).row();
    shell.add(footer).growX();
    addActor(shell);
  }

  protected final void help(String controls) {
    reference = new ProgrammingHelpUI(controls, this::helpEvent, () -> reference(false));
    reference.setVisible(false);
    body.add(reference);
    helpButton =
        ProgrammingUI.referenceButton(
            "Hilfe",
            () -> {
              if (referenceOpen) {
                reference.returnToPuzzle();
                return;
              }
              if (!referenceOpen)
                ProgrammingHelp.state().ifPresent(s -> helpEvent("help.open", s.puzzleId()));
              reference(!referenceOpen);
            });
    helpButton.setUserObject(Cursors.HELP);
    actions.add(helpButton).width(95).minHeight(44);
    actions
        .add(
            ProgrammingUI.button(
                "Quest-Log",
                false,
                () ->
                    ProgrammingHelp.state()
                        .ifPresent(s -> helpEvent("help.questlog", s.puzzleId()))))
        .width(120)
        .minHeight(44)
        .padLeft(8);
  }

  private void helpEvent(String action, String puzzleId) {
    area()
        .fireServerEvent(
            action, new engine.network.messages.c2s.DialogResponseMessage.StringValue(puzzleId));
  }

  private void reference(boolean open) {
    referenceOpen = open;
    helpButton.setChecked(open);
    reference.setVisible(open);
    scroll.setVisible(!open);
    if (getStage() != null) getStage().setScrollFocus(open ? null : scroll);
  }

  protected final boolean referenceOpen() {
    return referenceOpen;
  }

  @Override
  public void requestClose() {
    if (reference != null) reference.dismissConfirmation();
    super.requestClose();
  }

  /**
   * Fits and centers the workspace, scaling its contents with the available window size.
   *
   * @param width available workspace width
   * @param height visible workspace height
   * @return natural content height, including scrollable overflow
   */
  protected abstract float layoutWorkspace(float width, float height);

  @Override
  public void draw(Batch batch, float alpha) {
    setSize(Game.windowWidth(), Game.windowHeight());
    shell.validate();
    float width = Math.max(1, body.getWidth() - 18);
    float height = Math.max(body.getHeight(), layoutWorkspace(width, body.getHeight()));
    if (area().getWidth() != width || area().getHeight() != height) {
      canvasContent.getCell(area()).width(width).height(height);
      canvasContent.invalidateHierarchy();
      shell.validate();
    }
    super.draw(batch, alpha);
  }
}
