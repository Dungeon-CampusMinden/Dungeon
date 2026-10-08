package rooms.soulweaver.modules.variables;

import java.util.Map;
import rooms.soulweaver.state.VariablePuzzleStage;

/**
 * Shared workbench state. Canvas positions are deliberately client-local.
 *
 * @param stage assembly stage
 * @param essencesCollected whether the essence chest has been opened
 * @param vesselsCollected whether the vessel chest has been opened
 * @param vessels fixed storage kinds already assigned
 * @param essences current stored values, including compatible but unwanted values
 * @param feedback last assignment result
 */
public record BindingState(
    VariablePuzzleStage stage,
    boolean essencesCollected,
    boolean vesselsCollected,
    Map<GolemProperty, SoulVessel> vessels,
    Map<GolemProperty, MagicalEssence> essences,
    String feedback) {
  /** Copies assignments so later server updates cannot mutate an earlier snapshot. */
  public BindingState {
    vessels = Map.copyOf(vessels);
    essences = Map.copyOf(essences);
  }

  /**
   * @return whether programming names may now be shown
   */
  public boolean revealed() {
    return stage == VariablePuzzleStage.REVEAL || stage == VariablePuzzleStage.COMPLETE;
  }
}
