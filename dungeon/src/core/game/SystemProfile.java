package core.game;

/**
 * Defines which default ECS systems should be bootstrapped for a specific host/backend.
 *
 * <p>This profile only describes core-level optional groups. Backend- or contrib-specific systems
 * (for example HUD systems) must be wired explicitly by the host/bootstrap code.
 */
public enum SystemProfile {
  /** libGDX client host: rendering and local input are available. */
  GDX_CLIENT(true, true),

  /** LITIENGINE client host: rendering and local input are available. */
  LITIENGINE_CLIENT(true, true),

  /** LITIENGINE simulation host: no rendering, but local input may still be available. */
  LITIENGINE_SIMULATION(false, true),

  /** Dedicated authoritative server simulation: no rendering and no local input system. */
  SERVER_SIMULATION(false, false);

  private final boolean includeRendering;
  private final boolean includeInput;

  SystemProfile(boolean includeRendering, boolean includeInput) {
    this.includeRendering = includeRendering;
    this.includeInput = includeInput;
  }

  public boolean includeRendering() {
    return includeRendering;
  }

  public boolean includeInput() {
    return includeInput;
  }
}
