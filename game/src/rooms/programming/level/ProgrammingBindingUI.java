package rooms.programming.level;

import feature.canvas.CanvasNode;
import java.util.ArrayList;
import java.util.List;
import rooms.programming.modules.variables.BindingState;
import rooms.programming.modules.variables.GolemProperty;
import rooms.programming.modules.variables.MagicalEssence;
import rooms.programming.modules.variables.SoulVessel;

/** One workbench: reusable vessel stamps, named storage and replaceable value tokens. */
final class ProgrammingBindingUI extends ProgrammingWorkbenchUI {
  private BindingState state;
  private ProgrammingBindingNode.Kind selectedKind;
  private String selectedSupply = "";

  ProgrammingBindingUI(String dialogId, BindingState initial) {
    super(
        ProgrammingBinding.ID,
        dialogId,
        "Nox · Seelenwerkbank",
        "Binde Gefäß, Name und Wert und erwecke Nox.",
        nodes());
    help(
        "Seelenbindung\n\nWähle ein Gefäß und klicke auf eine Fassung, oder ziehe es direkt dorthin. Fülle es danach ebenso mit einer passenden Essenz. Gefäße und Essenzen lassen sich mehrfach verwenden.\n\nMit × leerst du eine Fassung. Sobald die Bindung vollständig ist, kannst du Nox aktivieren.\n\nValerius' Bindungsplan liegt im Raum. Er beschreibt die benötigten Werte.");
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
    for (var node : area().nodes())
      if (node instanceof ProgrammingBindingNode binding) {
        binding.selection(selectedSupply, this::select);
        binding.update(next);
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
