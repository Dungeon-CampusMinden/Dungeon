package rooms.morphco.petrinet;

import engine.Entity;
import engine.Game;
import feature.petrinet.PetriNetSystem;
import feature.petrinet.PlaceComponent;
import feature.petrinet.TransitionComponent;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Server-owned Petri net of the room progress.
 *
 * <p>Every milestone owns a pending and a completed place. Its transition consumes two tokens from
 * the pending place, so it only fires after {@link #complete(MorphCoMilestone)} added the second
 * one. Completed prerequisites are read arcs: they gate the transition without being consumed. This
 * keeps the parallel repair of F-04 and A-12 and the final join in one marking.
 */
public final class MorphCoProgressNet {
  private static MorphCoProgressNet instance;

  private final PetriNetSystem system;
  private final EnumMap<MorphCoMilestone, Places> places = new EnumMap<>(MorphCoMilestone.class);

  private MorphCoProgressNet(PetriNetSystem system) {
    this.system = system;
    for (MorphCoMilestone milestone : MorphCoMilestone.values()) {
      Entity pendingEntity = new Entity("morph-co-" + milestone + "-pending");
      PlaceComponent pending = new PlaceComponent();
      pending.produce();
      pendingEntity.add(pending);
      Game.add(pendingEntity);

      Entity completedEntity = new Entity("morph-co-" + milestone + "-complete");
      PlaceComponent completed = new PlaceComponent();
      completedEntity.add(completed);
      Game.add(completedEntity);
      places.put(milestone, new Places(pendingEntity, pending, completedEntity, completed));
    }
    for (MorphCoMilestone milestone : MorphCoMilestone.values()) {
      TransitionComponent transition = new TransitionComponent();
      Places pair = places.get(milestone);
      system.addInputArc(transition, pair.pending(), 2);
      system.addOutputArc(transition, pair.completed());
      for (MorphCoMilestone prerequisite : milestone.prerequisites()) {
        PlaceComponent gate = places.get(prerequisite).completed();
        system.addInputArc(transition, gate);
        system.addOutputArc(transition, gate);
      }
    }
  }

  /** Creates the initial marking once the server has installed its {@link PetriNetSystem}. */
  public static synchronized void initialize() {
    if (instance != null) return;
    Game.system(PetriNetSystem.class, system -> instance = new MorphCoProgressNet(system));
  }

  /** Removes the places and arcs before a fresh level is built. */
  public static synchronized void reset() {
    if (instance == null) return;
    instance.system.clear();
    instance
        .places
        .values()
        .forEach(
            pair -> {
              Game.remove(pair.pendingEntity());
              Game.remove(pair.completedEntity());
            });
    instance = null;
  }

  /**
   * Fires a milestone exactly once if all its prerequisites are complete.
   *
   * @param milestone milestone to complete
   * @return whether the milestone was completed by this call
   */
  public static synchronized boolean complete(MorphCoMilestone milestone) {
    if (instance == null || milestone == null) return false;
    Places pair = instance.places.get(milestone);
    if (pair.pending().tokenCount() != 1 || pair.completed().tokenCount() != 0) return false;
    if (!instance.completed().containsAll(milestone.prerequisites())) return false;
    pair.pending().produce();
    instance.system.execute();
    if (pair.pending().tokenCount() == 0 && pair.completed().tokenCount() == 1) return true;
    pair.pending().consume();
    return false;
  }

  /**
   * Checks whether a milestone has been completed.
   *
   * @param milestone milestone to check
   * @return whether it is complete
   */
  public static synchronized boolean isComplete(MorphCoMilestone milestone) {
    return completedMilestones().contains(milestone);
  }

  /**
   * Returns the completed milestones.
   *
   * @return immutable set of completed milestones
   */
  public static synchronized Set<MorphCoMilestone> completedMilestones() {
    return instance == null ? Set.of() : Set.copyOf(instance.completed());
  }

  /**
   * Restores a saved marking without firing gameplay callbacks.
   *
   * @param completed milestones to restore as completed
   * @return whether the marking was consistent and has been restored
   */
  public static synchronized boolean restore(Set<MorphCoMilestone> completed) {
    if (instance == null || completed == null) return false;
    for (MorphCoMilestone milestone : completed) {
      if (milestone == null || !completed.containsAll(milestone.prerequisites())) return false;
    }
    for (Map.Entry<MorphCoMilestone, Places> entry : instance.places.entrySet()) {
      Places pair = entry.getValue();
      clear(pair.pending());
      clear(pair.completed());
      if (completed.contains(entry.getKey())) pair.completed().produce();
      else pair.pending().produce();
    }
    return instance.completed().equals(completed);
  }

  private EnumSet<MorphCoMilestone> completed() {
    EnumSet<MorphCoMilestone> result = EnumSet.noneOf(MorphCoMilestone.class);
    places.forEach(
        (milestone, pair) -> {
          if (pair.pending().tokenCount() == 0 && pair.completed().tokenCount() == 1) {
            result.add(milestone);
          }
        });
    return result;
  }

  private static void clear(PlaceComponent place) {
    if (place.tokenCount() > 0) place.consume(place.tokenCount());
  }

  private record Places(
      Entity pendingEntity,
      PlaceComponent pending,
      Entity completedEntity,
      PlaceComponent completed) {}
}
