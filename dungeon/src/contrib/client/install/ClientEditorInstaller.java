package contrib.client.install;

import contrib.editor.level.LevelEditorSystem;
import core.game.systems.SystemRegistration;
import core.platform.client.loop.ClientLoopHostInstaller;

/**
 * A specialized client loop host installer that registers systems for editor functionality.
 *
 * <p>The {@code ClientEditorInstaller} class contributes systems enabling editor-specific
 * functionality, particularly level editing capabilities. The following systems are added to the
 * client system execution pipeline if they are absent:
 *
 * <ul>
 *   <li>{@link LevelEditorSystem}: Adds support for runtime level editing and testing.
 * </ul>
 */
public final class ClientEditorInstaller implements ClientLoopHostInstaller {

  /** Creates an editor client installer. */
  public ClientEditorInstaller() {}

  @Override
  public void installRuntimeSystems() {
    SystemRegistration.addIfAbsent(LevelEditorSystem.class, LevelEditorSystem::new);
  }
}
