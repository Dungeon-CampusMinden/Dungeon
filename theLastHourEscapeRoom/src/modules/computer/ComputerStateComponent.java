package modules.computer;

import core.Component;
import core.Entity;
import core.game.ECSManagement;
import core.network.messages.c2s.DialogResponseMessage;

import java.io.Serializable;
import java.util.Set;

/**
 * Component that stores the state of the computer.
 *
 * @param state the current progress state of the computer
 * @param isInfected whether the computer is currently infected with a virus
 * @param virusType the type of virus currently infecting the computer, or an empty string if not
 *     infected
 */
public record ComputerStateComponent(ComputerProgress state, boolean isInfected, String virusType)
    implements Component, Serializable, DialogResponseMessage.Payload {

  /**
   * Create a new ComputerStateComponent with the given state.
   *
   * @param newState the new state of the computer
   * @return a new ComputerStateComponent with the updated state
   */
  public ComputerStateComponent withState(ComputerProgress newState) {
    return new ComputerStateComponent(newState, this.isInfected, this.virusType);
  }

  /**
   * Create a new ComputerStateComponent with the given infection status.
   *
   * @param infected the new infection status
   * @return a new ComputerStateComponent with the updated infection status
   */
  public ComputerStateComponent withInfection(boolean infected) {
    return new ComputerStateComponent(this.state, infected, this.virusType);
  }

  /**
   * Create a new ComputerStateComponent with the given virus type.
   *
   * @param virusType the new type of virus
   * @return a new ComputerStateComponent with the updated virus type
   */
  public ComputerStateComponent withVirusType(String virusType) {
    return new ComputerStateComponent(this.state, this.isInfected, virusType);
  }

  /**
   * Updates the state of the computer on the existing state entity within the current level.
   *
   * @param state the new computer progress state to set
   */
  public static void setState(ComputerProgress state) {
    Entity e = getStateEntity();
    ComputerStateComponent csc = e.fetch(ComputerStateComponent.class).orElseThrow();
    e.remove(ComputerStateComponent.class);
    e.add(csc.withState(state));
  }

  /**
   * Updates the infection status on the existing state entity within the current level.
   *
   * @param infected the new infection status to set
   */
  public static void setInfection(boolean infected) {
    Entity e = getStateEntity();
    ComputerStateComponent csc = e.fetch(ComputerStateComponent.class).orElseThrow();
    e.remove(ComputerStateComponent.class);
    e.add(csc.withInfection(infected));
  }

  /**
   * Updates the virus type on the existing state entity within the current level.
   *
   * @param virusType the new virus type string to set
   */
  public static void setVirusType(String virusType) {
    Entity e = getStateEntity();
    ComputerStateComponent csc = e.fetch(ComputerStateComponent.class).orElseThrow();
    e.remove(ComputerStateComponent.class);
    e.add(csc.withVirusType(virusType));
  }

  /**
   * Retrieves the entity in the current level that holds the ComputerStateComponent.
   *
   * @return the Entity containing the state component
   * @throws IllegalStateException if no such entity exists in the current level
   */
  private static Entity getStateEntity() {
    return ECSManagement.levelEntities(Set.of(ComputerStateComponent.class))
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException("No ComputerStateComponent found in current level!"));
  }

  /**
   * Retrieves the current ComputerStateComponent from the state entity.
   *
   * @return the current ComputerStateComponent instance
   */
  public static ComputerStateComponent getState() {
    Entity e = getStateEntity();
    return e.fetch(ComputerStateComponent.class).orElseThrow();
  }
}
