package rooms.programming.level;

import engine.utils.CursorUtil;
import engine.utils.Cursors;
import feature.canvas.CanvasNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import rooms.programming.modules.variables.BindingState;
import rooms.programming.modules.variables.GolemProperty;
import rooms.programming.modules.variables.MagicalEssence;
import rooms.programming.modules.variables.SoulVessel;

/** One workbench: reusable vessel stamps, named storage and replaceable value tokens. */
final class ProgrammingBindingUI extends ProgrammingWorkbenchUI
    implements CursorUtil.CursorOverride {
  private BindingState state;
  private ProgrammingBindingNode.Kind selectedKind;
  private String selectedSupply = "";

  @Override
  public Optional<Cursors> cursorOverride() {
    for (var node : area().nodes()) {
      if (node instanceof ProgrammingBindingNode source && source.dragging()) {
        boolean valid =
            area().intersectsAll(source).stream()
                .anyMatch(
                    target ->
                        target instanceof ProgrammingBindingNode socket && socket.accepts(source));
        return Optional.of(valid ? Cursors.GRABBING : Cursors.DISABLED);
      }
    }
    return Optional.empty();
  }

  ProgrammingBindingUI(String dialogId, BindingState initial) {
    super(
        ProgrammingBinding.ID,
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
        new ProgrammingBindingNode("essence-heading", ProgrammingBindingNode.Kind.HEADING)
            .position(0, 20));
    for (var essence : MagicalEssence.values())
      nodes.add(
          new ProgrammingBindingNode(
                  "essence-" + essence.name(), ProgrammingBindingNode.Kind.ESSENCE)
              .position(essence.ordinal() * 156, -80));
    return nodes;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    ProgrammingBinding.state().ifPresent(this::update);
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
    boolean vessels = next.stage() == rooms.programming.state.VariablePuzzleStage.VESSELS;
    boolean aided = ProgrammingHelpUI.simplified(vessels ? "vessels" : "essences");
    GolemProperty focus =
        java.util.Arrays.stream(GolemProperty.values())
            .filter(
                property ->
                    vessels
                        ? next.vessels().get(property)
                            != rooms.programming.modules.variables.VariablePuzzle.vesselSolution()
                                .get(property)
                        : next.essences().get(property)
                            != rooms.programming.modules.variables.VariablePuzzle.essenceSolution()
                                .get(property))
            .findFirst()
            .orElse(null);
    if (aided && focus != null)
      status.setText("Markierte Fassung: " + focus.label() + " · Passende Vorräte sind sichtbar.");
    for (var node : area().nodes())
      if (node instanceof ProgrammingBindingNode binding) {
        binding.selection(selectedSupply, this::select);
        binding.update(next);
        binding.simplify(aided, focus);
      }
    if (!selectedSupply.isEmpty()
        && area().nodeById(selectedSupply).filter(CanvasNode::isVisible).isEmpty()) {
      selectedSupply = "";
      for (var node : area().nodes())
        if (node instanceof ProgrammingBindingNode binding)
          binding.selection(selectedSupply, this::select);
    }
  }

  private void select(ProgrammingBindingNode.Kind kind, String id) {
    if (state.revealed()) return;
    if (kind == ProgrammingBindingNode.Kind.VESSEL || kind == ProgrammingBindingNode.Kind.ESSENCE) {
      selectedKind = kind;
      selectedSupply = id;
      update(state);
    } else if (kind == ProgrammingBindingNode.Kind.SOCKET && !selectedSupply.isEmpty()) {
      boolean vessel = selectedKind == ProgrammingBindingNode.Kind.VESSEL;
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
