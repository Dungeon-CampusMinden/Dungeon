package dsl.parser;

import dsl.error.ErrorStrategy;
import dsl.interpreter.TestEnvironment;
import java.util.Arrays;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Assert;
import org.junit.Test;

public class TestParseTreeValidator {
  @Test
  public void testTreeReconstruction() {
    // dot_edge_RHS: is not correctly put as child node to dot_edge_stmt
    String tree =
        """
        dot_edge_stmt
          dot_node_list
            id task1_a
          dot_edge_RHS
            dot_node_list
      """;

    ParseTreeValidator ptv = new ParseTreeValidator();
    var reconstructedTree = ptv.reconstructTree(tree);
    String textTree = reconstructedTree.toStringTree();
    String expected =
        "( name: root c: "
            + "( name: dot_edge_stmt c: "
            + "( name: dot_node_list c: "
            + "( name: id  text: 'task1_a' )"
            + "), "
            + "( name: dot_edge_RHS c: "
            + "( name: dot_node_list )"
            + ")"
            + ")"
            + ")";
    Assert.assertEquals(expected, textTree);
  }

  @Test
  public void testTreeEquality() {
    // dot_edge_RHS: is not correctly put as child node to dot_edge_stmt
    String tree =
        """
      program
        definition
          entity_type_def entity_type
            id type1
            {
            component_def_list
              aggregate_value_def
                id comp1
                {
                property_def_list
                  property_def
                    id val1
                    :
                    expression
                      logic_or
                        logic_and
                          equality
                            comparison
                              term
                                factor
                                  unary
                                    primary
                                      id id
                }
            }
        <EOF>
      """;

    String program =
        """
            entity_type type1 {
                comp1 {
                  val1: id
                }
            }
        """;

    var stream = CharStreams.fromString(program);
    TestEnvironment testEnvironment = new TestEnvironment();
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, testEnvironment);
    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, testEnvironment);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    parser.setTrace(false);
    var parseTree = parser.program();

    ParseTreeValidator ptv = new ParseTreeValidator();
    var mismatches = ptv.validate(tree, parseTree, Arrays.stream(parser.getRuleNames()).toList());
    System.out.println(mismatches);
    Assert.assertEquals(0, mismatches.size());
  }
}
