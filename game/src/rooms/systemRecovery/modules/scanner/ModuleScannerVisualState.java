package rooms.systemRecovery.modules.scanner;

import engine.Component;

/** Client-side state that tells the renderer whether the module scanner is currently running. */
public final class ModuleScannerVisualState implements Component {

  private boolean scanning;

  /**
   * @return whether the scanner is currently examining modules
   */
  public boolean scanning() {
    return scanning;
  }

  /**
   * Updates the current scan state.
   *
   * @param scanning whether the scanner is currently examining modules
   */
  public void scanning(boolean scanning) {
    this.scanning = scanning;
  }
}
