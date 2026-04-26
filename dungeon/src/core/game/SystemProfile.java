package core.game;

/**
 * Enumeration defining system configuration profiles for different deployment scenarios.
 *
 * <p>SystemProfile determines which optional systems should be initialized based on the deployment
 * context (client or server). It controls whether rendering and input systems are enabled.
 *
 * <p>Available profiles:
 *
 * <ul>
 *   <li>CLIENT - Full client profile with rendering and input systems enabled
 *   <li>SERVER - Headless server profile with rendering and input systems disabled
 * </ul>
 */
public enum SystemProfile {

  /**
   * Client profile: Rendering and input systems enabled.
   *
   * <p>Used for standard client installations where the game is rendered and user input is
   * processed.
   */
  CLIENT(true, true),

  /**
   * Server profile: Rendering and input systems disabled.
   *
   * <p>Used for headless server installations where game simulation runs without rendering or
   * direct user input.
   */
  SERVER(false, false);

  private final boolean includeRendering;
  private final boolean includeInput;

  SystemProfile(boolean includeRendering, boolean includeInput) {
    this.includeRendering = includeRendering;
    this.includeInput = includeInput;
  }

  /**
   * Checks whether rendering systems should be included in this profile.
   *
   * @return true if rendering systems should be initialized, false otherwise
   */
  public boolean includeRendering() {
    return includeRendering;
  }

  /**
   * Checks whether input systems should be included in this profile.
   *
   * @return true if input systems should be initialized, false otherwise
   */
  public boolean includeInput() {
    return includeInput;
  }
}
