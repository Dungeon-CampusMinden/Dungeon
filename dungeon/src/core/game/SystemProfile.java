package core.game;

/**
 * Defines which default ECS systems should be bootstrapped for a specific host/backend.
 *
 * <p>This profile only describes core-level optional groups. Backend- or contrib-specific systems
 * (for example HUD systems) must be wired explicitly by the host/bootstrap code.
 */
public enum SystemProfile {
  /** libGDX client host: rendering/audio are available. */
  GDX_CLIENT(true),

  /** LITIENGINE client host: rendering/audio are available, HUD still wired separately. */
  LITIENGINE_CLIENT(true),

  /** LITIENGINE simulation host: no rendering/audio. */
  LITIENGINE_SIMULATION(false);

  private final boolean includeRendering;

  SystemProfile(boolean includeRendering) {
    this.includeRendering = includeRendering;
  }

  public boolean includeRendering() {
    return includeRendering;
  }
}
