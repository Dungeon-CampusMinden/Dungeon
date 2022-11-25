package parser;

import static org.junit.Assert.assertEquals;

import helpers.Helpers;
import org.junit.Test;
import parser.AST.*;

public class TestDungeonASTConverter {

    // TODO: checking the structure in this way is very verbose and
    // gets old quickly, should create more comfortable way of testing AST-Structure..
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

    @Test
    public void testStringLiteral() {
        String program = "t g { x : \"He\\tllo\\n\"}";

        var ast = Helpers.getASTFromString(program);
    }

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
}
