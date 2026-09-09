package rooms.programming.level;

import feature.canvas.CanvasLayout;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasOptions;
import feature.canvas.CanvasSnapshot;
import feature.canvas.CanvasUI;
import feature.canvas.NodeOrigin;
import rooms.programming.modules.loops.TerminalState;

/** Keeps live server updates separate from the player's canvas arrangement. */
final class ProgrammingTerminalUI extends CanvasUI {
  private TerminalState current;
  private boolean initialViewPlaced;

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
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    ProgrammingTerminal.state().ifPresent(this::update);
  }

  @Override
  public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float alpha) {
    if (!initialViewPlaced) {
      for (var child : getChildren()) {
        if (child instanceof com.badlogic.gdx.scenes.scene2d.utils.Layout layout) layout.validate();
      }
      float zoom =
          Math.min(1, Math.min((area().getWidth() - 64) / 1160, (area().getHeight() - 110) / 580));
      area().zoom(zoom);
      area().pan(32, area().getHeight() - 72 - 580 * zoom);
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
