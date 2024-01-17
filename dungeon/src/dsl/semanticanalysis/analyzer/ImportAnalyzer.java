package dsl.semanticanalysis.analyzer;

import dsl.parser.DungeonASTConverter;
import dsl.parser.ast.*;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.scope.FileScope;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.ImportFunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.ImportAggregateTypeSymbol;
import entrypoint.DSLFileLoader;
import entrypoint.ParsedFile;
import java.io.File;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class ImportAnalyzer implements AstVisitor<Void> {
  private IEnvironment environment;
  // the analyzer, which called this ImportAnalyzer and should be used for
  // semantic analysis of imported files
  private SemanticAnalyzer analyzer;
  private IScope parentScope;

  public ImportAnalyzer(IEnvironment environment) {
    this.environment = environment;
  }

  // TODO: need current scope
  public void analyze(Node node, SemanticAnalyzer analyzer, IScope parentScope) {
    this.analyzer = analyzer;
    this.parentScope = parentScope;
    node.accept(this);
  }

  @Override
  public Void visit(Node node) {
    switch (node.type) {
      case Program:
        visitChildren(node);
        break;
      default:
        break;
    }
    return null;
  }

  @Override
  public Void visit(ImportNode node) {
    // TODO:
    //  - [x] check for well-formed-ness of path
    //  - [x] get file of path
    //  - [x] call semantic analysis for file
    //  - [ ] -> need to check-for/resolve/handle recursive imports
    //       (non trivial, need to think about that)
    //  - [x] resolve symbol in file-scope
    //  - [ ] create "proxy"-symbol for resolved symbol

    // TODO: should handle missing ".dng" file-extension

    // check for well-formed-ness of path
    String pathString = ((StringNode) node.pathNode()).getValue();
    Path path = Path.of(pathString);
    Path libPath = this.environment.libPath();
    Path filePath = Path.of(libPath.toString(), path.toString()).toAbsolutePath();
    Path realPath;
    try {
      realPath = filePath.toRealPath(LinkOption.NOFOLLOW_LINKS);
    } catch (Exception ex) {
      // TODO: let it throw for now, should rethink this, when partial parsing
      //  will be implemented
      throw new RuntimeException("No file with the given path '" + filePath + "' found!");
    }
    Path relativeFilePath = libPath.relativize(realPath);
    if (relativeFilePath.startsWith("..")) {
      throw new RuntimeException(
          "Importing from files outside of the 'lib' directory is not allowed!");
    }

    // get the file from the path
    File file = realPath.toFile();

    // get file scope associated with files path
    FileScope fileScope = this.environment.getFileScope(realPath);
    FileScope nullScope = environment.getNullFileScope();
    // -> either just get it or parse it first
    if (fileScope.equals(environment.getNullFileScope())) {
      // need to parse it and add it to file scopes
      String content = DSLFileLoader.fileToString(filePath);
      var programAST = DungeonASTConverter.getProgramAST(content);
      ParsedFile parsedFile = new ParsedFile(filePath, programAST);
      fileScope = new FileScope(parsedFile, environment.getGlobalScope());
      environment.addFileScope(fileScope);

      // TODO: this is pretty naive, should modify this to handle recursive file imports
      this.analyzer.walk(parsedFile);
    } else {
      fileScope = (FileScope) this.environment.getFileScope(realPath);
    }

    // get the symbol from the file scope
    String symbolName = ((IdNode) node.idNode()).getName();
    Symbol symbol = fileScope.resolve(symbolName);
    if (symbol.equals(Symbol.NULL)) {
      throw new RuntimeException("Cannot resolve symbol with name '" + symbolName + "'");
    }

    if (symbol instanceof ImportAggregateTypeSymbol || symbol instanceof ImportFunctionSymbol) {
      // if the resolved symbol is itself an imported symbol, we throw an exception for now
      // this has the effect, that the imported and maybe renamed symbols of a filescope
      // cannot be imported by another file -> no chained importing
      throw new RuntimeException("Importing an imported symbol is not allowed!");
    }

    Symbol importSymbol;
    // create import symbol
    if (symbol instanceof AggregateType aggregateType) {
      if (node.importType().equals(ImportNode.Type.unnamed)) {
        importSymbol = new ImportAggregateTypeSymbol(aggregateType, this.parentScope);
      } else {
        String name = ((IdNode) node.asIdNode()).getName();
        importSymbol = new ImportAggregateTypeSymbol(aggregateType, name, this.parentScope);
      }
    } else if (symbol instanceof FunctionSymbol functionSymbol) {
      if (node.importType().equals(ImportNode.Type.unnamed)) {
        importSymbol = new ImportFunctionSymbol(functionSymbol, this.parentScope);
      } else {
        String name = ((IdNode) node.asIdNode()).getName();
        importSymbol = new ImportFunctionSymbol(functionSymbol, name, this.parentScope);
      }
    } else {
      throw new RuntimeException(
          "Invalid import symbol! Only type definitions and functions can be imported!");
    }
    this.parentScope.bind(importSymbol);
    return null;
  }

  @Override
  public Void visit(DotDefNode node) {
    return null;
  }

  @Override
  public Void visit(ObjectDefNode node) {
    return null;
  }

  @Override
  public Void visit(FuncDefNode node) {
    return null;
  }

  @Override
  public Void visit(PrototypeDefinitionNode node) {
    return null;
  }

  @Override
  public Void visit(ItemPrototypeDefinitionNode node) {
    return null;
  }
}
