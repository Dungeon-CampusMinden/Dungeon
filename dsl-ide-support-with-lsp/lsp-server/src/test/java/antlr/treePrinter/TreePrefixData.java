package antlr.treePrinter;

/** Date to display a tree structure as a string. */
public class TreePrefixData {
  private static final String lastNodeOnLevelPrefix = "L  ";
  private static final String childrenPrefixWhenNodesFollowOnLevel = "|- ";
  private static final String childrenPrefixForLastNodeOnLevel = "   ";
  private static final String prefixToFill = "|  ";
  private final String prefix;
  private final String childrenPrefix;

  TreePrefixData() {
    prefix = "";
    childrenPrefix = "";
  }

  private TreePrefixData(String prefix, String childrenPrefix) {
    this.prefix = prefix;
    this.childrenPrefix = childrenPrefix;
  }

  /**
   * Create the data for the next tree level.
   *
   * @return the data for the next tree level.
   */
  public TreePrefixData createForNextLevel() {
    return new TreePrefixData(
        childrenPrefix + childrenPrefixWhenNodesFollowOnLevel, childrenPrefix + prefixToFill);
  }

  /**
   * Create the data for the last element of the next tree level.
   *
   * @return the data for the last element of the next tree level.
   */
  public TreePrefixData createForLastElementOfNextLevel() {
    return new TreePrefixData(
        childrenPrefix + lastNodeOnLevelPrefix, childrenPrefix + childrenPrefixForLastNodeOnLevel);
  }

  /**
   * Creates a {@code StringBuilder} containing the prefix of this data.
   *
   * @return a {@code StringBuilder} containing the prefix of this data.
   */
  public StringBuilder createStringBuilderStartingWithPrefix() {
    return new StringBuilder(prefix);
  }
}
