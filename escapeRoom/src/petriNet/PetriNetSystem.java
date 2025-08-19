package petriNet;

import core.System;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Petri net system with transitions and places. Each transition can have input places
 * and output places.
 *
 * <p>Only Petri nets are supported where each transition requires exactly one token from each of
 * its input places to fire, and produces exactly one token in each of its output places.
 *
 * <p>When a Petri net is no longer needed, {@link #clear()} should be called to release its
 * resources.
 */
public class PetriNetSystem extends System {

  private static Map<TransitionComponent, TransitionBinding> transitions = new HashMap<>();

  @Override
  public void execute() {
    transitions.forEach(
        (transitionComponent, binding) -> {

          // Check if the transition is enabled (all input places have at least one token)
          boolean enabled =
              binding.inputPlaces().stream().allMatch(place -> place.tokenCount() > 0);
          if (enabled) {
            // Consume one token from each input place
            binding.inputPlaces().forEach(PlaceComponent::consume);
            // Produce one token in each output place
            binding.outputPlaces().forEach(PlaceComponent::produce);
          }
        });
  }

  /**
   * Adds a place as an input to the given transition. If the transition is not yet registered, a
   * new binding is created.
   *
   * @param transition the transition to which the place should be connected as input
   * @param place the input place to add
   */
  public static void addInputArc(TransitionComponent transition, PlaceComponent place) {
    transitions
        .computeIfAbsent(transition, t -> new TransitionBinding(new HashSet<>(), new HashSet<>()))
        .inputPlaces()
        .add(place);
  }

  /**
   * Adds a place as an output to the given transition. If the transition is not yet registered, a
   * new binding is created.
   *
   * @param transition the transition from which the place should be connected as output
   * @param place the output place to add
   */
  public static void addOutputArc(TransitionComponent transition, PlaceComponent place) {
    transitions
        .computeIfAbsent(transition, t -> new TransitionBinding(new HashSet<>(), new HashSet<>()))
        .outputPlaces()
        .add(place);
  }

  /**
   * Clears all transitions from the Petri net system.
   *
   * <p>This effectively removes the current Petri net configuration, preventing any transitions
   * from being executed.
   *
   * <p>Note: This does not remove any entities or components from the game itself; it only clears
   * the internal transition mapping within the Petri net system.
   */
  public static void clear() {
    transitions.clear();
  }

  /**
   * Inner record that represents the input and output places associated with a transition in the
   * Petri net.
   *
   * @param inputPlaces Set of input places of the transition
   * @param outputPlaces Set of output places of the transition
   */
  private record TransitionBinding(
      Set<PlaceComponent> inputPlaces, Set<PlaceComponent> outputPlaces) {}
}
