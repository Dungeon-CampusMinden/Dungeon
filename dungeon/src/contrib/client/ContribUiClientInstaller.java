package contrib.client;

import contrib.hud.dialogs.DialogBackendInstaller;
import contrib.hud.systems.AttributeBarSystem;
import contrib.hud.systems.HudSystem;
import contrib.modules.interaction.InteractionSelection;
import contrib.modules.interaction.ui.InteractionSelectionOverlayUi;
import core.game.loop.ClientLoopHostInstaller;

/** Installs contrib UI and interaction services for the client loop host. */
public final class ContribUiClientInstaller implements ClientLoopHostInstaller {

  /** Creates a contrib UI client installer. */
  public ContribUiClientInstaller() {}

  @Override
  public void installPlatformServices() {
    InteractionSelection.install(InteractionSelectionOverlayUi.INSTANCE);
  }

  @Override
  public void installRuntimeSystems() {
    DialogBackendInstaller.install();
    ContribClientSystemInstaller.addIfAbsent(HudSystem.class, HudSystem::new);
    ContribClientSystemInstaller.addIfAbsent(AttributeBarSystem.class, AttributeBarSystem::new);
  }
}
