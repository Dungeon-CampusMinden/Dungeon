package dsl.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import dsl.helpers.Helpers;
import dsl.parser.ast.*;
import graph.taskdependencygraph.TaskEdge;
import java.util.List;
import org.junit.jupiter.api.Test;

// CHECKSTYLE:OFF: AvoidStarImport

// CHECKSTYLE:ON: AvoidStarImport

/** WTF? . */
public class TestDungeonASTConverter {

  /** Test AST structure of a simple dot definition. */
  @Test
  public void testSimpleDotDef() {
    String program = "graph g { a -> b }";

    var ast = Helpers.getASTFromString(program);

    var dot_def = ast.getChild(0);
    assertEquals(dot_def.type, Node.Type.DotDefinition);

    var id = dot_def.getChild(0);
    assertEquals(id.type, Node.Type.Identifier);
    var idNode = (IdNode) id;
    assertEquals("g", idNode.getName());

    var edgeStmt = dot_def.getChild(1);
    assertEquals(Node.Type.DotEdgeStmt, edgeStmt.type);
    var edgeStmtNode = (DotEdgeStmtNode) edgeStmt;

    List<DotIdList> idLists = edgeStmtNode.getIdLists();
    DotIdList firstIdList = idLists.get(0);
    var lhsId = firstIdList.getIdNodes().get(0);
    assertEquals("a", lhsId.getName());

    var secondIdList = idLists.get(1);
    var secondIdNode = secondIdList.getIdNodes().get(0);
    assertEquals("b", secondIdNode.getName());
  }

  /**
   * Test AST structure of a chained edge statement, that is multiple edge definitions in one line.
   */
  @Test
  public void testChainedEdgeStmt() {
    String program = "graph g { a -> b -> c }";

    var ast = Helpers.getASTFromString(program);

    var dot_def = ast.getChild(0);
    assertEquals(Node.Type.DotDefinition, dot_def.type);

    var edgeStmt = dot_def.getChild(1);
    assertEquals(Node.Type.DotEdgeStmt, edgeStmt.type);
    var edgeStmtNode = (DotEdgeStmtNode) edgeStmt;

    var firstIdList = edgeStmtNode.getIdLists().get(0);
    var lhsIdNode = firstIdList.getIdNodes().get(0);
    assertEquals("a", lhsIdNode.getName());

    var secondIdList = edgeStmtNode.getIdLists().get(1);
    var secondIdNode = secondIdList.getIdNodes().get(0);
    assertEquals("b", secondIdNode.getName());

    var thirdIdList = edgeStmtNode.getIdLists().get(2);
    var thirdIdNode = thirdIdList.getIdNodes().get(0);
    assertEquals("c", thirdIdNode.getName());
  }

  /** WTF? . */
  @Test
  public void testChainedEdgeStmtIdGroups() {
    String program = "graph g { a,b,c -> d,e,f -> g,h,j }";

    var ast = Helpers.getASTFromString(program);

    var dot_def = ast.getChild(0);
    assertEquals(Node.Type.DotDefinition, dot_def.type);

    var edgeStmt = dot_def.getChild(1);
    assertEquals(Node.Type.DotEdgeStmt, edgeStmt.type);
    var edgeStmtNode = (DotEdgeStmtNode) edgeStmt;

    var firstIdList = edgeStmtNode.getIdLists().get(0);
    var idNodes = firstIdList.getIdNodes();
    assertEquals("a", idNodes.get(0).getName());
    assertEquals("b", idNodes.get(1).getName());
    assertEquals("c", idNodes.get(2).getName());

    var secondIdList = edgeStmtNode.getIdLists().get(1);
    idNodes = secondIdList.getIdNodes();
    assertEquals("d", idNodes.get(0).getName());
    assertEquals("e", idNodes.get(1).getName());
    assertEquals("f", idNodes.get(2).getName());

    var thirdIdList = edgeStmtNode.getIdLists().get(2);
    idNodes = thirdIdList.getIdNodes();
    assertEquals("g", idNodes.get(0).getName());
    assertEquals("h", idNodes.get(1).getName());
    assertEquals("j", idNodes.get(2).getName());
  }

  /** Test AST of a function call inside a property definition. */
  @Test
  public void testFuncCall() {
    String program = "quest_config q { \n test: hello_world(x, \"wuppi\" ,42)\n }";
    var ast = Helpers.getASTFromString(program);

    var questDef = ast.getChild(0);
    var propertyDefList = questDef.getChild(2);
    var firstPropDef = propertyDefList.getChild(0);

    var funcCall = firstPropDef.getChild(1);
    assertEquals(Node.Type.FuncCall, funcCall.type);

    var funcCallNode = (FuncCallNode) funcCall;
    assertEquals("hello_world", funcCallNode.getIdName());

    var paramList = funcCallNode.getParameters();
    assertEquals(Node.Type.Identifier, paramList.get(0).type);
    assertEquals(Node.Type.StringLiteral, paramList.get(1).type);
    assertEquals(Node.Type.Number, paramList.get(2).type);
  }

  /** Test AST of function call as parameter ot another function call. */
  @Test
  public void testFuncCallAsParam() {
    String program = "quest_config q { \n test: hello_world(other_func())\n }";
    var ast = Helpers.getASTFromString(program);

    var questDef = ast.getChild(0);
    var propertyDefList = questDef.getChild(2);
    var firstPropDef = propertyDefList.getChild(0);

    var funcCall = firstPropDef.getChild(1);
    assertEquals(Node.Type.FuncCall, funcCall.type);

    var funcCallNode = (FuncCallNode) funcCall;
    assertEquals("hello_world", funcCallNode.getIdName());

    var paramList = funcCallNode.getParameters();
    assertEquals(Node.Type.FuncCall, paramList.get(0).type);
  }

  /** Test the definition of a game object with one trivial component definition. */
  @Test
  public void testGameObjectDefinitionSimpleComponent() {
    String program =
        """
                entity_type test_object {
                    this_is_a_component
                    }
                """;
    var ast = Helpers.getASTFromString(program);

    var objDef = ast.getChild(0);
    assertEquals(Node.Type.PrototypeDefinition, objDef.type);

    var componentDefListNode = ((PrototypeDefinitionNode) objDef).getComponentDefinitionListNode();
    assertEquals(Node.Type.ComponentDefinitionList, componentDefListNode.type);

    var componentDefinitions = componentDefListNode.getChildren();
    assertEquals(1, componentDefinitions.size());

    var component = componentDefinitions.get(0);
    assertEquals(Node.Type.AggregateValueDefinition, component.type);

    String componentName = ((AggregateValueDefinitionNode) component).getIdName();
    assertEquals("this_is_a_component", componentName);

    var propertyDefinitionListNode =
        ((AggregateValueDefinitionNode) component).getPropertyDefinitionListNode();
    assertEquals(Node.NONE, propertyDefinitionListNode);
  }

  /** Test the definition of a game object with one trivial component definition. */
  @Test
  public void testItemTypeDefinition() {
    String program =
        """
                item_type test_object {
                    value1: 1,
                    value2: 2,
                    value3: 3
                }
                """;
    var ast = Helpers.getASTFromString(program);

    var objDef = ast.getChild(0);
    assertEquals(Node.Type.ItemPrototypeDefinition, objDef.type);

    var propertyDefinitionListNode =
        ((ItemPrototypeDefinitionNode) objDef).getPropertyDefinitionListNode();
    assertEquals(Node.Type.PropertyDefinitionList, propertyDefinitionListNode.type);

    var propertyDefinitions = propertyDefinitionListNode.getChildren();
    assertEquals(3, propertyDefinitions.size());

    var property1 = (PropertyDefNode) propertyDefinitions.get(0);
    assertEquals("value1", property1.getIdName());

    var property2 = (PropertyDefNode) propertyDefinitions.get(1);
    assertEquals("value2", property2.getIdName());

    var property3 = (PropertyDefNode) propertyDefinitions.get(2);
    assertEquals("value3", property3.getIdName());
  }

  /**
   * Test the definition of a game object with one component definition with property definitions.
   */
  @Test
  public void testGameObjectDefinition() {
    String program =
        """
                entity_type test_object {
                    complex_component {
                        prop1: 123,
                        prop2: "Hello, World!"
                    }
                }
                """;
    var ast = Helpers.getASTFromString(program);

    var objDef = ast.getChild(0);
    assertEquals(Node.Type.PrototypeDefinition, objDef.type);

    var componentDefListNode = ((PrototypeDefinitionNode) objDef).getComponentDefinitionListNode();
    assertEquals(Node.Type.ComponentDefinitionList, componentDefListNode.type);

    var componentDefinitions = componentDefListNode.getChildren();
    assertEquals(1, componentDefinitions.size());

    var component = componentDefinitions.get(0);
    assertEquals(Node.Type.AggregateValueDefinition, component.type);

    String componentName = ((AggregateValueDefinitionNode) component).getIdName();
    assertEquals("complex_component", componentName);

    var propertyDefinitions =
        ((AggregateValueDefinitionNode) component).getPropertyDefinitionNodes();
    assertEquals(2, propertyDefinitions.size());

    var firstPropertyDefNode = (PropertyDefNode) propertyDefinitions.get(0);
    assertEquals("prop1", firstPropertyDefNode.getIdName());

    var secondPropertyDefNode = (PropertyDefNode) propertyDefinitions.get(1);
    assertEquals("prop2", secondPropertyDefNode.getIdName());
  }

  /** Test the definition of a game object with multiple component definitions. */
  @Test
  public void testGameObjectDefinitionMultiComponent() {
    String program =
        """
                entity_type test_object {
                    complex_component1 {
                        prop1: 123,
                        prop2: "Hello, World!"
                    },
                    complex_component2 {
                        prop3: func(test),
                        prop4: "42"
                    }
                }
                """;
    var ast = Helpers.getASTFromString(program);

    var objDef = ast.getChild(0);
    var componentDefListNode = ((PrototypeDefinitionNode) objDef).getComponentDefinitionListNode();
    var componentDefinitions = componentDefListNode.getChildren();
    assertEquals(2, componentDefinitions.size());

    // test first component
    var component = componentDefinitions.get(0);
    String componentName = ((AggregateValueDefinitionNode) component).getIdName();
    assertEquals("complex_component1", componentName);

    var propertyDefinitions =
        ((AggregateValueDefinitionNode) component).getPropertyDefinitionNodes();
    assertEquals(2, propertyDefinitions.size());

    var firstPropertyDefNode = (PropertyDefNode) propertyDefinitions.get(0);
    assertEquals("prop1", firstPropertyDefNode.getIdName());

    var secondPropertyDefNode = (PropertyDefNode) propertyDefinitions.get(1);
    assertEquals("prop2", secondPropertyDefNode.getIdName());

    // test second component
    component = componentDefinitions.get(1);
    componentName = ((AggregateValueDefinitionNode) component).getIdName();
    assertEquals("complex_component2", componentName);

    propertyDefinitions = ((AggregateValueDefinitionNode) component).getPropertyDefinitionNodes();
    assertEquals(2, propertyDefinitions.size());

    firstPropertyDefNode = (PropertyDefNode) propertyDefinitions.get(0);
    assertEquals("prop3", firstPropertyDefNode.getIdName());

    secondPropertyDefNode = (PropertyDefNode) propertyDefinitions.get(1);
    assertEquals("prop4", secondPropertyDefNode.getIdName());
  }

  /** WTF? . */
  @Test
  public void adaptedAggregateType() {
    String program =
        """
                entity_type my_obj {
                    test_component_with_external_type {
                        member_external_type: external_type { str: "Hello, World!", n: 42 }
                    }
                }

                quest_config config {
                    entity: my_obj
                }
                """;

    var ast = Helpers.getASTFromString(program);
    var gameObjectDef = (PrototypeDefinitionNode) ast.getChild(0);
    var componentDef =
        (AggregateValueDefinitionNode) gameObjectDef.getComponentDefinitionNodes().get(0);
    var propertyDef = (PropertyDefNode) componentDef.getPropertyDefinitionNodes().get(0);
    var stmtNode = propertyDef.getStmtNode();
    assertEquals(stmtNode.type, Node.Type.AggregateValueDefinition);
  }

  /** WTF? . */
  @Test
  public void funcDefMinimal() {
    String program = """
            fn test_func() { }
            """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);

    assertEquals(Node.Type.FuncDef, funcDefNode.type);
    assertEquals("test_func", funcDefNode.getIdName());
    assertEquals(Node.NONE, funcDefNode.getRetTypeId());

    var parameters = funcDefNode.getParameters();
    assertEquals(0, parameters.size());

    var stmts = funcDefNode.getStmts();
    assertEquals(0, stmts.size());
  }

  /** WTF? . */
  @Test
  public void funcDefFull() {
    String program =
        """
                fn test_func(int param1, float param2, string param3) -> ret_type {
                    print("hello");
                }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);

    assertEquals(Node.Type.FuncDef, funcDefNode.type);
    assertEquals("test_func", funcDefNode.getIdName());
    assertEquals("ret_type", funcDefNode.getRetTypeName());

    var parameters = funcDefNode.getParameters();
    for (var parameter : parameters) {
      assertEquals(Node.Type.ParamDef, parameter.type);
    }
    assertEquals("param1", ((ParamDefNode) parameters.get(0)).getIdName());
    assertEquals("param2", ((ParamDefNode) parameters.get(1)).getIdName());
    assertEquals("param3", ((ParamDefNode) parameters.get(2)).getIdName());
    assertEquals("int", ((ParamDefNode) parameters.get(0)).getTypeName());
    assertEquals("float", ((ParamDefNode) parameters.get(1)).getTypeName());
    assertEquals("string", ((ParamDefNode) parameters.get(2)).getTypeName());

    var stmts = funcDefNode.getStmts();
    assertEquals(Node.Type.FuncCall, stmts.get(0).type);

    assertNotEquals(Node.NONE, funcDefNode);
  }

  /** WTF? . */
  @Test
  public void returnStmt() {
    String program =
        """
                    fn test_func() -> ret_type {
                        return 42;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);

    var stmts = funcDefNode.getStmts();
    var returnStmt = stmts.get(0);
    assertEquals(Node.Type.ReturnStmt, returnStmt.type);

    var innerStmt = ((ReturnStmtNode) returnStmt).getInnerStmtNode();
    assertEquals(Node.Type.Number, innerStmt.type);
  }

  /** WTF? . */
  @Test
  public void nestedBlocks() {
    String program =
        """
                fn test_func(int param1, float param2, string param3) -> int
                {
                    {
                        {
                            print(param1);
                        }
                    }
                }
                """;

    var ast = Helpers.getASTFromString(program);

    FuncDefNode funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmtList = funcDefNode.getStmts();
    assertEquals(1, stmtList.size());

    Node outerStmtBlock = funcDefNode.getStmtBlock();
    assertEquals(Node.Type.Block, outerStmtBlock.type);
    Node outerBlocksStmtList = outerStmtBlock.getChild(0);
    assertEquals(Node.Type.StmtList, outerBlocksStmtList.type);
    Node middleStmtBlock = outerBlocksStmtList.getChild(0);
    assertEquals(Node.Type.Block, middleStmtBlock.type);
    Node middleBlocksStmtList = middleStmtBlock.getChild(0);
    assertEquals(Node.Type.StmtList, middleBlocksStmtList.type);
    Node innerStmtBlock = middleBlocksStmtList.getChild(0);
    assertEquals(Node.Type.Block, innerStmtBlock.type);
    Node funcCallStmt = ((StmtBlockNode) innerStmtBlock).getStmts().get(0);
  }

  /** WTF? . */
  @Test
  public void ifStmt() {
    String program =
        """
                    fn test_func() {
                        if expr {
                            print("hello");
                        }
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var conditionalIfStmt = stmts.get(0);
    assertEquals(Node.Type.ConditionalStmtIf, conditionalIfStmt.type);

    var condition = ((ConditionalStmtNodeIf) conditionalIfStmt).getCondition();
    assertEquals(Node.Type.Identifier, condition.type);
    assertEquals("expr", ((IdNode) condition).getName());

    var stmt = ((ConditionalStmtNodeIf) conditionalIfStmt).getIfStmt();
    assertEquals(Node.Type.Block, stmt.type);
  }

  /** WTF? . */
  @Test
  public void ifElseStmt() {
    String program =
        """
                    fn test_func() {
                        if expr {
                            print("hello");
                        } else
                          print("world");
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var conditionalStmt = stmts.get(0);
    assertEquals(Node.Type.ConditionalStmtIfElse, conditionalStmt.type);

    var condition = ((ConditionalStmtNodeIfElse) conditionalStmt).getCondition();
    assertEquals(Node.Type.Identifier, condition.type);
    assertEquals("expr", ((IdNode) condition).getName());

    var ifStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getIfStmt();
    assertEquals(Node.Type.Block, ifStmt.type);

    var elseStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getElseStmt();
    assertEquals(Node.Type.FuncCall, elseStmt.type);
  }

  /** WTF? . */
  @Test
  public void elseIfStmt() {
    String program =
        """
                    fn test_func() {
                        if expr {
                            print("hello");
                        } else if other_expr
                          print("world");
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var conditionalStmt = stmts.get(0);
    assertEquals(Node.Type.ConditionalStmtIfElse, conditionalStmt.type);

    var condition = ((ConditionalStmtNodeIfElse) conditionalStmt).getCondition();
    assertEquals(Node.Type.Identifier, condition.type);
    assertEquals("expr", ((IdNode) condition).getName());

    var ifStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getIfStmt();
    assertEquals(Node.Type.Block, ifStmt.type);

    var elseIfStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getElseStmt();
    assertEquals(Node.Type.ConditionalStmtIf, elseIfStmt.type);

    var elseIfStmtCondition = ((ConditionalStmtNodeIf) elseIfStmt).getCondition();
    assertEquals(Node.Type.Identifier, elseIfStmtCondition.type);
    assertEquals("other_expr", ((IdNode) elseIfStmtCondition).getName());
  }

  /** WTF? . */
  @Test
  public void elseIfElseStmt() {
    String program =
        """
                    fn test_func() {
                        if expr {
                            print("hello");
                        } else if other_expr {
                          print("world");
                        } else {
                          print("!");
                        }
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var conditionalStmt = stmts.get(0);
    assertEquals(Node.Type.ConditionalStmtIfElse, conditionalStmt.type);

    var condition = ((ConditionalStmtNodeIfElse) conditionalStmt).getCondition();
    assertEquals(Node.Type.Identifier, condition.type);
    assertEquals("expr", ((IdNode) condition).getName());

    var ifStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getIfStmt();
    assertEquals(Node.Type.Block, ifStmt.type);

    var elseIfStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getElseStmt();
    assertEquals(Node.Type.ConditionalStmtIfElse, elseIfStmt.type);

    var elseIfStmtCondition = ((ConditionalStmtNodeIfElse) elseIfStmt).getCondition();
    assertEquals(Node.Type.Identifier, elseIfStmtCondition.type);
    assertEquals("other_expr", ((IdNode) elseIfStmtCondition).getName());

    var elseStmt = ((ConditionalStmtNodeIfElse) elseIfStmt).getElseStmt();
    assertEquals(Node.Type.Block, elseStmt.type);
  }

  /** WTF? . */
  void nestedIfElseStmts() {
    String program =
        """
                    fn test_func() {
                        if outer_expr {
                          if inner_expr {
                            print("hello");
                          } else if inner_else_if_expr {
                            print("moin");
                          }
                        } else {
                          print("world");
                        }
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var conditionalStmt = stmts.get(0);
    var outerCondition = ((ConditionalStmtNodeIfElse) conditionalStmt).getCondition();
    assertEquals("outer_expr", ((IdNode) outerCondition).getName());

    var ifStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getIfStmt();
    var innerConditionalStmt = ((StmtBlockNode) ifStmt).getStmts().get(0);
    assertEquals(Node.Type.ConditionalStmtIfElse, innerConditionalStmt.type);

    var innerIfCondition = ((ConditionalStmtNodeIfElse) innerConditionalStmt).getCondition();
    assertEquals("inner_expr", ((IdNode) innerIfCondition).getName());

    var innerElseStmt = ((ConditionalStmtNodeIfElse) innerConditionalStmt).getElseStmt();
    var innerElseIfCondition = ((ConditionalStmtNodeIf) innerElseStmt).getCondition();
    assertEquals("inner_else_if_expr", ((IdNode) innerElseIfCondition).getName());

    var elseStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getElseStmt();
    assertEquals(Node.Type.Block, elseStmt.type);
  }

  // TODO: tests for complex expressions

  /** WTF? . */
  void testUnary() {
    String program =
        """
                    fn test_func() {
                        !true;
                        -4;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    // first statement
    var unaryStmt = stmts.get(0);
    assertEquals(Node.Type.Unary, unaryStmt.type);
    var unaryNode = (UnaryNode) unaryStmt;
    assertEquals(UnaryNode.UnaryType.not, unaryNode.getUnaryType());

    // second statement
    unaryStmt = stmts.get(1);
    assertEquals(Node.Type.Unary, unaryStmt.type);
    unaryNode = (UnaryNode) unaryStmt;
    assertEquals(UnaryNode.UnaryType.minus, unaryNode.getUnaryType());
  }

  /** WTF? . */
  void testFactor() {
    String program =
        """
                    fn test_func() {
                        4 * 2;
                        3 / 1;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    // first statement
    var factorStmt = stmts.get(0);
    assertEquals(Node.Type.Factor, factorStmt.type);

    FactorNode factorNode = (FactorNode) factorStmt;
    assertEquals(FactorNode.FactorType.multiply, factorNode.getFactorType());

    var lhs = factorNode.getLhs();
    assertEquals(Node.Type.Number, lhs.type);
    assertEquals(4, ((NumNode) lhs).getValue());

    var rhs = factorNode.getRhs();
    assertEquals(Node.Type.Number, rhs.type);
    assertEquals(2, ((NumNode) rhs).getValue());

    // second statement
    factorStmt = stmts.get(1);
    assertEquals(Node.Type.Factor, factorStmt.type);

    factorNode = (FactorNode) factorStmt;
    assertEquals(FactorNode.FactorType.divide, factorNode.getFactorType());

    lhs = factorNode.getLhs();
    assertEquals(Node.Type.Number, lhs.type);
    assertEquals(3, ((NumNode) lhs).getValue());

    rhs = factorNode.getRhs();
    assertEquals(Node.Type.Number, rhs.type);
    assertEquals(1, ((NumNode) rhs).getValue());
  }

  /** WTF? . */
  void testTerm() {
    String program =
        """
                    fn test_func() {
                        4 + 2;
                        3 - 1;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    // first statement
    var termStmt = stmts.get(0);
    assertEquals(Node.Type.Term, termStmt.type);

    TermNode termNode = (TermNode) termStmt;
    assertEquals(TermNode.TermType.plus, termNode.getTermType());

    var lhs = termNode.getLhs();
    assertEquals(Node.Type.Number, lhs.type);
    assertEquals(4, ((NumNode) lhs).getValue());

    var rhs = termNode.getRhs();
    assertEquals(Node.Type.Number, rhs.type);
    assertEquals(2, ((NumNode) rhs).getValue());

    // second statement
    termStmt = stmts.get(1);
    assertEquals(Node.Type.Term, termStmt.type);

    termNode = (TermNode) termStmt;
    assertEquals(TermNode.TermType.minus, termNode.getTermType());

    lhs = termNode.getLhs();
    assertEquals(Node.Type.Number, lhs.type);
    assertEquals(3, ((NumNode) lhs).getValue());

    rhs = termNode.getRhs();
    assertEquals(Node.Type.Number, rhs.type);
    assertEquals(1, ((NumNode) rhs).getValue());
  }

  /** WTF? . */
  void testComparison() {
    String program =
        """
                    fn test_func() {
                        1 > 5;
                        2 >= 6;
                        3 < 7;
                        4 <= 8;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    // first statement
    var compStmt = stmts.get(0);
    assertEquals(Node.Type.Comparison, compStmt.type);

    ComparisonNode compNode = (ComparisonNode) compStmt;
    assertEquals(ComparisonNode.ComparisonType.greaterThan, compNode.getComparisonType());

    // second statement
    compStmt = stmts.get(1);
    assertEquals(Node.Type.Comparison, compStmt.type);

    compNode = (ComparisonNode) compStmt;
    assertEquals(ComparisonNode.ComparisonType.greaterEquals, compNode.getComparisonType());

    // third statement
    compStmt = stmts.get(2);
    assertEquals(Node.Type.Comparison, compStmt.type);

    compNode = (ComparisonNode) compStmt;
    assertEquals(ComparisonNode.ComparisonType.lessThan, compNode.getComparisonType());

    // fourth statement
    compStmt = stmts.get(3);
    assertEquals(Node.Type.Comparison, compStmt.type);

    compNode = (ComparisonNode) compStmt;
    assertEquals(ComparisonNode.ComparisonType.lessEquals, compNode.getComparisonType());
  }

  /** WTF? . */
  void testEquality() {
    String program =
        """
                    fn test_func() {
                        test == other_test;
                        test != other_test;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    // first statement
    var equalityStmt = stmts.get(0);
    assertEquals(Node.Type.Equality, equalityStmt.type);

    EqualityNode equalityNode = (EqualityNode) equalityStmt;
    assertEquals(EqualityNode.EqualityType.equals, equalityNode.getEqualityType());

    // second statement
    equalityStmt = stmts.get(1);
    assertEquals(Node.Type.Equality, equalityStmt.type);

    equalityNode = (EqualityNode) equalityStmt;
    assertEquals(EqualityNode.EqualityType.notEquals, equalityNode.getEqualityType());
  }

  /** WTF? . */
  void testLogicAnd() {
    String program =
        """
                    fn test_func() {
                        lhs and rhs;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var andStmt = stmts.get(0);
    assertEquals(Node.Type.LogicAnd, andStmt.type);
  }

  /** WTF? . */
  void testLogicOr() {
    String program =
        """
                    fn test_func() {
                        lhs or rhs;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var orStmt = stmts.get(0);
    assertEquals(Node.Type.LogicOr, orStmt.type);
  }

  /** WTF? . */
  void testAssignmentId() {
    String program =
        """
                    fn test_func() {
                        my_var = 4;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var assignmentStmt = stmts.get(0);
    assertEquals(Node.Type.Assignment, assignmentStmt.type);

    AssignmentNode assignmentNode = (AssignmentNode) assignmentStmt;
    assertEquals(Node.Type.Identifier, assignmentNode.getLhs().type);
  }

  /** WTF? . */
  void testAssignmentMemberAccessWithCall() {
    String program =
        """
                    fn test_func() {
                        my_func().test = 4;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var assignmentStmt = stmts.get(0);
    assertEquals(Node.Type.Assignment, assignmentStmt.type);

    AssignmentNode assignmentNode = (AssignmentNode) assignmentStmt;
    assertEquals(Node.Type.MemberAccess, assignmentNode.getLhs().type);
    assertEquals(Node.Type.Number, assignmentNode.getRhs().type);

    MemberAccessNode memberAccessNode = (MemberAccessNode) assignmentNode.getLhs();
    assertEquals(Node.Type.FuncCall, memberAccessNode.getLhs().type);
    assertEquals(Node.Type.Identifier, memberAccessNode.getRhs().type);
  }

  /** WTF? . */
  void testAssignmentMemberAccess() {
    String program =
        """
                    fn test_func() {
                        my_var.test = 4;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var assignmentStmt = stmts.get(0);
    assertEquals(Node.Type.Assignment, assignmentStmt.type);

    AssignmentNode assignmentNode = (AssignmentNode) assignmentStmt;
    assertEquals(Node.Type.MemberAccess, assignmentNode.getLhs().type);
    assertEquals(Node.Type.Number, assignmentNode.getRhs().type);

    MemberAccessNode memberAccessNode = (MemberAccessNode) assignmentNode.getLhs();
    assertEquals(Node.Type.Identifier, memberAccessNode.getLhs().type);
    assertEquals(Node.Type.Identifier, memberAccessNode.getRhs().type);
  }

  /** WTF? . */
  void testMethodCallExpression() {
    String program =
        """
                    fn test_func() {
                        test = expr.func();
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var assignmentStmt = stmts.get(0);
    assertEquals(Node.Type.Assignment, assignmentStmt.type);

    AssignmentNode assignmentNode = (AssignmentNode) assignmentStmt;
    var rhs = assignmentNode.getRhs();
    assertEquals(Node.Type.MemberAccess, rhs.type);

    MemberAccessNode memberAccessNode = (MemberAccessNode) rhs;
    assertEquals(Node.Type.Identifier, memberAccessNode.getLhs().type);
    assertEquals(Node.Type.FuncCall, memberAccessNode.getRhs().type);
  }

  /** WTF? . */
  void testMemberAccessExpression() {
    String program =
        """
                    fn test_func() {
                        test = expr.identifier;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var assignmentStmt = stmts.get(0);
    assertEquals(Node.Type.Assignment, assignmentStmt.type);

    AssignmentNode assignmentNode = (AssignmentNode) assignmentStmt;
    var rhs = assignmentNode.getRhs();
    assertEquals(Node.Type.MemberAccess, rhs.type);

    MemberAccessNode memberAccessNode = (MemberAccessNode) rhs;
    assertEquals(Node.Type.Identifier, memberAccessNode.getLhs().type);
    assertEquals(Node.Type.Identifier, memberAccessNode.getRhs().type);
  }

  /** WTF? . */
  void testListDefinition() {
    String program =
        """
                    fn test_func() {
                        [1,2,3];
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var listDefinitionStmt = stmts.get(0);
    assertEquals(Node.Type.ListDefinitionNode, listDefinitionStmt.type);

    ListDefinitionNode listDefinitionNode = (ListDefinitionNode) listDefinitionStmt;

    assertEquals(1, ((NumNode) listDefinitionNode.getEntries().get(0)).getValue());
    assertEquals(2, ((NumNode) listDefinitionNode.getEntries().get(1)).getValue());
    assertEquals(3, ((NumNode) listDefinitionNode.getEntries().get(2)).getValue());
  }

  /** WTF? . */
  void testSetDefinition() {
    String program =
        """
                    fn test_func() {
                        <1,2,3>;
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var setDefinitionStmt = stmts.get(0);
    assertEquals(Node.Type.SetDefinitionNode, setDefinitionStmt.type);

    SetDefinitionNode setDefinitionNode = (SetDefinitionNode) setDefinitionStmt;

    assertEquals(1, ((NumNode) setDefinitionNode.getEntries().get(0)).getValue());
    assertEquals(2, ((NumNode) setDefinitionNode.getEntries().get(1)).getValue());
    assertEquals(3, ((NumNode) setDefinitionNode.getEntries().get(2)).getValue());
  }

  /** WTF? . */
  void testForLoop() {
    String program =
        """
                    fn test_func() {
                        for var_type var_name in iterable {
                            print(id);
                        }
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var loopStmt = stmts.get(0);
    assertEquals(Node.Type.LoopStmtNode, loopStmt.type);
    ForLoopStmtNode forLoopStmtNode = (ForLoopStmtNode) loopStmt;
    assertEquals(LoopStmtNode.LoopType.forLoop, forLoopStmtNode.loopType());

    IdNode typeIdNode = (IdNode) forLoopStmtNode.getTypeIdNode();
    assertEquals("var_type", typeIdNode.getName());

    IdNode varNameIdNode = (IdNode) forLoopStmtNode.getVarIdNode();
    assertEquals("var_name", varNameIdNode.getName());

    IdNode iterableIdNode = (IdNode) forLoopStmtNode.getIterableIdNode();
    assertEquals("iterable", iterableIdNode.getName());
  }

  /** WTF? . */
  void testCountingForLoop() {
    String program =
        """
                    fn test_func() {
                        for var_type var_name in iterable count i {
                            print(id);
                        }
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var loopStmt = stmts.get(0);
    assertEquals(Node.Type.LoopStmtNode, loopStmt.type);
    CountingLoopStmtNode forLoopStmtNode = (CountingLoopStmtNode) loopStmt;
    assertEquals(LoopStmtNode.LoopType.countingForLoop, forLoopStmtNode.loopType());

    IdNode typeIdNode = (IdNode) forLoopStmtNode.getTypeIdNode();
    assertEquals("var_type", typeIdNode.getName());

    IdNode varNameIdNode = (IdNode) forLoopStmtNode.getVarIdNode();
    assertEquals("var_name", varNameIdNode.getName());

    IdNode iterableIdNode = (IdNode) forLoopStmtNode.getIterableIdNode();
    assertEquals("iterable", iterableIdNode.getName());

    IdNode counterIdNode = (IdNode) forLoopStmtNode.getCounterIdNode();
    assertEquals("i", counterIdNode.getName());
  }

  /** WTF? . */
  void testWhileLoop() {
    String program =
        """
                    fn test_func() {
                        while expr {
                            print(id);
                        }
                    }
                """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var loopStmt = stmts.get(0);
    assertEquals(Node.Type.LoopStmtNode, loopStmt.type);
    WhileLoopStmtNode forLoopStmtNode = (WhileLoopStmtNode) loopStmt;
    assertEquals(LoopStmtNode.LoopType.whileLoop, forLoopStmtNode.loopType());

    IdNode counterIdNode = (IdNode) forLoopStmtNode.getExpressionNode();
    assertEquals("expr", counterIdNode.getName());
  }

  /** WTF? . */
  void testGraphEdgeAttribute() {
    String program =
        """
                graph g {
                    t1 -> t2 [type=seq]
                }
                """;

    var ast = Helpers.getASTFromString(program);
    var dotDefNode = (DotDefNode) ast.getChild(0);
    var stmts = dotDefNode.getStmtNodes();

    var edgeDefinitionNode = stmts.get(0);
    assertEquals(Node.Type.DotEdgeStmt, edgeDefinitionNode.type);

    DotEdgeStmtNode edgeStmtNode = (DotEdgeStmtNode) edgeDefinitionNode;
    var attrList = edgeStmtNode.getAttrListNode();

    assertEquals(Node.Type.DotAttrList, attrList.type);
    DotAttrListNode attrListNode = (DotAttrListNode) attrList;

    var firstAttr = attrListNode.getChildren().get(0);
    assertEquals(Node.Type.DotDependencyTypeAttr, firstAttr.type);

    var attrNode = (DotDependencyTypeAttrNode) firstAttr;
    assertEquals("type", attrNode.getLhsIdName());
    assertEquals("seq", attrNode.getRhsIdName());
    assertEquals(TaskEdge.Type.sequence, attrNode.getDependencyType());
  }
}
