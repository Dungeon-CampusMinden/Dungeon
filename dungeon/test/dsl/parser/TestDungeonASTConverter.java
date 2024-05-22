package dsl.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import dsl.helpers.Helpers;
import dsl.interpreter.TestEnvironment;
import dsl.parser.ast.*;
import dsl.semanticanalysis.environment.GameEnvironment;
import graph.taskdependencygraph.TaskEdge;
import java.util.List;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.InputMismatchException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

// CHECKSTYLE:OFF: AvoidStarImport

// CHECKSTYLE:ON: AvoidStarImport

public class TestDungeonASTConverter {
  TestEnvironment env = new TestEnvironment();

  /** Test AST structure of a simple dot definition */
  @Test
  public void testSimpleDotDef() {
    String program = "graph g { a -> b }";

    TestEnvironment env = new TestEnvironment();
    String tree = Helpers.getPrettyPrintedParseTree(program, env);
    System.out.println(tree);

    var ast = Helpers.getASTFromString(program, env);

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
   * Test AST structure of a chained edge statement, that is multiple edge definitions in one line
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
    var lhsIdNode = (IdNode) firstIdList.getIdNodes().get(0);
    assertEquals("a", lhsIdNode.getName());

    var secondIdList = edgeStmtNode.getIdLists().get(1);
    var secondIdNode = (IdNode) secondIdList.getIdNodes().get(0);
    assertEquals("b", secondIdNode.getName());

    var thirdIdList = edgeStmtNode.getIdLists().get(2);
    var thirdIdNode = (IdNode) thirdIdList.getIdNodes().get(0);
    assertEquals("c", thirdIdNode.getName());
  }

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

  /** Test AST of a function call inside a property definition */
  @Test
  public void testFuncCall() {
    String program = "quest_config q { \n test: hello_world(x, \"wuppi\" ,42)\n }";
    var ast = Helpers.getASTFromString(program, env);

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

  /** Test AST of function call as parameter ot another function call */
  @Test
  public void testFuncCallAsParam() {
    String program =
        """
    quest_config q
      {
        test: hello_world(other_func())
      }
      """;
    var ast = Helpers.getASTFromString(program, env);

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

  /** Test the definition of a game object with one trivial component definition */
  @Test
  public void testGameObjectDefinitionSimpleComponent() {
    String program =
        """
            entity_type test_object {
                this_is_a_component {}
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

  /** Test the definition of a game object with one trivial component definition */
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
   * Test the definition of a game object with one component definition with property definitions
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

  /** Test the definition of a game object with multiple component definitions */
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

    TestEnvironment env = new TestEnvironment();
    var ast = Helpers.getASTFromString(program, env);
    var gameObjectDef = (PrototypeDefinitionNode) ast.getChild(0);
    var componentDef =
        (AggregateValueDefinitionNode) gameObjectDef.getComponentDefinitionNodes().get(0);
    var propertyDef = (PropertyDefNode) componentDef.getPropertyDefinitionNodes().get(0);
    var stmtNode = propertyDef.getStmtNode();
    assertEquals(stmtNode.type, Node.Type.AggregateValueDefinition);
  }

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
    Assert.assertEquals(1, stmtList.size());

    Node outerStmtBlock = funcDefNode.getStmtBlock();
    Assert.assertEquals(Node.Type.Block, outerStmtBlock.type);
    Node middleStmtBlock = outerStmtBlock.getChild(0);
    Assert.assertEquals(Node.Type.Block, middleStmtBlock.type);
    Node innerStmtBlock = middleStmtBlock.getChild(0);
    Assert.assertEquals(Node.Type.Block, innerStmtBlock.type);
    Node funcCallStmt = ((StmtBlockNode) innerStmtBlock).getStmts().get(0);
  }

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
    Assert.assertEquals(Node.Type.ConditionalStmtIf, conditionalIfStmt.type);

    var condition = ((ConditionalStmtNodeIf) conditionalIfStmt).getCondition();
    Assert.assertEquals(Node.Type.Identifier, condition.type);
    Assert.assertEquals("expr", ((IdNode) condition).getName());

    var stmt = ((ConditionalStmtNodeIf) conditionalIfStmt).getIfStmt();
    Assert.assertEquals(Node.Type.Block, stmt.type);
  }

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
    Assert.assertEquals(Node.Type.ConditionalStmtIfElse, conditionalStmt.type);

    var condition = ((ConditionalStmtNodeIfElse) conditionalStmt).getCondition();
    Assert.assertEquals(Node.Type.Identifier, condition.type);
    Assert.assertEquals("expr", ((IdNode) condition).getName());

    var ifStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getIfStmt();
    Assert.assertEquals(Node.Type.Block, ifStmt.type);

    var elseStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getElseStmt();
    Assert.assertEquals(Node.Type.FuncCall, elseStmt.type);
  }

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
    Assert.assertEquals(Node.Type.ConditionalStmtIfElse, conditionalStmt.type);

    var condition = ((ConditionalStmtNodeIfElse) conditionalStmt).getCondition();
    Assert.assertEquals(Node.Type.Identifier, condition.type);
    Assert.assertEquals("expr", ((IdNode) condition).getName());

    var ifStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getIfStmt();
    Assert.assertEquals(Node.Type.Block, ifStmt.type);

    var elseIfStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getElseStmt();
    Assert.assertEquals(Node.Type.ConditionalStmtIf, elseIfStmt.type);

    var elseIfStmtCondition = ((ConditionalStmtNodeIf) elseIfStmt).getCondition();
    Assert.assertEquals(Node.Type.Identifier, elseIfStmtCondition.type);
    Assert.assertEquals("other_expr", ((IdNode) elseIfStmtCondition).getName());
  }

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
    Assert.assertEquals(Node.Type.ConditionalStmtIfElse, conditionalStmt.type);

    var condition = ((ConditionalStmtNodeIfElse) conditionalStmt).getCondition();
    Assert.assertEquals(Node.Type.Identifier, condition.type);
    Assert.assertEquals("expr", ((IdNode) condition).getName());

    var ifStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getIfStmt();
    Assert.assertEquals(Node.Type.Block, ifStmt.type);

    var elseIfStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getElseStmt();
    Assert.assertEquals(Node.Type.ConditionalStmtIfElse, elseIfStmt.type);

    var elseIfStmtCondition = ((ConditionalStmtNodeIfElse) elseIfStmt).getCondition();
    Assert.assertEquals(Node.Type.Identifier, elseIfStmtCondition.type);
    Assert.assertEquals("other_expr", ((IdNode) elseIfStmtCondition).getName());

    var elseStmt = ((ConditionalStmtNodeIfElse) elseIfStmt).getElseStmt();
    Assert.assertEquals(Node.Type.Block, elseStmt.type);
  }

  @Test
  public void nestedIfElseStmts() {
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
    Assert.assertEquals("outer_expr", ((IdNode) outerCondition).getName());

    var ifStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getIfStmt();
    var innerConditionalStmt = ((StmtBlockNode) ifStmt).getStmts().get(0);
    Assert.assertEquals(Node.Type.ConditionalStmtIfElse, innerConditionalStmt.type);

    var innerIfCondition = ((ConditionalStmtNodeIfElse) innerConditionalStmt).getCondition();
    Assert.assertEquals("inner_expr", ((IdNode) innerIfCondition).getName());

    var innerElseStmt = ((ConditionalStmtNodeIfElse) innerConditionalStmt).getElseStmt();
    var innerElseIfCondition = ((ConditionalStmtNodeIf) innerElseStmt).getCondition();
    Assert.assertEquals("inner_else_if_expr", ((IdNode) innerElseIfCondition).getName());

    var elseStmt = ((ConditionalStmtNodeIfElse) conditionalStmt).getElseStmt();
    Assert.assertEquals(Node.Type.Block, elseStmt.type);
  }

  // TODO: tests for complex expressions

  @Test
  public void testUnary() {
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
    Assert.assertEquals(Node.Type.Unary, unaryStmt.type);
    var unaryNode = (UnaryNode) unaryStmt;
    Assert.assertEquals(UnaryNode.UnaryType.not, unaryNode.getUnaryType());

    // second statement
    unaryStmt = stmts.get(1);
    Assert.assertEquals(Node.Type.Unary, unaryStmt.type);
    unaryNode = (UnaryNode) unaryStmt;
    Assert.assertEquals(UnaryNode.UnaryType.minus, unaryNode.getUnaryType());
  }

  @Test
  public void testFactor() {
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
    Assert.assertEquals(Node.Type.Factor, factorStmt.type);

    FactorNode factorNode = (FactorNode) factorStmt;
    Assert.assertEquals(FactorNode.FactorType.multiply, factorNode.getFactorType());

    var lhs = factorNode.getLhs();
    Assert.assertEquals(Node.Type.Number, lhs.type);
    Assert.assertEquals(4, ((NumNode) lhs).getValue());

    var rhs = factorNode.getRhs();
    Assert.assertEquals(Node.Type.Number, rhs.type);
    Assert.assertEquals(2, ((NumNode) rhs).getValue());

    // second statement
    factorStmt = stmts.get(1);
    Assert.assertEquals(Node.Type.Factor, factorStmt.type);

    factorNode = (FactorNode) factorStmt;
    Assert.assertEquals(FactorNode.FactorType.divide, factorNode.getFactorType());

    lhs = factorNode.getLhs();
    Assert.assertEquals(Node.Type.Number, lhs.type);
    Assert.assertEquals(3, ((NumNode) lhs).getValue());

    rhs = factorNode.getRhs();
    Assert.assertEquals(Node.Type.Number, rhs.type);
    Assert.assertEquals(1, ((NumNode) rhs).getValue());
  }

  @Test
  public void testTerm() {
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
    Assert.assertEquals(Node.Type.Term, termStmt.type);

    TermNode termNode = (TermNode) termStmt;
    Assert.assertEquals(TermNode.TermType.plus, termNode.getTermType());

    var lhs = termNode.getLhs();
    Assert.assertEquals(Node.Type.Number, lhs.type);
    Assert.assertEquals(4, ((NumNode) lhs).getValue());

    var rhs = termNode.getRhs();
    Assert.assertEquals(Node.Type.Number, rhs.type);
    Assert.assertEquals(2, ((NumNode) rhs).getValue());

    // second statement
    termStmt = stmts.get(1);
    Assert.assertEquals(Node.Type.Term, termStmt.type);

    termNode = (TermNode) termStmt;
    Assert.assertEquals(TermNode.TermType.minus, termNode.getTermType());

    lhs = termNode.getLhs();
    Assert.assertEquals(Node.Type.Number, lhs.type);
    Assert.assertEquals(3, ((NumNode) lhs).getValue());

    rhs = termNode.getRhs();
    Assert.assertEquals(Node.Type.Number, rhs.type);
    Assert.assertEquals(1, ((NumNode) rhs).getValue());
  }

  @Test
  public void testComparison() {
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
    Assert.assertEquals(Node.Type.Comparison, compStmt.type);

    ComparisonNode compNode = (ComparisonNode) compStmt;
    Assert.assertEquals(ComparisonNode.ComparisonType.greaterThan, compNode.getComparisonType());

    // second statement
    compStmt = stmts.get(1);
    Assert.assertEquals(Node.Type.Comparison, compStmt.type);

    compNode = (ComparisonNode) compStmt;
    Assert.assertEquals(ComparisonNode.ComparisonType.greaterEquals, compNode.getComparisonType());

    // third statement
    compStmt = stmts.get(2);
    Assert.assertEquals(Node.Type.Comparison, compStmt.type);

    compNode = (ComparisonNode) compStmt;
    Assert.assertEquals(ComparisonNode.ComparisonType.lessThan, compNode.getComparisonType());

    // fourth statement
    compStmt = stmts.get(3);
    Assert.assertEquals(Node.Type.Comparison, compStmt.type);

    compNode = (ComparisonNode) compStmt;
    Assert.assertEquals(ComparisonNode.ComparisonType.lessEquals, compNode.getComparisonType());
  }

  @Test
  public void testEquality() {
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
    Assert.assertEquals(Node.Type.Equality, equalityStmt.type);

    EqualityNode equalityNode = (EqualityNode) equalityStmt;
    Assert.assertEquals(EqualityNode.EqualityType.equals, equalityNode.getEqualityType());

    // second statement
    equalityStmt = stmts.get(1);
    Assert.assertEquals(Node.Type.Equality, equalityStmt.type);

    equalityNode = (EqualityNode) equalityStmt;
    Assert.assertEquals(EqualityNode.EqualityType.notEquals, equalityNode.getEqualityType());
  }

  @Test
  public void testLogicAnd() {
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
    Assert.assertEquals(Node.Type.LogicAnd, andStmt.type);
  }

  @Test
  public void testLogicOr() {
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
    Assert.assertEquals(Node.Type.LogicOr, orStmt.type);
  }

  @Test
  public void testAssignmentId() {
    String program = """
        fn test_func() {
            my_var = 4;
        }
    """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var assignmentStmt = stmts.get(0);
    Assert.assertEquals(Node.Type.Assignment, assignmentStmt.type);

    AssignmentNode assignmentNode = (AssignmentNode) assignmentStmt;
    Assert.assertEquals(Node.Type.Identifier, assignmentNode.getLhs().type);
  }

  @Test
  public void testAssignmentMemberAccessWithCall() {
    String program = """
        fn test_func() {
            my_func().test = 4;
        }
    """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var assignmentStmt = stmts.get(0);
    Assert.assertEquals(Node.Type.Assignment, assignmentStmt.type);

    AssignmentNode assignmentNode = (AssignmentNode) assignmentStmt;
    Assert.assertEquals(Node.Type.MemberAccess, assignmentNode.getLhs().type);
    Assert.assertEquals(Node.Type.Number, assignmentNode.getRhs().type);

    MemberAccessNode memberAccessNode = (MemberAccessNode) assignmentNode.getLhs();
    Assert.assertEquals(Node.Type.FuncCall, memberAccessNode.getLhs().type);
    Assert.assertEquals(Node.Type.Identifier, memberAccessNode.getRhs().type);
  }

  @Test
  public void testAssignmentMemberAccess() {
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
    Assert.assertEquals(Node.Type.Assignment, assignmentStmt.type);

    AssignmentNode assignmentNode = (AssignmentNode) assignmentStmt;
    Assert.assertEquals(Node.Type.MemberAccess, assignmentNode.getLhs().type);
    Assert.assertEquals(Node.Type.Number, assignmentNode.getRhs().type);

    MemberAccessNode memberAccessNode = (MemberAccessNode) assignmentNode.getLhs();
    Assert.assertEquals(Node.Type.Identifier, memberAccessNode.getLhs().type);
    Assert.assertEquals(Node.Type.Identifier, memberAccessNode.getRhs().type);
  }

  @Test
  public void testMethodCallExpression() {
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
    Assert.assertEquals(Node.Type.Assignment, assignmentStmt.type);

    AssignmentNode assignmentNode = (AssignmentNode) assignmentStmt;
    var rhs = assignmentNode.getRhs();
    Assert.assertEquals(Node.Type.MemberAccess, rhs.type);

    MemberAccessNode memberAccessNode = (MemberAccessNode) rhs;
    Assert.assertEquals(Node.Type.Identifier, memberAccessNode.getLhs().type);
    Assert.assertEquals(Node.Type.FuncCall, memberAccessNode.getRhs().type);
  }

  @Test
  public void testMemberAccessExpression() {
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
    Assert.assertEquals(Node.Type.Assignment, assignmentStmt.type);

    AssignmentNode assignmentNode = (AssignmentNode) assignmentStmt;
    var rhs = assignmentNode.getRhs();
    Assert.assertEquals(Node.Type.MemberAccess, rhs.type);

    MemberAccessNode memberAccessNode = (MemberAccessNode) rhs;
    Assert.assertEquals(Node.Type.Identifier, memberAccessNode.getLhs().type);
    Assert.assertEquals(Node.Type.Identifier, memberAccessNode.getRhs().type);
  }

  @Test
  public void testListDefinition() {
    String program = """
        fn test_func() {
            [1,2,3];
        }
    """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var listDefinitionStmt = stmts.get(0);
    Assert.assertEquals(Node.Type.ListDefinitionNode, listDefinitionStmt.type);

    ListDefinitionNode listDefinitionNode = (ListDefinitionNode) listDefinitionStmt;

    Assert.assertEquals(1, ((NumNode) listDefinitionNode.getEntries().get(0)).getValue());
    Assert.assertEquals(2, ((NumNode) listDefinitionNode.getEntries().get(1)).getValue());
    Assert.assertEquals(3, ((NumNode) listDefinitionNode.getEntries().get(2)).getValue());
  }

  @Test
  public void testSetDefinition() {
    String program = """
        fn test_func() {
            <1,2,3>;
        }
    """;

    var ast = Helpers.getASTFromString(program);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var setDefinitionStmt = stmts.get(0);
    Assert.assertEquals(Node.Type.SetDefinitionNode, setDefinitionStmt.type);

    SetDefinitionNode setDefinitionNode = (SetDefinitionNode) setDefinitionStmt;

    Assert.assertEquals(1, ((NumNode) setDefinitionNode.getEntries().get(0)).getValue());
    Assert.assertEquals(2, ((NumNode) setDefinitionNode.getEntries().get(1)).getValue());
    Assert.assertEquals(3, ((NumNode) setDefinitionNode.getEntries().get(2)).getValue());
  }

  @Test
  public void testForLoop() {
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
    Assert.assertEquals(Node.Type.LoopStmtNode, loopStmt.type);
    ForLoopStmtNode forLoopStmtNode = (ForLoopStmtNode) loopStmt;
    Assert.assertEquals(LoopStmtNode.LoopType.forLoop, forLoopStmtNode.loopType());

    IdNode typeIdNode = (IdNode) forLoopStmtNode.getTypeIdNode();
    Assert.assertEquals("var_type", typeIdNode.getName());

    IdNode varNameIdNode = (IdNode) forLoopStmtNode.getVarIdNode();
    Assert.assertEquals("var_name", varNameIdNode.getName());

    IdNode iterableIdNode = (IdNode) forLoopStmtNode.getIterableIdNode();
    Assert.assertEquals("iterable", iterableIdNode.getName());
  }

  @Test
  public void testCountingForLoop() {
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
    Assert.assertEquals(Node.Type.LoopStmtNode, loopStmt.type);
    CountingLoopStmtNode forLoopStmtNode = (CountingLoopStmtNode) loopStmt;
    Assert.assertEquals(LoopStmtNode.LoopType.countingForLoop, forLoopStmtNode.loopType());

    IdNode typeIdNode = (IdNode) forLoopStmtNode.getTypeIdNode();
    Assert.assertEquals("var_type", typeIdNode.getName());

    IdNode varNameIdNode = (IdNode) forLoopStmtNode.getVarIdNode();
    Assert.assertEquals("var_name", varNameIdNode.getName());

    IdNode iterableIdNode = (IdNode) forLoopStmtNode.getIterableIdNode();
    Assert.assertEquals("iterable", iterableIdNode.getName());

    IdNode counterIdNode = (IdNode) forLoopStmtNode.getCounterIdNode();
    Assert.assertEquals("i", counterIdNode.getName());
  }

  @Test
  public void testWhileLoop() {
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
    Assert.assertEquals(Node.Type.LoopStmtNode, loopStmt.type);
    WhileLoopStmtNode forLoopStmtNode = (WhileLoopStmtNode) loopStmt;
    Assert.assertEquals(LoopStmtNode.LoopType.whileLoop, forLoopStmtNode.loopType());

    IdNode counterIdNode = (IdNode) forLoopStmtNode.getExpressionNode();
    Assert.assertEquals("expr", counterIdNode.getName());
  }

  @Test
  public void testGraphEdgeAttribute() {
    String program = """
        graph g {
            t1 -> t2 [type=seq]
        }
        """;

    String tree = Helpers.printParseTreeForProgram(program);
    var ast = Helpers.getASTFromString(program);

    var dotDefNode = (DotDefNode) ast.getChild(0);
    var stmts = dotDefNode.getStmtNodes();

    var edgeDefinitionNode = stmts.get(0);
    Assert.assertEquals(Node.Type.DotEdgeStmt, edgeDefinitionNode.type);

    DotEdgeStmtNode edgeStmtNode = (DotEdgeStmtNode) edgeDefinitionNode;
    var attrList = edgeStmtNode.getAttrListNode();

    Assert.assertEquals(Node.Type.DotAttrList, attrList.type);
    DotAttrListNode attrListNode = (DotAttrListNode) attrList;

    var firstAttr = attrListNode.getChildren().get(0);
    Assert.assertEquals(Node.Type.DotDependencyTypeAttr, firstAttr.type);

    var attrNode = (DotDependencyTypeAttrNode) firstAttr;
    Assert.assertEquals("type", attrNode.getLhsIdName());
    Assert.assertEquals("seq", attrNode.getRhsIdName());
    Assert.assertEquals(TaskEdge.Type.sequence, attrNode.getDependencyType());
  }

  @Test
  public void testCountAsIdNode() {
    String program = """
        fn test() {
          var count = "Hello";
        }
        """;

    String tree = Helpers.printParseTreeForProgram(program);
    var ast = Helpers.getASTFromString(program);

    var fnDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = fnDefNode.getStmts();

    var stmt = stmts.get(0);
    Assert.assertEquals(Node.Type.VarDeclNode, stmt.type);

    var varDeclNode = (VarDeclNode) stmt;
    var id = varDeclNode.getIdentifier();
    Assert.assertEquals(Node.Type.Identifier, id.type);

    var idNode = (IdNode) id;
    Assert.assertEquals("count", idNode.getName());
  }

  @Test
  public void testImportStmtUnnamed() {
    String program = """
            #import "hello.dng":moin
            """;

    // Helpers.getPrettyPrintedParseTree(program, )

    var ast = Helpers.getASTFromString(program, new GameEnvironment());
    var unnamedImportStmt = ast.getChild(0);
    Assert.assertEquals(Node.Type.ImportNode, unnamedImportStmt.type);
    ImportNode unnamedImportNode = (ImportNode) unnamedImportStmt;
    Assert.assertEquals(ImportNode.Type.unnamed, unnamedImportNode.importType());

    var pathNode = unnamedImportNode.pathNode();
    Assert.assertEquals(Node.Type.StringLiteral, pathNode.type);
    StringNode stringNode = (StringNode) pathNode;
    Assert.assertEquals("hello.dng", stringNode.getValue());

    var idChild = unnamedImportNode.idNode();
    Assert.assertEquals(Node.Type.Identifier, idChild.type);
    IdNode idNode = (IdNode) idChild;
    Assert.assertEquals("moin", idNode.getName());
  }

  @Test
  public void testImportStmtNamed() {

    String program = """
            #import "hello.dng":kuckuck as no
            """;

    var ast = Helpers.getASTFromString(program, new GameEnvironment());
    var namedImportStmt = ast.getChild(0);
    Assert.assertEquals(Node.Type.ImportNode, namedImportStmt.type);
    ImportNode namedImportNode = (ImportNode) namedImportStmt;
    Assert.assertEquals(ImportNode.Type.named, namedImportNode.importType());

    var pathNode = namedImportNode.pathNode();
    Assert.assertEquals(Node.Type.StringLiteral, pathNode.type);
    StringNode stringNode = (StringNode) pathNode;
    Assert.assertEquals("hello.dng", stringNode.getValue());

    var idChild = namedImportNode.idNode();
    Assert.assertEquals(Node.Type.Identifier, idChild.type);
    IdNode idNode = (IdNode) idChild;
    Assert.assertEquals("kuckuck", idNode.getName());

    var asIdChild = namedImportNode.asIdNode();
    Assert.assertEquals(Node.Type.Identifier, asIdChild.type);
    IdNode asIdNode = (IdNode) asIdChild;
    Assert.assertEquals("no", asIdNode.getName());
  }

  @Test
  public void testEqualityWithMemberAccess() {
    String program =
        """
          fn test() {
              x.y.z == u.v.w;
          }
          """;

    var ast = DungeonASTConverter.getProgramAST(program, env);
    var funcDefNode = (FuncDefNode) ast.getChild(0);
    var stmts = funcDefNode.getStmts();

    var equalityStmt = stmts.get(0);
    Assert.assertEquals(Node.Type.Equality, equalityStmt.type);
    var equalityNode = (EqualityNode) equalityStmt;

    var lhs = equalityNode.getLhs();
    Assert.assertEquals(Node.Type.MemberAccess, lhs.type);
    var lhsMemAccess = (MemberAccessNode) lhs;
    var xNode = (IdNode) lhsMemAccess.getLhs();
    Assert.assertEquals("x", xNode.getName());
    var yzNode = (MemberAccessNode) lhsMemAccess.getRhs();
    var yNode = (IdNode) yzNode.getLhs();
    Assert.assertEquals("y", yNode.getName());
    var zNode = (IdNode) yzNode.getRhs();
    Assert.assertEquals("z", zNode.getName());

    var rhs = equalityNode.getRhs();
    Assert.assertEquals(Node.Type.MemberAccess, rhs.type);
    var rhsMemAccess = (MemberAccessNode) rhs;
    var uNode = (IdNode) rhsMemAccess.getLhs();
    Assert.assertEquals("u", uNode.getName());
    var vwNode = (MemberAccessNode) rhsMemAccess.getRhs();
    var vNode = (IdNode) vwNode.getLhs();
    Assert.assertEquals("v", vNode.getName());
    var wNode = (IdNode) vwNode.getRhs();
    Assert.assertEquals("w", wNode.getName());
  }

  @Test
  public void testErrorNodeComplex() {
    String program =
        """
        graph name {
          // statement bound
            task1_a -> ;// missing rhs and missing semicolon

            -> task2_2_a [type=c_f]; // missing lhs; sync auf nächstes statement
            task3 -> task4 [type=c_f];
        }

        asdf_type obj1 {
          val1: id
        }
        """;

    // FYI, this is the parsetree
    String expectedTree =
        """
        program
          definition
            dot_def graph
              id name
              {
              dot_stmt_list
                dot_stmt
                  dot_edge_stmt
                    dot_node_list
                      id task1_a
                    dot_edge_RHS ->
                      dot_node_list
                        id[EXCEPTION IN NODE]
                  ;
              ->[ERROR_NODE]
              task2_2_a[ERROR_NODE]
              [[ERROR_NODE]
              type[ERROR_NODE]
              $DC$ // don't care about all nodes
          definition
            object_def asdf_typeobj1{
              property_def_list
                property_def
                  id val1
                  :
                  expression
                    $DC$ // don't care about all following child nodes
              }
          <EOF>
    """;
    var env = new TestEnvironment();
    env.addMockTypeName("asdf_type");

    String parseTree = Helpers.getPrettyPrintedParseTree(program, env, false);
    System.out.println(parseTree);

    var ast = DungeonASTConverter.getProgramAST(program, env);
    var dotDef = ast.getChild(0);
    var idNode = dotDef.getChild(0);
    Assert.assertEquals(Node.Type.Identifier, idNode.type);
    var stmtList = dotDef.getChild(1);
    Assert.assertEquals(Node.Type.DotStmtList, stmtList.type);

    var children = dotDef.getChildren();
    Assert.assertEquals(20, children.size());
    for (int i = 2; i < children.size(); i++) {
      System.out.println("Checking child of dot def with idx " + i);
      var child = children.get(i);
      Assert.assertEquals(Node.Type.ErrorNode, child.type);
    }

    var objDef = (ObjectDefNode) ast.getChild(1);
    Assert.assertEquals("obj1", objDef.getIdName());
    Assert.assertEquals("asdf_type", objDef.getTypeSpecifierName());
    var propDef = (PropertyDefNode) objDef.getPropertyDefinitions().get(0);
    Assert.assertEquals("val1", propDef.getIdName());
    var expr = (IdNode) propDef.getStmtNode();
    Assert.assertEquals("id", expr.getName());
  }

  @Test
  public void testErrorNodeSimpleNoExceptionInRuleNode() {
    String program =
        """
      graph name {
        // statement bound
          task1_a -> // missing rhs and missing semicolon

          -> task2_2_a [type=c_f]; // missing lhs; sync auf nächstes statement
          task3 -> task4 [type=c_f];
      }
      """;

    // FYI, this is the parsetree
    String expectedTree =
        """
        program
          definition
            dot_def graph
              id name
              {
              dot_stmt_list
                dot_stmt
                  dot_edge_stmt
                    dot_node_list
                      id task1_a
                    dot_edge_RHS ->
                      dot_node_list
                        id
                          ->[ERROR_NODE]
                          task2_2_a
                    dot_attr_list [
                      dot_attr
                        id type
                        =
                        id
                          dependency_type c_f
                      ]
                  ;
                dot_stmt
                  dot_edge_stmt
                    dot_node_list
                      id task3
                    dot_edge_RHS ->
                      dot_node_list
                        id task4
                    dot_attr_list [
                      dot_attr
                        id type
                        =
                        id
                          dependency_type c_f
                      ]
                  ;
              }
          <EOF>
    """;
    var env = new TestEnvironment();
    env.addMockTypeName("asdf_type");

    String parseTree = Helpers.getPrettyPrintedParseTree(program, env, false);
    System.out.println(parseTree);

    var ast = DungeonASTConverter.getProgramAST(program, env);
    var dotDef = (DotDefNode) ast.getChild(0);

    var firstStmtNode = (DotEdgeStmtNode) dotDef.getStmtNodes().get(0);
    var idListLhs = (DotIdList) firstStmtNode.getIdLists().get(0);
    var idLhs = (IdNode) idListLhs.getIdNodes().get(0);
    Assert.assertEquals("task1_a", idLhs.getName());

    var idListRhs = (DotIdList) firstStmtNode.getIdLists().get(1);
    Assert.assertTrue(idListRhs.subTreeHasError());
    var idListEntry = idListRhs.getChild(0);
    Assert.assertEquals(Node.Type.Identifier, idListEntry.type);

    var firstIdListEntryChild = idListEntry.getChild(0);
    Assert.assertEquals(Node.Type.ErrorNode, firstIdListEntryChild.type);
    Assert.assertEquals("->", firstIdListEntryChild.getErrorRecord().offendingSymbol().getText());

    var secondIdListEntryChild = idListEntry.getChild(1);
    Assert.assertEquals(Node.Type.Identifier, secondIdListEntryChild.type);
    Assert.assertEquals("task2_2_a", ((IdNode) secondIdListEntryChild).getName());

    var secondStmtNode = (DotEdgeStmtNode) dotDef.getStmtNodes().get(1);
    Assert.assertFalse(secondStmtNode.hasErrorChild());
    idListLhs = secondStmtNode.getIdLists().get(0);
    idLhs = idListLhs.getIdNodes().get(0);
    Assert.assertEquals("task3", idLhs.getName());

    idListRhs = secondStmtNode.getIdLists().get(1);
    var idRhs = idListRhs.getIdNodes().get(0);
    Assert.assertEquals("task4", idRhs.getName());
  }

  @Test
  public void testErrorNodeSimple() {
    // FYI, this is the parsetree
    String expectedTree =
        """
        program
          definition
            dot_def
              $'graph'
              id
                $'name'
              $'{'
              dot_stmt_list
                dot_stmt
                  dot_edge_stmt
                    dot_node_list
                      id
                        $'task1_a'
                    dot_edge_RHS
                      $'->'
                      dot_node_list
                        id [EXCEPTION IN NODE]
                          $'('[ERROR_NODE]
                  $';'
              $'}'
          $'<EOF>'
    """;

    String program = """
    graph name {
        task1_a -> (; // missing rhs
    }
    """;

    var env = new TestEnvironment();
    env.addMockTypeName("asdf_type");

    var ast = DungeonASTConverter.getProgramAST(program, env);
    var dotDef = (DotDefNode) ast.getChild(0);
    var stmtNode = (DotEdgeStmtNode) dotDef.getStmtNodes().get(0);
    Assert.assertFalse(stmtNode.hasAttrList());
    var idListLhs = (DotIdList) stmtNode.getIdLists().get(0);
    var idLhs = (IdNode) idListLhs.getIdNodes().get(0);
    Assert.assertEquals("task1_a", idLhs.getName());

    var idListRhs = (DotIdList) stmtNode.getIdLists().get(1);
    Assert.assertTrue(idListRhs.subTreeHasError());
    var firstChildRhs = idListRhs.getChild(0);
    Assert.assertEquals(Node.Type.Identifier, firstChildRhs.type);

    // the whole id-list should also contain the error record corresponding to the exception
    var idListErrorRecord = firstChildRhs.getErrorRecord();
    Assert.assertEquals(2, idListErrorRecord.line());
    Assert.assertEquals(15, idListErrorRecord.charPositionInLine());
    Assert.assertEquals("(", ((CommonToken) idListErrorRecord.offendingSymbol()).getText());
    Assert.assertTrue(idListErrorRecord.exception() instanceof InputMismatchException);

    // this errorNode should correspond to the extraneous ')'
    var errorNode = (ASTErrorNode) firstChildRhs.getChild(0);
    var errorNodeErrorRecord = errorNode.getErrorRecord();
    Assert.assertEquals(2, errorNodeErrorRecord.line());
    Assert.assertEquals(15, errorNodeErrorRecord.charPositionInLine());
    Assert.assertTrue(errorNodeErrorRecord.exception() instanceof InputMismatchException);
  }

  @Test
  public void testErrorAlternativeParamDef() {
    String program = """
      fn test (int x, int, int) {
      }
      """;

    var env = new TestEnvironment();
    String parseTree = Helpers.getPrettyPrintedParseTree(program, env, false);
    System.out.println(parseTree);

    var ast = DungeonASTConverter.getProgramAST(program, env);
    var parameterDefNodes = ((FuncDefNode) ast.getChild(0)).getParameters();

    var secondParamDef = parameterDefNodes.get(1);
    Assert.assertTrue(secondParamDef.hasErrorChild());
    var typeId = secondParamDef.getChild(0);
    assertEquals(Node.Type.Identifier, typeId.type);
    var typeIdNode = (IdNode) typeId;
    assertEquals("int", typeIdNode.getName());
    var offendingSymbol = secondParamDef.getChild(1);
    assertEquals(Node.Type.ErrorNode, offendingSymbol.type);
    var errorRecord = offendingSymbol.getErrorRecord();
    assertEquals(1, errorRecord.line());
    assertEquals(19, errorRecord.charPositionInLine());
    assertEquals(",", ((CommonToken) errorRecord.offendingSymbol()).getText());

    var thirdParamDef = parameterDefNodes.get(2);
    Assert.assertTrue(thirdParamDef.hasErrorChild());
    typeId = thirdParamDef.getChild(0);
    assertEquals(Node.Type.Identifier, typeId.type);
    typeIdNode = (IdNode) typeId;
    assertEquals("int", typeIdNode.getName());
    offendingSymbol = thirdParamDef.getChild(1);
    assertEquals(Node.Type.ErrorNode, offendingSymbol.type);
    errorRecord = offendingSymbol.getErrorRecord();
    assertEquals(1, errorRecord.line());
    assertEquals(24, errorRecord.charPositionInLine());
    assertEquals(")", ((CommonToken) errorRecord.offendingSymbol()).getText());
  }

  @Test
  public void testErrorAlternativePropertyDef() {
    String program = """
      asdf_type obj1 {
        val1: ,
        val2: id
      }
      """;

    var env = new TestEnvironment();
    env.addMockTypeName("asdf_type");
    String parseTree = Helpers.getPrettyPrintedParseTree(program, env, false);
    System.out.println(parseTree);

    var ast = DungeonASTConverter.getProgramAST(program, env);
    var propertyDefinitionNodes = ((ObjectDefNode) ast.getChild(0)).getPropertyDefinitions();
    var firstPropertyDefinition = propertyDefinitionNodes.get(0);
    Assert.assertTrue(firstPropertyDefinition.hasErrorChild());
    var id = firstPropertyDefinition.getChild(0);
    Assert.assertEquals(Node.Type.Identifier, id.type);
    var idNode = (IdNode) id;
    Assert.assertEquals("val1", idNode.getName());
    var offendingSymbol = firstPropertyDefinition.getChild(1);
    Assert.assertEquals(Node.Type.ErrorNode, offendingSymbol.type);
    var errorRecord = offendingSymbol.getErrorRecord();
    Assert.assertEquals(2, errorRecord.line());
    Assert.assertEquals(8, errorRecord.charPositionInLine());
  }

  @Test
  public void testErrorCompletelyBroken() {
    String program =
        """
      graph name {
        // statement bound
          task1_a -> ;// missing rhs and missing semicolon

          -> task2_2_a [type=c_f]; // missing lhs; sync auf nächstes statement
          task3 -> task4 [type=c_f]

      asdf_type obj1 {
        val1: id
      }
      """;

    // FYI, this is the parsetree
    String expectedTree =
        """
      program
        definition
          dot_def graph
            id name
            {
            dot_stmt_list
              dot_stmt
                dot_edge_stmt
                  dot_node_list
                    id task1_a
                  dot_edge_RHS ->
                    dot_node_list
                      id[EXCEPTION IN NODE]
                ;
            ->[ERROR_NODE]
            task2_2_a[ERROR_NODE]
            [[ERROR_NODE]
            type[ERROR_NODE]
            $DC$ // don't care about all nodes
        definition
          object_def asdf_typeobj1{
            property_def_list
              property_def
                id val1
                :
                expression
                  $DC$ // don't care about all following child nodes
            }
        <EOF>
  """;
    var env = new TestEnvironment();
    env.addMockTypeName("asdf_type");

    String parseTree = Helpers.getPrettyPrintedParseTree(program, env, false);
    System.out.println(parseTree);

    var ast = DungeonASTConverter.getProgramAST(program, env);
    boolean b = true;
  }

  @Test
  public void testLexingError() {
    String program =
        """
        #impot "test.dng;kjl;ajsdf

        single_choice_task t1 {
          description:"hello",
          correct_answer_index: 1,
          answers: [1,2,3]
        }

        graph g {
          t1
        }

        dungeon_config c {
          dependency_graph: g
        }
        """;

    var env = new TestEnvironment();
    env.addMockTypeName("single_choice_task");
    env.addMockTypeName("dungeon_config");

    String parseTree = Helpers.getPrettyPrintedParseTree(program, env, false);
    System.out.println(parseTree);

    var ast = DungeonASTConverter.getProgramAST(program, env);
    Assert.assertEquals(5, ast.getChildren().size());
    var child1 = ast.getChild(0);
    Assert.assertEquals(Node.Type.ObjectDefinition, child1.type);
    var child2 = ast.getChild(1);
    Assert.assertEquals(Node.Type.DotDefinition, child2.type);
    var child3 = ast.getChild(2);
    Assert.assertEquals(Node.Type.ObjectDefinition, child3.type);
    var child4 = ast.getChild(3);
    Assert.assertEquals(Node.Type.ErrorNode, child4.type);
    Assert.assertTrue(child4 instanceof ASTLexerErrorNode);
    var child5 = ast.getChild(4);
    Assert.assertEquals(Node.Type.ErrorNode, child5.type);
    Assert.assertTrue(child5 instanceof ASTLexerErrorNode);
  }

  @Test
  public void testTopLevelSync() {
    String program =
        """
      ;asa(asa 2asa<<<derp;return;while

      // Aufgabendefinition
      single_choice_task meine_aufgabe {
          description: "Das ist der Aufgabentext",
          grading_function: grade_single_choice_task
      }
      """;

    var env = new TestEnvironment();
    env.addMockTypeName("single_choice_task");
    env.addMockTypeName("assign_task");
    env.addMockTypeName("dungeon_config");

    String parseTree = Helpers.getPrettyPrintedParseTree(program, env, true);
    System.out.println(parseTree);

    var ast = DungeonASTConverter.getProgramAST(program, env);

    List<String> expectedTokenStrings =
        List.of(
            ";", "asa", "(", "asa", "2", "asa", "<", "<", "<", "derp", ";", "return", ";", "while");

    var errorNodes =
        ast.getChildren().stream()
            .filter(n -> n instanceof ASTErrorNode)
            .map(n -> (ASTErrorNode) n)
            .toList();
    Assert.assertEquals(expectedTokenStrings.size(), errorNodes.size());

    for (int i = 0; i < errorNodes.size(); i++) {
      var errorNode = errorNodes.get(i);
      String errorNodeStringActual = errorNode.internalErrorNode().toString();
      String errorNodeStringExpected = expectedTokenStrings.get(i);
      Assert.assertEquals(errorNodeStringExpected, errorNodeStringActual);
    }

    var objectDefNode = ast.getChild(14);
    Assert.assertFalse(objectDefNode.subTreeHasError());
    Assert.assertEquals(Node.Type.ObjectDefinition, objectDefNode.type);
  }

  @Test
  public void testASTErrorBrokenIdList() {
    String program =
        """
      //#imIpot "test.dng;kjl;ajsdf

      single_choice_task t1 {
          description:"hello",
          correct_answer_index: 1,
          answers: [1,2,3]
      }

      graph g {
          t1,;khas // broken
      }

      dungeon_config c {
          dependency_graph: ga
      }
      """;

    var env = new TestEnvironment();
    env.addMockTypeName("single_choice_task");
    env.addMockTypeName("assign_task");
    env.addMockTypeName("dungeon_config");

    String parseTree = Helpers.getPrettyPrintedParseTree(program, env, true);
    System.out.println(parseTree);

    var ast = DungeonASTConverter.getProgramAST(program, env);
    var dotEdgeStmtNode = (DotEdgeStmtNode) ast.getChild(1).getChild(1);

    var idList = dotEdgeStmtNode.getIdLists().get(0);
    Assert.assertEquals(2, idList.getChildren().size());

    var lhsId = idList.getChild(0);
    Assert.assertEquals("t1", ((IdNode) lhsId).getName());

    var rhsIdNode = idList.getChild(1);
    Assert.assertEquals(2, rhsIdNode.getChildren().size());
    Assert.assertTrue(rhsIdNode.subTreeHasError());

    var expectedToBeErrorNode = rhsIdNode.getChild(0);
    Assert.assertTrue(expectedToBeErrorNode instanceof ASTErrorNode);

    var expectedToBeIdNode = rhsIdNode.getChild(1);
    Assert.assertEquals(Node.Type.Identifier, expectedToBeIdNode.type);
    Assert.assertEquals("khas", ((IdNode) expectedToBeIdNode).getName());
  }

  @Test
  @Ignore // TODO: for later
  public void testPlusInGraphDefinition() {
    // NOTE: The DotDefNode will contain only the 't1' identifier, the rest of the definition from
    // the '+' on
    // will be contained in ASTErrorNodes; would be nice, if the '+' got kicked out via single token
    // deletion
    // followed by single token insertion (basically a 'single token substitution')
    String program =
        """
      single_choice_task t1 {
          description:"hello",
          correct_answer_index: 1,
          answers: [1,2,3]
      }

      graph g {
          t1+id,derp
      }

      dungeon_config c {
          dependency_graph: ga
      }
      """;

    var env = new TestEnvironment();
    env.addMockTypeName("single_choice_task");
    env.addMockTypeName("assign_task");
    env.addMockTypeName("dungeon_config");

    String parseTree = Helpers.getPrettyPrintedParseTree(program, env, true);
    System.out.println(parseTree);

    var ast = DungeonASTConverter.getProgramAST(program, env);
    boolean b = true;
    // TODO: test implementation
  }

  @Test
  public void testListDefinitionSourceReference() {
    String program = """
        fn test_func() {
            [1,2,3];
        }
    """;

    var ast = Helpers.getASTFromString(program);

    SourceFileReference reference = ast.getChild(0).getSourceFileReference();
    assertEquals(0, reference.getStartLine());
    assertEquals(2, reference.getEndLine());
    assertEquals(4, reference.getStartColumn());
    assertEquals(4, reference.getEndColumn());
  }

  @Test
  public void incompleteMemberAccess() {
    String program =
        """
        fn test(entity ent, int x) {
            if x {
                ent.
            }
        }
    """;

    var env = new GameEnvironment();
    String parseTree = Helpers.getPrettyPrintedParseTree(program, env, false);
    System.out.println(parseTree);

    var ast = DungeonASTConverter.getProgramAST(program, env);
    var funcDef = (FuncDefNode) ast.getChild(0);
    var condStmt = (ConditionalStmtNodeIf) funcDef.getStmts().get(0);
    var condStmtBlock = (StmtBlockNode) condStmt.getIfStmt();
    var incompleteMemberAccess = condStmtBlock.getStmts().get(0);
    assertEquals(Node.Type.Stmt, incompleteMemberAccess.type);
    var memberAccess = incompleteMemberAccess.getChild(0);
    assertEquals(Node.Type.MemberAccess, memberAccess.type);
    var idNode = (IdNode) memberAccess.getChild(0);
    assertEquals("ent", idNode.getName());
  }
}
