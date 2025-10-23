package core.utils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.logging.Logger;

/** Utility class for clipboard operations. */
public class ClipboardUtil {

  private static final Logger LOGGER = Logger.getLogger(ClipboardUtil.class.getName());

  /**
   * Copies the given text to the system clipboard.
   *
   * @param text The text to be copied to the clipboard.
   */
  public static void copyToClipboard(String text) {
    try {
      StringSelection stringSelection = new StringSelection(text);
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, null);
    } catch (Exception e) {
      LOGGER.warning("Failed to copy dungeon layout to clipboard: " + e.getMessage());
    }
  }
}
