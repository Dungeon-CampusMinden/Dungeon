package modules.computer;

import core.Component;
import core.Entity;
import core.game.ECSManagement;
import core.network.messages.c2s.DialogResponseMessage;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Component that stores the state of the computer.
 *
 * @param state the current progress state of the computer
 * @param isInfected whether the computer is currently infected with a virus
 * @param virusType the type of virus currently infecting the computer, or an empty string if not
 *     infected
 * @param timestampOfLogin the timestamp of the last login to the computer, or 0 if never logged in
 * @param usbInserted whether the correct USB stick has been plugged into the computer
 * @param lightsOn whether room lights are switched on from the control panel
 * @param heaterCelsius the heater temperature set on the control panel, in degrees Celsius
 * @param door1Open whether door 1 (storage) is currently open according to the control panel
 * @param door2Unlocked whether the door 2 password has already been entered correctly
 * @param door2Open whether door 2 (exit) is currently open according to the control panel
 * @param acOn whether the air conditioning has been turned on from the control panel
 * @param camerasOn whether the security cameras have been turned on from the control panel
 * @param acVentConnected whether the control panel has been connected to the air conditioning vent
 *     by entering the correct vent serial number
 */
public record ComputerStateComponent(
    ComputerProgress state,
    boolean isInfected,
    String virusType,
    int timestampOfLogin,
    boolean usbInserted,
    boolean lightsOn,
    int heaterCelsius,
    boolean door1Open,
    boolean door2Unlocked,
    boolean door2Open,
    boolean acOn,
    boolean camerasOn,
    boolean acVentConnected)
    implements Component, Serializable, DialogResponseMessage.Payload {

  /** Default heater temperature in Celsius. */
  public static final int DEFAULT_HEATER_CELSIUS = 18;

  /**
   * Creates a new ComputerStateComponent with sensible defaults for the control-panel fields.
   *
   * @param state the current computer progress state
   * @param isInfected whether the computer is infected
   * @param virusType the virus type, or {@code null}
   * @param timestampOfLogin the unix timestamp of the last login
   * @return a new ComputerStateComponent
   */
  public static ComputerStateComponent of(
      ComputerProgress state, boolean isInfected, String virusType, int timestampOfLogin) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        false,
        true,
        DEFAULT_HEATER_CELSIUS,
        true,
        false,
        false,
        false,
        false,
        false);
  }

  /**
   * Create a new ComputerStateComponent with the given state.
   *
   * @param newState the new state of the computer
   * @return a new ComputerStateComponent with the updated state
   */
  public ComputerStateComponent withState(ComputerProgress newState) {
    return new ComputerStateComponent(
        newState,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given infection status.
   *
   * @param infected the new infection status to set
   * @return a new ComputerStateComponent with the updated infection status
   */
  public ComputerStateComponent withInfection(boolean infected) {
    return new ComputerStateComponent(
        state,
        infected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given virus type.
   *
   * @param virusType the new type of virus
   * @return a new ComputerStateComponent with the updated virus type
   */
  public ComputerStateComponent withVirusType(String virusType) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given timestamp of login.
   *
   * @param timestampOfLogin the new timestamp of login
   * @return a new ComputerStateComponent with the updated timestamp of login
   */
  public ComputerStateComponent withTimestampOfLogin(int timestampOfLogin) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given USB-inserted flag.
   *
   * @param usbInserted whether the correct USB stick is now considered to be plugged in
   * @return a new ComputerStateComponent with the updated USB flag
   */
  public ComputerStateComponent withUsbInserted(boolean usbInserted) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given lights-on flag.
   *
   * @param lightsOn whether the lights are on
   * @return a new ComputerStateComponent with the updated lights flag
   */
  public ComputerStateComponent withLightsOn(boolean lightsOn) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given heater temperature.
   *
   * @param heaterCelsius the new heater temperature in Celsius
   * @return a new ComputerStateComponent with the updated heater temperature
   */
  public ComputerStateComponent withHeaterCelsius(int heaterCelsius) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given door 1 open flag.
   *
   * @param door1Open whether door 1 should now be open
   * @return a new ComputerStateComponent with the updated flag
   */
  public ComputerStateComponent withDoor1Open(boolean door1Open) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given door 2 unlocked flag.
   *
   * @param door2Unlocked whether the door 2 password has been entered correctly
   * @return a new ComputerStateComponent with the updated flag
   */
  public ComputerStateComponent withDoor2Unlocked(boolean door2Unlocked) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given door 2 open flag.
   *
   * @param door2Open whether door 2 should now be open
   * @return a new ComputerStateComponent with the updated flag
   */
  public ComputerStateComponent withDoor2Open(boolean door2Open) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given air conditioning flag.
   *
   * @param acOn whether the AC is on
   * @return a new ComputerStateComponent with the updated flag
   */
  public ComputerStateComponent withAcOn(boolean acOn) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given security cameras flag.
   *
   * @param camerasOn whether the cameras are on
   * @return a new ComputerStateComponent with the updated flag
   */
  public ComputerStateComponent withCamerasOn(boolean camerasOn) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Create a new ComputerStateComponent with the given air conditioning vent connection flag.
   *
   * @param acVentConnected whether the control panel is connected to the AC vent
   * @return a new ComputerStateComponent with the updated flag
   */
  public ComputerStateComponent withAcVentConnected(boolean acVentConnected) {
    return new ComputerStateComponent(
        state,
        isInfected,
        virusType,
        timestampOfLogin,
        usbInserted,
        lightsOn,
        heaterCelsius,
        door1Open,
        door2Unlocked,
        door2Open,
        acOn,
        camerasOn,
        acVentConnected);
  }

  /**
   * Updates the state of the computer on the existing state entity within the current level.
   *
   * @param state the new computer progress state to set
   * @throws java.util.NoSuchElementException if no state Entity is found
   */
  public static void setState(ComputerProgress state) {
    replace(csc -> csc.withState(state));
  }

  /**
   * Updates the infection status on the existing state entity within the current level.
   *
   * @param infected the new infection status to set
   * @throws java.util.NoSuchElementException if no state Entity is found
   */
  public static void setInfection(boolean infected) {
    replace(csc -> csc.withInfection(infected));
  }

  /**
   * Updates the virus type on the existing state entity within the current level.
   *
   * @param virusType the new virus type string to set
   * @throws java.util.NoSuchElementException if no state Entity is found
   */
  public static void setVirusType(String virusType) {
    replace(csc -> csc.withVirusType(virusType));
  }

  /**
   * Updates the timestamp of login on the existing state entity within the current level.
   *
   * @param timestampOfLogin the new timestamp of login to set
   * @throws java.util.NoSuchElementException if no state Entity is found
   */
  public static void setTimestampOfLogin(int timestampOfLogin) {
    replace(csc -> csc.withTimestampOfLogin(timestampOfLogin));
  }

  /**
   * Updates the USB-inserted flag on the existing state entity within the current level.
   *
   * @param usbInserted the new USB-inserted flag
   * @throws java.util.NoSuchElementException if no state Entity is found
   */
  public static void setUsbInserted(boolean usbInserted) {
    replace(csc -> csc.withUsbInserted(usbInserted));
  }

  private static void replace(UnaryOperator<ComputerStateComponent> op) {
    Entity e = getStateEntity().orElseThrow();
    ComputerStateComponent csc = e.fetch(ComputerStateComponent.class).orElseThrow();
    e.remove(ComputerStateComponent.class);
    e.add(op.apply(csc));
  }

  /**
   * Retrieves the entity in the current level that holds the ComputerStateComponent.
   *
   * @return An {@link Optional} containing the Entity with the ComputerStateComponent if it exists,
   *     or an empty Optional if it does not
   */
  private static Optional<Entity> getStateEntity() {
    return ECSManagement.levelEntities(Set.of(ComputerStateComponent.class)).findFirst();
  }

  /**
   * Retrieves the current ComputerStateComponent from the state entity.
   *
   * @return An {@link Optional} containing the ComputerStateComponent if it exists, or an empty
   *     Optional if it does not
   */
  public static Optional<ComputerStateComponent> getState() {
    return getStateEntity().flatMap(entity -> entity.fetch(ComputerStateComponent.class));
  }
}
