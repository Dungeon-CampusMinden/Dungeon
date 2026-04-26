package core.platform.client.adapters;

import core.platform.adapters.ClipboardAdapter;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Provides an implementation of the {@link ClipboardAdapter} interface for interacting with the system clipboard
 * using the AWT (Abstract Window Toolkit) framework.
 *
 * <p>This implementation supports clipboard access universally, allowing text to be copied to the clipboard and
 * confirming support for clipboard functionality on all platforms.
 */
public final class ClientClipboardAdapter implements ClipboardAdapter {

  @Override
  public void setContents(String text) {
    StringSelection selection = new StringSelection(text == null ? "" : text);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, null);
  }

  @Override
  public boolean isSupported() {
    return true;
  }
}
