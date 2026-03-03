package core.game;

/**
 * Defines which default ECS systems should be bootstrapped for a specific host/backend.
 *
 * <p>This replaces boolean flags (which become ambiguous quickly) with explicit, documented
 * profiles.
 */
public enum SystemProfile {
  /** libGDX client host: rendering + audio + HUD are available. */
  GDX_CLIENT(true, true),

  /** LITIENGINE client host: rendering + audio are available, HUD still disabled for now. */
  LITIENGINE_CLIENT(true, false),

  /** LITIENGINE host (current stage): simulation only, no libGDX rendering/audio/HUD. */
  LITIENGINE_SIMULATION(false, false);

  private final boolean includeRendering;
  private final boolean includeHud;

  SystemProfile(boolean includeRendering, boolean includeHud) {
    this.includeRendering = includeRendering;
    this.includeHud = includeHud;
  }

  public boolean includeRendering() {
    return includeRendering;
  }

  public boolean includeHud() {
    return includeHud;
  }
}
