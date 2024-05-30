package dsl.semanticanalysis.symbol;

import dsl.IndexGenerator;
import dsl.parser.ast.Node;
import org.neo4j.ogm.annotation.*;

// Note: still used but not actively saved into db
@RelationshipEntity(type = "CREATES")
public class SymbolCreation {
  @Id @GeneratedValue private Long id;
  @Property public Long internalId = IndexGenerator.getUniqueIdx();
  @StartNode private Node astNode;
  @EndNode private Symbol symbol;

  public Symbol getSymbol() {
    return symbol;
  }

  public Node getAstNode() {
    return astNode;
  }

  public SymbolCreation() {
    this.astNode = Node.NONE;
    this.symbol = Symbol.NULL;
  }

  public SymbolCreation(Node astNode, Symbol symbol) {
    this.astNode = astNode;
    this.symbol = symbol;
  }
}
