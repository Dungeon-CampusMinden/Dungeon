package dsl.programmanalyzer;

import dsl.antlr.DungeonDSLLexer;
import dsl.antlr.DungeonDSLParser;
import dsl.error.ErrorListener;
import dsl.error.ErrorStrategy;
import dsl.parser.DungeonASTConverter;
import dsl.semanticanalysis.analyzer.SemanticAnalyzer;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.environment.IEnvironment;
import entrypoint.ParsedFile;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.Arrays;

public class ProgrammAnalyzer {

  private final boolean trace;
  private final DungeonASTConverter astConverter;
  private GameEnvironment environment;
  private SemanticAnalyzer semanticAnalyzer;

  public ProgrammAnalyzer(boolean trace) {
    this.trace = trace;
    // TODO: add libPath
    this.environment = new GameEnvironment();
    this.semanticAnalyzer = new SemanticAnalyzer();
    this.semanticAnalyzer.setup(environment);
    this.astConverter = new DungeonASTConverter();
  }

  public void analyzeFile(String content, String path) {

  }

  public void analyzeFileDelta(String content, String path) {

  }

  public void analyze(String configScript) {
    // TODO: make relLibPath settable (or make the Environment settable)
    var stream = CharStreams.fromString(configScript);
    ErrorListener el = new ErrorListener();
    var lexer = new DungeonDSLLexer(stream, environment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(el);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new DungeonDSLParser(tokenStream, environment);
    parser.removeErrorListeners();
    parser.addErrorListener(el);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    var programParseTree = parser.program();

    astConverter.setRuleNames(Arrays.stream(parser.getRuleNames()).toList());
    astConverter.setTrace(this.trace);
    var programAST = astConverter.walk(programParseTree, el.getErrors());

    var result = semanticAnalyzer.walk(programAST);
    ParsedFile pf = semanticAnalyzer.latestParsedFile;

    // TODO: grouming
  }
}
