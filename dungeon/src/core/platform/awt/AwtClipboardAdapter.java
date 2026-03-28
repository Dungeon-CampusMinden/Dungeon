package core.platform.awt;

import core.platform.ClipboardAdapter;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/** Desktop clipboard adapter based on AWT. */
public final class AwtClipboardAdapter implements ClipboardAdapter {

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
