package rooms.programming.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasGraphics;
import feature.canvas.CanvasLayout;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasOptions;
import feature.canvas.CanvasSnapshot;
import feature.canvas.CanvasUI;
import feature.canvas.NodeOrigin;
import java.util.Comparator;
import rooms.programming.modules.loops.TerminalState;

/** Keeps live server updates separate from the player's canvas arrangement. */
final class ProgrammingTerminalUI extends CanvasUI {
  private TerminalState current;
  private boolean initialViewPlaced;
  private final Label code = Scene2dElementFactory.createLabel("", 18, Color.valueOf("f1eadc"));
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
        new CanvasLayout(
            "Golem · Schleifenterminal",
            1050,
            720,
            new CanvasOptions()
                .backgroundColor(ProgrammingTerminal.INK)
                .grid(32, true)
                .gridColor(com.badlogic.gdx.graphics.Color.valueOf("1c2730"))
                .selectionColor(ProgrammingTerminal.ACCENT)
                .multiSelectEnabled(false)
                .initialZoom(1)
                .zoom(.3f, 2.5f),
            true),
        new CanvasSnapshot(
            ProgrammingTerminal.nodes(initial).stream()
                .map(CanvasNode::toState)
                .map(s -> s.withOrigin(NodeOrigin.DEFAULT))
                .toList()),
        dialogId,
        ProgrammingTerminal.nodes(initial));
    current = initial;
    update(initial);
    tooltip.setTransform(false);
    tooltip.setTouchable(Touchable.disabled);
    tooltip.setVisible(false);
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
    if (getStage() == null || Gdx.input.isTouched()) return;
    Vector2 pointer =
        area().screenToLocalCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
    if (pointer.x < 0
        || pointer.y < 0
        || pointer.x > area().getWidth()
        || pointer.y > area().getHeight()) return;
    Vector2 world = area().areaToWorld(pointer.x, pointer.y);
    area().nodes().stream()
        .filter(CanvasNode::isVisible)
        .filter(node -> node.bounds().contains(world))
        .max(Comparator.comparingInt(CanvasNode::z))
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
  public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float alpha) {
    if (!initialViewPlaced) {
      for (var child : getChildren()) {
        if (child instanceof com.badlogic.gdx.scenes.scene2d.utils.Layout layout) layout.validate();
      }
      var bounds = new com.badlogic.gdx.math.Rectangle(area().nodes().getFirst().bounds());
      for (var node : area().nodes()) bounds.merge(node.bounds());
      float zoom =
          Math.min(
              1,
              Math.min(
                  (area().getWidth() - 64) / bounds.width,
                  (area().getHeight() - 104) / bounds.height));
      area().zoom(zoom);
      area()
          .pan(
              (area().getWidth() - bounds.width * zoom) / 2 - bounds.x * zoom,
              32 + (area().getHeight() - 104 - bounds.height * zoom) / 2 - bounds.y * zoom);
      initialViewPlaced = true;
    }
    super.draw(batch, alpha);
  }

  private void update(TerminalState state) {
    current = state;
    for (int i = 0; i < state.collectedRunes().size(); i++) {
      String id = state.collectedRunes().get(i);
      if (area().nodeById(id).isEmpty()) area().addNode(ProgrammingTerminal.card(id, i));
    }
    for (CanvasNode node : area().nodes()) {
      if (node instanceof ProgrammingTerminalNode terminal) terminal.update(current);
    }
  }
}
