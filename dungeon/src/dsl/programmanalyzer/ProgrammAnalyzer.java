package dsl.programmanalyzer;

import dsl.antlr.DungeonDSLLexer;
import dsl.antlr.DungeonDSLParser;
import dsl.error.ErrorListener;
import dsl.error.ErrorRecordFactory;
import dsl.error.ErrorStrategy;
import dsl.parser.DungeonASTConverter;
import dsl.parser.ast.ParentOf;
import dsl.parser.ast.RelationshipRecorder;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.analyzer.SemanticAnalyzer;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.groum.FinalGroumBuilder;
import dsl.semanticanalysis.groum.Groum;
import dsl.semanticanalysis.scope.FileScope;
import dsl.semanticanalysis.scope.IScope;
import entrypoint.ParsedFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class ProgrammAnalyzer {

  private final boolean trace;
  private final DungeonASTConverter astConverter;
  private GameEnvironment environment;
  private SemanticAnalyzer semanticAnalyzer;

  public IEnvironment getEnvironment() {
    return this.environment;
  }

  public record AnalyzedProgramComplete(
      List<ParsedFile> parsedFiles,
      List<IScope> scopes,
      SymbolTable symboltable,
      List<ParentOf> nodeRelationships,
      Groum groum) {}

  // TODO: what to put in this?
  public record analyzedProgramDelta(
      ParsedFile parsedFile,
      List<IScope> scopes,
      SymbolTable symboltable,
      List<ParentOf> nodeRelationships,
      Groum groum) {}

  public ProgrammAnalyzer(boolean trace) {
    this.trace = trace;
    // TODO: add libPath
    this.environment = new GameEnvironment();
    this.semanticAnalyzer = new SemanticAnalyzer();
    this.semanticAnalyzer.setup(environment);
    this.astConverter = new DungeonASTConverter();
  }

  public void analyzeFile(String content, String uri) {}

  public void analyzeFileDelta(String content, String path) {}

  public AnalyzedProgramComplete analyze(String configScript, String configFileUri) {
    RelationshipRecorder.instance.clear();
    ErrorRecordFactory.instance.clear();

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

    var configFilePath = Path.of(configFileUri);
    ParsedFile parsedFile = new ParsedFile(configFilePath, programAST);

    if (this.environment.getFileScopes().containsKey(configFilePath)) {
      var fileScope = this.environment.getFileScope(configFilePath);
      var symTable = this.environment.getSymbolTable();
      symTable.removeScope(fileScope);
      this.environment.removeFileScope(configFilePath);
    }

    var currentFileScope = new FileScope(parsedFile, this.environment.getGlobalScope());
    this.environment.addFileScope(currentFileScope);

    // TODO: link session to analysis pass and file scope
    this.environment.getSymbolTable().pushNewSession();
    var result = semanticAnalyzer.walk(parsedFile);
    FinalGroumBuilder groumBuilder = new FinalGroumBuilder();
    var finalGroum = groumBuilder.build(programAST, result.symbolTable, this.environment);
    finalGroum.setFileScope(currentFileScope);

    var nodeRelationShips = RelationshipRecorder.instance.get();
    return new AnalyzedProgramComplete(
        List.of(parsedFile),
        new ArrayList<>(this.environment.getFileScopes().values()),
        this.environment.getSymbolTable(),
        nodeRelationShips,
        finalGroum);
  }
}
