package parser;

import static org.junit.Assert.assertEquals;

import helpers.Helpers;
import org.junit.Test;
// CHECKSTYLE:OFF: AvoidStarImport
import parser.AST.*;
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
                game_object test_object {
                    this_is_a_component
                    }
                """;
        var ast = Helpers.getASTFromString(program);

        var objDef = ast.getChild(0);
        assertEquals(Node.Type.GameObjectDefinition, objDef.type);

        var componentDefListNode =
                ((GameObjectDefinitionNode) objDef).getComponentDefinitionListNode();
        assertEquals(Node.Type.ComponentDefinitionList, componentDefListNode.type);

        var componentDefinitions = componentDefListNode.getChildren();
        assertEquals(1, componentDefinitions.size());

        var component = componentDefinitions.get(0);
        assertEquals(Node.Type.ComponentDefinition, component.type);

        String componentName = ((ComponentDefinitionNode) component).getIdName();
        assertEquals("this_is_a_component", componentName);

        var propertyDefinitionListNode =
                ((ComponentDefinitionNode) component).getPropertyDefinitionListNode();
        assertEquals(Node.NONE, propertyDefinitionListNode);
    }

    /**
     * Test the definition of a game object with one component definition with property definitions
     */
    @Test
    public void testGameObjectDefinition() {
        String program =
                """
                game_object test_object {
                    complex_component {
                        prop1: 123,
                        prop2: "Hello, World!"
                    }
                }
                """;
        var ast = Helpers.getASTFromString(program);

        var objDef = ast.getChild(0);
        assertEquals(Node.Type.GameObjectDefinition, objDef.type);

        var componentDefListNode =
                ((GameObjectDefinitionNode) objDef).getComponentDefinitionListNode();
        assertEquals(Node.Type.ComponentDefinitionList, componentDefListNode.type);

        var componentDefinitions = componentDefListNode.getChildren();
        assertEquals(1, componentDefinitions.size());

        var component = componentDefinitions.get(0);
        assertEquals(Node.Type.ComponentDefinition, component.type);

        String componentName = ((ComponentDefinitionNode) component).getIdName();
        assertEquals("complex_component", componentName);

        var propertyDefinitions =
                ((ComponentDefinitionNode) component).getPropertyDefinitionNodes();
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
            game_object test_object {
                complex_component1 {
                    prop1: 123,
                    prop2: "Hello, World!"
                },
                complex_component2 {
                    prop3: fn(test),
                    prop4: "42"
                }
            }
                """;
        var ast = Helpers.getASTFromString(program);

        var objDef = ast.getChild(0);
        var componentDefListNode =
                ((GameObjectDefinitionNode) objDef).getComponentDefinitionListNode();
        var componentDefinitions = componentDefListNode.getChildren();
        assertEquals(2, componentDefinitions.size());

        // test first component
        var component = componentDefinitions.get(0);
        String componentName = ((ComponentDefinitionNode) component).getIdName();
        assertEquals("complex_component1", componentName);

        var propertyDefinitions =
                ((ComponentDefinitionNode) component).getPropertyDefinitionNodes();
        assertEquals(2, propertyDefinitions.size());

        var firstPropertyDefNode = (PropertyDefNode) propertyDefinitions.get(0);
        assertEquals("prop1", firstPropertyDefNode.getIdName());

        var secondPropertyDefNode = (PropertyDefNode) propertyDefinitions.get(1);
        assertEquals("prop2", secondPropertyDefNode.getIdName());

        // test second component
        component = componentDefinitions.get(1);
        componentName = ((ComponentDefinitionNode) component).getIdName();
        assertEquals("complex_component2", componentName);

        propertyDefinitions = ((ComponentDefinitionNode) component).getPropertyDefinitionNodes();
        assertEquals(2, propertyDefinitions.size());

        firstPropertyDefNode = (PropertyDefNode) propertyDefinitions.get(0);
        assertEquals("prop3", firstPropertyDefNode.getIdName());

        secondPropertyDefNode = (PropertyDefNode) propertyDefinitions.get(1);
        assertEquals("prop4", secondPropertyDefNode.getIdName());
    }
}
