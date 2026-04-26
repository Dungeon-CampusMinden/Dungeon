package contrib.client.install;

import contrib.hud.dialogs.DialogRegistryInstaller;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import contrib.modules.interaction.InteractionSelection;
import contrib.modules.interaction.ui.InteractionMenuUi;
import contrib.modules.keypad.KeypadDialogInstaller;
import core.game.systems.SystemRegistration;
import core.platform.client.loop.ClientLoopHostInstaller;

/**
 * Installs client-side UI systems and services into the client loop host.
 *
 * <p>This class provides implementations for the {@link ClientLoopHostInstaller} interface,
 * focusing on UI presentation layer systems. It handles the installation of interaction menus,
 * dialogs, and HUD systems.
 *
 * <p>{@code ClientUiInstaller} installs the following components:
 *
 * <ul>
 *   <li>{@link InteractionSelection}: Configures the interaction selection backend to use the
 *       {@link InteractionMenuUi} singleton instance for presenting interaction overlays.
 *   <li>{@link DialogRegistryInstaller}: Wires up the shared dialog backend registry to support
 *       various custom and default dialog types.
 *   <li>{@link KeypadDialogInstaller}: Registers keypad-module UI bindings for keypad dialogs.
 *   <li>{@link HudSystem}: Ensures HUD elements are available on the client.
 *   <li>{@link AttributeBarSystem}: Ensures attribute bars are available on the client.
 * </ul>
 */
public final class ClientUiInstaller implements ClientLoopHostInstaller {

  /** Creates a UI client installer. */
  public ClientUiInstaller() {}

  @Override
  public void installPlatformServices() {
    InteractionSelection.install(InteractionMenuUi.INSTANCE);
  }

  @Override
  public void installRuntimeSystems() {
    DialogRegistryInstaller.install();
    KeypadDialogInstaller.install();
    SystemRegistration.addIfAbsent(HudSystem.class, HudSystem::new);
    SystemRegistration.addIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }
}
