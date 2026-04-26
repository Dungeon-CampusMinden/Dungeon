package contrib.client.install;

import contrib.client.DefaultClientLoopHostFactory;
import contrib.client.render.PresentationRenderAdapter;
import contrib.hud.dialogs.DialogRegistryInstaller;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import contrib.game.LevelContentInstaller;
import contrib.modules.interaction.InteractionSelection;
import contrib.modules.interaction.ui.InteractionMenuUi;
import contrib.modules.keypad.KeypadDialogInstaller;
import contrib.modules.levelhide.LevelHideSystem;
import contrib.systems.ColliderSyncSystem;
import core.platform.client.loop.ClientLoopHostInstaller;
import core.platform.Platform;

/**
 * Installs client-side presentation systems and services into the client loop host.
 *
 * <p>This class provides implementations for the {@link ClientLoopHostInstaller} interface,
 * allowing for the installation of necessary client-facing systems and services as part of client
 * initialization. It adds both platform-specific services and runtime systems needed for the
 * interactive presentation layer.
 *
 * <p>{@code ClientPresentationInstaller} integrates with the following key parts:
 *
 * <ul>
 *   <li>{@link InteractionSelection}: Configures the interaction selection backend to use the
 *       {@link InteractionMenuUi} singleton instance for presenting interaction
 *       overlays.</li>
 *   <li>{@link DialogRegistryInstaller}: Wires up the shared dialog backend registry to support
 *       various custom and default dialog types.</li>
 *   <li>{@link KeypadDialogInstaller}: Registers keypad-module UI bindings for keypad dialogs.</li>
 *   <li>{@link LevelHideSystem}: Maintains the client-side visibility state of hidden or revealed
 *       world regions.</li>
 *   <li>{@link HudSystem} and {@link AttributeBarSystem}: Ensure HUD elements and attribute bars
 *       are available on the client.</li>
 * </ul>
 *
 * <p>This installer is registered explicitly through {@link DefaultClientLoopHostFactory}.
 */
public final class ClientPresentationInstaller implements ClientLoopHostInstaller {

  /** Creates a presentation client installer. */
  public ClientPresentationInstaller() {}

  @Override
  public void installPlatformServices() {
    InteractionSelection.install(InteractionMenuUi.INSTANCE);
    LevelContentInstaller.install();
    if (!(Platform.render() instanceof PresentationRenderAdapter)) {
      Platform.render(new PresentationRenderAdapter(Platform.render()));
    }
  }

  @Override
  public void installRuntimeSystems() {
    DialogRegistryInstaller.install();
    KeypadDialogInstaller.install();
    ClientLoopHostInstaller.addSystemIfAbsent(ColliderSyncSystem.class, ColliderSyncSystem::new);
    ClientLoopHostInstaller.addSystemIfAbsent(LevelHideSystem.class, LevelHideSystem::new);
    ClientLoopHostInstaller.addSystemIfAbsent(HudSystem.class, HudSystem::new);
    ClientLoopHostInstaller.addSystemIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }
}
