package dsl.parser;

import dsl.antlr.DungeonDSLLexer;
import dsl.antlr.DungeonDSLParser;
import dsl.error.ErrorListener;
import dsl.error.ErrorStrategy;
import dsl.parser.ast.*;
import dsl.semanticanalysis.environment.IEnvironment;
import graph.taskdependencygraph.TaskEdge;
// CHECKSTYLE:ON: AvoidStarImport
import java.util.*;
import java.util.logging.Logger;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

// importing all required classes from symbolTable will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport

/**
 * This class converts the {@link ParseTree} created by the antlr parser into an AST. While walking
 * the parse tree, the astStack is used to combine multiple Nodes into more complex ones. This works
 * in a bottom-up fashion: in a specific exit-method (of the DungeonDSLListener interface) we use
 * the invariant, that we combined all child-nodes of the specific rule. Therefore, they are in
 * reverse order on the astStack and can be added as children to a {@link Node} (or a
 * specialization), representing the currently exited rule.
 */
// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class DungeonASTConverter implements dsl.antlr.DungeonDSLParserListener {

  private static Logger LOGGER = Logger.getLogger(DungeonASTConverter.class.getName());
  private List<String> ruleNames;
  private boolean errorMode = false;
  private Stack<ParserRuleContext> errorRuleStack;
  private HashMap<Token, ErrorListener.ErrorRecord> tokensErrorRecords;
  private HashSet<Token> offendingTokens;
  private HashMap<ParserRuleContext, List<ErrorListener.ErrorRecord>> lexerErrors;
  private HashMap<ParserRuleContext, List<TerminalNode>> rulesWithOffendingTerminalNodes;
  private DungeonErrorNodeConverter errorNodeConverter;

  CountingStack<Node> astStack;
  Integer previousAstStackFrameCount = 0;
  private boolean trace;

  /** Constructor */
  public DungeonASTConverter(List<String> parserRuleNames) {
    this();

    this.ruleNames = parserRuleNames;
  }

  /** Constructor */
  public DungeonASTConverter() {
    this.astStack = new CountingStack<>();
    this.ruleNames = new ArrayList<>();
    this.lexerErrors = new HashMap<>();
    this.rulesWithOffendingTerminalNodes = new HashMap<>();
    this.errorNodeConverter = new DungeonErrorNodeConverter();
  }

  public void setTrace(boolean trace) {
    this.trace = trace;
  }

  public static Node getProgramAST(String program, IEnvironment environment) {
    var stream = CharStreams.fromString(program);
    ErrorListener listener = new ErrorListener();
    var lexer = new DungeonDSLLexer(stream, environment);
    lexer.removeErrorListeners();
    lexer.addErrorListener(listener);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new DungeonDSLParser(tokenStream, environment);
    parser.removeErrorListeners();
    parser.addErrorListener(listener);
    parser.setErrorHandler(new ErrorStrategy(lexer.getVocabulary(), true, true));
    var programParseTree = parser.program();

    DungeonASTConverter astConverter =
        new DungeonASTConverter(Arrays.stream(parser.getRuleNames()).toList());
    return astConverter.walk(programParseTree, listener.getErrors());
  }

  /**
   * Walk the passed parseTree and create an AST from it
   *
   * @param parseTree The ParseTree to walk
   * @return Root Node of the AST.
   */
  public Node walk(ParseTree parseTree) {
    return this.walk(parseTree, new ArrayList<>());
  }

  // TODO: where to store the Errors encountered while converting the whole parsetree
  public Node walk(ParseTree parseTree, List<ErrorListener.ErrorRecord> errors) {
    this.offendingTokens = new HashSet<>();
    this.tokensErrorRecords = new HashMap<>();
    this.lexerErrors.clear();
    this.rulesWithOffendingTerminalNodes.clear();

    for (var error : errors) {
      var offendingSymbol = error.offendingSymbol();
      if (offendingSymbol instanceof CommonToken t) {
        // TODO: getting the parent of t would be a nice optimization!
        this.offendingTokens.add(t);
        this.tokensErrorRecords.put(t, error);
      } else if (error.exception() instanceof LexerNoViableAltException) { // lexer error!
        var ctx = (ParserRuleContext) parseTree;
        if (!this.lexerErrors.containsKey(ctx)) {
          this.lexerErrors.put(ctx, new ArrayList<>());
        }
        this.lexerErrors.get(parseTree).add(error);
      }
    }

    this.astStack = new CountingStack<>();
    this.errorRuleStack = new Stack<>();
    // ParseTreeWalker.DEFAULT.walk(this, parseTree);
    DungeonParseTreeWalker.DEFAULT.walk(this, parseTree);
    return astStack.peek();
  }

  @Override
  public void enterProgram(DungeonDSLParser.ProgramContext ctx) {}

  /**
   * Pops all remaining AST-Nodes from the stack (they will be in reverse order) and adds all as
   * children to the root node of the program
   *
   * @param ctx the parse tree
   */
  @Override
  public void exitProgram(DungeonDSLParser.ProgramContext ctx) {
    int symbolCount = astStack.size();
    LinkedList<Node> nodes = new LinkedList<>();
    for (int i = 0; i < symbolCount; i++) {
      var node = astStack.pop();
      nodes.addFirst(node);
    }

    // add lexer errors as nodes?

    var programNode = new Node(Node.Type.Program, new ArrayList<>(nodes));
    astStack.push(programNode);
  }

  @Override
  public void enterDefinition(DungeonDSLParser.DefinitionContext ctx) {}

  @Override
  public void exitDefinition(DungeonDSLParser.DefinitionContext ctx) {}

  @Override
  public void enterImport_unnamed(DungeonDSLParser.Import_unnamedContext ctx) {}

  @Override
  public void exitImport_unnamed(DungeonDSLParser.Import_unnamedContext ctx) {
    // pop id node
    Node idNode = this.astStack.pop();

    // pop path node
    Node pathNode = this.astStack.pop();

    Node importNode = new ImportNode(pathNode, idNode);
    this.astStack.push(importNode);
  }

  @Override
  public void enterImport_named(DungeonDSLParser.Import_namedContext ctx) {}

  @Override
  public void exitImport_named(DungeonDSLParser.Import_namedContext ctx) {
    // pop "as" id node
    Node asIdNode = this.astStack.pop();

    // pop id node
    Node idNode = this.astStack.pop();

    // pop path node
    Node pathNode = this.astStack.pop();

    Node importNode = new ImportNode(pathNode, idNode, asIdNode);
    this.astStack.push(importNode);
  }

  @Override
  public void enterFn_def(DungeonDSLParser.Fn_defContext ctx) {}

  @Override
  public void exitFn_def(DungeonDSLParser.Fn_defContext ctx) {
    // pop everything (depending on ctx) and create fnDefNode
    Node stmtBlock = Node.NONE;
    if (ctx.stmt_block() != null) {
      // no stmt list
      stmtBlock = astStack.pop();
    }

    Node retType = Node.NONE;
    if (ctx.ret_type_def() != null) {
      retType = astStack.pop();
    }

    Node paramDefList = Node.NONE;
    if (ctx.param_def_list() != null) {
      paramDefList = astStack.pop();
    }

    Node functionName = astStack.pop();

    var funcDefNode = new FuncDefNode(functionName, paramDefList, retType, stmtBlock);
    astStack.push(funcDefNode);
  }

  @Override
  public void enterStmt(DungeonDSLParser.StmtContext ctx) {}

  @Override
  public void exitStmt(DungeonDSLParser.StmtContext ctx) {
    // just let it bubble up, we don't need to store the information, that it is a stmt
  }

  @Override
  public void enterFor_loop(DungeonDSLParser.For_loopContext ctx) {}

  @Override
  public void exitFor_loop(DungeonDSLParser.For_loopContext ctx) {
    Node stmtNode = astStack.pop();

    Node iterableNode = astStack.pop();

    Node varIdNode = astStack.pop();
    assert errorMode() || varIdNode.type.equals(Node.Type.Identifier);

    Node varTypeIdNode = astStack.pop();

    ForLoopStmtNode loopStmtNode =
        new ForLoopStmtNode(varTypeIdNode, varIdNode, iterableNode, stmtNode);
    astStack.push(loopStmtNode);
  }

  @Override
  public void enterFor_loop_counting(DungeonDSLParser.For_loop_countingContext ctx) {}

  @Override
  public void exitFor_loop_counting(DungeonDSLParser.For_loop_countingContext ctx) {
    Node stmtNode = astStack.pop();

    Node counterIdNode = astStack.pop();
    assert errorMode() || counterIdNode.type.equals(Node.Type.Identifier);

    Node iterableNode = astStack.pop();

    Node varIdNode = astStack.pop();
    assert errorMode() || varIdNode.type.equals(Node.Type.Identifier);

    Node varTypeIdNode = astStack.pop();

    CountingLoopStmtNode loopStmtNode =
        new CountingLoopStmtNode(varTypeIdNode, varIdNode, iterableNode, counterIdNode, stmtNode);
    astStack.push(loopStmtNode);
  }

  @Override
  public void enterWhile_loop(DungeonDSLParser.While_loopContext ctx) {}

  @Override
  public void exitWhile_loop(DungeonDSLParser.While_loopContext ctx) {
    Node stmtNode = astStack.pop();
    Node expressionNode = astStack.pop();

    WhileLoopStmtNode loopStmtNode = new WhileLoopStmtNode(expressionNode, stmtNode);
    astStack.push(loopStmtNode);
  }

  @Override
  public void enterVar_decl_assignment(DungeonDSLParser.Var_decl_assignmentContext ctx) {}

  @Override
  public void exitVar_decl_assignment(DungeonDSLParser.Var_decl_assignmentContext ctx) {
    Node expression = astStack.pop();
    Node identifier = astStack.pop();
    assert errorMode() || identifier.type == Node.Type.Identifier;

    Node varDeclNode =
        new VarDeclNode(VarDeclNode.DeclType.assignmentDecl, (IdNode) identifier, expression);
    astStack.push(varDeclNode);
  }

  @Override
  public void enterVar_decl_type_decl(DungeonDSLParser.Var_decl_type_declContext ctx) {}

  @Override
  public void exitVar_decl_type_decl(DungeonDSLParser.Var_decl_type_declContext ctx) {
    Node typeDecl = astStack.pop();
    Node identifier = astStack.pop();
    assert errorMode() || identifier.type == Node.Type.Identifier;

    Node varDeclNode =
        new VarDeclNode(VarDeclNode.DeclType.typeDecl, (IdNode) identifier, typeDecl);
    astStack.push(varDeclNode);
  }

  @Override
  public void enterExpr_assignment(DungeonDSLParser.Expr_assignmentContext ctx) {}

  @Override
  public void exitExpr_assignment(DungeonDSLParser.Expr_assignmentContext ctx) {
    // pop the inner nodes
    Node expression = astStack.pop();
    Node assignee = astStack.pop();

    Node newExpression = new AssignmentNode(assignee, expression);
    astStack.push(newExpression);
  }

  @Override
  public void enterExpr_trivial(DungeonDSLParser.Expr_trivialContext ctx) {}

  @Override
  public void exitExpr_trivial(DungeonDSLParser.Expr_trivialContext ctx) {}

  @Override
  public void enterMethod_call_expression(DungeonDSLParser.Method_call_expressionContext ctx) {}

  @Override
  public void exitMethod_call_expression(DungeonDSLParser.Method_call_expressionContext ctx) {
    if (ctx.member_access_rhs() != null) {
      Node expressionRhs = astStack.pop();
      Node funcCall = astStack.pop();
      var memberAccessNode = new MemberAccessNode(funcCall, expressionRhs);
      astStack.push(memberAccessNode);
    }
  }

  @Override
  public void enterMember_access_expression(DungeonDSLParser.Member_access_expressionContext ctx) {}

  @Override
  public void exitMember_access_expression(DungeonDSLParser.Member_access_expressionContext ctx) {
    if (ctx.member_access_rhs() != null) {
      Node expressionRhs = astStack.pop();
      Node identifier = astStack.pop();
      var memberAccessNode = new MemberAccessNode(identifier, expressionRhs);
      astStack.push(memberAccessNode);
    }
  }

  @Override
  public void enterAssignee_func(DungeonDSLParser.Assignee_funcContext ctx) {}

  @Override
  public void exitAssignee_func(DungeonDSLParser.Assignee_funcContext ctx) {
    Node rhs = astStack.pop();
    Node funcCall = astStack.pop();
    Node assignee = new MemberAccessNode(funcCall, rhs);
    astStack.push(assignee);
  }

  @Override
  public void enterAssignee_member_access(DungeonDSLParser.Assignee_member_accessContext ctx) {}

  @Override
  public void exitAssignee_member_access(DungeonDSLParser.Assignee_member_accessContext ctx) {
    Node rhs = astStack.pop();
    Node identifier = astStack.pop();
    Node assignee = new MemberAccessNode(identifier, rhs);
    astStack.push(assignee);
  }

  @Override
  public void enterAssignee_identifier(DungeonDSLParser.Assignee_identifierContext ctx) {}

  @Override
  public void exitAssignee_identifier(DungeonDSLParser.Assignee_identifierContext ctx) {
    // just let it bubble up, nothing to do
  }

  @Override
  public void enterLogic_or(DungeonDSLParser.Logic_orContext ctx) {}

  @Override
  public void exitLogic_or(DungeonDSLParser.Logic_orContext ctx) {
    Node rhs = astStack.pop();
    Node lhs;
    Node logicOrNode = rhs;
    if (ctx.or != null) {
      lhs = astStack.pop();
      logicOrNode = new LogicOrNode(lhs, rhs);
    }
    astStack.push(logicOrNode);
  }

  @Override
  public void enterLogic_and(DungeonDSLParser.Logic_andContext ctx) {}

  @Override
  public void exitLogic_and(DungeonDSLParser.Logic_andContext ctx) {
    Node rhs = astStack.pop();
    Node lhs;
    Node logicAndNode = rhs;
    if (ctx.and != null) {
      lhs = astStack.pop();
      logicAndNode = new LogicAndNode(lhs, rhs);
    }
    astStack.push(logicAndNode);
  }

  @Override
  public void enterEquality(DungeonDSLParser.EqualityContext ctx) {}

  @Override
  public void exitEquality(DungeonDSLParser.EqualityContext ctx) {
    Node rhs = astStack.pop();
    Node lhs;
    Node equalityNode = rhs;
    if (ctx.eq != null) {
      lhs = astStack.pop();
      equalityNode = new EqualityNode(EqualityNode.EqualityType.equals, lhs, rhs);
    } else if (ctx.neq != null) {
      lhs = astStack.pop();
      equalityNode = new EqualityNode(EqualityNode.EqualityType.notEquals, lhs, rhs);
    }
    astStack.push(equalityNode);
  }

  @Override
  public void enterComparison(DungeonDSLParser.ComparisonContext ctx) {}

  @Override
  public void exitComparison(DungeonDSLParser.ComparisonContext ctx) {
    Node rhs = astStack.pop();
    Node lhs;
    Node comparisonNode = rhs;
    if (ctx.gt != null) {
      lhs = astStack.pop();
      comparisonNode = new ComparisonNode(ComparisonNode.ComparisonType.greaterThan, lhs, rhs);
    } else if (ctx.geq != null) {
      lhs = astStack.pop();
      comparisonNode = new ComparisonNode(ComparisonNode.ComparisonType.greaterEquals, lhs, rhs);
    } else if (ctx.lt != null) {
      lhs = astStack.pop();
      comparisonNode = new ComparisonNode(ComparisonNode.ComparisonType.lessThan, lhs, rhs);
    } else if (ctx.leq != null) {
      lhs = astStack.pop();
      comparisonNode = new ComparisonNode(ComparisonNode.ComparisonType.lessEquals, lhs, rhs);
    }
    astStack.push(comparisonNode);
  }

  @Override
  public void enterTerm(DungeonDSLParser.TermContext ctx) {}

  @Override
  public void exitTerm(DungeonDSLParser.TermContext ctx) {
    Node rhs = astStack.pop();
    Node lhs;
    Node termNode = rhs;
    if (ctx.minus != null) {
      lhs = astStack.pop();
      termNode = new TermNode(TermNode.TermType.minus, lhs, rhs);
    } else if (ctx.plus != null) {
      lhs = astStack.pop();
      termNode = new TermNode(TermNode.TermType.plus, lhs, rhs);
    }
    astStack.push(termNode);
  }

  @Override
  public void enterFactor(DungeonDSLParser.FactorContext ctx) {}

  @Override
  public void exitFactor(DungeonDSLParser.FactorContext ctx) {
    Node rhs = astStack.pop();
    Node lhs;
    Node factorNode = rhs;
    if (ctx.div != null) {
      lhs = astStack.pop();
      factorNode = new FactorNode(FactorNode.FactorType.divide, lhs, rhs);
    } else if (ctx.mult != null) {
      lhs = astStack.pop();
      factorNode = new FactorNode(FactorNode.FactorType.multiply, lhs, rhs);
    }
    astStack.push(factorNode);
  }

  @Override
  public void enterUnary(DungeonDSLParser.UnaryContext ctx) {}

  @Override
  public void exitUnary(DungeonDSLParser.UnaryContext ctx) {
    Node innerNode = astStack.pop();
    Node unaryNode = innerNode;
    if (ctx.bang != null) {
      unaryNode = new UnaryNode(UnaryNode.UnaryType.not, innerNode);
    } else if (ctx.minus != null) {
      unaryNode = new UnaryNode(UnaryNode.UnaryType.minus, innerNode);
    }
    astStack.push(unaryNode);
  }

  @Override
  public void enterStmt_block(DungeonDSLParser.Stmt_blockContext ctx) {}

  @Override
  public void exitStmt_block(DungeonDSLParser.Stmt_blockContext ctx) {
    var stmtList = Node.NONE;
    int listSize = ctx.stmt().size();
    var list = new ArrayList<>(Collections.nCopies(listSize, Node.NONE));
    for (int i = 0; i < listSize; i++) {
      // reverse order
      var stmt = astStack.pop();
      list.set(listSize - i - 1, stmt);
    }

    var blockNode = new StmtBlockNode(list);
    astStack.push(blockNode);
  }

  @Override
  public void enterReturn_stmt(DungeonDSLParser.Return_stmtContext ctx) {}

  @Override
  public void exitReturn_stmt(DungeonDSLParser.Return_stmtContext ctx) {
    // pop the inner statement
    assert errorMode() || astStack.size() > 0;
    var innerStmt = Node.NONE;
    if (ctx.expression() != null) {
      innerStmt = astStack.pop();
    }

    var returnStmt = new ReturnStmtNode(innerStmt);
    astStack.push(returnStmt);
  }

  @Override
  public void enterConditional_stmt(DungeonDSLParser.Conditional_stmtContext ctx) {}

  @Override
  public void exitConditional_stmt(DungeonDSLParser.Conditional_stmtContext ctx) {
    // check, whether we have an else stmt
    var elseStmt = Node.NONE;
    if (ctx.else_stmt() != null) {
      elseStmt = astStack.pop();
    }

    var stmt = astStack.pop();
    var condition = astStack.pop();

    var conditionalStmtNode = Node.NONE;
    if (elseStmt == Node.NONE) {
      // we have no else stmt
      conditionalStmtNode = new ConditionalStmtNodeIf(condition, stmt);
    } else {
      // we have an else stmt
      conditionalStmtNode = new ConditionalStmtNodeIfElse(condition, stmt, elseStmt);
    }
    astStack.push(conditionalStmtNode);
  }

  @Override
  public void enterElse_stmt(DungeonDSLParser.Else_stmtContext ctx) {}

  @Override
  public void exitElse_stmt(DungeonDSLParser.Else_stmtContext ctx) {}

  @Override
  public void enterRet_type_def(DungeonDSLParser.Ret_type_defContext ctx) {}

  @Override
  public void exitRet_type_def(DungeonDSLParser.Ret_type_defContext ctx) {
    Node retTypeId = astStack.pop();

    // remove the arrow
    astStack.pop();
    astStack.push(retTypeId);
  }

  @Override
  public void enterParam_def_correct(DungeonDSLParser.Param_def_correctContext ctx) {}

  @Override
  public void exitParam_def_correct(DungeonDSLParser.Param_def_correctContext ctx) {
    // topmost id on stack: id of parameter
    var id = astStack.pop();
    assert errorMode() || id.type == Node.Type.Identifier;

    // after that: type id
    var typeId = astStack.pop();
    assert errorMode() || typeId instanceof IdNode;

    var paramNode = new ParamDefNode(typeId, id);
    astStack.push(paramNode);
  }

  @Override
  public void enterParam_def_error(DungeonDSLParser.Param_def_errorContext ctx) {}

  @Override
  public void exitParam_def_error(DungeonDSLParser.Param_def_errorContext ctx) {
    // id could not be matched in this error alternative
    // get the offending symbol
    Node id = getOffendingSymbolNode(ctx);

    // type id
    var typeId = astStack.pop();
    assert errorMode() || typeId instanceof IdNode;

    var paramNode = new ParamDefNode(typeId, id);
    astStack.push(paramNode);
  }

  private Node getOffendingSymbolNode(ParserRuleContext ctx) {
    Node offendingSymbolNode = Node.NONE;
    var ctxStart = ctx.getStart().getStartIndex();
    ParserRuleContext ctxToSearch = ctx;
    HashSet<ParseTree> ctxsToSkip = new HashSet<>();
    ctxsToSkip.add(ctx);
    while (offendingSymbolNode.equals(Node.NONE) && ctxToSearch != null) {
      int count = ctxToSearch.getChildCount();
      for (int i = 0; i < count && offendingSymbolNode.equals(Node.NONE); i++) {
        var child = ctxToSearch.getChild(i);

        if (ctxsToSkip.contains(child)) {
          // skip the originally passed ctx on a pass, where we iterate over it's parent
          // this will be a problem, if we widen the search successively!
          // should somehow store the ctx's to skip
          ctxsToSkip.add(ctxToSearch);
          continue;
        }

        if (child
            instanceof
            TerminalNode
                    tn) { // TODO: what happens, if ctx does not contain a terminal node straight
          // away?
          var symbol = tn.getSymbol();
          if (this.offendingTokens.contains(symbol)) {
            if (symbol.getStartIndex() >= ctxStart) {
              var errorRecord = this.tokensErrorRecords.get(symbol);
              offendingSymbolNode = new ASTOffendingSymbol(tn, errorRecord);
            }
          }
        } else {
          Stack<ParseTree> s = new Stack<>();
          ParseTree curr = child;

          // algorithmus:
          // - wenn node ein terminal node ist, check, ob es einen hinter dem Startindex liegt und
          // ob es einen
          //   ErrorRecord dafür gibt
          // - wenn kein terminal node, dann von links nach rechts alle knoten durchgehen

          // Traverse the tree
          while (curr != null || !s.isEmpty()) {
            if (curr instanceof TerminalNode tn) {
              var symbol = tn.getSymbol();
              if (this.offendingTokens.contains(symbol)) {
                if (symbol.getStartIndex() >= ctxStart) {
                  var errorRecord = this.tokensErrorRecords.get(symbol);
                  offendingSymbolNode = new ASTOffendingSymbol(tn, errorRecord);
                  break;
                }
              }
            } else {
              for (int j = 0; j < curr.getChildCount(); j++) {
                var c = curr.getChild(curr.getChildCount() - 1 - j);
                s.push(c);
              }
            }
            if (!s.empty()) {
              curr = s.pop();
            } else {
              curr = null;
            }
          }
        }
      }
      ctxToSearch = ctxToSearch.getParent();
    }
    return offendingSymbolNode;
  }

  @Override
  public void enterMap_param_type(DungeonDSLParser.Map_param_typeContext ctx) {}

  @Override
  public void exitMap_param_type(DungeonDSLParser.Map_param_typeContext ctx) {
    Node rhsTypeNode = astStack.pop();
    // pop the arrow
    astStack.pop();
    Node lhsTypeNode = astStack.pop();
    MapTypeIdentifierNode mapTypeIdentifierNode =
        new MapTypeIdentifierNode((IdNode) lhsTypeNode, (IdNode) rhsTypeNode);
    astStack.push(mapTypeIdentifierNode);
  }

  @Override
  public void enterId_param_type(DungeonDSLParser.Id_param_typeContext ctx) {}

  @Override
  public void exitId_param_type(DungeonDSLParser.Id_param_typeContext ctx) {
    // nothing to do
  }

  @Override
  public void enterList_param_type(DungeonDSLParser.List_param_typeContext ctx) {}

  @Override
  public void exitList_param_type(DungeonDSLParser.List_param_typeContext ctx) {
    Node innerTypeNode = astStack.pop();
    ListTypeIdentifierNode listTypeIdentifierNode =
        new ListTypeIdentifierNode((IdNode) innerTypeNode);
    astStack.push(listTypeIdentifierNode);
  }

  @Override
  public void enterSet_param_type(DungeonDSLParser.Set_param_typeContext ctx) {}

  @Override
  public void exitSet_param_type(DungeonDSLParser.Set_param_typeContext ctx) {
    Node innerTypeNode = astStack.pop();
    SetTypeIdentifierNode setTypeIdentifierNode = new SetTypeIdentifierNode((IdNode) innerTypeNode);
    astStack.push(setTypeIdentifierNode);
  }

  @Override
  public void enterParam_def_list(DungeonDSLParser.Param_def_listContext ctx) {}

  @Override
  public void exitParam_def_list(DungeonDSLParser.Param_def_listContext ctx) {
    int listSize = ctx.param_def().size();
    var list = new ArrayList<>(Collections.nCopies(listSize, Node.NONE));
    for (int i = 0; i < listSize; i++) {
      // reverse order
      var paramDef = astStack.pop();
      list.set(listSize - i - 1, paramDef);
    }
    Node stmtList = new Node(Node.Type.ParamDefList, list);
    astStack.push(stmtList);
  }

  @Override
  public void enterEntity_type_def(DungeonDSLParser.Entity_type_defContext ctx) {}

  @Override
  public void exitEntity_type_def(DungeonDSLParser.Entity_type_defContext ctx) {
    // if we have a component definition list, it will be on the stack
    var componentDefList = Node.NONE;
    if (ctx.component_def_list() != null) {
      componentDefList = astStack.pop();
      assert errorMode() || componentDefList.type == Node.Type.ComponentDefinitionList;
    }

    // id will be on the stack
    var idNode = astStack.pop();
    assert errorMode() || idNode.type == Node.Type.Identifier;

    var prototypeDefinitionNode = new PrototypeDefinitionNode(idNode, componentDefList);
    astStack.push(prototypeDefinitionNode);
  }

  @Override
  public void enterItem_type_def(DungeonDSLParser.Item_type_defContext ctx) {}

  @Override
  public void exitItem_type_def(DungeonDSLParser.Item_type_defContext ctx) {
    // if we have a component definition list, it will be on the stack
    var propertyDefList = Node.NONE;
    if (ctx.property_def_list() != null) {
      propertyDefList = astStack.pop();
      assert errorMode() || propertyDefList.type == Node.Type.PropertyDefinitionList;
    }

    // id will be on the stack
    var idNode = astStack.pop();
    assert errorMode() || idNode.type == Node.Type.Identifier;

    var itemPrototypeDefinitionNode = new ItemPrototypeDefinitionNode(idNode, propertyDefList);
    astStack.push(itemPrototypeDefinitionNode);
  }

  @Override
  public void enterComponent_def_list(DungeonDSLParser.Component_def_listContext ctx) {}

  @Override
  public void exitComponent_def_list(DungeonDSLParser.Component_def_listContext ctx) {
    int listSize = ctx.aggregate_value_def().size();
    var list = new ArrayList<>(Collections.nCopies(listSize, Node.NONE));
    for (int i = 0; i < listSize; i++) {
      // reverse order
      var aggregateValueDef = astStack.pop();
      list.set(listSize - i - 1, aggregateValueDef);
    }
    var aggregateValueDefList = new Node(Node.Type.ComponentDefinitionList, list);
    astStack.push(aggregateValueDefList);
  }

  @Override
  public void enterAggregate_value_def(DungeonDSLParser.Aggregate_value_defContext ctx) {}

  @Override
  public void exitAggregate_value_def(DungeonDSLParser.Aggregate_value_defContext ctx) {
    // if we have a propertyDefList, it will be on the stack
    var propertyDefListNode = Node.NONE;
    if (ctx.property_def_list() != null) {
      propertyDefListNode = astStack.pop();
      assert errorMode() || propertyDefListNode.type == Node.Type.PropertyDefinitionList;
    }

    // id of the component will be on the stack
    var idNode = astStack.pop();
    assert errorMode() || idNode.type == Node.Type.Identifier;

    var componentDefinitionNode = new AggregateValueDefinitionNode(idNode, propertyDefListNode);
    astStack.push(componentDefinitionNode);
  }

  @Override
  public void enterObject_def(DungeonDSLParser.Object_defContext ctx) {}

  @Override
  public void exitObject_def(DungeonDSLParser.Object_defContext ctx) {
    var propertyDefList = Node.NONE;
    if (ctx.property_def_list() != null) {
      propertyDefList = astStack.pop();
      assert errorMode() || (propertyDefList.type == Node.Type.PropertyDefinitionList);
    }

    // id on stack
    var id = astStack.pop();
    assert errorMode() || (id.type == Node.Type.Identifier);

    // type specifier (ID) on stack
    var typeSpecifier = astStack.pop();
    assert errorMode() || (typeSpecifier.type == Node.Type.Identifier);

    var objectDef = new ObjectDefNode(typeSpecifier, id, propertyDefList);
    astStack.push(objectDef);
  }

  @Override
  public void enterProperty_def_list(DungeonDSLParser.Property_def_listContext ctx) {}

  @Override
  public void exitProperty_def_list(DungeonDSLParser.Property_def_listContext ctx) {
    int listSize = ctx.property_def().size();
    var list = new ArrayList<>(Collections.nCopies(listSize, Node.NONE));
    for (int i = 0; i < listSize; i++) {
      // reverse order
      var propertyDef = astStack.pop();
      list.set(listSize - i - 1, propertyDef);
    }
    var propertyDefList = new Node(Node.Type.PropertyDefinitionList, list);
    astStack.push(propertyDefList);
  }

  @Override
  public void enterProperty_def_correct(DungeonDSLParser.Property_def_correctContext ctx) {}

  @Override
  public void exitProperty_def_correct(DungeonDSLParser.Property_def_correctContext ctx) {
    // stmt on stack
    var stmtNode = astStack.pop();

    // ID (lhs) is on stack
    var id = astStack.pop();
    assert errorMode() || (id.type == Node.Type.Identifier);

    var propertyDefNode = new PropertyDefNode(id, stmtNode);
    astStack.push(propertyDefNode);
  }

  @Override
  public void enterProperty_def_error(DungeonDSLParser.Property_def_errorContext ctx) {}

  @Override
  public void exitProperty_def_error(DungeonDSLParser.Property_def_errorContext ctx) {
    // expression could not be matched in this error alternative
    // get the offending symbol
    Node expression = getOffendingSymbolNode(ctx);

    // id on lhs
    var typeId = astStack.pop();
    assert errorMode() || typeId instanceof IdNode;

    var paramNode = new ParamDefNode(typeId, expression);
    astStack.push(paramNode);
  }

  @Override
  public void enterFunc_call(DungeonDSLParser.Func_callContext ctx) {}

  @Override
  public void exitFunc_call(DungeonDSLParser.Func_callContext ctx) {

    // if there are parameters, a paramList will be on stack
    var paramList = Node.NONE;
    if (ctx.expression_list() != null) {
      paramList = astStack.pop();
      assert errorMode() || paramList.type == Node.Type.ExpressionList;
    }

    // function id will be on stack
    var funcId = astStack.pop();
    assert errorMode() || funcId.type == Node.Type.Identifier;

    var funcCallNode = new FuncCallNode(funcId, paramList);
    astStack.push(funcCallNode);
  }

  @Override
  public void enterExpression_list(DungeonDSLParser.Expression_listContext ctx) {}

  @Override
  public void exitExpression_list(DungeonDSLParser.Expression_listContext ctx) {
    int listSize = ctx.expression().size();
    var list = new ArrayList<>(Collections.nCopies(listSize, Node.NONE));
    for (int i = 0; i < listSize; i++) {
      // reverse order
      var expression = astStack.pop();
      list.set(listSize - i - 1, expression);
    }
    var expressionList = new Node(Node.Type.ExpressionList, list);
    astStack.push(expressionList);
  }

  @Override
  public void enterGrouped_expression(DungeonDSLParser.Grouped_expressionContext ctx) {}

  @Override
  public void exitGrouped_expression(DungeonDSLParser.Grouped_expressionContext ctx) {
    Node innerExpression = astStack.pop();
    ArrayList<Node> list = new ArrayList<>();
    list.add(innerExpression);
    Node groupedExpression = new Node(Node.Type.GroupedExpression, list);
    astStack.push(groupedExpression);
  }

  @Override
  public void enterList_definition(DungeonDSLParser.List_definitionContext ctx) {}

  @Override
  public void exitList_definition(DungeonDSLParser.List_definitionContext ctx) {
    // pop expression list
    Node expressionList = astStack.pop();
    assert errorMode() || expressionList.type == Node.Type.ExpressionList;

    Node listDefinitionNode = new ListDefinitionNode(expressionList);
    astStack.push(listDefinitionNode);
  }

  @Override
  public void enterSet_definition(DungeonDSLParser.Set_definitionContext ctx) {}

  @Override
  public void exitSet_definition(DungeonDSLParser.Set_definitionContext ctx) {
    // pop expression list
    Node expressionList = astStack.pop();
    assert errorMode() || expressionList.type == Node.Type.ExpressionList;

    Node setDefinitionNode = new SetDefinitionNode(expressionList);
    astStack.push(setDefinitionNode);
  }

  @Override
  public void enterPrimary(DungeonDSLParser.PrimaryContext ctx) {}

  @Override
  public void exitPrimary(DungeonDSLParser.PrimaryContext ctx) {
    if (ctx.member_access_rhs() != null) {
      var rhsExpression = astStack.pop();
      var lhs = astStack.pop();
      var memberAccess = new MemberAccessNode(lhs, rhsExpression);
      astStack.push(memberAccess);
    }
  }

  @Override
  public void enterId(DungeonDSLParser.IdContext ctx) {}

  @Override
  public void exitId(DungeonDSLParser.IdContext ctx) {

    Node node = Node.NONE;
    // as we enter this rule, the Token was matched as an `id`, so we should
    // convert from the concrete Token Type (which represents a keyword in the
    // language) to the identifier representation of the keyword
    if (ctx.COUNT() != null
        || ctx.GRAPH() != null
        || ctx.TYPE() != null
        || ctx.WHILE() != null
        || ctx.dependency_type() != null) {
      var symbol = ctx.getStart();
      var text = ctx.getText();
      SourceFileReference sfr =
          new SourceFileReference(symbol.getLine(), symbol.getCharPositionInLine());
      node = new IdNode(text, sfr);
    }
    if (ctx.dependency_type() != null) {
      // we keep the kind of dependency_type in this case and add it as a child of
      // the idNode
      var inner = astStack.pop();
      node.addChild(inner);
    }
    // push the new node onto the stack
    if (!node.equals(Node.NONE)) astStack.push(node);
  }

  @Override
  public void enterId_no_type(DungeonDSLParser.Id_no_typeContext ctx) {}

  @Override
  public void exitId_no_type(DungeonDSLParser.Id_no_typeContext ctx) {
    Node node = Node.NONE;
    // as we enter this rule, the Token was matched as an `id`, so we should
    // convert from the concrete Token Type (which represents a keyword in the
    // language) to the identifier representation of the keyword
    if (ctx.COUNT() != null
        || ctx.GRAPH() != null
        || ctx.TYPE() != null
        || ctx.WHILE() != null
        || ctx.dependency_type() != null) {
      var symbol = ctx.getStart();
      var text = ctx.getText();
      SourceFileReference sfr =
          new SourceFileReference(symbol.getLine(), symbol.getCharPositionInLine());
      node = new IdNode(text, sfr);
    }
    if (ctx.dependency_type() != null) {
      // we keep the kind of dependency_type in this case and add it as a child of
      // the idNode
      var inner = astStack.pop();
      node.addChild(inner);
    }
    // push the new node onto the stack
    if (!node.equals(Node.NONE)) astStack.push(node);
  }

  @Override
  public void enterDot_def(DungeonDSLParser.Dot_defContext ctx) {}

  /**
   * Adds the Node, representing the dot_stmt_list of the dot definition, as child to a {@link
   * DotDefNode}. Validates consistency of all edge operators with the type of the defined graph
   * ('graph' or 'digraph').
   *
   * @param ctx the parse tree
   */
  @Override
  public void exitDot_def(DungeonDSLParser.Dot_defContext ctx) {
    // if dot_stmt_list is not empty, it will be on stack
    Node stmtList = Node.NONE;
    if (ctx.dot_stmt_list() != null) {
      stmtList = astStack.pop();
      assert errorMode() || (stmtList.type == Node.Type.DotStmtList);
    }

    // graph ID will be on stack
    Node idNode = astStack.pop();

    // create dotDefNode and directly add stmts as list
    DotDefNode dotDef = new DotDefNode(idNode, stmtList.getChildren());
    astStack.push(dotDef);
  }

  @Override
  public void enterDot_stmt_list(DungeonDSLParser.Dot_stmt_listContext ctx) {}

  @Override
  public void exitDot_stmt_list(DungeonDSLParser.Dot_stmt_listContext ctx) {
    int listSize = ctx.dot_stmt().size();
    var list = new ArrayList<>(Collections.nCopies(listSize, Node.NONE));
    for (int i = 0; i < listSize; i++) {
      // reverse order
      var dotStmt = astStack.pop();
      list.set(listSize - i - 1, dotStmt);
    }
    var dotStmtList = new Node(Node.Type.DotStmtList, list);
    astStack.push(dotStmtList);

    /*
    // if there is a rhs dot_stmt_list, it will be on top of stack
    Node rhsStmtList = Node.NONE;
    if (ctx.dot_stmt_list() != null) {
      rhsStmtList = astStack.pop();
    }

    // lhsStmt will be on top of stack
    Node lhsStmt = astStack.pop();

    // flatten list, condense all stmt's to one list
    ArrayList<Node> stmtListChildren = new ArrayList<>(rhsStmtList.getChildren().size() + 1);
    stmtListChildren.add(lhsStmt);
    stmtListChildren.addAll(rhsStmtList.getChildren());

    Node stmtListNode = new Node(Node.Type.DotStmtList, stmtListChildren);
    astStack.push(stmtListNode);
     */
  }

  @Override
  public void enterDot_stmt(DungeonDSLParser.Dot_stmtContext ctx) {}

  @Override
  public void exitDot_stmt(DungeonDSLParser.Dot_stmtContext ctx) {
    // just let it bubble up, no need to simplify
  }

  @Override
  public void enterDot_edge_stmt(DungeonDSLParser.Dot_edge_stmtContext ctx) {}

  @Override
  public void exitDot_edge_stmt(DungeonDSLParser.Dot_edge_stmtContext ctx) {
    // if the ctx contains a dot_attr_list, the corresponding Node will
    // be on the astStack
    var attr_list = Node.NONE;
    if (ctx.dot_attr_list() != null) {
      attr_list = astStack.pop();
      assert errorMode() || (attr_list.type == Node.Type.DotAttrList);
    }

    LinkedList<Node> ids = new LinkedList<>();

    // pop all DotEdgeRHS Nodes from the stack and add them to one list
    for (int i = 0; i < ctx.dot_edge_RHS().size(); i++) {
      var rhs = astStack.pop();
      assert errorMode() || (rhs.type == Node.Type.DotEdgeRHS);
      Node idNodeList = ((EdgeRhsNode) rhs).getIdNodeList();
      ids.addFirst(idNodeList);
    }

    // get the first identifier of the statement (left-hand-side)
    var lhsIdNodeList = astStack.pop();
    assert errorMode() || (lhsIdNodeList.type == Node.Type.DotIdList);
    ids.addFirst(lhsIdNodeList);

    var edgeStmtNode = new DotEdgeStmtNode(ids, attr_list);
    astStack.push(edgeStmtNode);
  }

  @Override
  public void enterDot_node_list(DungeonDSLParser.Dot_node_listContext ctx) {}

  @Override
  public void exitDot_node_list(DungeonDSLParser.Dot_node_listContext ctx) {
    int listSize = ctx.id().size();
    var list = new ArrayList<>(Collections.nCopies(listSize, Node.NONE));
    for (int i = 0; i < listSize; i++) {
      // reverse order
      var id = astStack.pop();
      list.set(listSize - i - 1, id);
    }
    var dotNodeList = new DotIdList(list);
    astStack.push(dotNodeList);
  }

  @Override
  public void enterDot_edge_RHS(DungeonDSLParser.Dot_edge_RHSContext ctx) {}

  @Override
  public void exitDot_edge_RHS(DungeonDSLParser.Dot_edge_RHSContext ctx) {
    // ID will be identifier on stack
    var idNode = astStack.pop();

    // edge_op will be on stack
    var edgeOp = astStack.pop();

    var edgeRhs = new EdgeRhsNode(edgeOp, idNode);
    astStack.push(edgeRhs);
  }

  @Override
  public void enterDot_node_stmt(DungeonDSLParser.Dot_node_stmtContext ctx) {}

  @Override
  public void exitDot_node_stmt(DungeonDSLParser.Dot_node_stmtContext ctx) {
    Node attrList = Node.NONE;
    if (ctx.dot_attr_list() != null) {
      attrList = astStack.pop();
    }

    Node id = astStack.pop();

    Node nodeStmtNode = new DotNodeStmtNode(id, attrList);
    astStack.push(nodeStmtNode);
  }

  @Override
  public void enterDot_attr_list(DungeonDSLParser.Dot_attr_listContext ctx) {}

  @Override
  public void exitDot_attr_list(DungeonDSLParser.Dot_attr_listContext ctx) {
    LinkedList<Node> attrNodes = new LinkedList<>();
    for (int i = 0; i < ctx.dot_attr().size(); i++) {
      Node attr = astStack.pop();
      attrNodes.addFirst(attr);
    }

    Node attrListNode = new DotAttrListNode(attrNodes);
    astStack.push(attrListNode);
  }

  @Override
  public void enterDot_attr_id(DungeonDSLParser.Dot_attr_idContext ctx) {}

  @Override
  public void exitDot_attr_id(DungeonDSLParser.Dot_attr_idContext ctx) {
    IdNode rhsId = (IdNode) astStack.pop();
    IdNode lhsId = (IdNode) astStack.pop();
    if (lhsId.getName().equals("type") && rhsId.getChild(0).type == Node.Type.DotDependencyType) {
      var attributeNode = new DotDependencyTypeAttrNode((DotDependencyTypeNode) rhsId.getChild(0));
      astStack.push(attributeNode);
    } else {
      var attrNode = new DotAttrNode(lhsId, rhsId);
      astStack.push(attrNode);
    }
  }

  private SourceFileReference getSourceFileReference(TerminalNode node) {
    var symbol = node.getSymbol();
    var line = symbol.getLine();
    var column = symbol.getCharPositionInLine();
    return new SourceFileReference(line, column);
  }

  /**
   * Convert the relevant antlr terminal nodes into {@link Node} objects for further usage by this
   * converter.
   *
   * @param node the terminal node
   */
  @Override
  public void visitTerminal(TerminalNode node) {
    var nodeType = node.getSymbol().getType();
    if (nodeType == DungeonDSLLexer.ID) {
      var idNode = new IdNode(node.getText(), getSourceFileReference(node));
      astStack.push(idNode);
    } else if (nodeType == DungeonDSLLexer.TYPE_ID) {
      var idNode = new IdNode(node.getText(), getSourceFileReference(node));
      astStack.push(idNode);
    } else if (nodeType == DungeonDSLLexer.ARROW) {
      var arrowNode = new Node(Node.Type.Arrow, getSourceFileReference(node));
      astStack.push(arrowNode);
    } else if (nodeType == DungeonDSLLexer.DOUBLE_LINE) {
      var doubleLineNode = new Node(Node.Type.DoubleLine, getSourceFileReference(node));
      astStack.push(doubleLineNode);
    } else if (nodeType == DungeonDSLLexer.STRING_LITERAL) {
      // TODO: add test for this
      String value = node.getText();

      // trim leading and trailing quotes
      String trimmedValue = value.subSequence(1, value.length() - 1).toString();

      // escape sequences
      String escapedValue = trimmedValue.translateEscapes();

      var stringNode = new StringNode(escapedValue, getSourceFileReference(node));
      astStack.push(stringNode);
    } else if (nodeType == DungeonDSLLexer.NUM_DEC) {
      // TODO: add test for this
      float value = Float.parseFloat(node.getText());
      var numNode = new DecNumNode(value, getSourceFileReference(node));
      astStack.push(numNode);
    } else if (nodeType == DungeonDSLLexer.NUM) {
      // TODO: add test for this
      int value = Integer.parseInt(node.getText());
      var numNode = new NumNode(value, getSourceFileReference(node));
      astStack.push(numNode);
    } else if (nodeType == DungeonDSLLexer.TRUE) {
      var boolNode = new BoolNode(true, getSourceFileReference(node));
      astStack.push(boolNode);
    } else if (nodeType == DungeonDSLLexer.FALSE) {
      var boolNode = new BoolNode(false, getSourceFileReference(node));
      astStack.push(boolNode);
    }
  }

  @Override
  public void visitErrorNode(ErrorNode node) {
    var parent = node.getParent();
    var text = node.getText();
    var symbol = node.getSymbol();
    String msg =
        String.format(
            "Visitting Error node, parent: '%s', text: '%s', symbol: '%s'", parent, text, symbol);
    LOGGER.warning(msg);

    // the parent of this node is responsible for clearing up the astStack!
    if (this.errorRuleStack.empty() || this.errorRuleStack.peek() != parent) {
      this.errorRuleStack.push((ParserRuleContext) parent);
    }

    var offendingSymbol = node.getSymbol();
    ErrorListener.ErrorRecord errorRecord = null;
    if (this.offendingTokens.contains(offendingSymbol)) {
      errorRecord = this.tokensErrorRecords.get(offendingSymbol);
    }

    var errorNode = new ASTErrorNode(node, errorRecord);
    astStack.push(errorNode);
  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
    // Note: this is executed before specific enter
    String ruleName = getRuleName(ctx);
    if (trace) {
      String msg = String.format("Entering rule '%s'", ruleName);
      LOGGER.info(msg);
    }

    this.astStack.pushCounter(ruleName);

    List<TerminalNode> offendingChildren = new ArrayList<>();
    var childCount = ctx.getChildCount();
    // TODO: this could be optimized, so that the converter stores offending symbols indexed on
    //  their lines / sourcefile ranges and checks, whether
    //  the current rule contains these ranges
    for (int i = 0; i < childCount; i++) {
      var child = ctx.getChild(i);
      if (child instanceof TerminalNodeImpl terminalNode) {
        if (offendingTokens.contains(terminalNode.symbol)) {
          offendingChildren.add(terminalNode);
          terminalNode.getSourceInterval();
        }
      }
    }

    if (ctx.exception != null) {
      LOGGER.warning("Rule context contains exception: " + ctx.exception + "; pushing error ctx!");
      this.errorRuleStack.push(ctx);
    }
  }

  public boolean preExitEveryRule(ParserRuleContext ctx) {
    if (trace) {
      String rulename = getRuleName(ctx);
      String msg = String.format("Pre exit rule '%s'", rulename);
      LOGGER.info(msg);
    }

    if (ctx.exception != null) {
      LOGGER.warning("Rule context contains exception: " + ctx.exception);
    }

    var errorNode = handleParserError(ctx);
    if (!errorNode.equals(Node.NONE)) {
      astStack.push(errorNode);
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    // TODO: use for error handling in order to add error information?
    handleLexerError(ctx);

    this.previousAstStackFrameCount = this.astStack.popCounter();
    this.astStack.getCurrentCounter().add(previousAstStackFrameCount);

    // Note: this is executed after specific exit
    if (trace) {
      String rulename = getRuleName(ctx);
      String msg = String.format("Exiting rule '%s'", rulename);
      LOGGER.info(msg);
    }
  }

  // region dependency_type
  @Override
  public void enterDt_sequence(DungeonDSLParser.Dt_sequenceContext ctx) {}

  @Override
  public void exitDt_sequence(DungeonDSLParser.Dt_sequenceContext ctx) {
    var text = ctx.getText();
    astStack.push(new DotDependencyTypeNode(TaskEdge.Type.sequence, text));
  }

  @Override
  public void enterDt_subtask_mandatory(DungeonDSLParser.Dt_subtask_mandatoryContext ctx) {}

  @Override
  public void exitDt_subtask_mandatory(DungeonDSLParser.Dt_subtask_mandatoryContext ctx) {
    var text = ctx.getText();
    astStack.push(new DotDependencyTypeNode(TaskEdge.Type.subtask_mandatory, text));
  }

  @Override
  public void enterDt_subtask_optional(DungeonDSLParser.Dt_subtask_optionalContext ctx) {}

  @Override
  public void exitDt_subtask_optional(DungeonDSLParser.Dt_subtask_optionalContext ctx) {
    var text = ctx.getText();
    astStack.push(new DotDependencyTypeNode(TaskEdge.Type.subtask_optional, text));
  }

  @Override
  public void enterDt_conditional_correct(DungeonDSLParser.Dt_conditional_correctContext ctx) {}

  @Override
  public void exitDt_conditional_correct(DungeonDSLParser.Dt_conditional_correctContext ctx) {
    var text = ctx.getText();
    astStack.push(new DotDependencyTypeNode(TaskEdge.Type.conditional_correct, text));
  }

  @Override
  public void enterDt_conditional_false(DungeonDSLParser.Dt_conditional_falseContext ctx) {}

  @Override
  public void exitDt_conditional_false(DungeonDSLParser.Dt_conditional_falseContext ctx) {
    var text = ctx.getText();
    astStack.push(new DotDependencyTypeNode(TaskEdge.Type.conditional_false, text));
  }

  @Override
  public void enterDt_sequence_and(DungeonDSLParser.Dt_sequence_andContext ctx) {}

  @Override
  public void exitDt_sequence_and(DungeonDSLParser.Dt_sequence_andContext ctx) {
    var text = ctx.getText();
    astStack.push(new DotDependencyTypeNode(TaskEdge.Type.sequence_and, text));
  }

  @Override
  public void enterDt_sequence_or(DungeonDSLParser.Dt_sequence_orContext ctx) {}

  @Override
  public void exitDt_sequence_or(DungeonDSLParser.Dt_sequence_orContext ctx) {
    var text = ctx.getText();
    astStack.push(new DotDependencyTypeNode(TaskEdge.Type.sequence_or, text));
  }

  // endregion

  private String getRuleName(ParserRuleContext ctx) {
    int index = ctx.getRuleIndex();
    if (index >= this.ruleNames.size()) {
      return "[RULE INDEX OUT OF BOUNDS OF RULE NAME LIST]";
    } else {
      return this.ruleNames.get(index);
    }
  }

  private boolean errorMode() {
    return !this.errorRuleStack.empty();
  }

  private void handleLexerError(ParserRuleContext ctx) {
    if (this.lexerErrors.containsKey(ctx)) {
      LOGGER.info("Handling lexer error");
      // pop current top level node from astStack
      var parentNode = astStack.pop();
      try {
        // put all lexer errors in error nodes
        var errors = this.lexerErrors.get(ctx);
        for (var error : errors) {
          ASTLexerErrorNode errorNode = new ASTLexerErrorNode(error);
          parentNode.addChild(errorNode);
        }
      } catch (Exception ex) {
        ;
      }
      astStack.push(parentNode);
    }
  }

  private Node handleParserError(ParserRuleContext ctx) {
    Node nodeToReturn = Node.NONE;
    if (!this.errorRuleStack.empty() && this.errorRuleStack.peek().equals(ctx)) {
      LOGGER.info("Handling error mode");
      int currentASTStackCount = this.astStack.getCurrentCount();
      var list = new ArrayList<>(Collections.nCopies(currentASTStackCount, Node.NONE));
      for (int i = 0; i < currentASTStackCount; i++) {
        // reverse order
        var node = astStack.pop();
        list.set(currentASTStackCount - i - 1, node);
      }

      // add all nodes under node
      var errorNode = this.errorNodeConverter.createErrorNode(ctx, list);

      if (ctx.exception != null) {
        var record = ErrorListener.ErrorRecord.fromRecognitionException(ctx.exception);
        errorNode.setErrorRecord(record);
      }

      if (!errorNode.hasErrorRecord()) {
        // try to get it
        var offendingSymbolNode = getOffendingSymbolNode(ctx);
        if (offendingSymbolNode.equals(Node.NONE)) {
          // TODO: handle
        } else {
          var offendingSymbol = ((ASTOffendingSymbol) offendingSymbolNode).getOffendingTerminal();
          var record = this.tokensErrorRecords.get(offendingSymbol.getSymbol());
          errorNode.setErrorRecord(record);
        }
      }

      // pop current ctx from error rule stack
      this.errorRuleStack.pop();

      nodeToReturn = errorNode;
    }
    return nodeToReturn;
  }
}
