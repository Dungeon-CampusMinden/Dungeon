package contrib.client.install;

import contrib.hud.dialogs.DialogRegistryInstaller;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import contrib.modules.interaction.InteractionSelection;
import contrib.modules.interaction.ui.InteractionSelectionPresenter;
import core.game.systems.SystemRegistration;
import core.platform.client.loop.ClientLoopHostInstaller;

/**
 * A client installer for setting up and integrating UI-related platform services and runtime systems.
 *
 * <p>This class implements the {@link ClientLoopHostInstaller} interface to contribute
 * client-specific functionality, such as interaction panels and dialog systems, to the client loop host.
 *
 * <p>The installer is responsible for wiring platform services necessary for interaction selection
 * and for enabling runtime systems such as heads-up-display (HUD) and attribute bars.
 *
 * <p>This class cannot be subclassed as it is declared {@code final}.
 */
public final class ClientUiInstaller implements ClientLoopHostInstaller {

  /** Creates a UI client installer. */
  public ClientUiInstaller() {}

   @Override
   public void installPlatformServices() {
     InteractionSelection.install(InteractionSelectionPresenter.INSTANCE);
  }

  @Override
  public void installRuntimeSystems() {
    DialogRegistryInstaller.install();
    SystemRegistration.addIfAbsent(HudSystem.class, HudSystem::new);
    SystemRegistration.addIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }
}
