package contrib.client;

import contrib.hud.dialogs.DialogBackendInstaller;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import contrib.modules.interaction.InteractionSelection;
import contrib.modules.interaction.ui.InteractionSelectionOverlayUi;
import contrib.modules.levelhide.LevelHideSystem;
import core.game.loop.ClientLoopHostInstaller;

/**
 * Installs client-side presentation systems and services into the client loop host.
 *
 * <p>This class provides implementations for the {@link ClientLoopHostInstaller} interface,
 * allowing for the installation of necessary client-facing systems and services as part of client
 * initialization. It adds both platform-specific services and runtime systems needed for the
 * interactive presentation layer.
 *
 * <p>{@code UiClientInstaller} integrates with the following key components:
 *
 * <ul>
 *   <li>{@link InteractionSelection}: Configures the interaction selection backend to use the
 *       {@link InteractionSelectionOverlayUi} singleton instance for presenting interaction
 *       overlays.</li>
 *   <li>{@link DialogBackendInstaller}: Wires up the shared dialog backend registry to support
 *       various custom and default dialog types.</li>
 *   <li>{@link LevelHideSystem}: Maintains the client-side visibility state of hidden or revealed
 *       world regions.</li>
 *   <li>{@link HudSystem} and {@link AttributeBarSystem}: Ensure HUD elements and attribute bars
 *       are available on the client.</li>
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
    ClientLoopHostInstaller.addSystemIfAbsent(LevelHideSystem.class, LevelHideSystem::new);
    ClientLoopHostInstaller.addSystemIfAbsent(HudSystem.class, HudSystem::new);
    ClientLoopHostInstaller.addSystemIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }
}
