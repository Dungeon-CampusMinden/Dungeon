package contrib.hud.newhud;

/** A HUDElement represents a single visual component of the head up display. */
public interface HUDElement {
  /**
   * Should contain all element-specific initialization logic required before the HUD element is
   * used.
   */
  void init();

  /** Should contain all element-specific logic necessary for e.g. placement and size. */
  void layoutElement();

  /** Should contain all element-specific update logic that needs to be executed every frame. */
  void update();
}
