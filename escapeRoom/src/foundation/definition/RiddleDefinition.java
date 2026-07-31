package foundation.definition;

import java.util.List;

/** Immutable active-profile riddle definition understood by Foundation authority. */
public sealed interface RiddleDefinition permits ComposedRiddleDefinition {
  /**
   * Returns the stable riddle identifier.
   *
   * @return stable identifier
   */
  String id();

  /**
   * Returns hints in authored release order.
   *
   * @return immutable ordered hints
   */
  List<HintDefinition> hints();
}
