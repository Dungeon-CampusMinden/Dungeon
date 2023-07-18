package parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import helpers.Helpers;

import org.junit.Assert;
import org.junit.Test;
// CHECKSTYLE:OFF: AvoidStarImport

import parser.ast.*;
// CHECKSTYLE:ON: AvoidStarImport

public class TestDungeonASTConverter {

    // TODO: checking the structure in this way is very verbose and
    // gets old quickly, should create more comfortable way of testing AST-Structure..

    /** Test AST structure of a simple dot definition */
    @Test
    public void testSimpleDotDef() {
        String program = "graph g { a -- b }";

        var ast = Helpers.getASTFromString(program);

        var dot_def = ast.getChild(0);
        assertEquals(dot_def.type, Node.Type.DotDefinition);

        var id = dot_def.getChild(0);
        assertEquals(id.type, Node.Type.Identifier);
        var idNode = (IdNode) id;
        assertEquals("g", idNode.getName());

        var edgeStmt = dot_def.getChild(1);
        assertEquals(Node.Type.DotEdgeStmt, edgeStmt.type);
        var edgeStmtNode = (EdgeStmtNode) edgeStmt;

        var lhsId = edgeStmtNode.getLhsId();
        assertEquals(Node.Type.Identifier, lhsId.type);
        var lhsIdNode = (IdNode) lhsId;
        assertEquals("a", lhsIdNode.getName());

        var rhsStmts = edgeStmtNode.getRhsStmts();
        assertEquals(1, rhsStmts.size());

        var rhsEdge = rhsStmts.get(0);
        assertEquals(Node.Type.DotEdgeRHS, rhsEdge.type);

        var rhsEdgeNode = (EdgeRhsNode) rhsEdge;
        var edgeOp = rhsEdgeNode.getEdgeOpNode();
        assertEquals(Node.Type.DotEdgeOp, edgeOp.type);

        var edgeOpNode = (EdgeOpNode) edgeOp;
        assertEquals(EdgeOpNode.Type.doubleLine, edgeOpNode.getEdgeOpType());

        var rhsId = rhsEdgeNode.getIdNode();
        assertEquals(Node.Type.Identifier, rhsId.type);

        var rhsIdNode = (IdNode) rhsId;
        assertEquals("b", rhsIdNode.getName());
    }

    /**
     * Test AST structure of a chained edge statement, that is multiple edge definitions in one line
     */
    @Test
    public void testChainedEdgeStmt() {
        String program = "graph g { a -- b -- c }";

        var ast = Helpers.getASTFromString(program);

        var dot_def = ast.getChild(0);
        assertEquals(Node.Type.DotDefinition, dot_def.type);

        var edgeStmt = dot_def.getChild(1);
        assertEquals(Node.Type.DotEdgeStmt, edgeStmt.type);
        var edgeStmtNode = (EdgeStmtNode) edgeStmt;

        var lhsId = edgeStmtNode.getLhsId();
        assertEquals(Node.Type.Identifier, lhsId.type);
        var lhsIdNode = (IdNode) lhsId;
        assertEquals("a", lhsIdNode.getName());

        var rhsStmts = edgeStmtNode.getRhsStmts();
        assertEquals(2, rhsStmts.size());

        var rhsEdge = rhsStmts.get(0);
        assertEquals(Node.Type.DotEdgeRHS, rhsEdge.type);

        var rhsEdgeNode = (EdgeRhsNode) rhsEdge;
        var rhsId = rhsEdgeNode.getIdNode();
        assertEquals(Node.Type.Identifier, rhsId.type);

        var rhsIdNode = (IdNode) rhsId;
        assertEquals("b", rhsIdNode.getName());

        rhsEdge = rhsStmts.get(1);
        assertEquals(Node.Type.DotEdgeRHS, rhsEdge.type);

        rhsEdgeNode = (EdgeRhsNode) rhsEdge;
        rhsId = rhsEdgeNode.getIdNode();
        assertEquals(Node.Type.Identifier, rhsId.type);

        rhsIdNode = (IdNode) rhsId;
        assertEquals("c", rhsIdNode.getName());
    }

    /** Test AST of a function call inside a property definition */
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

    /** Test AST of function call as parameter ot another function call */
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

    /** Test the definition of a game object with one trivial component definition */
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

        var componentDefListNode =
                ((PrototypeDefinitionNode) objDef).getComponentDefinitionListNode();
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

        var componentDefListNode =
                ((PrototypeDefinitionNode) objDef).getComponentDefinitionListNode();
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
        var componentDefListNode =
                ((PrototypeDefinitionNode) objDef).getComponentDefinitionListNode();
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

        propertyDefinitions =
                ((AggregateValueDefinitionNode) component).getPropertyDefinitionNodes();
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

        var ast = Helpers.getASTFromString(program);
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
        Node outerBlocksStmtList = outerStmtBlock.getChild(0);
        Assert.assertEquals(Node.Type.StmtList, outerBlocksStmtList.type);
        Node middleStmtBlock = outerBlocksStmtList.getChild(0);
        Assert.assertEquals(Node.Type.Block, middleStmtBlock.type);
        Node middleBlocksStmtList = middleStmtBlock.getChild(0);
        Assert.assertEquals(Node.Type.StmtList, middleBlocksStmtList.type);
        Node innerStmtBlock = middleBlocksStmtList.getChild(0);
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
        Assert.assertEquals(4, ((NumNode)lhs).getValue());

        var rhs = factorNode.getRhs();
        Assert.assertEquals(Node.Type.Number, rhs.type);
        Assert.assertEquals(2, ((NumNode)rhs).getValue());

        // second statement
        factorStmt = stmts.get(1);
        Assert.assertEquals(Node.Type.Factor, factorStmt.type);

        factorNode = (FactorNode) factorStmt;
        Assert.assertEquals(FactorNode.FactorType.divide, factorNode.getFactorType());

        lhs = factorNode.getLhs();
        Assert.assertEquals(Node.Type.Number, lhs.type);
        Assert.assertEquals(3, ((NumNode)lhs).getValue());

        rhs = factorNode.getRhs();
        Assert.assertEquals(Node.Type.Number, rhs.type);
        Assert.assertEquals(1, ((NumNode)rhs).getValue());
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
        Assert.assertEquals(4, ((NumNode)lhs).getValue());

        var rhs = termNode.getRhs();
        Assert.assertEquals(Node.Type.Number, rhs.type);
        Assert.assertEquals(2, ((NumNode)rhs).getValue());

        // second statement
        termStmt = stmts.get(1);
        Assert.assertEquals(Node.Type.Term, termStmt.type);

        termNode = (TermNode) termStmt;
        Assert.assertEquals(TermNode.TermType.minus, termNode.getTermType());

        lhs = termNode.getLhs();
        Assert.assertEquals(Node.Type.Number, lhs.type);
        Assert.assertEquals(3, ((NumNode)lhs).getValue());

        rhs = termNode.getRhs();
        Assert.assertEquals(Node.Type.Number, rhs.type);
        Assert.assertEquals(1, ((NumNode)rhs).getValue());
    }

    @Test
    public void testEquality() {
        String program =
                """
                fn test_func() {
                    !true;
                }
            """;

        var ast = Helpers.getASTFromString(program);
        Assert.assertTrue(false);
    }

    @Test
    public void testLogicAnd() {
        String program =
                """
                fn test_func() {
                    !true;
                }
            """;

        var ast = Helpers.getASTFromString(program);
        Assert.assertTrue(false);
    }

    @Test
    public void testLogicOr() {
        String program =
                """
                fn test_func() {
                    !true;
                }
            """;

        var ast = Helpers.getASTFromString(program);
        Assert.assertTrue(false);
    }

    @Test
    public void testAssignment() {
        String program =
                """
                fn test_func() {
                    !true;
                }
            """;

        var ast = Helpers.getASTFromString(program);
        Assert.assertTrue(false);
    }

    @Test
    public void testMethodCallExpression() {
        String program =
                """
                fn test_func() {
                    !true;
                }
            """;

        var ast = Helpers.getASTFromString(program);
        Assert.assertTrue(false);
    }

    @Test
    public void testMemberAccessExpression() {
        String program =
                """
                fn test_func() {
                    !true;
                }
            """;

        var ast = Helpers.getASTFromString(program);
        Assert.assertTrue(false);
    }
}
