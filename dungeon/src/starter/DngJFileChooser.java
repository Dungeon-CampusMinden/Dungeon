package starter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DngJFileChooser {
  /**
   * Selects a single DNG file using a file chooser dialog.
   *
   * @return the absolute path of the selected DNG file, or null if no file was selected
   */
  public static String selectSingleDngFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Dungeon: Bitte öffne eine DNG-Datei (siehe auch Readme)");
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setFileFilter(new FileNameExtensionFilter("Nur DNG Dateien", "dng"));
    fileChooser.setAcceptAllFileFilterUsed(false);
    int result = fileChooser.showOpenDialog(null);
    if (result == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile().getAbsolutePath();
    }
    return null;
  }
}
