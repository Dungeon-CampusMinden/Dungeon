package rooms.lasthour.petrinet;

import engine.Entity;
import engine.Game;
import feature.petrinet.PetriNetSystem;
import feature.petrinet.PlaceComponent;
import feature.petrinet.TransitionComponent;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Server-owned Petri marking for the irreversible progress of The Last Hour. */
public final class LastHourProgressNet {
  private static LastHourProgressNet instance;

  private final PetriNetSystem system;
  private final EnumMap<LastHourMilestone, Places> places = new EnumMap<>(LastHourMilestone.class);

  private LastHourProgressNet(PetriNetSystem system) {
    this.system = system;
    for (LastHourMilestone milestone : LastHourMilestone.values()) {
      Entity pendingEntity = new Entity("last-hour-" + milestone + "-pending");
      PlaceComponent pending = new PlaceComponent();
      pending.produce();
      pendingEntity.add(pending);
      Game.add(pendingEntity);

      Entity completedEntity = new Entity("last-hour-" + milestone + "-complete");
      PlaceComponent completed = new PlaceComponent();
      completedEntity.add(completed);
      Game.add(completedEntity);
      places.put(milestone, new Places(pendingEntity, pending, completedEntity, completed));
    }
    for (LastHourMilestone milestone : LastHourMilestone.values()) {
      TransitionComponent transition = new TransitionComponent();
      Places pair = places.get(milestone);
      system.addInputArc(transition, pair.pending(), 2);
      system.addOutputArc(transition, pair.completed());
      for (LastHourMilestone prerequisite : milestone.prerequisites()) {
        PlaceComponent gate = places.get(prerequisite).completed();
        system.addInputArc(transition, gate);
        system.addOutputArc(transition, gate);
      }
    }
  }

  /** Creates the initial marking once the server has installed its PetriNetSystem. */
  public static synchronized void initialize() {
    if (instance != null) return;
    Game.system(PetriNetSystem.class, system -> instance = new LastHourProgressNet(system));
  }

  /** Removes this room's places and transition bindings before a fresh level is built. */
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

  /** Fires a validated, irreversible milestone exactly once. */
  public static synchronized boolean complete(LastHourMilestone milestone) {
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

  /** Returns the stable marking. */
  public static synchronized Set<LastHourMilestone> completedMilestones() {
    return instance == null ? Set.of() : Set.copyOf(instance.completed());
  }

  /** Restores a validated marking without firing gameplay callbacks. */
  public static synchronized boolean restore(Set<LastHourMilestone> completed) {
    if (instance == null || completed == null) return false;
    for (LastHourMilestone milestone : completed) {
      if (milestone == null || !completed.containsAll(milestone.prerequisites())) return false;
    }
    for (Map.Entry<LastHourMilestone, Places> entry : instance.places.entrySet()) {
      Places pair = entry.getValue();
      clear(pair.pending());
      clear(pair.completed());
      if (completed.contains(entry.getKey())) pair.completed().produce();
      else pair.pending().produce();
    }
    return instance.completed().equals(completed);
  }

  private EnumSet<LastHourMilestone> completed() {
    EnumSet<LastHourMilestone> result = EnumSet.noneOf(LastHourMilestone.class);
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
