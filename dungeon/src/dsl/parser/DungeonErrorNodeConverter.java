package dsl.parser;

import dsl.antlr.DungeonDSLParser;
import dsl.antlr.DungeonDSLParserVisitor;
import dsl.parser.ast.*;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class DungeonErrorNodeConverter implements DungeonDSLParserVisitor<Node> {

  private ArrayList<Node> childNodes = new ArrayList<>();

  public Node createErrorNode(ParserRuleContext ctx, List<Node> errorNodes) {
    this.childNodes = new ArrayList<>(errorNodes);
    var node = ctx.accept(this);
    this.childNodes = new ArrayList<>();
    return node;
  }

  @Override
  public Node visitProgram(DungeonDSLParser.ProgramContext ctx) {
    return new Node(Node.Type.Program, this.childNodes);
  }

  @Override
  public Node visitDefinition(DungeonDSLParser.DefinitionContext ctx) {
    return genericErrorNode(this.childNodes);
  }

  @Override
  public Node visitImport_unnamed(DungeonDSLParser.Import_unnamedContext ctx) {
    var node = new ImportNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitImport_named(DungeonDSLParser.Import_namedContext ctx) {
    var node = new ImportNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitFn_def(DungeonDSLParser.Fn_defContext ctx) {
    var node = new FuncDefNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitStmt(DungeonDSLParser.StmtContext ctx) {
    return new Node(Node.Type.Stmt, this.childNodes);
  }

  @Override
  public Node visitFor_loop(DungeonDSLParser.For_loopContext ctx) {
    var node = new ForLoopStmtNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitFor_loop_counting(DungeonDSLParser.For_loop_countingContext ctx) {
    var node = new ForLoopStmtNode(LoopStmtNode.LoopType.countingForLoop);
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitWhile_loop(DungeonDSLParser.While_loopContext ctx) {
    var node = new ForLoopStmtNode(LoopStmtNode.LoopType.whileLoop);
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitVar_decl_assignment(DungeonDSLParser.Var_decl_assignmentContext ctx) {
    var node = new VarDeclNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitVar_decl_type_decl(DungeonDSLParser.Var_decl_type_declContext ctx) {
    var node = new VarDeclNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitExpr_assignment(DungeonDSLParser.Expr_assignmentContext ctx) {
    return genericErrorNode(this.childNodes);
  }

  @Override
  public Node visitExpr_trivial(DungeonDSLParser.Expr_trivialContext ctx) {
    return genericErrorNode(this.childNodes);
  }

  @Override
  public Node visitMethod_call_expression(DungeonDSLParser.Method_call_expressionContext ctx) {
    var node = new FuncCallNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitMember_access_expression(DungeonDSLParser.Member_access_expressionContext ctx) {
    var node = new MemberAccessNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitAssignee_func(DungeonDSLParser.Assignee_funcContext ctx) {
    var node = new MemberAccessNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitAssignee_member_access(DungeonDSLParser.Assignee_member_accessContext ctx) {
    var node = new MemberAccessNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitAssignee_identifier(DungeonDSLParser.Assignee_identifierContext ctx) {
    var node = new IdNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitLogic_or(DungeonDSLParser.Logic_orContext ctx) {
    var node = new LogicOrNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitLogic_and(DungeonDSLParser.Logic_andContext ctx) {
    var node = new LogicAndNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitEquality(DungeonDSLParser.EqualityContext ctx) {
    var node = new EqualityNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitComparison(DungeonDSLParser.ComparisonContext ctx) {
    var node = new ComparisonNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitTerm(DungeonDSLParser.TermContext ctx) {
    var node = new TermNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitFactor(DungeonDSLParser.FactorContext ctx) {
    var node = new FactorNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitUnary(DungeonDSLParser.UnaryContext ctx) {
    var node = new UnaryNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitFunc_call(DungeonDSLParser.Func_callContext ctx) {
    var node = new FuncCallNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitStmt_block(DungeonDSLParser.Stmt_blockContext ctx) {
    var node = new StmtBlockNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitReturn_stmt(DungeonDSLParser.Return_stmtContext ctx) {
    var node = new ReturnStmtNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitConditional_stmt(DungeonDSLParser.Conditional_stmtContext ctx) {
    Node node;
    if (ctx.else_stmt() != null) {
      node = new ConditionalStmtNodeIfElse();
    } else {
      node = new ConditionalStmtNodeIf();
    }
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitElse_stmt(DungeonDSLParser.Else_stmtContext ctx) {
    var node = new ConditionalStmtNodeIfElse();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitRet_type_def(DungeonDSLParser.Ret_type_defContext ctx) {
    return genericErrorNode(this.childNodes);
  }

  @Override
  public Node visitParam_def_correct(DungeonDSLParser.Param_def_correctContext ctx) {
    var node = new ParamDefNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitParam_def_error(DungeonDSLParser.Param_def_errorContext ctx) {
    var node = new ParamDefNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitMap_param_type(DungeonDSLParser.Map_param_typeContext ctx) {
    var node = new MapTypeIdentifierNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitId_param_type(DungeonDSLParser.Id_param_typeContext ctx) {
    var node = new IdNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitList_param_type(DungeonDSLParser.List_param_typeContext ctx) {
    var node = new ListTypeIdentifierNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitSet_param_type(DungeonDSLParser.Set_param_typeContext ctx) {
    var node = new SetTypeIdentifierNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitParam_def_list(DungeonDSLParser.Param_def_listContext ctx) {
    var node = new ParamDefNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitEntity_type_def(DungeonDSLParser.Entity_type_defContext ctx) {
    var node = new PrototypeDefinitionNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitItem_type_def(DungeonDSLParser.Item_type_defContext ctx) {
    var node = new ItemPrototypeDefinitionNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitComponent_def_list(DungeonDSLParser.Component_def_listContext ctx) {
    var node = new Node(Node.Type.ComponentDefinitionList);
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitAggregate_value_def(DungeonDSLParser.Aggregate_value_defContext ctx) {
    var node = new AggregateValueDefinitionNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitObject_def(DungeonDSLParser.Object_defContext ctx) {
    var node = new ObjectDefNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitProperty_def_list(DungeonDSLParser.Property_def_listContext ctx) {
    var node = new Node(Node.Type.PropertyDefinitionList);
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitProperty_def_correct(DungeonDSLParser.Property_def_correctContext ctx) {
    var node = new PropertyDefNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitProperty_def_error(DungeonDSLParser.Property_def_errorContext ctx) {
    var node = new PropertyDefNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitExpression_list(DungeonDSLParser.Expression_listContext ctx) {
    var node = new Node(Node.Type.ExpressionList);
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitGrouped_expression(DungeonDSLParser.Grouped_expressionContext ctx) {
    var node = new Node(Node.Type.GroupedExpression);
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitList_definition(DungeonDSLParser.List_definitionContext ctx) {
    var node = new ListDefinitionNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitSet_definition(DungeonDSLParser.Set_definitionContext ctx) {
    var node = new SetDefinitionNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitPrimary(DungeonDSLParser.PrimaryContext ctx) {
    return genericErrorNode(this.childNodes);
  }

  @Override
  public Node visitId(DungeonDSLParser.IdContext ctx) {
    var node = new IdNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitId_no_type(DungeonDSLParser.Id_no_typeContext ctx) {
    var node = new IdNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDot_def(DungeonDSLParser.Dot_defContext ctx) {
    var node = new DotDefNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDot_stmt_list(DungeonDSLParser.Dot_stmt_listContext ctx) {
    var node = new Node(Node.Type.DotStmtList);
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDot_stmt(DungeonDSLParser.Dot_stmtContext ctx) {
    var node = new DotNodeStmtNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDot_edge_stmt(DungeonDSLParser.Dot_edge_stmtContext ctx) {
    var node = new DotEdgeStmtNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDot_node_list(DungeonDSLParser.Dot_node_listContext ctx) {
    var node = new DotIdList();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDot_edge_RHS(DungeonDSLParser.Dot_edge_RHSContext ctx) {
    var node = new Node(Node.Type.DotEdgeRHS);
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDot_node_stmt(DungeonDSLParser.Dot_node_stmtContext ctx) {
    var node = new DotNodeStmtNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDot_attr_list(DungeonDSLParser.Dot_attr_listContext ctx) {
    var node = new DotAttrListNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDot_attr_id(DungeonDSLParser.Dot_attr_idContext ctx) {
    var node = new DotAttrNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDt_sequence(DungeonDSLParser.Dt_sequenceContext ctx) {
    var node = new DotDependencyTypeNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDt_subtask_mandatory(DungeonDSLParser.Dt_subtask_mandatoryContext ctx) {
    var node = new DotDependencyTypeNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDt_subtask_optional(DungeonDSLParser.Dt_subtask_optionalContext ctx) {
    var node = new DotDependencyTypeNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDt_conditional_correct(DungeonDSLParser.Dt_conditional_correctContext ctx) {
    var node = new DotDependencyTypeNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDt_conditional_false(DungeonDSLParser.Dt_conditional_falseContext ctx) {
    var node = new DotDependencyTypeNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDt_sequence_and(DungeonDSLParser.Dt_sequence_andContext ctx) {
    var node = new DotDependencyTypeNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visitDt_sequence_or(DungeonDSLParser.Dt_sequence_orContext ctx) {
    var node = new DotDependencyTypeNode();
    node.addChildren(this.childNodes);
    return node;
  }

  @Override
  public Node visit(ParseTree parseTree) {
    return null;
  }

  @Override
  public Node visitChildren(RuleNode ruleNode) {
    // if we land here, there is no specific implementation of visitor pattern generated by ANTLR...
    if (ruleNode instanceof DungeonDSLParser.Import_defContext) {
      var node = new ImportNode(Node.NONE, Node.NONE);
      this.childNodes.forEach(node::addChild);
      return node;
    }
    return Node.NONE;
  }

  private Node genericErrorNode(ArrayList<Node> children) {
    return new Node(Node.Type.ErrorNode, children);
  }

  @Override
  public Node visitTerminal(TerminalNode terminalNode) {
    return null;
  }

  @Override
  public Node visitErrorNode(ErrorNode errorNode) {
    return null;
  }
}
