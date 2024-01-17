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

import dsl.parser.ast.*;
import dsl.runtime.callable.ICallable;
import dsl.runtime.callable.NativeFunction;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.scope.FileScope;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.ScopedSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.*;
import entrypoint.ParsedFile;
import java.util.Stack;

// importing all required classes from symbolTable will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport

/** Creates a symbol table for an AST node for a DSL program */
// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class SemanticAnalyzer implements AstVisitor<Void> {
  private SymbolTable symbolTable;
  private IEnvironment environment;

  // TODO: this is just for testing and will be set to the NULL-File created in
  //  this.walk in order to reconstruct the status quo while working on implementation of
  //  multifile
  public ParsedFile latestParsedFile = null;
  Stack<IScope> scopeStack = new Stack<>();
  StringBuilder errorStringBuilder = new StringBuilder();
  private boolean setup = false;

  public class Result {
    public final SymbolTable symbolTable;
    public final boolean gotError;
    public final String errorString;

    /**
     * Constructor. If the errorString is empty, gotError will be set to false
     *
     * @param symbolTable the symbol table
     * @param errorString the errorString which was generated during semantic analysis
     */
    public Result(SymbolTable symbolTable, String errorString) {
      this.symbolTable = symbolTable;
      this.errorString = errorString;
      this.gotError = !errorString.equals("");
    }
  }

  /**
   * Helper method for getting the current scope (the top of the scopeStack)
   *
   * @return the top of the scopeStack
   */
  private IScope currentScope() {
    return scopeStack.peek();
  }

  /**
   * Helper method for getting the global scope (the bottom of the scopeStack)
   *
   * @return the bottom of the scopeStack
   */
  // TODO: needs to be modified/assessed -> when is the global scope really needed and
  //  when is the current scope / file scope adequate
  private IScope globalScope() {
    return scopeStack.get(0);
  }

  public IEnvironment getEnvironment() {
    return this.environment;
  }

  /**
   * Setup environment for semantic analysis (setup builtin types and native functions); use an
   * externally provided symbol table, which will be used and extended during semantic analysis
   *
   * @param environment environment to use for setup of built in types and native functions
   */
  public void setup(IEnvironment environment) {
    setup(environment, false);
  }

  /**
   * Setup environment for semantic analysis (setup builtin types and native functions); use an
   * externally provided symbol table, which will be used and extended during semantic analysis
   *
   * @param environment environment to use for setup of built in types and native functions
   * @param force force the initialization of this {@link SemanticAnalyzer} with the given {@link
   *     IEnvironment} (ignore and overwrite previous initializations).
   */
  public void setup(IEnvironment environment, boolean force) {
    if (!force && setup) {
      return;
    }

    this.errorStringBuilder = new StringBuilder();
    this.scopeStack = new Stack<>();

    this.scopeStack.push(environment.getGlobalScope());
    this.symbolTable = environment.getSymbolTable();
    this.environment = environment;

    // ensure, that all FunctionTypes of the native functions are correctly bound
    // in the symbolTable and remove redundancies
    var globalScope = this.symbolTable.globalScope();
    for (var func : environment.getFunctions()) {
      if (func instanceof NativeFunction) {
        var funcType = (FunctionType) func.getDataType();
        var funcTypeSymbol = globalScope.resolve(funcType.getName());
        if (funcTypeSymbol.equals(Symbol.NULL)) {
          globalScope.bind(funcType);
        } else if (funcType.hashCode() != funcTypeSymbol.hashCode()) {
          // use the funcType already in symbolTable
          ((NativeFunction) func).overwriteFunctionType((FunctionType) funcTypeSymbol);
        }
      }
    }

    this.setup = true;
  }

  /**
   * Built in types (such as all BuiltIns and built in AggregateTypes such as quest_config) have no
   * parent Scope. This leads to situations, in which the topmost scope is such an aggregate type.
   * Resolving a reference to a global variable does not work in this context, so this method
   * implements a manual stack walk of the scope stack to deal with this case.
   *
   * @param name the name of the symbol to resolve
   * @return the resolved symbol or Symbol.NULL, if the name could not be resolved
   */
  private Symbol resolve(String name) {
    // if the type of the current scope is an AggregateType, first resolve in this type
    // and then resolve in the enclosing scope
    var symbol = Symbol.NULL;
    var stackIterator = this.scopeStack.listIterator(scopeStack.size());
    IScope scope;

    while (symbol.equals(Symbol.NULL) && stackIterator.hasPrevious()) {
      scope = stackIterator.previous();
      symbol = scope.resolve(name);
    }
    return symbol;
  }

  /**
   * Visit children node in node, create symbol table and resolve function calls
   *
   * @return The symbol table for given node
   */
  public Result walk(ParsedFile file) {
    if (!setup) {
      errorStringBuilder.append("Symbol table parser was not setup with an environment");
      return new Result(symbolTable, errorStringBuilder.toString());
    }

    var path = file.filePath();
    IScope scope = this.environment.getFileScope(path);
    if (scope == Scope.NULL) {
      // create new file-scope
      FileScope fs = new FileScope(file, this.environment.getGlobalScope());
      this.environment.addFileScope(fs);
      scope = fs;
    }

    this.scopeStack.push(scope);

    Node node = file.rootASTNode();
    node.accept(this);

    this.scopeStack.pop();

    return new Result(symbolTable, errorStringBuilder.toString());
  }

  /**
   * Visit children node in node, create symbol table and resolve function calls
   *
   * @param node The node to walk
   * @return The symbol table for given node
   */
  public Result walk(Node node) {
    if (!setup) {
      errorStringBuilder.append("Symbol table parser was not setup with an environment");
      return new Result(symbolTable, errorStringBuilder.toString());
    }

    ParsedFile pf = new ParsedFile(null, node);
    latestParsedFile = pf;
    FileScope fs = new FileScope(pf, this.globalScope());
    this.environment.addFileScope(fs);

    this.scopeStack.push(fs);
    node.accept(this);
    this.scopeStack.pop();

    // TODO: got a feeling, this should also return the file scope -> otherwise it won't be
    //  accessed afterwards by the DSLInterpreter, i guess
    return new Result(symbolTable, errorStringBuilder.toString());
  }

  @Override
  public Void visit(Node node) {
    switch (node.type) {
      case Program:
        IScope topMostScope = this.scopeStack.peek();
        // bind all type definitions
        TypeBinder tb = new TypeBinder();
        tb.bindTypes(environment, topMostScope, node, errorStringBuilder);

        // bind all object definitions / variable assignments to enable object
        // references before
        // definition
        VariableBinder vb = new VariableBinder();
        vb.bindVariables(symbolTable, topMostScope, node, errorStringBuilder);

        FunctionDefinitionBinder fdb = new FunctionDefinitionBinder();
        fdb.bindFunctionDefinitions(symbolTable, topMostScope, node);

        ImportAnalyzer ia = new ImportAnalyzer(this.environment);
        ia.analyze(node, this, topMostScope);

        visitChildren(node);

        break;
      case PropertyDefinitionList:
      case ParamList:
      case GroupedExpression:
        visitChildren(node);
      default:
        break;
    }
    return null;
  }

  // if we reach this point in the walk, the given IdNode
  // is part of an expression and therefore needs to be resolved in the current scope.
  // Binding of the names of objects etc. is done selectively in Visit-methods for object
  // definitions
  @Override
  public Void visit(IdNode node) {
    var idName = node.getName();
    var symbol = resolve(idName);
    if (symbol.equals(Symbol.NULL)) {
      errorStringBuilder.append(
          "Reference of undefined identifier: "
              + node.getName()
              + " at: "
              + node.getSourceFileReference()
              + "\n");
    } else {
      symbolTable.addSymbolNodeRelation(symbol, node, false);
    }
    return null;
  }

  @Override
  public Void visit(BinaryNode node) {
    for (var child : node.getChildren()) {
      child.accept(this);
    }
    return null;
  }

  @Override
  public Void visit(ItemPrototypeDefinitionNode node) {
    // resolve datatype of definition
    var typeName = node.getIdName();
    var typeSymbol = currentScope().resolve(typeName);
    if (typeSymbol.equals(Symbol.NULL) || typeSymbol == null) {
      errorStringBuilder.append("Could not resolve type " + typeName);
    } else {
      scopeStack.push((AggregateType) typeSymbol);
      for (var propertyDef : node.getPropertyDefinitionNodes()) {
        propertyDef.accept(this);
      }
      scopeStack.pop();
    }
    return null;
  }

  @Override
  public Void visit(PrototypeDefinitionNode node) {
    // resolve datatype of definition
    var typeName = node.getIdName();
    var typeSymbol = this.currentScope().resolve(typeName);
    if (typeSymbol.equals(Symbol.NULL) || typeSymbol == null) {
      errorStringBuilder.append("Could not resolve type " + typeName);
    } else {
      scopeStack.push((AggregateType) typeSymbol);
      for (var componentDef : node.getComponentDefinitionNodes()) {
        componentDef.accept(this);
      }
      scopeStack.pop();
    }
    return null;
  }

  // TODO: Symbols for members?
  @Override
  public Void visit(AggregateValueDefinitionNode node) {
    // push datatype of component
    // resolve in current scope, which will be datatype of game object definition
    IType membersType;
    String valueName = node.getIdName();

    // TODO: for an anonymous object (inline defined aggregate value), the memberSymbol will be
    // Symbol.NULL
    //  could just resolve the name as a datatype and resolve any member in it..
    //  but this is not really clean. Should make this explicit at a higher level

    // get the type of the aggregate value
    var memberSymbol = currentScope().resolve(valueName);
    if (memberSymbol == Symbol.NULL) {
      var type = this.environment.getGlobalScope().resolve(valueName);
      assert type instanceof AggregateType;
      membersType = (IType) type;
    } else {
      membersType = memberSymbol.getDataType();
    }

    // TODO: errorhandling
    if (membersType == Symbol.NULL || membersType == null) {
      errorStringBuilder.append("Could not resolve type " + "TODO");
    } else {
      // visit all property-definitions of the aggregate value definition
      scopeStack.push((AggregateType) membersType);
      for (var propertyDef : node.getPropertyDefinitionNodes()) {
        propertyDef.accept(this);
      }
      scopeStack.pop();
    }

    return null;
  }

  @Override
  public Void visit(PropertyDefNode node) {
    var propertyIdName = node.getIdName();

    // the current scope will be the type of the object definition
    var propertySymbol = currentScope().resolve(propertyIdName);
    if (propertySymbol == Symbol.NULL) {
      errorStringBuilder.append("no property with name " + propertyIdName + " could be found");
    } else {
      // link the propertySymbol in the dataType to the astNode of this concrete property
      // definition
      this.symbolTable.addSymbolNodeRelation(propertySymbol, node, true);
    }

    var stmtNode = node.getStmtNode();
    stmtNode.accept(this);

    // TODO: check, if the types of the property and the stmt are compatible

    return null;
  }

  @Override
  public Void visit(ObjectDefNode node) {
    // resolve the type of the object definition and push it on the stack
    var typeName = node.getTypeSpecifierName();
    // TODO: this should be revised to be the current scope
    var typeSymbol = this.currentScope().resolve(typeName);

    // TODO: errorhandling
    if (typeSymbol == Symbol.NULL) {
      errorStringBuilder.append("Could not resolve type " + typeName);
    } else if (typeSymbol.getSymbolType() != Symbol.Type.Scoped) {
      errorStringBuilder.append("Type " + typeName + " is not scoped!");
    } else {
      scopeStack.push((AggregateType) typeSymbol);

      for (var propertyDef : node.getPropertyDefinitions()) {
        propertyDef.accept(this);
      }
      scopeStack.pop();
    }
    return null;
  }

  @Override
  public Void visit(ParamDefNode node) {
    // is handled in FunctionDefinitionBinder
    return null;
  }

  @Override
  public Void visit(FuncCallNode node) {
    Node parentNode = node.getParent();
    if (parentNode.type.equals(Node.Type.MemberAccess)) {
      // symbol will be resolved in the visit-implementation of MemberAccessNode, as it
      // requires
      // resolving in the datatype of the preceding member-access expression
    } else {
      Symbol funcSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
      if (funcSymbol == Symbol.NULL) {
        String funcName = node.getIdName();
        funcSymbol = resolve(funcName);
        if (funcSymbol.equals(Symbol.NULL)) {
          throw new RuntimeException("Function with name " + funcName + " could not be resolved!");
        }

        if (!(funcSymbol instanceof ICallable)) {
          throw new RuntimeException("Symbol with name " + funcName + " is not callable!");
        }

        this.symbolTable.addSymbolNodeRelation(funcSymbol, node, false);
      }
    }

    for (var parameter : node.getParameters()) {
      parameter.accept(this);
    }
    return null;
  }

  @Override
  public Void visit(FuncDefNode node) {
    var funcName = node.getIdName();
    // TODO: current scope
    Symbol resolved = resolve(funcName);
    if (resolved == Symbol.NULL) {
      errorStringBuilder.append(
          "Could not resolve Identifier with name " + funcName + " in global scope!");
    } else {
      FunctionSymbol funcSymbol = (FunctionSymbol) resolved;
      scopeStack.push(funcSymbol);

      // visit statements
      node.getStmtBlock().accept(this);

      // create symbol table entry
      symbolTable.addSymbolNodeRelation(funcSymbol, node, false);

      scopeStack.pop();
    }
    // }
    return null;
  }

  @Override
  public Void visit(ReturnStmtNode node) {
    node.getInnerStmtNode().accept(this);
    return null;
  }

  @Override
  public Void visit(StmtBlockNode node) {
    var blockScope = new Scope(scopeStack.peek());
    scopeStack.push(blockScope);
    for (var stmt : node.getStmts()) {
      stmt.accept(this);
    }
    scopeStack.pop();
    return null;
  }

  @Override
  public Void visit(ConditionalStmtNodeIf node) {
    node.getCondition().accept(this);

    // if the statement is not a block (i.e. there is only one statement in the if-statements
    // body),
    // we need to create a new scope here (because it won't be created in a block-statement)
    if (!node.getIfStmt().type.equals(Node.Type.Block)) {
      var scope = new Scope(scopeStack.peek());
      scopeStack.push(scope);
      node.getIfStmt().accept(this);
      scopeStack.pop();
    } else {
      node.getIfStmt().accept(this);
    }

    return null;
  }

  @Override
  public Void visit(ConditionalStmtNodeIfElse node) {
    node.getCondition().accept(this);

    // if the statements are not blocks (i.e. there is only one statement in the if-statements
    // body),
    // we need to create new scopes here (because it won't be created in block-statements)
    if (!node.getIfStmt().type.equals(Node.Type.Block)) {
      var ifScope = new Scope(scopeStack.peek());
      scopeStack.push(ifScope);
      node.getIfStmt().accept(this);
      scopeStack.pop();
    } else {
      node.getIfStmt().accept(this);
    }

    if (!node.getElseStmt().type.equals(Node.Type.Block)) {
      var elseScope = new Scope(scopeStack.peek());
      scopeStack.push(elseScope);
      node.getElseStmt().accept(this);
      scopeStack.pop();
    } else {
      node.getElseStmt().accept(this);
    }

    return null;
  }

  @Override
  public Void visit(MemberAccessNode node) {
    Node currentNode = node;
    Node lhs = Node.NONE;
    Node rhs = Node.NONE;
    IType lhsDataType = BuiltInType.noType;
    IScope scopeToUse = this.currentScope();

    while (currentNode.type.equals(Node.Type.MemberAccess)) {
      lhs = ((MemberAccessNode) currentNode).getLhs();
      rhs = ((MemberAccessNode) currentNode).getRhs();

      // resolve name of lhs in scope
      // lhsDataType = BuiltInType.noType;
      if (lhs.type.equals(Node.Type.Identifier)) {
        String nameToResolve = ((IdNode) lhs).getName();
        Symbol symbol = scopeToUse.resolve(nameToResolve);
        IType symbolsType = symbol.getDataType();

        if (symbolsType != null && symbolsType.getTypeKind().equals(IType.Kind.EnumType)) {
          // this is an illegal case, we can't resolve members inside an enum's variant
          String lhsFullName = symbol.getFullName();
          throw new RuntimeException("Member access on enum value is not allowed: " + lhsFullName);
        }

        if (symbol instanceof EnumType) {
          lhsDataType = (IType) symbol;
        } else {
          lhsDataType = symbolsType;
        }

        symbolTable.addSymbolNodeRelation(symbol, lhs, false);
      } else if (lhs.type.equals(Node.Type.FuncCall)) {
        // visit function call itself (resolve parameters etc.)
        lhs.accept(this);

        // resolve function definition
        String functionName = ((FuncCallNode) lhs).getIdName();
        Symbol resolvedFunction = scopeToUse.resolve(functionName);
        ICallable callable = (ICallable) resolvedFunction;
        FunctionType functionType = callable.getFunctionType();
        lhsDataType = functionType.getReturnType();

        symbolTable.addSymbolNodeRelation(resolvedFunction, lhs, false);
      }

      currentNode = rhs;

      if (!(lhsDataType instanceof ScopedSymbol lhsTypeScopedSymbol)) {
        throw new RuntimeException(
            "Datatype " + lhsDataType.getName() + " of lhs in member access is no scoped symbol!");
      }
      scopeToUse = lhsTypeScopedSymbol;
    }

    // if we arrive here, we have got two options:
    // 1. we resolve an IdNode at the rhs of the MemberAccessNode
    // 2. we resolve an FuncCallNode at the rhs of the MemberAccessNode
    if (rhs.type.equals(Node.Type.Identifier)) {
      // push lhsDataType on stack
      scopeStack.push(scopeToUse);
      rhs.accept(this);
      scopeStack.pop();
    } else if (rhs.type.equals(Node.Type.FuncCall)) {
      // resolve function name in scope to use
      String funcName = ((FuncCallNode) rhs).getIdName();
      Symbol funcSymbol = scopeToUse.resolve(funcName, true);
      if (funcSymbol.equals(Symbol.NULL)) {
        throw new RuntimeException("Function with name " + funcName + " could not be resolved!");
      }

      if (!(funcSymbol instanceof ICallable)) {
        throw new RuntimeException("Symbol with name " + funcName + " is not callable!");
      }
      this.symbolTable.addSymbolNodeRelation(funcSymbol, rhs, false);

      rhs.accept(this);
    }

    return null;
  }

  @Override
  public Void visit(LogicOrNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(LogicAndNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(EqualityNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(ComparisonNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(TermNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(FactorNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(UnaryNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(AssignmentNode node) {
    // TODO: typechcking

    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(ListTypeIdentifierNode node) {
    return null;
  }

  @Override
  public Void visit(SetTypeIdentifierNode node) {
    return null;
  }

  @Override
  public Void visit(MapTypeIdentifierNode node) {
    return null;
  }

  @Override
  public Void visit(ListDefinitionNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(SetDefinitionNode node) {
    visitChildren(node);
    return null;
  }

  private Symbol createVariableSymbolInScope(IType type, IdNode nameIdNode, IScope scope) {
    String name = nameIdNode.getName();

    // check if a variable is already defined in the current scope
    Symbol resolvedName = scope.resolve(name, false);
    if (!resolvedName.equals(Symbol.NULL)) {
      throw new RuntimeException("Redefinition of variable '" + name + "'");
    }

    // create variable symbol
    Symbol variableSymbol = new Symbol(name, scope, type);
    scope.bind(variableSymbol);
    this.symbolTable.addSymbolNodeRelation(variableSymbol, nameIdNode, true);

    return variableSymbol;
  }

  // TODO: make sure, that currentScope is passed to this
  private Symbol createVariableSymbolInScope(IdNode typeIdNode, IdNode nameIdNode, IScope scope) {
    // resolve the type name
    String typeName = typeIdNode.getName();
    if (!typeIdNode.type.equals(Node.Type.Identifier)) {
      // list or set type -> create type
      typeIdNode.accept(this);
    }

    Symbol typeSymbol = this.globalScope().resolve(typeName);
    if (!(typeSymbol instanceof IType variableType)) {
      throw new RuntimeException("Type of name '" + typeName + "' cannot be resolved!");
    }

    return createVariableSymbolInScope(variableType, nameIdNode, scope);
  }

  @Override
  public Void visit(VarDeclNode node) {
    // create new symbol for the variable
    // get the type of the variable
    if (node.getDeclType().equals(VarDeclNode.DeclType.assignmentDecl)) {
      throw new RuntimeException("Inference of variable type currently not supported!");
    }

    IdNode typeDeclNode = (IdNode) node.getRhs();
    IdNode nameIdNode = (IdNode) node.getIdentifier();
    Symbol symbol = createVariableSymbolInScope(typeDeclNode, nameIdNode, this.currentScope());
    this.symbolTable.addSymbolNodeRelation(symbol, node, true);

    return null;
  }

  @Override
  public Void visit(LoopStmtNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Void visit(WhileLoopStmtNode node) {
    // visit expression
    node.getExpressionNode().accept(this);

    var whileScope = new Scope(scopeStack.peek());
    scopeStack.push(whileScope);
    node.getStmtNode().accept(this);
    scopeStack.pop();

    return null;
  }

  @Override
  public Void visit(CountingLoopStmtNode node) {
    // visit iterable expression
    node.getIterableIdNode().accept(this);

    // create loop scope
    Scope loopScope = new Scope(scopeStack.peek());

    // create loop variable
    Node typeIdNode = node.getTypeIdNode();
    Node varIdNode = node.getVarIdNode();
    createVariableSymbolInScope((IdNode) typeIdNode, (IdNode) varIdNode, loopScope);

    // create counter variable
    Node counterIdNode = node.getCounterIdNode();
    createVariableSymbolInScope(BuiltInType.intType, (IdNode) counterIdNode, loopScope);

    // visit stmt node of loop
    scopeStack.push(loopScope);
    node.getStmtNode().accept(this);
    scopeStack.pop();

    return null;
  }

  @Override
  public Void visit(ForLoopStmtNode node) {
    // visit iterable expression
    node.getIterableIdNode().accept(this);

    // create loop scope
    Scope loopScope = new Scope(scopeStack.peek());

    // create loop variable
    Node typeIdNode = node.getTypeIdNode();
    Node varIdNode = node.getVarIdNode();
    createVariableSymbolInScope((IdNode) typeIdNode, (IdNode) varIdNode, loopScope);

    // visit stmt node of loop
    scopeStack.push(loopScope);
    node.getStmtNode().accept(this);
    scopeStack.pop();

    return null;
  }

  // region ASTVisitor implementation for nodes unrelated to semantic analysis

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
  public Void visit(StringNode node) {
    return null;
  }

  @Override
  public Void visit(DotDefNode node) {
    // TODO: should check, that all identifiers actually refer to
    //  task definitions -> should be implemented with type checking
    for (Node stmt : node.getStmtNodes()) {
      stmt.accept(this);
    }
    return null;
  }

  @Override
  public Void visit(DotNodeStmtNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(DotIdList node) {
    // visit all stored IdNodes
    visitChildren(node);
    return null;
  }

  @Override
  public Void visit(EdgeRhsNode node) {
    return null;
  }

  @Override
  public Void visit(DotEdgeStmtNode node) {
    for (Node id : node.getIdLists()) {
      id.accept(this);
    }
    return null;
  }

  @Override
  public Void visit(EdgeOpNode node) {
    return null;
  }

  @Override
  public Void visit(BoolNode node) {
    return null;
  }
  // endregion
}
