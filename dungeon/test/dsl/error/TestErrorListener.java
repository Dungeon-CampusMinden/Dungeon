package dsl.error;

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
    var lexer = new antlr.main.DungeonDSLLexer(stream);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new antlr.main.DungeonDSLParser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary()));

    // var eh = parser.getErrorHandler();

    var programParseTree = parser.program();
  }

  @Test
  public void testObjectDefinition() {
    String program =
        """
                    my_type obj {
                        val: id,
                        val:
                    }
                """;

    ErrorListener el = ErrorListener.INSTANCE;
    var stream = CharStreams.fromString(program);
    var lexer = new antlr.main.DungeonDSLLexer(stream);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new antlr.main.DungeonDSLParser(tokenStream);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary()));

    // var eh = parser.getErrorHandler();

    var programParseTree = parser.program();
  }
}
