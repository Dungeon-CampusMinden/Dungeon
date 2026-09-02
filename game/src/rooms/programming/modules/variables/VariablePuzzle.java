package rooms.programming.modules.variables;

import java.util.Map;
import java.util.Optional;
import rooms.programming.state.GolemState;

/** Authoritative solution data for the variable and data-type puzzle. */
public final class VariablePuzzle {

  private static final GolemState SOLVED_GOLEM = new GolemState("Nox", 125, 3.5, true, 'O', 17);

  private static final Map<GolemProperty, SoulVessel> VESSEL_SOLUTION =
      Map.of(
          GolemProperty.NAME,
          SoulVessel.PARCHMENT,
          GolemProperty.LIFE_ENERGY,
          SoulVessel.IRON_CHEST,
          GolemProperty.MANA,
          SoulVessel.CRYSTAL_BOTTLE,
          GolemProperty.ACTIVATED,
          SoulVessel.LIGHT_ORB,
          GolemProperty.VIEW_DIRECTION,
          SoulVessel.RUNE_STONE,
          GolemProperty.STEPS,
          SoulVessel.IRON_CHEST);

  private static final Map<GolemProperty, MagicalEssence> ESSENCE_SOLUTION =
      Map.of(
          GolemProperty.NAME,
          MagicalEssence.NAME_VALUE,
          GolemProperty.LIFE_ENERGY,
          MagicalEssence.LIFE_ENERGY_VALUE,
          GolemProperty.MANA,
          MagicalEssence.MANA_VALUE,
          GolemProperty.ACTIVATED,
          MagicalEssence.BOOLEAN_TRUE,
          GolemProperty.VIEW_DIRECTION,
          MagicalEssence.VIEW_DIRECTION_VALUE,
          GolemProperty.STEPS,
          MagicalEssence.STEPS_VALUE);

  private VariablePuzzle() {}

  /** Returns the expected vessel assignment. */
  public static Map<GolemProperty, SoulVessel> vesselSolution() {
    return VESSEL_SOLUTION;
  }

  /** Returns the expected essence assignment. */
  public static Map<GolemProperty, MagicalEssence> essenceSolution() {
    return ESSENCE_SOLUTION;
  }

  /** Reports whether all properties use the expected vessels. */
  public static boolean vesselsCorrect(Map<GolemProperty, SoulVessel> assignment) {
    return assignment != null && VESSEL_SOLUTION.equals(assignment);
  }

  /** Reports whether all properties use the expected essences. */
  public static boolean essencesCorrect(Map<GolemProperty, MagicalEssence> assignment) {
    return assignment != null && ESSENCE_SOLUTION.equals(assignment);
  }

  /** Returns the resulting golem only when both puzzle stages are correct. */
  public static Optional<GolemState> solve(
      Map<GolemProperty, SoulVessel> vessels, Map<GolemProperty, MagicalEssence> essences) {
    if (!vesselsCorrect(vessels) || !essencesCorrect(essences)) {
      return Optional.empty();
    }
    return Optional.of(SOLVED_GOLEM);
  }
}
