package rooms.programming.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import engine.utils.CursorUtil;
import engine.utils.Cursors;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasNode;
import java.util.Optional;
import rooms.programming.modules.loops.TerminalState;

/** Keeps live server updates separate from the player's canvas arrangement. */
final class ProgrammingTerminalUI extends ProgrammingWorkbenchUI
    implements CursorUtil.CursorOverride {
  @Override
  public Optional<Cursors> cursorOverride() {
    for (var node : area().nodes()) {
      if (node instanceof ProgrammingTerminalNode source && source.dragging()) {
        boolean blocked =
            area().intersectsAll(source).stream()
                .anyMatch(
                    target ->
                        target instanceof ProgrammingTerminalNode slot
                            && slot.executor()
                            && !slot.accepts(source));
        return Optional.of(blocked ? Cursors.DISABLED : Cursors.GRABBING);
      }
    }
    return Optional.empty();
  }

  private final Label code = ProgrammingUI.label("", 18, ProgrammingUI.TEXT);
  private final Group tooltip =
      new Group() {
        @Override
        public void draw(Batch batch, float alpha) {
          CanvasGraphics.fill(
              batch, ProgrammingTerminal.INK, alpha, getX(), getY(), getWidth(), getHeight());
          CanvasGraphics.outline(
              batch, ProgrammingTerminal.ACCENT, alpha, getX(), getY(), getWidth(), getHeight(), 1);
          super.draw(batch, alpha);
        }
      };

  ProgrammingTerminalUI(String dialogId, TerminalState initial) {
    super(
        ProgrammingTerminal.ID,
        dialogId,
        "Nox · Kellersteuerung",
        "Ziehe eine Rune in den Executor und führe Nox zu den fünf Wegzeichen.",
        ProgrammingTerminal.nodes(initial));
    footer.clearChildren();
    footer.add(status).growX().padTop(8);
    help(
        "Starten: Rune in den Executor ziehen.\nCode lesen: Maus über eine Rune halten.\nWechseln: Nach dem Lauf Rune herausziehen oder ersetzen.\nBeobachten: Die Karte zeigt Nox und das nächste Wegzeichen.");
    update(initial);
    tooltip.setTransform(false);
    tooltip.setTouchable(Touchable.disabled);
    tooltip.setVisible(false);
    code.setWrap(false);
    code.setPosition(14, 12);
    tooltip.addActor(code);
    addActor(tooltip);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    ProgrammingTerminal.state().ifPresent(this::update);
    updateTooltip();
  }

  private void updateTooltip() {
    tooltip.setVisible(false);
    if (getStage() == null || referenceOpen() || Gdx.input.isTouched()) return;
    Vector2 stagePointer =
        getStage().screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
    var target = getStage().hit(stagePointer.x, stagePointer.y, true);
    if (target == null || !target.isDescendantOf(area())) return;
    Vector2 pointer =
        area().screenToLocalCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
    if (pointer.x < 0
        || pointer.y < 0
        || pointer.x > area().getWidth()
        || pointer.y > area().getHeight()) return;
    Vector2 world = area().areaToWorld(pointer.x, pointer.y);
    area()
        .nodeAt(world.x, world.y)
        .filter(CanvasNode::isVisible)
        .filter(ProgrammingTerminalNode.class::isInstance)
        .map(ProgrammingTerminalNode.class::cast)
        .flatMap(ProgrammingTerminalNode::hoverCode)
        .ifPresent(
            source -> {
              code.setText(source);
              code.pack();
              tooltip.setSize(code.getWidth() + 28, code.getHeight() + 24);
              Vector2 local =
                  screenToLocalCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
              tooltip.setPosition(
                  Math.max(8, Math.min(local.x + 18, getWidth() - tooltip.getWidth() - 8)),
                  Math.max(
                      8,
                      Math.min(
                          local.y - tooltip.getHeight() - 14,
                          getHeight() - tooltip.getHeight() - 8)));
              tooltip.setVisible(true);
              tooltip.toFront();
            });
  }

  @Override
  protected float layoutWorkspace(float width, float height) {
    float zoom = Math.min(width / 888, Math.max(1, height) / 802);
    float contentHeight = Math.max(1, height);
    area().options().zoom(zoom, zoom);
    area().zoom(zoom);
    area().pan((width - 868 * zoom) / 2, 234 * zoom + (contentHeight - 802 * zoom) / 2);
    return contentHeight;
  }

  private void update(TerminalState state) {
    status.setText(
        ProgrammingHelp.state()
            .filter(s -> s.puzzleId().equals("cellar-" + state.checkpoint()) && s.level() >= 3)
            .map(
                s ->
                    s.status().isBlank()
                        ? "Gold zeigt den aktuellen Weg. Nur passende Runengruppen sind sichtbar."
                        : s.status())
            .orElse(""));
    for (int i = 0; i < state.collectedRunes().size(); i++) {
      String id = state.collectedRunes().get(i);
      if (area().nodeById(id).isEmpty()) area().addNode(ProgrammingTerminal.card(id, i));
    }
    for (CanvasNode node : area().nodes()) {
      if (node instanceof ProgrammingTerminalNode terminal) terminal.update(state);
    }
  }
}
