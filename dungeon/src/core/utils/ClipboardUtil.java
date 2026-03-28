package core.utils;

import core.platform.Platform;
import java.awt.*;
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
      Platform.clipboard().setContents(text);
    } catch (Exception e) {
      LOGGER.warning("Failed to copy dungeon layout to clipboard: " + e.getMessage());
    }
  }
}
