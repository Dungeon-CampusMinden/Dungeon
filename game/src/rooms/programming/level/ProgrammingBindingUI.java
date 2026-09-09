package rooms.programming.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import feature.canvas.CanvasLayout;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasOptions;
import feature.canvas.CanvasSnapshot;
import feature.canvas.CanvasUI;
import feature.canvas.NodeOrigin;
import java.util.ArrayList;
import java.util.List;
import rooms.programming.modules.variables.BindingState;
import rooms.programming.modules.variables.GolemProperty;
import rooms.programming.modules.variables.MagicalEssence;
import rooms.programming.modules.variables.SoulVessel;

/** One workbench: reusable vessel stamps, named storage and replaceable value tokens. */
final class ProgrammingBindingUI extends CanvasUI {
  private BindingState state;
  private boolean frame = true;
  private float viewportWidth;
  private float viewportHeight;

  ProgrammingBindingUI(String dialogId, BindingState initial) {
    super(
        ProgrammingBinding.ID,
        new CanvasLayout(
            "Nox · Seelenwerkbank",
            // Let the dialog shell size the viewport, even on smaller windows.
            1,
            1,
            new CanvasOptions()
                .backgroundColor(Color.valueOf("11191d"))
                .grid(32, false)
                .selectionColor(Color.valueOf("e8b566"))
                .multiSelectEnabled(false)
                .initialZoom(1)
                .zoom(.35f, 2),
            true),
        new CanvasSnapshot(
            nodes().stream()
                .map(CanvasNode::toState)
                .map(s -> s.withOrigin(NodeOrigin.DEFAULT))
                .toList()),
        dialogId,
        nodes());
    update(initial);
  }

  private static List<CanvasNode> nodes() {
    var nodes = new ArrayList<CanvasNode>();
    nodes.add(
        new ProgrammingBindingNode("vessel-heading", ProgrammingBindingNode.Kind.HEADING)
            .position(0, 586));
    for (var vessel : SoulVessel.values())
      nodes.add(
          new ProgrammingBindingNode("vessel-" + vessel.name(), ProgrammingBindingNode.Kind.VESSEL)
              .position(0, 490 - vessel.ordinal() * 100));
    for (var property : GolemProperty.values())
      nodes.add(
          new ProgrammingBindingNode(property.name(), ProgrammingBindingNode.Kind.SOCKET)
              .position(property.ordinal() < 3 ? 260 : 794, 420 - property.ordinal() % 3 * 168));
    nodes.add(
        new ProgrammingBindingNode("core", ProgrammingBindingNode.Kind.CORE).position(558, 176));
    nodes.add(
        new ProgrammingBindingNode("feedback", ProgrammingBindingNode.Kind.FEEDBACK)
            .position(260, -30));
    nodes.add(
        new ProgrammingBindingNode("essence-heading", ProgrammingBindingNode.Kind.HEADING)
            .position(0, -84));
    for (var essence : MagicalEssence.values())
      nodes.add(
          new ProgrammingBindingNode(
                  "essence-" + essence.name(), ProgrammingBindingNode.Kind.ESSENCE)
              .position(essence.ordinal() * 156, -186));
    return nodes;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    ProgrammingBinding.state().ifPresent(this::update);
  }

  private void update(BindingState next) {
    if (state != null
        && (state.propertiesCollected() != next.propertiesCollected()
            || state.vesselsCollected() != next.vesselsCollected())) frame = true;
    state = next;
    for (var node : area().nodes())
      if (node instanceof ProgrammingBindingNode binding) binding.update(next);
  }

  @Override
  protected void resetView() {
    frame = true;
  }

  @Override
  public void draw(Batch batch, float alpha) {
    if (getWidth() != viewportWidth || getHeight() != viewportHeight) {
      viewportWidth = getWidth();
      viewportHeight = getHeight();
      frame = true;
    }
    if (frame) {
      for (var child : getChildren())
        if (child instanceof com.badlogic.gdx.scenes.scene2d.utils.Layout layout) layout.validate();
      // Fixed workbench bounds prevent empty supplies from hiding the assembly's spatial structure.
      float bottom = state.vesselsCollected() ? -198 : -42;
      float height = 630 - bottom;
      float zoom =
          Math.min(
              1, Math.min((area().getWidth() - 64) / 1080, (area().getHeight() - 112) / height));
      area().zoom(zoom);
      area().pan((area().getWidth() - 1080 * zoom) / 2, 24 - bottom * zoom);
      frame = false;
    }
    super.draw(batch, alpha);
  }
}
