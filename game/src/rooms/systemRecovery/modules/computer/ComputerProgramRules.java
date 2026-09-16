package rooms.systemRecovery.modules.computer;

import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;

/** Defines which computer actions are available in each authoritative learning phase. */
final class ComputerProgramRules {

  private ComputerProgramRules() {}

  /**
   * Returns whether the current learning step permits mounting this program.
   *
   * @param kind program mounted in the computer
   * @param step current authoritative learning step
   * @return whether the program can be mounted at this step
   */
  static boolean canMount(ComputerProgramKind kind, SystemRecoveryLearningStep step) {
    return allowed(kind, step);
  }

  /**
   * Returns whether the current learning step permits saving this program.
   *
   * @param kind program being saved
   * @param step current authoritative learning step
   * @return whether the program can be saved at this step
   */
  static boolean canSave(ComputerProgramKind kind, SystemRecoveryLearningStep step) {
    return allowed(kind, step);
  }

  private static boolean allowed(ComputerProgramKind kind, SystemRecoveryLearningStep step) {
    if (kind == null || step == null) return false;
    return switch (kind) {
      case SORT -> step == SystemRecoveryLearningStep.BUBBLE_SORT_CONDITION;
      case SEARCH -> step == SystemRecoveryLearningStep.SEARCH_PROGRAM;
      case ACCESS -> step == SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS;
      case NONE -> false;
    };
  }
}
