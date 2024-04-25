package dsl.interpreter;

import dsl.parser.DungeonASTConverter;
import dsl.parser.ast.*;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import entrypoint.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/** AstVisitor implementation to search for quest_config definition. */
public class DSLEntryPointFinder implements AstVisitor<Object> {
  private ArrayList<DSLEntryPoint> entryPoints;
  private ParsedFile parsedFile;
  private final GameEnvironment environment;
  private AggregateType questConfigDataType;
  private final HashMap<Path, ParsedFile> parsedFiles;

  /**
   * Constructor.
   *
   * <p>Creates a GameEnvironment to get the {@link IType} for {@link DungeonConfig}.
   */
  public DSLEntryPointFinder() {
    this.environment = new GameEnvironment();
    this.parsedFiles = new HashMap<>();
    var symbols = environment.getGlobalScope().getSymbols();
    for (Symbol symbol : symbols) {
      if (symbol instanceof AggregateType aggregateType) {
        if (aggregateType.getOriginType().equals(DungeonConfig.class)) {
          this.questConfigDataType = aggregateType;
        }
      }
    }
  }

  /**
   * Creates an AST vor the file of the passed filePath, searches it for quest_config definitions
   * and creates {@link DSLEntryPoint} instances for each one.
   *
   * @param filePath the path of the file to search for quest_config definitions in
   * @return an empty optional, if reading the file caused an error or it does not contain any
   *     quest_config definitions, the list of found quest_config objects otherwise
   */
  public Optional<List<DSLEntryPoint>> getEntryPoints(Path filePath) {
    Node programAST;
    if (this.parsedFiles.containsKey(filePath)) {
      this.parsedFile = this.parsedFiles.get(filePath);
      programAST = parsedFile.rootASTNode();
    } else {
      String content = DSLFileLoader.fileToString(filePath);
      programAST = DungeonASTConverter.getProgramAST(content);
      ParsedFile parsedFile = new ParsedFile(filePath, programAST);
      this.parsedFiles.put(filePath, parsedFile);
      this.parsedFile = parsedFile;
    }
    // we don't want to do the whole interpretation here...
    // we only want to know, which (well formed) entry points exist
    // would be enough to do this in a light AST-Visitor..
    List<DSLEntryPoint> list = findEntryPoints(programAST);
    if (list.size() != 0) {
      return Optional.of(list);
    }
    return Optional.empty();
  }

  private List<DSLEntryPoint> findEntryPoints(Node programAST) {
    this.entryPoints = new ArrayList<>();
    programAST.accept(this);
    return entryPoints;
  }

  @Override
  public Object visit(Node node) {
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
  public Object visit(IdNode node) {
    return node.getName();
  }

  @Override
  public Object visit(StringNode node) {
    return node.getValue();
  }

  @Override
  public Object visit(ListTypeIdentifierNode node) {
    return node.getName();
  }

  @Override
  public Object visit(SetTypeIdentifierNode node) {
    return node.getName();
  }

  @Override
  public Object visit(MapTypeIdentifierNode node) {
    return node.getName();
  }

  @Override
  public Object visit(DotDefNode node) {
    return null;
  }

  private String getDisplayName(ObjectDefNode questConfigDefNode) {
    String displayName = questConfigDefNode.getIdName();
    for (Node node : questConfigDefNode.getPropertyDefinitions()) {
      PropertyDefNode propertyDefNode = (PropertyDefNode) node;
      if (propertyDefNode.getIdName().equals("name")) {
        displayName = (String) propertyDefNode.getStmtNode().accept(this);
      }
    }
    return displayName;
  }

  @Override
  public Object visit(ObjectDefNode node) {
    Node typeSpecifier = node.getTypeSpecifier();
    String typeSpecifierName = (String) typeSpecifier.accept(this);
    if (typeSpecifierName.equals(questConfigDataType.getName())) {
      // found one
      String displayName = getDisplayName(node);
      this.entryPoints.add(new DSLEntryPoint(this.parsedFile, displayName, node));
    }
    return null;
  }

  @Override
  public Object visit(PrototypeDefinitionNode node) {
    return null;
  }

  @Override
  public Object visit(ItemPrototypeDefinitionNode node) {
    return null;
  }

  @Override
  public Object visit(FuncDefNode node) {
    return null;
  }
}
