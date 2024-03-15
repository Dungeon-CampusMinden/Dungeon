package dsl.error;

import dsl.antlr.ParseTracerForTokenType;
import dsl.antlr.TreeUtils;
import dsl.interpreter.TestEnvironment;
import dsl.profile.ProfilingTimer;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

public class TestErrorListener {

  @Test
  public void test() {
    String program = """
                fn test(int x, int, int) { }
                """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    var lexer = new dsl.antlr.DungeonDSLLexer(stream);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));

    // var eh = parser.getErrorHandler();

    var programParseTree = parser.program();
  }

  @Test
  // TODO: does not sync to my_type...
  public void testObjectDefinition() {
    String program =
        """
                    //obj: my_type obj1 {
                    //    val: id,
                    //    val:
                    //}

                    //obj: my_type obj2 {
                    //    val: id,
                    //    val: id
                    //}

                    my_type obj1 {
                        val: id,
                        val:
                    }

                    my_type obj2 {
                        val: id,
                        val: id
                    }
                """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("my_type");
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, testEnvironment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, testEnvironment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    parser.setTrace(true);

    // var eh = parser.getErrorHandler();

    var programParseTree = parser.program();

    List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
    String prettyTree = TreeUtils.toPrettyTree(programParseTree, ruleNamesList);
    System.out.println(prettyTree);

    var logger = Logger.getLogger(ErrorListener.class.getName());
  }

  @Test
  public void testObjectDefinitionExtraCurly() {
    String program =
        """
                  my_type obj1 {
                      val: id,
                      val: id
                  } }

                  my_type obj2 {
                      val: id,
                      val: id
                  }
              """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("my_type");
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, testEnvironment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, testEnvironment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    parser.setTrace(true);

    // var eh = parser.getErrorHandler();

    var programParseTree = parser.program();

    List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
    String prettyTree = TreeUtils.toPrettyTree(programParseTree, ruleNamesList);
    System.out.println(prettyTree);

    var logger = Logger.getLogger(ErrorListener.class.getName());
  }

  @Test
  public void testObjectDefinitionExtraComma() {
    String program =
        """
                  my_type obj1 {
                      val: id,,
                      val: id
                  }

                  my_type obj2 {
                      val: id,
                      val: id
                  }
              """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("my_type");
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, testEnvironment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, testEnvironment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    parser.setTrace(true);

    // var eh = parser.getErrorHandler();

    var programParseTree = parser.program();

    List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
    String prettyTree = TreeUtils.toPrettyTree(programParseTree, ruleNamesList);
    System.out.println(prettyTree);

    var logger = Logger.getLogger(ErrorListener.class.getName());
  }

  @Test
  public void testObjectDefinitionBonkers() {
    String program =
        """
                asdf_type obj1 {
                    val1: id,,,,,asdfasl
                    val2:
                }

                asdf_type obj2 {
                    val3: id,
                    val4: id
                }

                /*my_type obj2 {
                    val: id,
                    val: id
                };*/

                fn test() {
                  print("Hello");
                  var count = 42;
                  var derp = 13;
                }
            """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    TestEnvironment testEnvironment = new TestEnvironment();
    testEnvironment.addMockTypeName("asdf_type");
    var lexer = new dsl.antlr.DungeonDSLLexer(stream, testEnvironment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new dsl.antlr.DungeonDSLParser(tokenStream, testEnvironment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    parser.setTrace(true);
    ParseTracerForTokenType ptftt = new ParseTracerForTokenType(parser);
    parser.addParseListener(ptftt);

    // var eh = parser.getErrorHandler();

    ProfilingTimer pt = new ProfilingTimer();
    pt.start();
    var programParseTree = parser.program();
    pt.stopAndPrint("After Parse");

    List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
    String prettyTree = TreeUtils.toPrettyTree(programParseTree, ruleNamesList);
    System.out.println(prettyTree);

    var logger = Logger.getLogger(ErrorListener.class.getName());
  }
}
