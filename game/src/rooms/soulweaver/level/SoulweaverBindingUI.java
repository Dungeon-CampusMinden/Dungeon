package rooms.soulweaver.level;

import engine.utils.CursorUtil;
import engine.utils.Cursors;
import feature.canvas.CanvasNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import rooms.soulweaver.modules.variables.BindingState;
import rooms.soulweaver.modules.variables.GolemProperty;
import rooms.soulweaver.modules.variables.MagicalEssence;
import rooms.soulweaver.modules.variables.SoulVessel;

/** One workbench: reusable vessel stamps, named storage and replaceable value tokens. */
final class SoulweaverBindingUI extends SoulweaverWorkbenchUI implements CursorUtil.CursorOverride {
  private BindingState state;
  private SoulweaverBindingNode.Kind selectedKind;
  private String selectedSupply = "";

  @Override
  public Optional<Cursors> cursorOverride() {
    for (var node : area().nodes()) {
      if (node instanceof SoulweaverBindingNode source && source.dragging()) {
        boolean valid =
            area().intersectsAll(source).stream()
                .anyMatch(
                    target ->
                        target instanceof SoulweaverBindingNode socket && socket.accepts(source));
        return Optional.of(valid ? Cursors.GRABBING : Cursors.DISABLED);
      }
    }
    return Optional.empty();
  }

  SoulweaverBindingUI(String dialogId, BindingState initial) {
    super(
        SoulweaverBinding.ID,
        dialogId,
        "Nox · Seelenwerkbank",
        "Binde Gefäß, Name und Wert und erwecke Nox.",
        nodes());
    help(
        "Ablegen: Gefäß oder Essenz ziehen. Alternativ erst den Vorrat, dann eine Fassung anklicken.\nEntfernen: × an der Fassung. Vorräte sind mehrfach verwendbar.\nPrüfen: Nach vollständiger Bindung Aktivieren wählen.");
    update(initial);
  }

  private static List<CanvasNode> nodes() {
    var nodes = new ArrayList<CanvasNode>();
    nodes.add(
        new SoulweaverBindingNode("vessel-heading", SoulweaverBindingNode.Kind.HEADING)
            .position(0, 586));
    for (var vessel : SoulVessel.values())
      nodes.add(
          new SoulweaverBindingNode("vessel-" + vessel.name(), SoulweaverBindingNode.Kind.VESSEL)
              .position(0, 490 - vessel.ordinal() * 100));
    for (var property : GolemProperty.values())
      nodes.add(
          new SoulweaverBindingNode(property.name(), SoulweaverBindingNode.Kind.SOCKET)
              .position(property.ordinal() < 3 ? 260 : 794, 420 - property.ordinal() % 3 * 168));
    nodes.add(
        new SoulweaverBindingNode("core", SoulweaverBindingNode.Kind.CORE).position(558, 176));
    nodes.add(
        new SoulweaverBindingNode("essence-heading", SoulweaverBindingNode.Kind.HEADING)
            .position(0, 20));
    for (var essence : MagicalEssence.values())
      nodes.add(
          new SoulweaverBindingNode("essence-" + essence.name(), SoulweaverBindingNode.Kind.ESSENCE)
              .position(essence.ordinal() * 156, -80));
    return nodes;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    SoulweaverBinding.state().ifPresent(this::update);
  }

  private void update(BindingState next) {
    if (state != null && state.stage() != next.stage()) selectedSupply = "";
    state = next;
    feedback.setText(next.feedback());
    status.setText(
        next.revealed()
            ? "Gefäß · Name · Wert · Seelenbindung vollständig"
            : "Gefäße "
                + next.vessels().size()
                + " / 6 · Essenzen "
                + next.essences().size()
                + " / 6 · Gemeinsam bearbeiten");
    boolean vessels = next.stage() == rooms.soulweaver.state.VariablePuzzleStage.VESSELS;
    boolean aided = SoulweaverHelpUI.simplified(vessels ? "vessels" : "essences");
    GolemProperty focus =
        java.util.Arrays.stream(GolemProperty.values())
            .filter(
                property ->
                    vessels
                        ? next.vessels().get(property)
                            != rooms.soulweaver.modules.variables.VariablePuzzle.vesselSolution()
                                .get(property)
                        : next.essences().get(property)
                            != rooms.soulweaver.modules.variables.VariablePuzzle.essenceSolution()
                                .get(property))
            .findFirst()
            .orElse(null);
    if (aided && focus != null)
      status.setText("Markierte Fassung: " + focus.label() + " · Passende Vorräte sind sichtbar.");
    for (var node : area().nodes())
      if (node instanceof SoulweaverBindingNode binding) {
        binding.selection(selectedSupply, this::select);
        binding.update(next);
        binding.simplify(aided, focus);
      }
    if (!selectedSupply.isEmpty()
        && area().nodeById(selectedSupply).filter(CanvasNode::isVisible).isEmpty()) {
      selectedSupply = "";
      for (var node : area().nodes())
        if (node instanceof SoulweaverBindingNode binding)
          binding.selection(selectedSupply, this::select);
    }
  }

  private void select(SoulweaverBindingNode.Kind kind, String id) {
    if (state.revealed()) return;
    if (kind == SoulweaverBindingNode.Kind.VESSEL || kind == SoulweaverBindingNode.Kind.ESSENCE) {
      selectedKind = kind;
      selectedSupply = id;
      update(state);
    } else if (kind == SoulweaverBindingNode.Kind.SOCKET && !selectedSupply.isEmpty()) {
      boolean vessel = selectedKind == SoulweaverBindingNode.Kind.VESSEL;
      area()
          .fireServerEvent(
              vessel ? "vessel" : "essence",
              new engine.network.messages.c2s.DialogResponseMessage.StringValue(
                  id + ":" + selectedSupply.substring(vessel ? 7 : 8)));
    }
  }

  @Override
  protected float layoutWorkspace(float width, float height) {
    float bottom = -92;
    float zoom = Math.min(width / 1080, Math.max(1, height) / (630 - bottom));
    float contentHeight = Math.max(1, height);
    area().options().zoom(zoom, zoom);
    area().zoom(zoom);
    area()
        .pan(
            (width - 1072 * zoom) / 2, (contentHeight - (630 - bottom) * zoom) / 2 - bottom * zoom);
    return contentHeight;
  }
}
