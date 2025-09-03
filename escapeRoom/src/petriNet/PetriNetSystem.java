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
 * <p>When a Petri net is no longer needed, {@link #clear()} should be called to release its
 * resources.
 */
public class PetriNetSystem extends System {

  private Map<TransitionComponent, TransitionBinding> transitions = new HashMap<>();

  @Override
  public void execute() {
    transitions.forEach(
        (transitionComponent, binding) -> {

          // Check if the transition is enabled (all input places have enough tokens)
          boolean enabled =
              binding.inputArcs().stream()
                  .allMatch(arc -> arc.place().tokenCount() >= arc.weight());

          if (enabled) {
            // Consume token from each input place
            binding.inputArcs().forEach(arc -> arc.place().consume(arc.weight()));
            // Produce token in each output place
            binding.outputArcs().forEach(arc -> arc.place().produce(arc.weight()));
          }
        });
  }

  /**
   * Adds a place as an input arc to the given transition. If the transition is not yet registered,
   * a new binding is created.
   *
   * @param transition the transition to which the place should be connected as input
   * @param place the input place to add
   * @param weight the number of tokens consumed when the transition fires; must be greater than
   *     zero
   * @throws IllegalArgumentException if {@code weight} is zero or less.
   */
  public void addInputArc(TransitionComponent transition, PlaceComponent place, int weight) {
    if (weight <= 0) {
      throw new IllegalArgumentException("Arc weight must not higher than zero.");
    }
    transitions
        .computeIfAbsent(transition, t -> new TransitionBinding(new HashSet<>(), new HashSet<>()))
        .inputArcs()
        .add(new Arc(place, weight));
  }

  /**
   * Adds a place as an input arc to the given transition with a default weight of {@code 1}. If the
   * transition is not yet registered, a new binding is created.
   *
   * @param transition the transition from which the place should be connected as input
   * @param place the input place to add
   */
  public void addInputArc(TransitionComponent transition, PlaceComponent place) {
    addInputArc(transition, place, 1);
  }

  /**
   * Adds a place as an output arc to the given transition. If the transition is not yet registered,
   * a new binding is created.
   *
   * @param transition the transition from which the place should be connected as output
   * @param place the output place to add
   * @param weight the number of tokens produced when the transition fires; must be greater than
   *     zero
   * @throws IllegalArgumentException if {@code weight} is zero or less.
   */
  public void addOutputArc(TransitionComponent transition, PlaceComponent place, int weight) {
    if (weight <= 0) {
      throw new IllegalArgumentException("Arc weight must not higher than zero.");
    }

    transitions
        .computeIfAbsent(transition, t -> new TransitionBinding(new HashSet<>(), new HashSet<>()))
        .outputArcs()
        .add(new Arc(place, weight));
  }

  /**
   * Adds a place as an output arc to the given transition with a default weight of {@code 1}. If
   * the transition is not yet registered, a new binding is created.
   *
   * @param transition the transition from which the place should be connected as output
   * @param place the output place to add
   */
  public void addOutputArc(TransitionComponent transition, PlaceComponent place) {
    addOutputArc(transition, place, 1);
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
  public void clear() {
    transitions.clear();
  }

  /**
   * Inner record that represents the input and output places associated with a transition in the
   * Petri net.
   *
   * @param inputArcs Set of input places of the transition
   * @param outputArcs Set of output places of the transition
   */
  private record TransitionBinding(Set<Arc> inputArcs, Set<Arc> outputArcs) {}

  private record Arc(PlaceComponent place, int weight) {}
}
