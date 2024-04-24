package manual.level.generator.graphBased;

import contrib.level.generator.graphBased.LevelGraphGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import core.Entity;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.HashSet;
import java.util.Set;

/**
 * Creates a LevelGraph whose DOT representation is directly copied to the clipboard, allowing for
 * quick integration into a DOT visualization.
 */
public class GraphGeneratorTest {

  /**
   * Main method.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    int nodes = 11;
    Set<Set<Entity>> set = new HashSet<>();
    for (int i = 0; i < nodes; i++) {
      Set<Entity> entities = new HashSet<>();
      entities.add(new Entity());
      entities.add(new Entity());
      entities.add(new Entity());
      set.add(entities);
    }

    LevelGraph g = LevelGraphGenerator.generate(set);
    String dot = g.toDot();
    // Get the system clipboard
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    // Create a StringSelection object with the text to copy
    StringSelection selection = new StringSelection(dot);

    // Set the StringSelection as the clipboard's contents
    clipboard.setContents(selection, null);
  }
}
