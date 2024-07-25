package antlr.treePrinter;

import org.antlr.v4.runtime.tree.ParseTree;

/** Transforms a {@code ParseTree} into a string representation. */
public class ParseTreeToString {
  /**
   * Transforms a {@code ParseTree} into a string representation.
   *
   * @param parseTree the {@code ParseTree}.
   * @return the string representation.
   */
  public static String parseTreeToString(ParseTree parseTree) {
    String resultWithEmptyLineAtEnd = parseTreeToString(parseTree, new TreePrefixData());
    return resultWithEmptyLineAtEnd.stripTrailing();
  }

  private static String parseTreeToString(ParseTree parseTree, TreePrefixData treePrefixData) {
    StringBuilder builder = treePrefixData.createStringBuilderStartingWithPrefix();
    if (!parseTree.getClass().getSimpleName().equals("TerminalNodeImpl")) {
      builder.append(parseTree.getClass().getSimpleName().split("Context")[0]);
      builder.append(": ");
    }
    builder.append(parseTree.getText()).append("\n");

    TreePrefixData nextLevelTreePrefixData = treePrefixData.createForNextLevel();
    for (int i = 0; i < parseTree.getChildCount(); i++) {
      if (i >= parseTree.getChildCount() - 1) {
        nextLevelTreePrefixData = treePrefixData.createForLastElementOfNextLevel();
      }
      builder.append(parseTreeToString(parseTree.getChild(i), nextLevelTreePrefixData));
    }

    return builder.toString();
  }
}
