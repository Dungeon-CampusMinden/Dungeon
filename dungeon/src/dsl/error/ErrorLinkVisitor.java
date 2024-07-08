package dsl.error;

import dsl.antlr.DungeonDSLParser;
import dsl.antlr.DungeonDSLParserVisitor;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.text.StringEscapeUtils;

public class ErrorLinkVisitor implements DungeonDSLParserVisitor<String> {

  private static String format = "For correct syntax of %s see: %s";

  @Override
  public String visitProgram(DungeonDSLParser.ProgramContext ctx) {
    return String.format(
      format,
      "a program",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md")
    );
  }

  @Override
  public String visitDefinition(DungeonDSLParser.DefinitionContext ctx) {
    return String.format(
      format,
      "an object definition",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#objektdefinition")
    );
  }

  @Override
  public String visitImport_unnamed(DungeonDSLParser.Import_unnamedContext ctx) {
    return String.format(
      format,
      "an import statement",
      StringEscapeUtils.escapeJava("placeholder")
    );
  }

  @Override
  public String visitImport_named(DungeonDSLParser.Import_namedContext ctx) {
    return String.format(
      format,
      "an import statement",
      StringEscapeUtils.escapeJava("placeholder")
    );
  }

  @Override
  public String visitFn_def(DungeonDSLParser.Fn_defContext ctx) {
    return String.format(
      format,
      "a function definition",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#funktionsdefinition")
    );
  }

  @Override
  public String visitStmt(DungeonDSLParser.StmtContext ctx) {
    return String.format(
      format,
      "a statement",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#funktionsdefinition")
    );
  }

  @Override
  public String visitFor_loop(DungeonDSLParser.For_loopContext ctx) {
    return String.format(
      format,
      "a for loop",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#schleifen")
    );
  }

  @Override
  public String visitFor_loop_counting(DungeonDSLParser.For_loop_countingContext ctx) {
    return String.format(
      format,
      "a counting for loop",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#schleifen")
    );
  }

  @Override
  public String visitWhile_loop(DungeonDSLParser.While_loopContext ctx) {
    return String.format(
      format,
      "a while loop",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#schleifen")
    );
  }

  @Override
  public String visitVar_decl_assignment(DungeonDSLParser.Var_decl_assignmentContext ctx) {
    return String.format(
      format,
      "a variable declaration",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#schleifen")
    );
  }

  @Override
  public String visitVar_decl_assignment_incomplete(DungeonDSLParser.Var_decl_assignment_incompleteContext ctx) {
    return String.format(
      format,
      "a variable declaration",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#schleifen")
    );
  }

  @Override
  public String visitVar_decl_type_decl(DungeonDSLParser.Var_decl_type_declContext ctx) {
    return String.format(
      format,
      "a variable declaration",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#schleifen")
    );
  }

  @Override
  public String visitVar_decl_type_decl_incomplete(DungeonDSLParser.Var_decl_type_decl_incompleteContext ctx) {
    return String.format(
      format,
      "a variable declaration",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#schleifen")
    );
  }

  @Override
  public String visitExpr_assignment(DungeonDSLParser.Expr_assignmentContext ctx) {
    return String.format(
      format,
      "an assignment",
      StringEscapeUtils.escapeJava("placeholder")
    );
  }

  @Override
  public String visitExpr_assignment_incomplete(DungeonDSLParser.Expr_assignment_incompleteContext ctx) {
    return String.format(
      format,
      "an assignment",
      StringEscapeUtils.escapeJava("placeholder")
    );
  }

  @Override
  public String visitExpr_trivial(DungeonDSLParser.Expr_trivialContext ctx) {
    return String.format(
      format,
      "an expression",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#ausdr%C3%BCcke-labela2ausdruecke")
    );
  }

  @Override
  public String visitMethod_call_expression(DungeonDSLParser.Method_call_expressionContext ctx) {
    return String.format(
      format,
      "a method call",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#member-funktionen-aufruf")
    );
  }

  @Override
  public String visitMember_access_expression(DungeonDSLParser.Member_access_expressionContext ctx) {
    return String.format(
      format,
      "a member access",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#member-zugriff")
    );
  }

  @Override
  public String visitMember_access_incomplete(DungeonDSLParser.Member_access_incompleteContext ctx) {
    return String.format(
      format,
      "a member access",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#member-zugriff")
    );
  }

  @Override
  public String visitAssignee_func(DungeonDSLParser.Assignee_funcContext ctx) {
    return String.format(
      format,
      "a method call",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#member-funktionen-aufruf")
    );
  }

  @Override
  public String visitAssignee_member_access(DungeonDSLParser.Assignee_member_accessContext ctx) {
    return String.format(
      format,
      "a method call",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#member-funktionen-aufruf")
    );
  }

  @Override
  public String visitAssignee_identifier(DungeonDSLParser.Assignee_identifierContext ctx) {
    return String.format(
      format,
      "an assignment",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#variablendefinition")
    );
  }

  @Override
  public String visitLogic_or(DungeonDSLParser.Logic_orContext ctx) {
    return String.format(
      format,
      "a logic or",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#logische-operationen-nicht-implementiert")
    );
  }

  @Override
  public String visitLogic_and(DungeonDSLParser.Logic_andContext ctx) {
    return String.format(
      format,
      "a logic and",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#logische-operationen-nicht-implementiert")
    );
  }

  @Override
  public String visitEquality(DungeonDSLParser.EqualityContext ctx) {
    return String.format(
      format,
      "equality",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#gleichheit-nicht-implementiert")
    );
  }

  @Override
  public String visitComparison(DungeonDSLParser.ComparisonContext ctx) {
    return String.format(
      format,
      "comparison",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#vergleichsoperationen-nicht-implementiert")
    );
  }

  @Override
  public String visitTerm(DungeonDSLParser.TermContext ctx) {
    return String.format(
      format,
      "terms",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#arithmetische-operationen-nicht-implementiert")
    );
  }

  @Override
  public String visitFactor(DungeonDSLParser.FactorContext ctx) {
    return String.format(
      format,
      "terms",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#arithmetische-operationen-nicht-implementiert")
    );
  }

  @Override
  public String visitUnary(DungeonDSLParser.UnaryContext ctx) {
    return String.format(
      format,
      "unary oparators",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#un%C3%A4re-operationen-nicht-implementiert")
    );
  }

  @Override
  public String visitFunc_call(DungeonDSLParser.Func_callContext ctx) {
    return String.format(
      format,
      "function calls",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#funktionsaufrufe")
    );
  }

  @Override
  public String visitStmt_block(DungeonDSLParser.Stmt_blockContext ctx) {
    return String.format(
      format,
      "statement blocks",
      StringEscapeUtils.escapeJava("placeholder")
    );
  }

  @Override
  public String visitReturn_stmt(DungeonDSLParser.Return_stmtContext ctx) {
    return String.format(
      format,
      "return statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#funktionsaufrufe")
    );
  }

  @Override
  public String visitConditional_stmt(DungeonDSLParser.Conditional_stmtContext ctx) {
    return String.format(
      format,
      "conditional statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#konditionale-ausdr%C3%BCcke")
    );
  }

  @Override
  public String visitElse_stmt(DungeonDSLParser.Else_stmtContext ctx) {
    return String.format(
      format,
      "conditional statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#konditionale-ausdr%C3%BCcke")
    );
  }

  @Override
  public String visitRet_type_def(DungeonDSLParser.Ret_type_defContext ctx) {
    return String.format(
      format,
      "function definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#funktionsdefinition")
    );
  }

  @Override
  public String visitParam_def_correct(DungeonDSLParser.Param_def_correctContext ctx) {
    return String.format(
      format,
      "function definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#funktionsdefinition")
    );
  }

  @Override
  public String visitParam_def_error(DungeonDSLParser.Param_def_errorContext ctx) {
    return String.format(
      format,
      "function definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#funktionsdefinition")
    );
  }

  @Override
  public String visitMap_param_type(DungeonDSLParser.Map_param_typeContext ctx) {
    return String.format(
      format,
      "map definitions",
      StringEscapeUtils.escapeJava("placeholder")
    );
  }

  @Override
  public String visitId_param_type(DungeonDSLParser.Id_param_typeContext ctx) {
    return String.format(
      format,
      "type definitions",
      StringEscapeUtils.escapeJava("placeholder")
    );
  }

  @Override
  public String visitList_param_type(DungeonDSLParser.List_param_typeContext ctx) {
    return String.format(
      format,
      "list definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#mengen")
    );
  }

  @Override
  public String visitSet_param_type(DungeonDSLParser.Set_param_typeContext ctx) {
    return String.format(
      format,
      "set definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#mengen")
    );
  }

  @Override
  public String visitParam_def_list(DungeonDSLParser.Param_def_listContext ctx) {
    return String.format(
      format,
      "function definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#funktionsdefinition")
    );
  }

  @Override
  public String visitEntity_type_def(DungeonDSLParser.Entity_type_defContext ctx) {
    return String.format(
      format,
      "entity type definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#entit%C3%A4tstyp-definition")
    );
  }

  @Override
  public String visitItem_type_def(DungeonDSLParser.Item_type_defContext ctx) {
    return String.format(
      format,
      "item type definitions",
      StringEscapeUtils.escapeJava("placeholder")
    );
  }

  @Override
  public String visitComponent_def_list(DungeonDSLParser.Component_def_listContext ctx) {
    return String.format(
      format,
      "component definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#entit%C3%A4tstyp-definition")
    );
  }

  @Override
  public String visitAggregate_value_def(DungeonDSLParser.Aggregate_value_defContext ctx) {
    return String.format(
      format,
      "property definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#inline-objektdefinition")
    );
  }

  @Override
  public String visitObject_def(DungeonDSLParser.Object_defContext ctx) {
    return String.format(
      format,
      "object definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#objektdefinition")
    );
  }

  @Override
  public String visitProperty_def_list(DungeonDSLParser.Property_def_listContext ctx) {
    return String.format(
      format,
      "property definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#inline-objektdefinition")
    );
  }

  @Override
  public String visitProperty_def_correct(DungeonDSLParser.Property_def_correctContext ctx) {
    return String.format(
      format,
      "property definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#inline-objektdefinition")
    );
  }

  @Override
  public String visitProperty_def_error(DungeonDSLParser.Property_def_errorContext ctx) {
    return String.format(
      format,
      "property definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#inline-objektdefinition")
    );
  }

  @Override
  public String visitExpression_list(DungeonDSLParser.Expression_listContext ctx) {
    return String.format(
      format,
      "expression lists",
      StringEscapeUtils.escapeJava("placeholder")
    );
  }

  @Override
  public String visitGrouped_expression(DungeonDSLParser.Grouped_expressionContext ctx) {
    return String.format(
      format,
      "grouped expressions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#ausdr%C3%BCcke-labela2ausdruecke")
    );
  }

  @Override
  public String visitList_definition(DungeonDSLParser.List_definitionContext ctx) {
    return String.format(
      format,
      "list definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#mengen")
    );
  }

  @Override
  public String visitSet_definition(DungeonDSLParser.Set_definitionContext ctx) {
    return String.format(
      format,
      "set definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#mengen")
    );
  }

  @Override
  public String visitPrimary(DungeonDSLParser.PrimaryContext ctx) {
    return "";
  }

  @Override
  public String visitId(DungeonDSLParser.IdContext ctx) {
    return "";
  }

  @Override
  public String visitId_no_type(DungeonDSLParser.Id_no_typeContext ctx) {
    return "";
  }

  @Override
  public String visitDot_def(DungeonDSLParser.Dot_defContext ctx) {
    return String.format(
      format,
      "graph definitions",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDot_stmt_list(DungeonDSLParser.Dot_stmt_listContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDot_stmt(DungeonDSLParser.Dot_stmtContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDot_edge_stmt(DungeonDSLParser.Dot_edge_stmtContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDot_node_list(DungeonDSLParser.Dot_node_listContext ctx) {
    return String.format(
      format,
      "graph node lists",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDot_edge_RHS(DungeonDSLParser.Dot_edge_RHSContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDot_node_stmt(DungeonDSLParser.Dot_node_stmtContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDot_attr_list(DungeonDSLParser.Dot_attr_listContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDot_attr_id(DungeonDSLParser.Dot_attr_idContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDt_sequence(DungeonDSLParser.Dt_sequenceContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDt_subtask_mandatory(DungeonDSLParser.Dt_subtask_mandatoryContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDt_subtask_optional(DungeonDSLParser.Dt_subtask_optionalContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDt_conditional_correct(DungeonDSLParser.Dt_conditional_correctContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDt_conditional_false(DungeonDSLParser.Dt_conditional_falseContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDt_sequence_and(DungeonDSLParser.Dt_sequence_andContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visitDt_sequence_or(DungeonDSLParser.Dt_sequence_orContext ctx) {
    return String.format(
      format,
      "graph statements",
      StringEscapeUtils.escapeJava("https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/sprachkonzepte.md#aufgabenabh%C3%A4ngigkeiten")
    );
  }

  @Override
  public String visit(ParseTree parseTree) {
    return "";
  }

  @Override
  public String visitChildren(RuleNode ruleNode) {
    return "";
  }

  @Override
  public String visitTerminal(TerminalNode terminalNode) {
    return "";
  }

  @Override
  public String visitErrorNode(ErrorNode errorNode) {
    return "";
  }
}
