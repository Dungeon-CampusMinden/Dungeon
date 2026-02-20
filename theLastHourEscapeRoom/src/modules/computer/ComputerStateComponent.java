package modules.computer;

import core.Component;
import core.Entity;
import core.game.ECSManagement;
import java.io.Serializable;
import java.util.Set;

public record ComputerStateComponent(ComputerProgress state, boolean isInfected, String virusType)
    implements Component, Serializable {

  public ComputerStateComponent withState(ComputerProgress newState) {
    return new ComputerStateComponent(newState, this.isInfected, this.virusType);
  }

  public ComputerStateComponent withInfection(boolean infected) {
    return new ComputerStateComponent(this.state, infected, this.virusType);
  }

  public ComputerStateComponent withVirusType(String virusType) {
    return new ComputerStateComponent(this.state, this.isInfected, virusType);
  }

  public static void setState(ComputerProgress state) {
    Entity e = getStateEntity();
    ComputerStateComponent csc = e.fetch(ComputerStateComponent.class).orElseThrow();
    e.remove(ComputerStateComponent.class);
    e.add(csc.withState(state));
  }

  public static void setInfection(boolean infected) {
    Entity e = getStateEntity();
    ComputerStateComponent csc = e.fetch(ComputerStateComponent.class).orElseThrow();
    e.remove(ComputerStateComponent.class);
    e.add(csc.withInfection(infected));
  }

  public static void setVirusType(String virusType) {
    Entity e = getStateEntity();
    ComputerStateComponent csc = e.fetch(ComputerStateComponent.class).orElseThrow();
    e.remove(ComputerStateComponent.class);
    e.add(csc.withVirusType(virusType));
  }

  private static Entity getStateEntity() {
    return ECSManagement.levelEntities(Set.of(ComputerStateComponent.class))
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException("No ComputerStateComponent found in current level!"));
  }

  public static ComputerStateComponent getState() {
    Entity e = getStateEntity();
    return e.fetch(ComputerStateComponent.class).orElseThrow();
  }
}
