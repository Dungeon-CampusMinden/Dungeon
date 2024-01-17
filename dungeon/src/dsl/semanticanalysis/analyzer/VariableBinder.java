/*
 * MIT License
 *
 * Copyright (c) 2022 Malte Reinsch, Florian Warzecha, Sebastian Steinmeyer, BC George, Carsten Gips
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dsl.semanticanalysis.analyzer;

// importing all required classes from symbolTable will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport
import dsl.parser.ast.*;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

// TODO: handle scoped Variables in stmt Blocks correctly, once
//  variable definitions are implemented

// TODO: rename this to ObjectBinder (only handles global definitions)
/** Creates symbols for definition nodes (graph, object) and binds these nodes to those symbols */
public class VariableBinder implements AstVisitor<Void> {
  SymbolTable symbolTable;
  IScope parentScope;
  StringBuilder errorStringBuilder;

  /**
   * Visits each child node of the passed rootNode and binds definition nodes.
   *
   * @param symbolTable The symbolTable to use for creation of relations between AST nodes and
   *     Symbols
   * @param parentScope The scope, in which the binding should be performed
   * @param rootNode The node to visit the children of
   * @param errorStringBuilder A string builder to append error messages to
   */
  public void bindVariables(
      SymbolTable symbolTable,
      IScope parentScope,
      Node rootNode,
      StringBuilder errorStringBuilder) {
    this.symbolTable = symbolTable;
    this.parentScope = parentScope;
    this.errorStringBuilder = errorStringBuilder;
    visitChildren(rootNode);
  }

  @Override
  public Void visit(Node node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(IdNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(StringNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(BinaryNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(StmtBlockNode node) {
    for (var stmt : node.getStmts()) {
      stmt.accept(this);
    }
    return null;
  }

  @Override
  public Void visit(ConditionalStmtNodeIf node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(ConditionalStmtNodeIfElse node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(FuncDefNode node) {
    node.getStmtBlock().accept(this);
    return null;
  }

  @Override
  public Void visit(DotDefNode node) {
    var id = node.getIdNode();
    assert id.type == Node.Type.Identifier;

    // Bind assignee in current scope
    var idNode = (IdNode) id;
    var idName = idNode.getName();

    // check, if assignee is already bound in current scope
    // if not, create it and bind it
    var objectSymbol = parentScope.resolve(idName);

    if (Symbol.NULL == objectSymbol) {
      objectSymbol = new Symbol(idName, parentScope, BuiltInType.graphType);
      if (parentScope.bind(objectSymbol)) {
        symbolTable.addSymbolNodeRelation(objectSymbol, node, true);
      }
    } else {
      errorStringBuilder.append("Already defined object of name...");
    }
    return null;
  }

  @Override
  public Void visit(ObjectDefNode node) {
    var id = node.getId();
    assert id.type == Node.Type.Identifier;

    // Bind assignee in current scope
    var idNode = (IdNode) id;
    var idName = idNode.getName();

    // check, if assignee is already bound in current scope
    // if not, create it and bind it
    var objectSymbol = parentScope.resolve(idName);

    if (Symbol.NULL == objectSymbol) {
      // resolve type name
      var typeName = node.getTypeSpecifierName();
      var type = this.parentScope.resolve(typeName);
      assert type != null;
      assert type instanceof IType;

      objectSymbol = new Symbol(idName, parentScope, (IType) type);
      if (parentScope.bind(objectSymbol)) {
        symbolTable.addSymbolNodeRelation(objectSymbol, node, true);
      }
    } else {
      errorStringBuilder.append("Already defined object of name...");
    }
    return null;
  }

  @Override
  public Void visit(AssignmentNode node) {
    // TODO: implement creation of new variable..
    //  and nothing more.. so this is currenlty not implemented
    //  but not 'unsupported'

    return null;
  }

  // region ASTVisitor implementation of Nodes unrelated to variable binding

  @Override
  public Void visit(ImportNode node) {
    return null;
  }

  @Override
  public Void visit(DecNumNode node) {
    return null;
  }

  @Override
  public Void visit(NumNode node) {
    return null;
  }

  @Override
  public Void visit(EdgeRhsNode node) {
    return null;
  }

  @Override
  public Void visit(DotEdgeStmtNode node) {
    return null;
  }

  @Override
  public Void visit(EdgeOpNode node) {
    return null;
  }

  @Override
  public Void visit(PropertyDefNode node) {
    return null;
  }

  @Override
  public Void visit(FuncCallNode node) {
    return null;
  }

  @Override
  public Void visit(AggregateValueDefinitionNode node) {
    return null;
  }

  @Override
  public Void visit(ParamDefNode node) {
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

  @Override
  public Void visit(ReturnStmtNode node) {
    return null;
  }

  @Override
  public Void visit(BoolNode node) {
    return null;
  }

  @Override
  public Void visit(MemberAccessNode node) {
    return null;
  }

  @Override
  public Void visit(LogicOrNode node) {
    return null;
  }

  @Override
  public Void visit(LogicAndNode node) {
    return null;
  }

  @Override
  public Void visit(EqualityNode node) {
    return null;
  }

  @Override
  public Void visit(ComparisonNode node) {
    return null;
  }

  @Override
  public Void visit(TermNode node) {
    return null;
  }

  @Override
  public Void visit(FactorNode node) {
    return null;
  }

  @Override
  public Void visit(UnaryNode node) {
    return null;
  }

  @Override
  public Void visit(ListDefinitionNode node) {
    return null;
  }

  @Override
  public Void visit(SetDefinitionNode node) {
    return null;
  }

  @Override
  public Void visit(VarDeclNode node) {
    return null;
  }

  @Override
  public Void visit(LoopStmtNode node) {
    return null;
  }

  @Override
  public Void visit(WhileLoopStmtNode node) {
    return null;
  }

  @Override
  public Void visit(CountingLoopStmtNode node) {
    return null;
  }

  @Override
  public Void visit(ForLoopStmtNode node) {
    return null;
  }

  // endregion
}
