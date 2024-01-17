package dsl.parser;

import antlr.main.DungeonDSLLexer;
import antlr.main.DungeonDSLParser;
import dsl.parser.ast.*;
import graph.taskdependencygraph.TaskEdge;
// CHECKSTYLE:ON: AvoidStarImport
import java.util.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

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
public class DungeonASTConverter implements antlr.main.DungeonDSLListener {

  Stack<Node> astStack;

  /** Constructor */
  public DungeonASTConverter() {
    astStack = new Stack<>();
  }

  public static Node getProgramAST(String program) {
    var stream = CharStreams.fromString(program);
    var lexer = new DungeonDSLLexer(stream);

    var tokenStream = new CommonTokenStream(lexer);
    var parser = new DungeonDSLParser(tokenStream);
    var programParseTree = parser.program();

    DungeonASTConverter astConverter = new DungeonASTConverter();
    return astConverter.walk(programParseTree);
  }

  /**
   * Walk the passed parseTree and create an AST from it
   *
   * @param parseTree The ParseTree to walk
   * @return Root Node of the AST.
   */
  public Node walk(ParseTree parseTree) {
    astStack = new Stack<>();
    ParseTreeWalker.DEFAULT.walk(this, parseTree);
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

    var programNode = new Node(Node.Type.Program, new ArrayList<Node>(nodes));
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
    assert varIdNode.type.equals(Node.Type.Identifier);

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
    assert counterIdNode.type.equals(Node.Type.Identifier);

    Node iterableNode = astStack.pop();

    Node varIdNode = astStack.pop();
    assert varIdNode.type.equals(Node.Type.Identifier);

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
    assert identifier.type == Node.Type.Identifier;

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
    assert identifier.type == Node.Type.Identifier;

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
    if (ctx.stmt_list() != null) {
      stmtList = astStack.pop();
    }

    var blockNode = new StmtBlockNode(stmtList);
    astStack.push(blockNode);
  }

  @Override
  public void enterReturn_stmt(DungeonDSLParser.Return_stmtContext ctx) {}

  @Override
  public void exitReturn_stmt(DungeonDSLParser.Return_stmtContext ctx) {
    // pop the inner statement
    assert astStack.size() > 0;
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
  public void enterStmt_list(DungeonDSLParser.Stmt_listContext ctx) {}

  @Override
  public void exitStmt_list(DungeonDSLParser.Stmt_listContext ctx) {
    // condense to actual list of stmt's
    if (ctx.stmt_list() == null) {
      // trivial stmt definition list (one stmt)
      var innerStmt = astStack.pop();

      var list = new ArrayList<Node>(1);
      list.add(innerStmt);

      var stmtList = new Node(Node.Type.StmtList, list);
      astStack.push(stmtList);
    } else {
      // rhs stmt list is on stack
      var rhsList = astStack.pop();
      assert (rhsList.type == Node.Type.StmtList);

      var leftStmt = astStack.pop();

      var childList = new ArrayList<Node>(rhsList.getChildren().size() + 1);
      childList.add(leftStmt);
      childList.addAll(rhsList.getChildren());

      var stmtList = new Node(Node.Type.StmtList, childList);
      astStack.push(stmtList);
    }
  }

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
  public void enterParam_def(DungeonDSLParser.Param_defContext ctx) {}

  @Override
  public void exitParam_def(DungeonDSLParser.Param_defContext ctx) {
    // topmost id on stack: id of parameter
    var id = astStack.pop();
    assert id.type == Node.Type.Identifier;

    // after that: type id
    var typeId = astStack.pop();
    assert typeId instanceof IdNode;

    var paramNode = new ParamDefNode(typeId, id);
    astStack.push(paramNode);
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
    // condense down to list of param def nodes
    if (ctx.param_def_list() == null) {
      // trivial parameter definition list
      var innerParamDef = astStack.pop();
      assert (innerParamDef.type == Node.Type.ParamDef);

      var list = new ArrayList<Node>(1);
      list.add(innerParamDef);

      var paramDefList = new Node(Node.Type.ParamDefList, list);
      astStack.push(paramDefList);
    } else {
      // rhs paramDefList is on stack
      var rhsList = astStack.pop();
      assert (rhsList.type == Node.Type.ParamDefList);

      var leftParamDef = astStack.pop();
      assert (leftParamDef.type == Node.Type.ParamDef);

      var childList = new ArrayList<Node>(rhsList.getChildren().size() + 1);
      childList.add(leftParamDef);
      childList.addAll(rhsList.getChildren());

      var paramDefList = new Node(Node.Type.ParamDefList, childList);
      astStack.push(paramDefList);
    }
  }

  @Override
  public void enterEntity_type_def(DungeonDSLParser.Entity_type_defContext ctx) {}

  @Override
  public void exitEntity_type_def(DungeonDSLParser.Entity_type_defContext ctx) {
    // if we have a component definition list, it will be on the stack
    var componentDefList = Node.NONE;
    if (ctx.component_def_list() != null) {
      componentDefList = astStack.pop();
      assert componentDefList.type == Node.Type.ComponentDefinitionList;
    }

    // id will be on the stack
    var idNode = astStack.pop();
    assert idNode.type == Node.Type.Identifier;

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
      assert propertyDefList.type == Node.Type.PropertyDefinitionList;
    }

    // id will be on the stack
    var idNode = astStack.pop();
    assert idNode.type == Node.Type.Identifier;

    var itemPrototypeDefinitionNode = new ItemPrototypeDefinitionNode(idNode, propertyDefList);
    astStack.push(itemPrototypeDefinitionNode);
  }

  @Override
  public void enterComponent_def_list(DungeonDSLParser.Component_def_listContext ctx) {}

  @Override
  public void exitComponent_def_list(DungeonDSLParser.Component_def_listContext ctx) {
    // TODO: add tests for this
    if (ctx.component_def_list() == null) {
      // trivial component definition list
      var innerComponentList = astStack.pop();
      assert (innerComponentList.type == Node.Type.AggregateValueDefinition);

      var list = new ArrayList<Node>(1);
      list.add(innerComponentList);

      var componentDefList = new Node(Node.Type.ComponentDefinitionList, list);
      astStack.push(componentDefList);
    } else {
      // rhs componentDefList is on stack
      var rhsList = astStack.pop();
      assert (rhsList.type == Node.Type.ComponentDefinitionList);

      var leftComponentDef = astStack.pop();
      assert (leftComponentDef.type == Node.Type.AggregateValueDefinition);

      var childList = new ArrayList<Node>(rhsList.getChildren().size() + 1);
      childList.add(leftComponentDef);
      childList.addAll(rhsList.getChildren());

      var componentDefList = new Node(Node.Type.ComponentDefinitionList, childList);
      astStack.push(componentDefList);
    }
  }

  @Override
  public void enterAggregate_value_def(DungeonDSLParser.Aggregate_value_defContext ctx) {}

  @Override
  public void exitAggregate_value_def(DungeonDSLParser.Aggregate_value_defContext ctx) {
    // if we have a propertyDefList, it will be on the stack
    var propertyDefListNode = Node.NONE;
    if (ctx.property_def_list() != null) {
      propertyDefListNode = astStack.pop();
      assert propertyDefListNode.type == Node.Type.PropertyDefinitionList;
    }

    // id of the component will be on the stack
    var idNode = astStack.pop();
    assert idNode.type == Node.Type.Identifier;

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
      assert (propertyDefList.type == Node.Type.PropertyDefinitionList);
    }

    // id on stack
    var id = astStack.pop();
    assert (id.type == Node.Type.Identifier);

    // type specifier (ID) on stack
    var typeSpecifier = astStack.pop();
    assert (typeSpecifier.type == Node.Type.Identifier);

    var objectDef = new ObjectDefNode(typeSpecifier, id, propertyDefList);
    astStack.push(objectDef);
  }

  @Override
  public void enterProperty_def_list(DungeonDSLParser.Property_def_listContext ctx) {}

  @Override
  public void exitProperty_def_list(DungeonDSLParser.Property_def_listContext ctx) {
    // TODO: add tests for this
    if (ctx.property_def_list() == null) {
      // trivial property definition
      var innerPropertyDef = astStack.pop();
      assert (innerPropertyDef.type == Node.Type.PropertyDefinition);

      var list = new ArrayList<Node>(1);
      list.add(innerPropertyDef);

      var propertyDefList = new Node(Node.Type.PropertyDefinitionList, list);
      astStack.push(propertyDefList);
    } else {
      // rhs propertyDefList is on stack
      var rhsList = astStack.pop();
      assert (rhsList.type == Node.Type.PropertyDefinitionList);

      var leftPropertyDef = astStack.pop();
      assert (leftPropertyDef.type == Node.Type.PropertyDefinition);

      var childList = new ArrayList<Node>(rhsList.getChildren().size() + 1);
      childList.add(leftPropertyDef);
      childList.addAll(rhsList.getChildren());

      var propertyDefList = new Node(Node.Type.PropertyDefinitionList, childList);
      astStack.push(propertyDefList);
    }
  }

  @Override
  public void enterProperty_def(DungeonDSLParser.Property_defContext ctx) {}

  @Override
  public void exitProperty_def(DungeonDSLParser.Property_defContext ctx) {
    // stmt on stack
    var stmtNode = astStack.pop();

    // ID (lhs) is on stack
    var id = astStack.pop();
    assert (id.type == Node.Type.Identifier);

    var propertyDefNode = new PropertyDefNode(id, stmtNode);
    astStack.push(propertyDefNode);
  }

  @Override
  public void enterFunc_call(DungeonDSLParser.Func_callContext ctx) {}

  @Override
  public void exitFunc_call(DungeonDSLParser.Func_callContext ctx) {

    // if there are parameters, a paramList will be on stack
    var paramList = Node.NONE;
    if (ctx.expression_list() != null) {
      paramList = astStack.pop();
      assert paramList.type == Node.Type.ParamList;
    }

    // function id will be on stack
    var funcId = astStack.pop();
    assert funcId.type == Node.Type.Identifier;

    var funcCallNode = new FuncCallNode(funcId, paramList);
    astStack.push(funcCallNode);
  }

  @Override
  public void enterExpression_list(DungeonDSLParser.Expression_listContext ctx) {}

  @Override
  public void exitExpression_list(DungeonDSLParser.Expression_listContext ctx) {
    if (ctx.expression_list() == null) {
      // trivial param
      var innerParam = astStack.pop();
      var list = new ArrayList<Node>(1);
      list.add(innerParam);

      var paramList = new Node(Node.Type.ParamList, list);
      astStack.push(paramList);
    } else {
      // rhs paramlist is on stack
      var rhsList = astStack.pop();
      assert (rhsList.type == Node.Type.ParamList);

      var leftParam = astStack.pop();
      var childList = new ArrayList<Node>(rhsList.getChildren().size() + 1);
      childList.add(leftParam);
      childList.addAll(rhsList.getChildren());

      var paramList = new Node(Node.Type.ParamList, childList);
      astStack.push(paramList);
    }
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
    assert expressionList.type == Node.Type.ParamList;

    Node listDefinitionNode = new ListDefinitionNode(expressionList);
    astStack.push(listDefinitionNode);
  }

  @Override
  public void enterSet_definition(DungeonDSLParser.Set_definitionContext ctx) {}

  @Override
  public void exitSet_definition(DungeonDSLParser.Set_definitionContext ctx) {
    // pop expression list
    Node expressionList = astStack.pop();
    assert expressionList.type == Node.Type.ParamList;

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
      assert (stmtList.type == Node.Type.DotStmtList);
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
      assert (attr_list.type == Node.Type.DotAttrList);
    }

    LinkedList<Node> ids = new LinkedList<>();

    // pop all DotEdgeRHS Nodes from the stack and add them to one list
    for (int i = 0; i < ctx.dot_edge_RHS().size(); i++) {
      var rhs = astStack.pop();
      assert (rhs.type == Node.Type.DotEdgeRHS);
      Node idNodeList = ((EdgeRhsNode) rhs).getIdNodeList();
      ids.addFirst(idNodeList);
    }

    // get the first identifier of the statement (left-hand-side)
    var lhsIdNodeList = astStack.pop();
    assert (lhsIdNodeList.type == Node.Type.DotIdList);
    ids.addFirst(lhsIdNodeList);

    var edgeStmtNode = new DotEdgeStmtNode(ids, attr_list);
    astStack.push(edgeStmtNode);
  }

  @Override
  public void enterDot_node_list(DungeonDSLParser.Dot_node_listContext ctx) {}

  @Override
  public void exitDot_node_list(DungeonDSLParser.Dot_node_listContext ctx) {
    Node nodeToPush;
    if (ctx.dot_node_list() != null) {
      Node rhsNodeList = astStack.pop();
      assert rhsNodeList.type.equals(Node.Type.DotIdList);
      List<Node> rhsChildren = rhsNodeList.getChildren();

      Node lhsIdNode = astStack.pop();
      assert lhsIdNode.type.equals(Node.Type.Identifier);

      ArrayList<Node> idNodes = new ArrayList<>(rhsChildren.size() + 1);
      idNodes.add(lhsIdNode);
      idNodes.addAll(rhsChildren);
      nodeToPush = new DotIdList(idNodes);
    } else {
      Node id = astStack.pop();
      nodeToPush = new DotIdList(List.of(id));
    }
    astStack.push(nodeToPush);
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
    Node rhsId = astStack.pop();
    Node lhsId = astStack.pop();
    var attrNode = new DotAttrNode(lhsId, rhsId);
    astStack.push(attrNode);
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
  public void visitErrorNode(ErrorNode node) {}

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {}

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {}

  // region dependency_type
  @Override
  public void enterDot_attr_dependency_type(DungeonDSLParser.Dot_attr_dependency_typeContext ctx) {}

  @Override
  public void exitDot_attr_dependency_type(DungeonDSLParser.Dot_attr_dependency_typeContext ctx) {
    var typeNode = astStack.pop();
    assert typeNode.type.equals(Node.Type.DotDependencyType);
    var attributeNode = new DotDependencyTypeAttrNode((DotDependencyTypeNode) typeNode);
    astStack.push(attributeNode);
  }

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
}
