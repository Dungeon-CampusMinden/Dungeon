package dsl.semanticanalysis.symbol;

import dsl.IndexGenerator;
import dsl.parser.ast.Node;
import org.neo4j.ogm.annotation.*;

// Note: this is currently used but not actively saved to db
@RelationshipEntity(type = "REFERENCES")
public class SymbolReference {
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

  public SymbolReference() {
    this.astNode = Node.NONE;
    this.symbol = Symbol.NULL;
  }

  public SymbolReference(Node astNode, Symbol symbol) {
    this.astNode = astNode;
    this.symbol = symbol;
  }
}
