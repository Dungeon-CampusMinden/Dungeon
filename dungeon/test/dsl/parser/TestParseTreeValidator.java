package dsl.parser;

import org.junit.Test;

public class TestParseTreeValidator {
  @Test
  public void testTreeReconstruction() {
    String tree= """
        dot_edge_stmt
          dot_node_list
            id task1_a
          dot_edge_RHS -> // TODO: this is not correctly added as child of dot_edge_stmt
            dot_node_list
      """;

    ParseTreeValidator ptv = new ParseTreeValidator();
    var reconstructedTree = ptv.reconstructTree(tree);
    String textTree = reconstructedTree.toStringTree();
    System.out.println(textTree);
  }

}
