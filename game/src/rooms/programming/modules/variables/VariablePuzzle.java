package rooms.programming.modules.variables;

import java.util.Map;

/** Authoritative solution data for the variable and data-type puzzle. */
public final class VariablePuzzle {

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

  /**
   * Checks the value category, independently of the workshop's requested value.
   *
   * @param vessel the fixed storage kind
   * @param essence the proposed value
   * @return whether the vessel can store the value
   */
  public static boolean fits(SoulVessel vessel, MagicalEssence essence) {
    return switch (vessel) {
      case IRON_CHEST ->
          essence == MagicalEssence.LIFE_ENERGY_VALUE || essence == MagicalEssence.STEPS_VALUE;
      case CRYSTAL_BOTTLE ->
          essence == MagicalEssence.MANA_VALUE || fits(SoulVessel.IRON_CHEST, essence);
      case PARCHMENT -> essence == MagicalEssence.NAME_VALUE;
      case RUNE_STONE -> essence == MagicalEssence.VIEW_DIRECTION_VALUE;
      case LIGHT_ORB ->
          essence == MagicalEssence.BOOLEAN_TRUE || essence == MagicalEssence.BOOLEAN_FALSE;
    };
  }

  /**
   * @return the immutable expected vessel assignment
   */
  public static Map<GolemProperty, SoulVessel> vesselSolution() {
    return VESSEL_SOLUTION;
  }

  /**
   * @return the immutable expected essence assignment
   */
  public static Map<GolemProperty, MagicalEssence> essenceSolution() {
    return ESSENCE_SOLUTION;
  }

  /**
   * Reports whether all properties use the expected vessels.
   *
   * @param assignment the submitted vessel assignment
   * @return true if the assignment exactly matches the solution
   */
  public static boolean vesselsCorrect(Map<GolemProperty, SoulVessel> assignment) {
    return assignment != null && VESSEL_SOLUTION.equals(assignment);
  }

  /**
   * Reports whether all properties use the expected essences.
   *
   * @param assignment the submitted essence assignment
   * @return true if the assignment exactly matches the solution
   */
  public static boolean essencesCorrect(Map<GolemProperty, MagicalEssence> assignment) {
    return assignment != null && ESSENCE_SOLUTION.equals(assignment);
  }
}
