package contrib.client;

import contrib.hud.dialogs.DialogBackendInstaller;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import contrib.modules.interaction.InteractionSelection;
import contrib.modules.interaction.ui.InteractionSelectionOverlayUi;
import core.game.loop.ClientLoopHostInstaller;

/**
 * Installs client-side UI systems and services into the client loop host.
 *
 * <p>This class provides implementations for the {@link ClientLoopHostInstaller} interface, allowing
 * for the installation of necessary UI systems and services as part of the client initialization process.
 * It adds both platform-specific services and runtime systems needed for the interactive graphical
 * user interface.
 *
 * <p>{@code UiClientInstaller} integrates with the following key components:
 * <ul>
 *   <li>{@link InteractionSelection}: Configures the interaction selection backend to use the
 *       {@link InteractionSelectionOverlayUi} singleton instance for presenting interaction overlays.</li>
 *   <li>{@link DialogBackendInstaller}: Wires up the shared dialog backend registry to support
 *       various custom and default dialog types.</li>
 *   <li>{@link HudSystem} and {@link AttributeBarSystem}: Ensures these runtime systems are added to the
 *       client, allowing for the display of heads-up display (HUD) components and attribute bars.</li>
 * </ul>
 *
 * <p>This installer is registered explicitly through {@link ClientInstaller}.
 */
public final class UiClientInstaller implements ClientLoopHostInstaller {

  /** Creates a UI client installer. */
  public UiClientInstaller() {}

  @Override
  public void installPlatformServices() {
    InteractionSelection.install(InteractionSelectionOverlayUi.INSTANCE);
  }

  @Override
  public void installRuntimeSystems() {
    DialogBackendInstaller.install();
    SystemClientInstaller.addIfAbsent(HudSystem.class, HudSystem::new);
    SystemClientInstaller.addIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }
}
